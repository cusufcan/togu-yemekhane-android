package com.mercan.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mercan.app.R
import com.mercan.app.ui.theme.LocalSeyirColors

private val ButtonPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
private val ButtonShape = RoundedCornerShape(24.dp)

/** Simge, başlık ve açıklama: ekranda gösterecek bir şey yokken. */
@Composable
fun EmptyState(
    @DrawableRes glyph: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) = Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        SeyirIcon(glyph, size = 26.dp, tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
    Text(title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
    Text(
        text = body,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    action()
}

@Composable
fun TonalButton(text: String, onClick: () -> Unit) = FilledTonalButton(
    onClick = onClick,
    modifier = Modifier.heightIn(min = 48.dp),
    shape = ButtonShape,
    colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    contentPadding = ButtonPadding,
) { Text(text) }

@Composable
fun RetryButton(onClick: () -> Unit) = Button(
    onClick = onClick,
    modifier = Modifier.heightIn(min = 48.dp),
    shape = ButtonShape,
    contentPadding = ButtonPadding,
) {
    SeyirIcon(R.drawable.ic_seyir_refresh)
    Spacer(Modifier.width(8.dp))
    Text(stringResource(R.string.action_retry))
}

/** Telefonda hiç menü yokken indirme başarısız oldu. */
@Composable
fun FirstError(onRetry: () -> Unit) = EmptyState(
    glyph = R.drawable.ic_seyir_offline,
    title = stringResource(R.string.first_error_title),
    body = stringResource(R.string.first_error_body),
) { RetryButton(onRetry) }

/** İlk indirme sürerken menünün yerini tutan kutular. */
@Composable
fun Skeleton() {
    val alpha by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    val loading = stringResource(R.string.loading)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = loading }
            .graphicsLayer { this.alpha = alpha },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Block(60.dp, 28.dp)
        Block(148.dp, 28.dp)
        Block(152.dp, 20.dp)
        Block(64.dp, 20.dp)
    }
}

@Composable
private fun Block(height: Dp, radius: Dp) = Box(
    Modifier
        .fillMaxWidth()
        .height(height)
        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(radius)),
)

/** Bu haftanın menüsü yokken, eldeki eski menünün üstündeki açıklama. */
@Composable
fun StaleCard(title: String, body: String) {
    val colors = LocalSeyirColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.warmContainer, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SeyirIcon(R.drawable.ic_seyir_offline, tint = colors.onWarmContainer)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onWarmContainer)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.onWarmContainer)
        }
    }
}
