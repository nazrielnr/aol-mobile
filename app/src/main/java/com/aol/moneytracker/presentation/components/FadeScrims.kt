package com.aol.moneytracker.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.aol.moneytracker.R

/**
 * Global Top Fade Gradient Scrim for edge-to-edge scrolling.
 * - Hidden (`alpha = 0f`) at initial rest position (`isScrolled = false`) so top content is completely untouched.
 * - Smoothly fades in (`alpha = 1f`) as soon as scrolling begins (`isScrolled = true`), keeping the status bar
 *   and TopBar solid while feathering out seamlessly below the TopBar so scrolled content melts into the header.
 */
@Composable
fun TopFadeScrim(
    modifier: Modifier = Modifier,
    height: Dp = 112.dp,
    isScrolled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.background
) {
    val fadeAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "TopFadeScrimAlpha"
    )

    if (fadeAlpha > 0.005f) {
        val brush = remember(color) {
            Brush.verticalGradient(
                0.00f to color.copy(alpha = 1.00f),
                0.35f to color.copy(alpha = 0.99f),
                0.48f to color.copy(alpha = 0.95f),
                0.58f to color.copy(alpha = 0.86f),
                0.67f to color.copy(alpha = 0.70f),
                0.75f to color.copy(alpha = 0.52f),
                0.82f to color.copy(alpha = 0.34f),
                0.89f to color.copy(alpha = 0.18f),
                0.95f to color.copy(alpha = 0.06f),
                1.00f to Color.Transparent
            )
        }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer { alpha = fadeAlpha }
                .background(brush = brush)
        )
    }
}

/**
 * Bottom fade gradient scrim.
 * Fades list content out gracefully before passing under the floating navigation bar
 * or bottom action buttons without any boxy card border.
 */
@Composable
fun BottomFadeScrim(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    val bgColor = MaterialTheme.colorScheme.background
    val brush = remember(bgColor) {
        Brush.verticalGradient(
            0.00f to Color.Transparent,
            0.16f to bgColor.copy(alpha = 0.18f),
            0.34f to bgColor.copy(alpha = 0.48f),
            0.54f to bgColor.copy(alpha = 0.78f),
            0.74f to bgColor.copy(alpha = 0.94f),
            1.00f to bgColor.copy(alpha = 1.00f)
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush = brush)
    )
}

/**
 * Universal Sub-Screen Scaffold used across secondary screens (AddTransactionScreen, AddWalletScreen,
 * TransferWalletScreen, SettingsScreen, etc.).
 * Automatically tracks scroll position so TopFadeScrim is 0% at rest (before scrolling) and smoothly
 * appears with a deep, unified feather below the TopAppBar once scrolled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSubScreenScaffold(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isScrolledOverride: Boolean? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: (@Composable BoxScope.(navigationBarPadding: Dp) -> Unit)? = null,
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeBottomHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val effectiveBottomInset = maxOf(navigationBarHeight, imeBottomHeight)
    val isImeVisible = imeBottomHeight > navigationBarHeight + 24.dp

    // Top bar occupies statusBarHeight + 64.dp; TopFadeScrim extends 48.dp below the TopAppBar
    // so when scrolled, the TopAppBar and content below it blend seamlessly without a hard cutoff.
    val topBarTotalHeight = statusBarHeight + 62.dp
    val topScrimHeight = statusBarHeight + 112.dp
    val bottomBarActionHeight = if (bottomBar != null) 74.dp else 0.dp

    var scrolledDownPx by remember { mutableFloatStateOf(0f) }
    val scrollTrackerConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0.5f) {
                    // Inner list reached the very top and had unconsumed upward scroll
                    scrolledDownPx = 0f
                } else if (consumed.y != 0f) {
                    scrolledDownPx = (scrolledDownPx - consumed.y).coerceAtLeast(0f)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y > 10f) {
                    scrolledDownPx = 0f
                }
                return Velocity.Zero
            }
        }
    }

    val isScrolled = isScrolledOverride ?: (scrolledDownPx > 4f)

    // When IME keyboard is visible, bound the scrollable container's viewport strictly between
    // TopAppBar and BottomBar+IME so Compose's BringIntoView never lets any TextField overlap
    // with the TopAppBar, BottomBar, or Keyboard.
    val containerTopPadding = if (isImeVisible) topBarTotalHeight else 0.dp
    val containerBottomPadding = if (isImeVisible) effectiveBottomInset + bottomBarActionHeight else 0.dp

    val contentTopPadding = if (isImeVisible) 6.dp else topBarTotalHeight
    val contentBottomPadding = if (isImeVisible) {
        20.dp
    } else if (bottomBar != null) {
        navigationBarHeight + 96.dp
    } else {
        navigationBarHeight + 48.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollTrackerConnection)
    ) {
        // Layer 1: Screen Scrollable Content (strictly bounded when IME is active)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = containerTopPadding,
                    bottom = containerBottomPadding
                )
        ) {
            content(
                PaddingValues(
                    top = contentTopPadding,
                    bottom = contentBottomPadding
                )
            )
        }

        // Layer 2: Scroll-Reactive Top Fade Scrim + TopAppBar
        TopFadeScrim(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            height = topScrimHeight,
            isScrolled = isScrolled
        )

        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 10.dp)
                )
            },
            navigationIcon = {
                UniversalBackButton(onClick = onBackClick)
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 12.dp),
            windowInsets = WindowInsets(0.dp)
        )

        // Layer 3: Optional Seamless Bottom Action Bar with Gradient Fade (no card/border)
        if (bottomBar != null) {
            BottomFadeScrim(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                height = effectiveBottomInset + 88.dp
            )
            bottomBar(effectiveBottomInset)
        }
    }
}

@Composable
fun UniversalBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f),
        border = BorderStroke(
            width = 0.8.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
