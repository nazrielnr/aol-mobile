package com.aol.moneytracker.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aol.moneytracker.presentation.screens.home.TransactionFilter
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme

/**
 * FilterDropdownMenu:
 * Specialized transaction filter dropdown powered by the universal [AppDropdownMenu].
 * - Full-rounded CircleShape pill trigger button matching the "Detail >" pill dimensions.
 * - Fluid popup enter/exit animation.
 * - Iconography for All, Income, and Expense filter types.
 */
@Composable
fun FilterDropdownMenu(
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
    triggerMinWidth: Dp = 0.dp
) {
    val context = LocalContext.current

    AppDropdownMenu(
        items = TransactionFilter.entries,
        selectedItem = selectedFilter,
        onItemSelected = onFilterSelected,
        dropdownName = "HomeFilterDropdown",
        itemLabel = { context.getString(it.labelRes) },
        itemIcon = { filter, isSelected ->
            val filterIcon = when (filter) {
                TransactionFilter.ALL -> Icons.Default.AllInclusive
                TransactionFilter.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown
                TransactionFilter.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
            }

            val iconTint = when {
                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                filter == TransactionFilter.INCOME -> MoneyTrackerTheme.colors.income
                filter == TransactionFilter.EXPENSE -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Icon(
                imageVector = filterIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        },
        triggerLeadingIcon = null,
        triggerShape = CircleShape,
        triggerHeight = 32.dp,
        triggerMinWidth = triggerMinWidth,
        triggerStartPadding = 10.dp,
        triggerEndPadding = 8.dp,
        triggerContentSpacing = 2.dp,
        menuAlignment = Alignment.TopEnd,
        menuWidth = 175.dp,
        popupOffsetY = 36.dp,
        modifier = modifier
    )
}
