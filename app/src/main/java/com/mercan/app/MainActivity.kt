package com.mercan.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mercan.app.platform.WidgetUpdater
import com.mercan.app.ui.AppViewModel
import com.mercan.app.ui.navigation.AppNavHost
import com.mercan.app.ui.theme.YemekhaneTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: AppViewModel by viewModels()

    @Inject lateinit var widgets: WidgetUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { viewModel.settings.value == null }
        enableEdgeToEdge()
        // Dil değişince ekran yeniden kurulur; widget da aynı dile geçsin.
        lifecycleScope.launch { widgets.update(this@MainActivity) }
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            settings?.let {
                YemekhaneTheme(it.theme) {
                    BarIcons()
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                        AppNavHost(viewModel)
                    }
                }
            }
        }
    }

    /** Sistem çubuklarındaki simgeler uygulamanın temasına uyar, telefonunkine değil. */
    @Composable
    private fun BarIcons() {
        val light = MaterialTheme.colorScheme.surface.luminance() > 0.5f
        SideEffect {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = light
                isAppearanceLightNavigationBars = light
            }
        }
    }
}
