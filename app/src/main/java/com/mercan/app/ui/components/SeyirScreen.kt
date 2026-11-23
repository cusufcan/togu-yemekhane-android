package com.mercan.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mercan.app.R

private val MaxWidth = 640.dp

/** Sabit üst satır ve altında kayan içerik. Geniş ekranda içerik ortada kalır. */
@Composable
fun SeyirScreen(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    /** Boş durumlar: içerik sığıyorsa dikeyde ortalanır. */
    centered: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) = Frame(title, subtitle, onBack, actions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = if (centered) {
            Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        } else {
            Arrangement.spacedBy(12.dp)
        },
        content = content,
    )
}

/** Uzun listeler için aynı çerçeve. */
@Composable
fun SeyirListScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    state: LazyListState = rememberLazyListState(),
    spacing: Dp = 12.dp,
    content: LazyListScope.() -> Unit,
) = Frame(title, null, onBack, {}) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

@Composable
private fun Frame(
    title: String,
    subtitle: String?,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    body: @Composable () -> Unit,
) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(Modifier.widthIn(max = MaxWidth).fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (onBack == null) 16.dp else 4.dp, top = 8.dp, end = 8.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                SeyirIconButton(R.drawable.ic_seyir_back, stringResource(R.string.action_back), onBack)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            actions()
        }
        Box(Modifier.weight(1f)) { body() }
    }
}

@Composable
fun SeyirIconButton(
    @DrawableRes glyph: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
) = IconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
    SeyirIcon(
        glyph = glyph,
        modifier = iconModifier,
        description = description,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
