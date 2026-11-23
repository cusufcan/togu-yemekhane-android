package com.mercan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mercan.app.R
import com.mercan.app.domain.WeekMenu
import com.mercan.app.domain.plate
import com.mercan.app.domain.shownDates
import java.time.LocalDate

/** Haftanın bütün günleri. [today] verilirse o günün satırı vurgulanır. */
@Composable
fun WeekRows(week: WeekMenu, today: LocalDate? = null, onDay: ((LocalDate) -> Unit)? = null) {
    week.shownDates().forEach { date ->
        WeekRow(
            week = week,
            date = date,
            highlighted = date == today,
            onClick = onDay?.let { open -> { open(date) } },
        )
    }
}

@Composable
private fun WeekRow(week: WeekMenu, date: LocalDate, highlighted: Boolean, onClick: (() -> Unit)?) {
    val formats = rememberFormats()
    val scheme = MaterialTheme.colorScheme
    val plate = week.menuOn(date)?.plate()
    val content = if (highlighted) scheme.onPrimaryContainer else scheme.onSurface
    val quiet = if (highlighted) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val full = formats.day(date)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (highlighted) scheme.primaryContainer else scheme.surfaceContainer)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 36.dp)
                .clearAndSetSemantics { contentDescription = full },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(formats.shortWeekday(date), style = MaterialTheme.typography.labelSmall, color = quiet)
            Text(
                text = formats.number(date.dayOfMonth),
                style = MaterialTheme.typography.titleLarge,
                color = content,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = plate?.title ?: stringResource(R.string.no_menu_day_title),
                style = MaterialTheme.typography.titleMedium,
                color = if (plate == null) quiet else content,
            )
            val rest = plate?.rest.orEmpty().joinToString(", ") { it.name }
            if (rest.isNotEmpty()) {
                Text(rest, style = MaterialTheme.typography.bodySmall, color = quiet)
            }
        }
    }
}
