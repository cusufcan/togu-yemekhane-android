package com.mercan.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SeyirIcon(
    @DrawableRes glyph: Int,
    modifier: Modifier = Modifier,
    description: String? = null,
    size: Dp = 20.dp,
    tint: Color = LocalContentColor.current,
) = Icon(
    painter = painterResource(glyph),
    contentDescription = description,
    modifier = modifier.size(size),
    tint = tint,
)
