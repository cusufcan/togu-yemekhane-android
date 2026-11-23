package com.mercan.app.ui.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mercan.app.R
import com.mercan.app.domain.DishHit
import com.mercan.app.domain.WeekMenu
import com.mercan.app.domain.plate
import com.mercan.app.ui.components.EmptyState
import com.mercan.app.ui.components.FirstError
import com.mercan.app.ui.components.SeyirIcon
import com.mercan.app.ui.components.SeyirIconButton
import com.mercan.app.ui.components.SeyirListScreen
import com.mercan.app.ui.components.SeyirScreen
import com.mercan.app.ui.components.Skeleton
import com.mercan.app.ui.components.WeekRows
import com.mercan.app.ui.components.rememberFormats
import java.time.LocalDate
import java.time.YearMonth

private const val MainDishesShown = 3

@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    onOpen: (LocalDate) -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val field = rememberTextFieldState()
    LaunchedEffect(field) {
        snapshotFlow { field.text.toString() }.collect(viewModel::search)
    }
    val ui = state ?: return
    val title = stringResource(R.string.archive_title)
    if (ui.weeks.isEmpty()) {
        val loading = ui.download == Download.RUNNING
        SeyirScreen(title, onBack = onBack, centered = !loading) {
            when (ui.download) {
                Download.RUNNING -> Skeleton()
                Download.FAILED -> FirstError(viewModel::refresh)
                Download.DONE -> EmptyState(
                    glyph = R.drawable.ic_seyir_archive,
                    title = stringResource(R.string.archive_empty_title),
                    body = stringResource(R.string.archive_empty_body),
                )
            }
        }
        return
    }

    val months = remember(ui.weeks) { ui.weeks.groupBy { YearMonth.from(it.weekStart) }.toList() }
    val formats = rememberFormats()
    // Arama ile liste ayrı yerlerde kayar; aramadan çıkınca liste kaldığı yerdedir.
    val searching = ui.query.isNotBlank()
    val listState = rememberLazyListState()
    val hitsState = rememberLazyListState()
    LaunchedEffect(ui.query) { hitsState.scrollToItem(0) }
    SeyirListScreen(title, onBack = onBack, state = if (searching) hitsState else listState) {
        item(key = "search") { SearchField(field) }
        if (!searching) {
            months.forEach { (month, weeks) ->
                item(key = month.toString()) { Header(formats.month(month)) }
                items(weeks, key = { it.weekStart.toString() }) { week ->
                    ArchiveRow(
                        title = AnnotatedString(formats.range(week)),
                        subtitle = week.summary(),
                        onClick = { onOpen(week.weekStart) },
                    )
                }
            }
        } else if (ui.hits.isEmpty()) {
            item(key = "none") {
                EmptyState(
                    glyph = R.drawable.ic_seyir_search,
                    title = stringResource(R.string.archive_none_title),
                    body = stringResource(R.string.archive_none_body),
                    modifier = Modifier.padding(top = 48.dp),
                )
            }
        } else {
            item(key = "hits") {
                val days = ui.hits.map { it.date }.distinct()
                Header(
                    pluralStringResource(R.plurals.archive_hits, days.size, days.size, formats.dayMonth(days.max())),
                )
            }
            itemsIndexed(ui.hits, key = { index, hit -> "$index/${hit.date}" }) { _, hit ->
                ArchiveRow(
                    title = hit.marked(),
                    subtitle = formats.hit(hit.date),
                    onClick = { onOpen(hit.weekStart) },
                )
            }
        }
    }
}

/** Haftanın ilk birkaç ana yemeği. */
private fun WeekMenu.summary(): String {
    val mains = days.filter { it.dishes.isNotEmpty() }.mapNotNull { it.plate().title }
    val shown = mains.take(MainDishesShown).joinToString(", ")
    return if (mains.size > MainDishesShown) "$shown…" else shown
}

@Composable
private fun DishHit.marked(): AnnotatedString = buildAnnotatedString {
    val scheme = MaterialTheme.colorScheme
    val name = dish
    append(name.substring(0, match.first))
    withStyle(SpanStyle(color = scheme.onPrimaryContainer, background = scheme.primaryContainer)) {
        append(name.substring(match.first, match.last + 1))
    }
    append(name.substring(match.last + 1))
}

@Composable
private fun Header(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier
        .padding(start = 4.dp, top = 8.dp, end = 4.dp)
        .semantics { heading() },
)

@Composable
private fun ArchiveRow(title: AnnotatedString, subtitle: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainer)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
        }
        SeyirIcon(R.drawable.ic_seyir_next, tint = scheme.onSurfaceVariant)
    }
}

@Composable
private fun SearchField(state: TextFieldState) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(26.dp)
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val focus = LocalFocusManager.current
    val label = stringResource(R.string.archive_search)
    BasicTextField(
        state = state,
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
        cursorBrush = SolidColor(scheme.primary),
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = { focus.clearFocus() },
        interactionSource = interactions,
        decorator = { inner ->
            Row(
                modifier = Modifier
                    .heightIn(min = 52.dp)
                    .clip(shape)
                    .background(scheme.surfaceContainerHigh)
                    .then(if (focused) Modifier.border(2.dp, scheme.primary, shape) else Modifier)
                    .padding(start = 18.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SeyirIcon(R.drawable.ic_seyir_search, tint = scheme.onSurfaceVariant)
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    inner()
                }
                if (state.text.isNotEmpty()) ClearButton { state.clearText() }
            }
        },
    )
}

@Composable
private fun ClearButton(onClick: () -> Unit) = SeyirIconButton(
    glyph = R.drawable.ic_seyir_clear,
    description = stringResource(R.string.archive_clear),
    onClick = onClick,
)

@Composable
fun ArchiveWeekScreen(onBack: () -> Unit, viewModel: ArchiveWeekViewModel = hiltViewModel()) {
    val state by viewModel.week.collectAsStateWithLifecycle()
    val week = state ?: return
    SeyirScreen(rememberFormats().range(week), onBack = onBack) { WeekRows(week) }
}
