package com.aol.moneytracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Top fade gradient scrim for ultra-clean edge-to-edge scrolling.
 * Provides a smooth multi-stop cubic transition from solid background
 * to transparent, allowing scrollable items to disappear gracefully beneath the status bar.
 */
@Composable
fun TopFadeScrim(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    val bgColor = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    0.00f to bgColor.copy(alpha = 1.00f),
                    0.35f to bgColor.copy(alpha = 1.00f),
                    0.45f to bgColor.copy(alpha = 0.95f),
                    0.55f to bgColor.copy(alpha = 0.85f),
                    0.64f to bgColor.copy(alpha = 0.72f),
                    0.72f to bgColor.copy(alpha = 0.56f),
                    0.80f to bgColor.copy(alpha = 0.40f),
                    0.87f to bgColor.copy(alpha = 0.25f),
                    0.94f to bgColor.copy(alpha = 0.11f),
                    1.00f to Color.Transparent
                )
            )
    )
}

/**
 * Bottom fade gradient scrim.
 * Fades list content out gracefully before passing under the floating navigation bar
 * and system navigation bar.
 */
@Composable
fun BottomFadeScrim(
    modifier: Modifier = Modifier,
    height: Dp = 135.dp
) {
    val bgColor = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.15f to bgColor.copy(alpha = 0.15f),
                    0.30f to bgColor.copy(alpha = 0.45f),
                    0.50f to bgColor.copy(alpha = 0.75f),
                    0.70f to bgColor.copy(alpha = 0.92f),
                    0.85f to bgColor.copy(alpha = 0.98f),
                    1.0f to bgColor.copy(alpha = 1.00f)
                )
            )
    )
}
