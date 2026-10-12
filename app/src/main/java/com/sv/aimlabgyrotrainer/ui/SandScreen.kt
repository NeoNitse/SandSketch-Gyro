package com.sv.aimlabgyrotrainer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.sv.aimlabgyrotrainer.model.SandConfig
import com.sv.aimlabgyrotrainer.viewmodel.SandViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * CAPA DE PRESENTACIÓN
 * Conecta el SandViewModel con los componentes visuales:
 * Lienzo (SandCanvas), HUD flotante, panel modal (MetricsSheet) y avisos.
 */
@Composable
fun SandScreen(viewModel: SandViewModel) {
    val haptic = LocalHapticFeedback.current

    var showMetrics by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(8000L)
        showHint = false
    }
    LaunchedEffect(viewModel.isTouching) {
        if (viewModel.isTouching) showHint = false
    }
    LaunchedEffect(viewModel.shakeSignal) {
        if (viewModel.shakeSignal > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()

        LaunchedEffect(screenW, screenH) {
            viewModel.setCanvasSize(screenW, screenH)
        }

        // Bucle de animación a 60 FPS coordinado con el ritmo de pantalla
        LaunchedEffect(viewModel.isCollapsing) {
            if (viewModel.isCollapsing) {
                viewModel.prepareCollapse()
                while (viewModel.sandGrains.isNotEmpty() && isActive) {
                    withFrameNanos {
                        viewModel.stepCollapse(screenH)
                    }
                }
                viewModel.finishCollapse()
            }
        }

        SandCanvas(
            grains = viewModel.sandGrains,
            penX = { viewModel.penX },
            penY = { viewModel.penY },
            isTouching = { viewModel.isTouching },
            isCollapsing = { viewModel.isCollapsing },
            brushColor = { viewModel.brushColor },
            brushSize = { viewModel.brushSize },
            frameTick = { viewModel.frameTick },
            onTouchChange = { viewModel.setTouching(it) },
            onDoubleTap = { viewModel.recenterCursor() }
        )

        val statusText = when {
            viewModel.isCollapsing -> "Vaciando..."
            viewModel.sandGrains.size >= SandConfig.MAX_GRAINS -> "Lienzo lleno, sacude para borrar"
            else -> "Arena: ${viewModel.sandGrains.size}"
        }

        HudBar(
            statusText = statusText,
            onOpenMetrics = {
                viewModel.setTouching(false)
                showMetrics = true
            }
        )

        AnimatedVisibility(
            visible = showHint,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            UsageHint()
        }

        if (!viewModel.hasGyroscope || !viewModel.hasAccelerometer) {
            SensorWarning(
                message = if (!viewModel.hasGyroscope) {
                    "Tu teléfono no tiene giroscopio, así que el cursor no se puede mover."
                } else {
                    "Tu teléfono no tiene acelerómetro, así que no se puede borrar sacudiendo."
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }

        if (showMetrics) {
            MetricsSheet(
                viewModel = viewModel,
                onDismiss = { showMetrics = false }
            )
        }
    }
}