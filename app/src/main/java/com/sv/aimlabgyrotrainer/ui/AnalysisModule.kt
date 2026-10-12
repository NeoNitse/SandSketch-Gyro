package com.sv.aimlabgyrotrainer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sv.aimlabgyrotrainer.model.SessionStats
import com.sv.aimlabgyrotrainer.model.TelemetryEvent
import com.sv.aimlabgyrotrainer.model.f1
import com.sv.aimlabgyrotrainer.model.f2

private val CardColor = Color(0xFF1E293B)
private val Cyan = Color(0xFF38BDF8)
private val Amber = Color(0xFFFBBF24)
private val TextSoft = Color(0xFFCBD5E1)
private val TextMuted = Color(0xFF94A3B8)

/**
 * REQ. C: análisis estructurado.
 * Top-Down: primero el resultado general y después el detalle de cada sensor.
 * Bottom-Up: se empieza en un evento puntual y se llega a una conclusión.
 */
@Composable
fun AnalysisModule(
    stats: SessionStats,
    target: Float,
    currentGrains: Int,
    eraseHistory: List<Int>,
    events: List<TelemetryEvent>,
    liveAccel: Float,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    topExpanded: Boolean,
    onTopToggle: () -> Unit,
    expandedEventId: Int,
    onEventToggle: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ModePills(selected = selectedTab, onSelect = onTabChange)

        Text(
            text = if (selectedTab == 0) {
                "Primero el resultado general y después el detalle."
            } else {
                "Se empieza en un evento y se llega a una conclusión."
            },
            fontSize = 12.sp,
            color = TextMuted
        )

        if (selectedTab == 0) {
            TopDownCard(stats, target, currentGrains, eraseHistory, topExpanded, onTopToggle)
        } else {
            BottomUpList(events, liveAccel, expandedEventId, onEventToggle)
        }
    }
}

/** Selector de dos opciones (Top-Down / Bottom-Up) dibujado con modificadores. */
@Composable
private fun ModePills(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardColor)
            .padding(4.dp)
    ) {
        listOf("Top-Down", "Bottom-Up").forEachIndexed { index, label ->
            val isSelected = selected == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Cyan else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF0F172A) else TextSoft
                )
            }
        }
    }
}

@Composable
private fun TopDownCard(
    stats: SessionStats,
    target: Float,
    currentGrains: Int,
    eraseHistory: List<Int>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    // La meta es de puntos pintados por dibujo, así que se compara con el dibujo que hay ahora en pantalla
    val progress = if (target > 0f) currentGrains / target else 0f
    val headline = when {
        stats.totalGrains == 0 -> "Todavía no has dibujado nada"
        currentGrains == 0 -> "Lienzo vacío, empieza otro dibujo"
        currentGrains >= target -> "¡Meta cumplida en este dibujo!"
        else -> "Vas en un ${(progress * 100f).toInt()}% de tu meta"
    }
    val reached = eraseHistory.count { it.toFloat() >= target }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onToggle() }
                .padding(16.dp)
        ) {
            Text(
                text = headline,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Barra de avance hacia la meta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF334155))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(Cyan, Color(0xFF34D399))))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$currentGrains de ${target.toInt()} puntos en el dibujo actual",
                fontSize = 12.sp,
                color = TextSoft
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (expanded) "Toca para ocultar los detalles" else "Toca para ver los detalles",
                fontSize = 11.sp,
                color = TextMuted
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GroupLabel("DIBUJO")
                    DetailRow("Tiempo usando la app", "${stats.seconds} s")
                    DetailRow("Puntos pintados en total", "${stats.totalGrains}")
                    DetailRow("Veces que borraste", "${stats.erases}")
                    DetailRow("Anteriores que llegaron a la meta", "$reached de ${eraseHistory.size}")

                    Spacer(modifier = Modifier.height(4.dp))
                    GroupLabel("GIROSCOPIO")
                    DetailRow("Giro más rápido", "${f2(stats.maxGyro)} rad/s")
                    DetailRow("Giro promedio", "${f2(stats.avgGyro)} rad/s")

                    Spacer(modifier = Modifier.height(4.dp))
                    GroupLabel("ACELERÓMETRO")
                    DetailRow(
                        "Sacudida más fuerte",
                        if (stats.maxShake > 0f) "${f1(stats.maxShake)} m/s²" else "–"
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomUpList(
    events: List<TelemetryEvent>,
    liveAccel: Float,
    expandedEventId: Int,
    onEventToggle: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (events.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Todavía no pasa nada",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sacude el teléfono o gíralo rápido y aquí aparece lo que detectó. " +
                                "Ahora mismo el acelerómetro marca ${f1(liveAccel)} m/s².",
                        fontSize = 12.sp,
                        color = TextSoft
                    )
                }
            }
        } else {
            events.take(8).forEach { event ->
                val expanded = expandedEventId == event.id
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .clickable { onEventToggle(event.id) }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${event.timestamp} · ${event.type}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${f1(event.magnitude)} ${event.unit}",
                                fontWeight = FontWeight.Bold,
                                color = Amber,
                                fontSize = 14.sp
                            )
                        }
                        if (!expanded) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Toca para ver qué significa",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StepText("1. Lo que se midió", event.reading)
                                StepText("2. Qué significa", event.interpretation)
                                StepText("3. Conclusión", event.conclusion)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Cyan)
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSoft)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun StepText(label: String, body: String) {
    Column {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Cyan)
        Text(text = body, fontSize = 13.sp, color = TextSoft)
    }
}