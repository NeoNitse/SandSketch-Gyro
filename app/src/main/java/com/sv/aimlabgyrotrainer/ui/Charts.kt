package com.sv.aimlabgyrotrainer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sv.aimlabgyrotrainer.model.SandConfig
import com.sv.aimlabgyrotrainer.model.f1
import com.sv.aimlabgyrotrainer.model.f2

@Composable
private fun AxisLabels(labels: List<String>, color: Color, horizontal: Alignment.Horizontal) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = horizontal
    ) {
        labels.forEach { label ->
            Text(text = label, fontSize = 9.sp, color = color)
        }
    }
}

private fun DrawScope.drawSeries(values: List<Float>, maxValue: Float, color: Color) {
    if (values.size < 2) return
    val w = size.width
    val h = size.height
    val step = w / (SandConfig.HISTORY_SIZE - 1)
    val path = Path()
    values.forEachIndexed { index, value ->
        val x = w - (values.size - 1 - index) * step
        val y = h - (value / maxValue).coerceIn(0f, 1f) * h
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}

/** REQ. B · GRÁFICO 1: línea en tiempo real, dos series, ejes con unidades y umbral. */
@Composable
fun LiveSensorChart(
    gyroHistory: List<Float>,
    accelHistory: List<Float>,
    liveGyro: Float,
    liveAccel: Float
) {
    val cyan = Color(0xFF38BDF8)
    val amber = Color(0xFFFBBF24)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Unidades de cada eje, con el color de su línea
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "rad/s", fontSize = 11.sp, color = cyan, fontWeight = FontWeight.Bold)
            Text(text = "m/s²", fontSize = 11.sp, color = amber, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF131B2E))
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            AxisLabels(listOf("5", "2.5", "0"), cyan, Alignment.End)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .drawWithCache {
                        onDrawBehind {
                            val w = size.width
                            val h = size.height

                            // Cuadrícula
                            for (i in 0..2) {
                                val y = h * i / 2f
                                drawLine(
                                    color = Color(0xFF334155).copy(alpha = 0.7f),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Umbral de sacudida (línea punteada)
                            val thresholdY =
                                h - (SandConfig.SHAKE_THRESHOLD / SandConfig.ACCEL_SCALE_MAX).coerceIn(0f, 1f) * h
                            drawLine(
                                color = Color(0xFFEF4444).copy(alpha = 0.85f),
                                start = Offset(0f, thresholdY),
                                end = Offset(w, thresholdY),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                            )

                            drawSeries(accelHistory, SandConfig.ACCEL_SCALE_MAX, amber)
                            drawSeries(gyroHistory, SandConfig.GYRO_SCALE_MAX, cyan)
                        }
                    }
            )
            Spacer(modifier = Modifier.width(6.dp))
            AxisLabels(listOf("25", "12.5", "0"), amber, Alignment.Start)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ValueChip(
                modifier = Modifier.weight(1f),
                color = cyan,
                title = "Giroscopio",
                value = "${f2(liveGyro)} rad/s"
            )
            ValueChip(
                modifier = Modifier.weight(1f),
                color = amber,
                title = "Acelerómetro",
                value = "${f1(liveAccel)} m/s²"
            )
        }
        Text(
            text = "La línea roja es el límite: si la aceleración la pasa (${f1(SandConfig.SHAKE_THRESHOLD)} m/s²), se borra el dibujo.",
            fontSize = 11.sp,
            color = Color(0xFFF87171)
        )
    }
}

/** Cuadro pequeño con el valor actual de un sensor; el color es el mismo de su línea en el gráfico. */
@Composable
private fun ValueChip(modifier: Modifier, color: Color, title: String, value: String) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131B2E))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(text = value, fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 11.sp, color = Color(0xFFCBD5E1))
    }
}

/** REQ. B · GRÁFICO 2: barras animadas (puntos por borrado) contra la meta del usuario. */
@Composable
fun ErasesBarChart(history: List<Int>, current: Int, target: Float) {
    // Barras 0..4: dibujos ya borrados. Última barra: el dibujo que tienes ahora en pantalla.
    val pastSlots = SandConfig.MAX_ERASE_BARS
    val barCount = pastSlots + 1
    val amber = Color(0xFFFBBF24)
    val blue = Color(0xFF60A5FA)
    val cyan = Color(0xFF38BDF8)

    val animated = (0 until barCount).map { i ->
        animateFloatAsState(
            targetValue = (if (i < pastSlots) history.getOrNull(i) else current)?.toFloat() ?: 0f
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF131B2E))
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .drawWithCache {
                        onDrawBehind {
                            val w = size.width
                            val h = size.height
                            val peak = maxOf(history.maxOrNull() ?: 0, current).toFloat()
                            val maxVal = maxOf(target, peak, 1f)

                            for (i in 0..2) {
                                val y = h * i / 2f
                                drawLine(
                                    color = Color(0xFF334155).copy(alpha = 0.6f),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            val slotW = w / barCount
                            val barW = slotW * 0.55f
                            for (i in 0 until barCount) {
                                val barH = ((animated[i].value / maxVal) * h).coerceIn(0f, h)
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        if (i == pastSlots) listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                        else listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    ),
                                    topLeft = Offset(i * slotW + (slotW - barW) / 2f, h - barH),
                                    size = Size(barW, barH)
                                )
                            }

                            val targetY = h - (target / maxVal) * h
                            drawLine(
                                color = blue,
                                start = Offset(0f, targetY),
                                end = Offset(w, targetY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f))
                            )
                        }
                    }
            ) {
                if (history.isEmpty() && current == 0) {
                    Text(
                        text = "Pinta algo y sacude el teléfono\npara guardar ese dibujo en la gráfica.",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until barCount) {
                    Text(
                        text = (if (i < pastSlots) history.getOrNull(i) else current)?.toString() ?: "",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = if (i < pastSlots) amber else cyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LegendDot(color = blue, text = "Meta (${target.toInt()})")
            LegendDot(color = amber, text = "Anteriores")
            LegendDot(color = cyan, text = "Dibujo actual")
        }
        Text(
            text = "Cada barra es un dibujo. Cuando sacudes el teléfono, el dibujo actual (celeste) pasa a ser una barra naranja y empiezas uno nuevo.",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
    }
}