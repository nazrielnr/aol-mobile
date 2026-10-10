package com.aol.moneytracker.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aol.moneytracker.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedTypeSwitch(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
    val selectedIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onTypeSelected(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                label = { Text(stringResource(type.labelRes)) }
            )
        }
    }
}
