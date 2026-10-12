package com.sv.aimlabgyrotrainer.model

import androidx.compose.ui.graphics.Color
import java.util.Locale

object SandConfig {
    const val SHAKE_THRESHOLD = 19.5f        // m/s² para borrar con sacudida
    const val GYRO_SPIKE_THRESHOLD = 3.5f    // rad/s para registrar un giro brusco
    const val HISTORY_SIZE = 100             // 100 muestras x 50 ms = 5 s de ventana
    const val GYRO_SCALE_MAX = 5f            // rad/s (eje izquierdo del gráfico 1)
    const val ACCEL_SCALE_MAX = 25f          // m/s² (eje derecho del gráfico 1)
    const val MAX_GRAINS = 1500
    const val MAX_ERASE_BARS = 5
    const val DEFAULT_SENSITIVITY = 1100f    // px por radián girado
}

data class SandGrain(
    var x: Float,
    var y: Float,
    val color: Color,
    val radius: Float,
    var vx: Float = 0f,
    var vy: Float = 0f
)

data class TelemetryEvent(
    val id: Int,
    val timestamp: String,
    val type: String,
    val magnitude: Float,
    val unit: String,
    val reading: String,
    val interpretation: String,
    val conclusion: String
)

data class SessionStats(
    val totalGrains: Int,
    val seconds: Int,
    val erases: Int,
    val maxShake: Float,
    val maxGyro: Float,
    val avgGyro: Float
)

fun f1(value: Float): String = String.format(Locale.US, "%.1f", value)

fun f2(value: Float): String = String.format(Locale.US, "%.2f", value)