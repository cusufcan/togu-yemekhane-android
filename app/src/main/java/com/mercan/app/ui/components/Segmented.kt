package com.mercan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mercan.app.ui.theme.LocalSeyirColors

/** Yan yana birkaç seçenekten biri. */
@Composable
fun <T> Segmented(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val colors = LocalSeyirColors.current
    val frame = Modifier
        .padding(top = 10.dp)
        .fillMaxWidth()
        .background(scheme.surfaceContainerHigh, RoundedCornerShape(24.dp))
        .padding(4.dp)
        .selectableGroup()
    val choice = @Composable { option: T, modifier: Modifier ->
        val active = option == selected
        Box(
            modifier = modifier
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (active) colors.selected else Color.Transparent)
                .selectable(selected = active, role = Role.RadioButton) { onSelect(option) }
                .padding(horizontal = 4.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label(option),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                color = if (active) colors.onSelected else scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
    if (largeText()) {
        Column(frame) { options.forEach { choice(it, Modifier.fillMaxWidth()) } }
    } else {
        Row(frame) { options.forEach { choice(it, Modifier.weight(1f)) } }
    }
}

/** Yazı, yan yana düzene sığmayacak kadar büyük mü? */
@Composable
fun largeText(): Boolean = LocalDensity.current.fontScale >= 1.5f
