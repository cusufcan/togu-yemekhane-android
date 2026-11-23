package com.mercan.app.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.mercan.app.R
import com.mercan.app.platform.WidgetPin
import com.mercan.app.ui.components.MotionMillis
import com.mercan.app.ui.components.SeyirIcon
import com.mercan.app.ui.components.SeyirIconButton

/** Widget'ı bir kez tanıtır. Eklenince ya da kapatılınca [onDone] çağrılır ve bir daha çıkmaz. */
@Composable
fun WidgetHint(done: Boolean, onDone: () -> Unit) {
    val context = LocalContext.current
    val supported = remember(context) { WidgetPin.isSupported(context) }
    var placed by remember(context) { mutableStateOf(WidgetPin.isPlaced(context)) }
    LifecycleResumeEffect(context) {
        placed = WidgetPin.isPlaced(context)
        onPauseOrDispose {}
    }
    LaunchedEffect(placed, done) { if (placed && !done) onDone() }
    AnimatedVisibility(
        visible = supported && !placed && !done,
        enter = fadeIn(tween(MotionMillis)),
        exit = fadeOut(tween(MotionMillis)) + shrinkVertically(tween(MotionMillis)),
        label = "widgetHint",
    ) { HintCard(onAdd = { WidgetPin.request(context) }, onDismiss = onDone) }
}

@Composable
private fun HintCard(onAdd: () -> Unit, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainer)
            .clickable(onClickLabel = stringResource(R.string.widget_add), role = Role.Button, onClick = onAdd)
            .padding(start = 16.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(40.dp)
                .background(scheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) { SeyirIcon(R.drawable.ic_seyir_widget, tint = scheme.onPrimaryContainer) }
        Column(
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(stringResource(R.string.widget_hint_title), style = MaterialTheme.typography.titleSmall)
            Text(
                text = stringResource(R.string.widget_hint_body),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.widget_add),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        SeyirIconButton(R.drawable.ic_seyir_clear, stringResource(R.string.action_close), onDismiss)
    }
}
