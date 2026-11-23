package com.mercan.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mercan.app.ui.theme.LocalSeyirColors
import java.time.LocalDate

/** Haftanın günleri. Seçili günün kapsülü kayar; bugün noktayla işaretlidir. */
@Composable
fun DaySelector(
    days: List<LocalDate>,
    selected: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
) {
    val colors = LocalSeyirColors.current
    val index = days.indexOf(selected).coerceAtLeast(0)
    val position by animateFloatAsState(index.toFloat(), tween(MotionMillis), label = "day")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(28.dp))
            .padding(4.dp)
            .drawBehind {
                val width = size.width / days.size
                val at = if (layoutDirection == LayoutDirection.Rtl) days.size - 1 - position else position
                drawRoundRect(
                    color = colors.selected,
                    topLeft = Offset(width * at, 0f),
                    size = Size(width, size.height),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                )
            }
            .selectableGroup(),
    ) {
        days.forEach { day -> DayButton(day, day == selected, day == today) { onSelect(day) } }
    }
}

@Composable
private fun RowScope.DayButton(day: LocalDate, selected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val formats = rememberFormats()
    val scheme = MaterialTheme.colorScheme
    val onSelected = LocalSeyirColors.current.onSelected
    val full = formats.day(day)
    Column(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(24.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = full }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = formats.shortWeekday(day),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) onSelected else scheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = formats.number(day.dayOfMonth),
            style = MaterialTheme.typography.titleMedium.copy(
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                fontWeight = FontWeight.Bold,
            ),
            color = if (selected) onSelected else scheme.onSurface,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.clearAndSetSemantics {},
        )
        if (isToday) {
            Box(
                Modifier
                    .offset(y = (-1).dp)
                    .size(4.dp)
                    .background(if (selected) onSelected else scheme.primary, CircleShape),
            )
        }
    }
}
