package com.mercan.app.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mercan.app.R
import com.mercan.app.ui.components.MotionMillis
import com.mercan.app.ui.components.SeyirIcon
import kotlin.math.roundToInt

enum class AppTab(@param:StringRes val label: Int, @param:DrawableRes val glyph: Int) {
    TODAY(R.string.tab_today, R.drawable.ic_seyir_today),
    WEEK(R.string.tab_week, R.drawable.ic_seyir_week),
    SETTINGS(R.string.tab_settings, R.drawable.ic_seyir_settings),
}

private val PillWidth = 48.dp
private val PillHeight = 26.dp
private val PillTop = 10.dp

/** Alt sekmeler. Seçili sekmenin kapsülü yerine kayar. */
@Composable
fun AppTabs(active: AppTab, onOpen: (AppTab) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val position by animateFloatAsState(active.ordinal.toFloat(), tween(MotionMillis), label = "tab")
    Column(
        Modifier
            .fillMaxWidth()
            .background(scheme.surfaceContainerHigh)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = scheme.outlineVariant)
        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            val tabWidth = maxWidth / AppTab.entries.size
            Box(
                Modifier
                    .offset {
                        val x = tabWidth.toPx() * position + (tabWidth - PillWidth).toPx() / 2
                        IntOffset(x.roundToInt(), PillTop.roundToPx())
                    }
                    .size(PillWidth, PillHeight)
                    .background(scheme.primaryContainer, CircleShape),
            )
            Row(Modifier.fillMaxWidth().selectableGroup()) {
                AppTab.entries.forEach { tab -> TabItem(tab, tab == active) { onOpen(tab) } }
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(tab: AppTab, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val label = stringResource(tab.label)
    val interactions = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 60.dp)
            .semantics { contentDescription = label }
            .selectable(
                selected = selected,
                interactionSource = interactions,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(top = PillTop, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(PillWidth, PillHeight)
                .clip(CircleShape)
                .indication(interactions, ripple()),
            contentAlignment = Alignment.Center,
        ) {
            SeyirIcon(tab.glyph, tint = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}
