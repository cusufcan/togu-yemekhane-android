package com.mercan.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import com.mercan.app.domain.ThemeMode
import com.mercan.app.ui.theme.YemekhaneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { YemekhaneTheme(ThemeMode.SYSTEM) { Surface {} } }
    }
}
