package com.sv.aimlabgyrotrainer.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sv.aimlabgyrotrainer.model.SandConfig
import com.sv.aimlabgyrotrainer.viewmodel.SandViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val SECTIONS = listOf("Sensores", "Mi meta", "Análisis", "Pincel")

/**
 * Panel inferior (ModalBottomSheet de Material 3).
 * En vez de poner todo en una sola lista larga, cada cosa tiene su pestaña:
 * Sensores (gráfico 1), Mi meta (gráfico 2), Análisis (Top-Down / Bottom-Up) y Pincel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsSheet(
    viewModel: SandViewModel,
    onDismiss: () -> Unit
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var section by remember { mutableIntStateOf(0) }
    var analysisTab by remember { mutableIntStateOf(0) }
    var topExpanded by remember { mutableStateOf(true) }
    var expandedEventId by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        while (isActive) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val target = viewModel.targetInput.toFloatOrNull()?.coerceIn(1f, SandConfig.MAX_GRAINS.toFloat()) ?: 300f
    val stats = viewModel.sessionStats(nowMs)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0D1322)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Datos y gráficos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )

            TabRow(
                selectedTabIndex = section,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF38BDF8)
            ) {
                SECTIONS.forEachIndexed { index, name ->
                    Tab(
                        selected = section == index,
                        onClick = { section = index },
                        text = { Text(name, fontSize = 13.sp, maxLines = 1) }
                    )
                }
            }

            Crossfade(targetState = section, label = "pestanas") { page ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (page) {
                        0 -> {
                            // REQ. B · GRÁFICO 1: línea en tiempo real
                            Text(
                                text = "Esto es lo que van midiendo los dos sensores en los últimos 5 segundos.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            LiveSensorChart(
                                gyroHistory = viewModel.gyroHistory,
                                accelHistory = viewModel.accelHistory,
                                liveGyro = viewModel.liveGyro,
                                liveAccel = viewModel.liveAccel
                            )
                        }

                        1 -> {
                            // REQ. B · GRÁFICO 2: barras con datos que escribe el usuario
                            Text(
                                text = "Escribe cuántos puntos quieres pintar en un dibujo y mira cómo va cada uno contra la línea azul.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            OutlinedTextField(
                                value = viewModel.targetInput,
                                onValueChange = { viewModel.updateTargetInput(it) },
                                label = { Text("Mi meta: puntos por dibujo") },
                                supportingText = { Text("De 1 a ${SandConfig.MAX_GRAINS}, que es lo que cabe en el lienzo") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            ErasesBarChart(
                                history = viewModel.eraseHistory,
                                current = viewModel.sandGrains.size,
                                target = target
                            )
                        }

                        2 -> {
                            // REQ. C · ANÁLISIS TOP-DOWN / BOTTOM-UP
                            AnalysisModule(
                                stats = stats,
                                target = target,
                                currentGrains = viewModel.sandGrains.size,
                                eraseHistory = viewModel.eraseHistory,
                                events = viewModel.eventLogs,
                                liveAccel = viewModel.liveAccel,
                                selectedTab = analysisTab,
                                onTabChange = { analysisTab = it },
                                topExpanded = topExpanded,
                                onTopToggle = { topExpanded = !topExpanded },
                                expandedEventId = expandedEventId,
                                onEventToggle = { id -> expandedEventId = if (expandedEventId == id) -1 else id }
                            )
                        }

                        else -> {
                            BrushSettings(
                                brushSize = viewModel.brushSize,
                                onSize = { viewModel.updateBrushSize(it) },
                                sensitivity = viewModel.sensitivity,
                                onSensitivity = { viewModel.updateSensitivity(it) },
                                selectedColor = viewModel.brushColor,
                                onColor = { viewModel.updateBrushColor(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrushSettings(
    brushSize: Float,
    onSize: (Float) -> Unit,
    sensitivity: Float,
    onSensitivity: (Float) -> Unit,
    selectedColor: Color,
    onColor: (Color) -> Unit
) {
    val palette = listOf(
        Color(0xFFFBBF24),
        Color(0xFF38BDF8),
        Color(0xFF34D399),
        Color(0xFFF472B6),
        Color(0xFFF8FAFC)
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Grosor: ${brushSize.toInt()} px",
            fontSize = 13.sp,
            color = Color(0xFFCBD5E1)
        )
        Slider(value = brushSize, onValueChange = onSize, valueRange = 4f..16f)

        Text(
            text = "Qué tan rápido se mueve el cursor: ${(sensitivity / SandConfig.DEFAULT_SENSITIVITY * 100f).toInt()}%",
            fontSize = 13.sp,
            color = Color(0xFFCBD5E1)
        )
        Slider(value = sensitivity, onValueChange = onSensitivity, valueRange = 500f..2200f)

        Text(text = "Color", fontSize = 13.sp, color = Color(0xFFCBD5E1))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            palette.forEach { c ->
                val selected = c == selectedColor
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) Color.White else Color(0xFF334155),
                            shape = CircleShape
                        )
                        .clickable { onColor(c) }
                )
            }
        }
    }
}