package com.aol.moneytracker.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.TransactionType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    type: TransactionType,
    selectedCategoryId: String,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = if (type == TransactionType.INCOME) {
        Category.INCOME_CATEGORIES
    } else {
        Category.EXPENSE_CATEGORIES
    }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = cat.id == selectedCategoryId
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat) },
                label = { Text(stringResource(cat.nameRes)) },
                leadingIcon = {
                    Icon(
                        imageVector = getCategoryIcon(cat.iconKey),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
