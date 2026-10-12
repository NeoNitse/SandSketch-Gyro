package com.sv.aimlabgyrotrainer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sv.aimlabgyrotrainer.R

/** REQ. D: HUD superior con recurso de imagen vectorial, sombras y capas gráficas. */
@Composable
fun HudBar(
    statusText: String,
    onOpenMetrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.90f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .graphicsLayer { alpha = 0.95f }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sketch_logo),
                    contentDescription = "Logo SandSketch",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC)
                )
            }
        }

        Button(
            onClick = onOpenMetrics,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.shadow(6.dp, RoundedCornerShape(20.dp)),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Datos y gráficos",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
        }
    }
}

/** Pista inicial que se desvanece sola. */
@Composable
fun UsageHint(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF0F172A).copy(alpha = 0.92f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.shadow(8.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Inclina el teléfono para mover el cursor",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Deja el dedo en la pantalla para pintar · Sacude para borrar",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Doble toque para centrar el cursor",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Aviso cuando el teléfono no tiene alguno de los sensores. */
@Composable
fun SensorWarning(message: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF7F1D1D).copy(alpha = 0.92f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}