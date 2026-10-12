package com.sv.aimlabgyrotrainer.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.lifecycle.AndroidViewModel
import com.sv.aimlabgyrotrainer.model.SandConfig
import com.sv.aimlabgyrotrainer.model.SandGrain
import com.sv.aimlabgyrotrainer.model.SessionStats
import com.sv.aimlabgyrotrainer.model.TelemetryEvent
import com.sv.aimlabgyrotrainer.model.f1
import com.sv.aimlabgyrotrainer.sensors.SensorSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * CAPA DE ESTADO Y LÓGICA
 * - Recibe lecturas crudas de SensorSource.
 * - Aplica filtro paso bajo, zona muerta de 0.03 rad/s e interpolación de trazo continuo.
 * - Expone estado observable inmutable para Compose.
 */
class SandViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorSource = SensorSource(application)

    val hasGyroscope: Boolean = sensorSource.hasGyroscope
    val hasAccelerometer: Boolean = sensorSource.hasAccelerometer
    val sessionStartTime: Long = System.currentTimeMillis()

    // ---------------------------------------------------------------
    // Estado del lienzo
    // ---------------------------------------------------------------
    val sandGrains = mutableStateListOf<SandGrain>()

    var penX by mutableFloatStateOf(0f)
        private set
    var penY by mutableFloatStateOf(0f)
        private set
    var isCollapsing by mutableStateOf(false)
        private set
    var isTouching by mutableStateOf(false)
        private set

    var frameTick by mutableIntStateOf(0)
        private set

    var shakeSignal by mutableIntStateOf(0)
        private set

    // ---------------------------------------------------------------
    // Telemetría en vivo e historiales
    // ---------------------------------------------------------------
    var liveGyro by mutableFloatStateOf(0f)
        private set
    var liveAccel by mutableFloatStateOf(9.8f)
        private set
    var maxGyroPublished by mutableFloatStateOf(0f)
        private set
    var avgGyroPublished by mutableFloatStateOf(0f)
        private set

    val gyroHistory = mutableStateListOf<Float>()
    val accelHistory = mutableStateListOf<Float>()
    val eraseHistory = mutableStateListOf<Int>()
    val eventLogs = mutableStateListOf<TelemetryEvent>()

    // ---------------------------------------------------------------
    // Métricas acumuladas
    // ---------------------------------------------------------------
    var totalGrains by mutableIntStateOf(0)
        private set
    var totalErases by mutableIntStateOf(0)
        private set
    var maxShakePeak by mutableFloatStateOf(0f)
        private set

    // ---------------------------------------------------------------
    // Ajustes del usuario
    // ---------------------------------------------------------------
    var brushSize by mutableFloatStateOf(7.5f)
        private set
    var brushColor by mutableStateOf(Color(0xFFFBBF24))
        private set
    var sensitivity by mutableFloatStateOf(SandConfig.DEFAULT_SENSITIVITY)
        private set
    var targetInput by mutableStateOf("300")
        private set

    // ---------------------------------------------------------------
    // Variables de control interno
    // ---------------------------------------------------------------
    private var limitX = 300f
    private var limitY = 600f
    private var lastGyroTimestamp = 0L
    private var lastHistoryTime = 0L
    private var lastSpikeTime = 0L
    private var lastShakeTime = 0L
    private var smoothGyroX = 0f
    private var smoothGyroY = 0f
    private var lastAccelMag = 9.8f
    private var gyroSum = 0f
    private var gyroCount = 0
    private var maxGyro = 0f
    private var eventCounter = 0

    // Coordenadas del último punto trazado para interpolar huecos
    private var lastPaintX: Float? = null
    private var lastPaintY: Float? = null

    fun startSensors() {
        lastGyroTimestamp = 0L
        sensorSource.start(
            onGyroscope = ::handleGyroscope,
            onAccelerometer = ::handleAccelerometer
        )
    }

    fun stopSensors() {
        sensorSource.stop()
    }

    override fun onCleared() {
        sensorSource.stop()
        super.onCleared()
    }

    fun setCanvasSize(widthPx: Float, heightPx: Float) {
        limitX = widthPx / 2f - 24f
        limitY = heightPx / 2f - 40f
    }

    @JvmName("updateTouchingState")
    fun setTouching(value: Boolean) {
        isTouching = value
        if (!value) {
            lastPaintX = null
            lastPaintY = null
        }
    }

    fun recenterCursor() {
        penX = 0f
        penY = 0f
        lastPaintX = null
        lastPaintY = null
    }

    fun updateBrushSize(value: Float) {
        brushSize = value
    }

    fun updateBrushColor(value: Color) {
        brushColor = value
    }

    fun updateSensitivity(value: Float) {
        sensitivity = value
    }

    fun updateTargetInput(text: String) {
        targetInput = text.filter { it.isDigit() }.take(5)
    }

    fun sessionStats(nowMs: Long): SessionStats {
        val seconds = ((nowMs - sessionStartTime) / 1000L).toInt().coerceAtLeast(0)
        return SessionStats(
            totalGrains = totalGrains,
            seconds = seconds,
            erases = totalErases,
            maxShake = maxShakePeak,
            maxGyro = maxGyroPublished,
            avgGyro = avgGyroPublished
        )
    }

    fun prepareCollapse() {
        lastPaintX = null
        lastPaintY = null
        sandGrains.forEach { grain ->
            grain.vx = Random.nextFloat() * 10f - 5f
            grain.vy = Random.nextFloat() * 6f + 4f
        }
    }

    fun stepCollapse(screenHeightPx: Float) {
        val bottomLimit = screenHeightPx / 2f + 50f
        val iterator = sandGrains.listIterator()
        while (iterator.hasNext()) {
            val grain = iterator.next()
            grain.vy += 2.4f
            grain.y += grain.vy
            grain.x += grain.vx
            if (grain.y > bottomLimit) {
                iterator.remove()
            }
        }
        frameTick += 1
    }

    fun finishCollapse() {
        isCollapsing = false
    }

    private fun handleGyroscope(rawX: Float, rawY: Float, rawZ: Float, timestampNs: Long) {
        val dt = if (lastGyroTimestamp != 0L) {
            ((timestampNs - lastGyroTimestamp) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        } else 0.016f
        lastGyroTimestamp = timestampNs

        smoothGyroX = smoothGyroX * 0.80f + rawX * 0.20f
        smoothGyroY = smoothGyroY * 0.80f + rawY * 0.20f

        val smoothMag = sqrt(smoothGyroX * smoothGyroX + smoothGyroY * smoothGyroY)
        val rawMag = sqrt(rawX * rawX + rawY * rawY)
        gyroSum += smoothMag
        gyroCount += 1
        if (smoothMag > maxGyro) maxGyro = smoothMag

        val now = System.currentTimeMillis()

        if (now - lastHistoryTime >= 50L) {
            lastHistoryTime = now
            if (gyroHistory.size >= SandConfig.HISTORY_SIZE) gyroHistory.removeAt(0)
            gyroHistory.add(smoothMag)
            if (accelHistory.size >= SandConfig.HISTORY_SIZE) accelHistory.removeAt(0)
            accelHistory.add(lastAccelMag)
            liveGyro = smoothMag
            liveAccel = lastAccelMag
            maxGyroPublished = maxGyro
            avgGyroPublished = if (gyroCount > 0) gyroSum / gyroCount else 0f
        }

        if (rawMag > SandConfig.GYRO_SPIKE_THRESHOLD && !isCollapsing && now - lastSpikeTime > 2500L) {
            lastSpikeTime = now
            logEvent(
                type = "Giro brusco",
                magnitude = rawMag,
                unit = "rad/s",
                reading = "El giroscopio midió ${f1(rawMag)} rad/s y el límite es ${f1(SandConfig.GYRO_SPIKE_THRESHOLD)} rad/s.",
                interpretation = "Giraste el teléfono muy rápido y el cursor se movió demasiado de golpe.",
                conclusion = "Con giros así de rápidos el trazo sale desordenado. Con giros suaves salen mejores líneas."
            )
        }

        if (!isCollapsing) {
            moveCursorAndPaint(dt, smoothMag)
        }
    }

    private fun moveCursorAndPaint(dt: Float, smoothMag: Float) {
        // Zona muerta para eliminar el temblor involuntario
        val effGyroX = if (smoothMag < 0.03f) 0f else smoothGyroX
        val effGyroY = if (smoothMag < 0.03f) 0f else smoothGyroY

        penX = (penX + effGyroY * dt * sensitivity).coerceIn(-limitX, limitX)
        penY = (penY + effGyroX * dt * sensitivity).coerceIn(-limitY, limitY)

        // Trazo continuo mediante interpolación de puntos
        if (isTouching && sandGrains.size < SandConfig.MAX_GRAINS) {
            val minGap = maxOf(4.5f, brushSize * 0.60f)
            val prevX = lastPaintX
            val prevY = lastPaintY

            if (prevX == null || prevY == null) {
                sandGrains.add(
                    SandGrain(
                        x = penX,
                        y = penY,
                        color = lerp(brushColor, Color.White, Random.nextFloat() * 0.35f),
                        radius = brushSize / 2f
                    )
                )
                totalGrains += 1
                lastPaintX = penX
                lastPaintY = penY
            } else {
                val dx = penX - prevX
                val dy = penY - prevY
                val dist = sqrt(dx * dx + dy * dy)

                if (dist >= minGap) {
                    val steps = (dist / minGap).toInt().coerceIn(1, 20)
                    for (step in 1..steps) {
                        if (sandGrains.size >= SandConfig.MAX_GRAINS) break
                        val fraction = step.toFloat() / steps
                        val interpX = prevX + dx * fraction
                        val interpY = prevY + dy * fraction
                        sandGrains.add(
                            SandGrain(
                                x = interpX,
                                y = interpY,
                                color = lerp(brushColor, Color.White, Random.nextFloat() * 0.35f),
                                radius = brushSize / 2f
                            )
                        )
                        totalGrains += 1
                    }
                    lastPaintX = penX
                    lastPaintY = penY
                }
            }
        }
    }

    private fun handleAccelerometer(ax: Float, ay: Float, az: Float, timestampNs: Long) {
        val total = sqrt(ax * ax + ay * ay + az * az)
        lastAccelMag = total

        val now = System.currentTimeMillis()
        if (total > SandConfig.SHAKE_THRESHOLD &&
            sandGrains.isNotEmpty() &&
            !isCollapsing &&
            now - lastShakeTime > 1400L
        ) {
            lastShakeTime = now
            if (total > maxShakePeak) maxShakePeak = total

            val countBeforeErase = sandGrains.size
            if (eraseHistory.size >= SandConfig.MAX_ERASE_BARS) eraseHistory.removeAt(0)
            eraseHistory.add(countBeforeErase)
            totalErases += 1

            logEvent(
                type = "Sacudida",
                magnitude = total,
                unit = "m/s²",
                reading = "El acelerómetro midió ${f1(total)} m/s² y el límite es ${f1(SandConfig.SHAKE_THRESHOLD)} m/s².",
                interpretation = "Sacudiste el teléfono con fuerza y la app lo toma como la orden de borrar.",
                conclusion = "Se borraron $countBeforeErase puntos y ese dibujo quedó como una barra en la pestaña Mi meta."
            )

            shakeSignal += 1
            isCollapsing = true
        }
    }

    private fun logEvent(
        type: String,
        magnitude: Float,
        unit: String,
        reading: String,
        interpretation: String,
        conclusion: String
    ) {
        eventCounter += 1
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        eventLogs.add(
            0,
            TelemetryEvent(eventCounter, time, type, magnitude, unit, reading, interpretation, conclusion)
        )
        if (eventLogs.size > 20) eventLogs.removeAt(eventLogs.size - 1)
    }
}