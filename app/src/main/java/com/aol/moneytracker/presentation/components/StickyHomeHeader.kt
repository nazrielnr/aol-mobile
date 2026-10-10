package com.aol.moneytracker.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.R
import com.aol.moneytracker.domain.model.FinancialSummary
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter

/**
 * Sticky Top Hero Header for Home Screen:
 * - Pinned at top with status bar padding and subtle hero gradient.
 * - Left: Profile avatar with verified badge.
 * - Center: Month selector dropdown pill.
 * - Right: Notification bell button with unread indicator dot.
 * - Center Hero: Simple & clean Total Saldo, prominent balance digits, and trend status pill.
 */
@Composable
fun StickyHomeHeader(
    summary: FinancialSummary,
    selectedMonth: String = "",
    onMonthSelected: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val appColors = MoneyTrackerTheme.colors
    var isMonthDropdownOpen by remember { mutableStateOf(false) }
    val months = listOf(
        stringResource(R.string.period_aug_2026),
        stringResource(R.string.period_sep_2026),
        stringResource(R.string.period_oct_2026),
        stringResource(R.string.period_nov_2026),
        stringResource(R.string.period_all)
    )

    val heroGradient = Brush.verticalGradient(
        colors = appColors.stickyHeaderGradientStops + MaterialTheme.colorScheme.background
    )

    val isSurplus = summary.balance >= 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(heroGradient)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // 1. Top Bar Row: Profile Avatar (Left), Month Selector (Center), Notif (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Profile Avatar
            Surface(
                onClick = onProfileClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.cd_profile),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Center: Month Selector Dropdown Pill
            Box {
                Surface(
                    onClick = { isMonthDropdownOpen = true },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = selectedMonth,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.cd_select_month),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isMonthDropdownOpen,
                    onDismissRequest = { isMonthDropdownOpen = false }
                ) {
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = month,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (month == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onMonthSelected(month)
                                isMonthDropdownOpen = false
                            }
                        )
                    }
                }
            }

            // Right: Notification Bell Button with Red Unread Dot
            Surface(
                onClick = onNotificationClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.cd_notifications),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                    // Unread notification red dot badge
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .clip(CircleShape)
                            .background(appColors.notificationBadge)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Centered Balance Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_total_balance),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = CurrencyFormatter.formatRupiah(summary.balance),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trend / Comparison Pill Badge
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSurplus) {
                    appColors.income.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                },
                border = BorderStroke(
                    1.dp,
                    if (isSurplus) appColors.income.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (isSurplus) Icons.AutoMirrored.Filled.TrendingUp
                        else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isSurplus) appColors.income else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isSurplus) {
                            stringResource(R.string.sticky_header_surplus_month)
                        } else {
                            stringResource(R.string.sticky_header_deficit_month)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = if (isSurplus) appColors.income else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
