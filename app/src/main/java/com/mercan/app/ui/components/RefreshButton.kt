package com.mercan.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import com.mercan.app.R

/** Yenileme sürerken simgesi döner. */
@Composable
fun RefreshButton(running: Boolean, onClick: () -> Unit) {
    val spin = if (running) {
        val transition = rememberInfiniteTransition(label = "refresh")
        val angle by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "refreshAngle",
        )
        Modifier.graphicsLayer { rotationZ = angle }
    } else {
        Modifier
    }
    SeyirIconButton(
        glyph = R.drawable.ic_seyir_refresh,
        description = stringResource(R.string.action_refresh),
        onClick = onClick,
        iconModifier = spin,
    )
}
