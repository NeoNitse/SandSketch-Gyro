package com.sv.aimlabgyrotrainer.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.sv.aimlabgyrotrainer.model.SandGrain

@Composable
fun SandCanvas(
    grains: List<SandGrain>,
    penX: () -> Float,
    penY: () -> Float,
    isTouching: () -> Boolean,
    isCollapsing: () -> Boolean,
    brushColor: () -> Color,
    brushSize: () -> Float, // MEJORA 3: Grosor dinámico del cursor
    frameTick: () -> Int,
    onTouchChange: (Boolean) -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnTouchChange by rememberUpdatedState(onTouchChange)
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.maxDimension * 0.75f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF020408)),
                        center = c,
                        radius = r
                    )
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        currentOnTouchChange(true)
                        tryAwaitRelease()
                        currentOnTouchChange(false)
                    },
                    onDoubleTap = {
                        currentOnDoubleTap()
                    }
                )
            }
            .drawWithCache {
                onDrawBehind {
                    @Suppress("UNUSED_VARIABLE")
                    val tick = frameTick()

                    val center = Offset(size.width / 2f, size.height / 2f)

                    drawLine(
                        Color(0xFF1E293B).copy(alpha = 0.35f),
                        Offset(0f, center.y),
                        Offset(size.width, center.y),
                        strokeWidth = 1f
                    )
                    drawLine(
                        Color(0xFF1E293B).copy(alpha = 0.35f),
                        Offset(center.x, 0f),
                        Offset(center.x, size.height),
                        strokeWidth = 1f
                    )

                    grains.forEach { g ->
                        drawCircle(
                            color = g.color,
                            radius = g.radius,
                            center = Offset(center.x + g.x, center.y + g.y)
                        )
                    }

                    if (!isCollapsing()) {
                        val touching = isTouching()
                        val cursorCenter = Offset(center.x + penX(), center.y + penY())
                        val ringColor = if (touching) Color(0xFF10B981) else Color(0xFF38BDF8)

                        // MEJORA 3: El anillo y el núcleo crecen con el tamaño del pincel
                        val currentSize = brushSize()
                        val innerRadius = currentSize / 2f
                        val mainRingRadius = maxOf(currentSize * 1.35f, 12f)
                        val outerGlowRadius = mainRingRadius * 1.6f

                        drawCircle(color = ringColor.copy(alpha = 0.08f), radius = outerGlowRadius * 1.5f, center = cursorCenter)
                        drawCircle(color = ringColor.copy(alpha = 0.18f), radius = outerGlowRadius, center = cursorCenter)
                        drawCircle(color = ringColor, radius = mainRingRadius, center = cursorCenter, style = Stroke(width = 2.5f))

                        // Núcleo que muestra la anchura real que dejará el grano de arena
                        drawCircle(
                            color = if (touching) brushColor() else Color.White,
                            radius = innerRadius,
                            center = cursorCenter
                        )
                    }
                }
            }
    )
}