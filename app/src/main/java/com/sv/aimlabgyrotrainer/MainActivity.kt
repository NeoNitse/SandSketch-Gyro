package com.sv.aimlabgyrotrainer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sv.aimlabgyrotrainer.ui.SandScreen
import com.sv.aimlabgyrotrainer.viewmodel.SandViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SandViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // La pantalla no se apaga mientras se usa la pizarra
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF04060E)
                ) {
                    SandScreen(viewModel = viewModel)
                }
            }
        }

        // Modo inmersivo: toda la pantalla es la pizarra (las barras aparecen al deslizar)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onResume() {
        super.onResume()
        viewModel.startSensors()
    }

    override fun onPause() {
        viewModel.stopSensors()
        super.onPause()
    }
}