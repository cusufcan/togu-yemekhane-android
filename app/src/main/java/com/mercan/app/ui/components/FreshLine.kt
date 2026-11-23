package com.mercan.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mercan.app.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Menünün en son ne zaman indirildiği. Henüz bilinmiyorsa yer kaplamaz. */
@Composable
fun FreshLine(lastSuccess: Instant?, today: LocalDate) {
    if (lastSuccess == null) return
    val formats = rememberFormats()
    val at = lastSuccess.atZone(ZoneId.systemDefault())
    val time = formats.timeInSentence(at.toLocalTime())
    val text = when (at.toLocalDate()) {
        today -> stringResource(R.string.updated_today, time)
        today.minusDays(1) -> stringResource(R.string.updated_yesterday, time)
        else -> stringResource(R.string.updated_on, formats.dayMonth(at.toLocalDate()), time)
    }
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SeyirIcon(R.drawable.ic_seyir_clock, size = 15.dp, tint = color)
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}
