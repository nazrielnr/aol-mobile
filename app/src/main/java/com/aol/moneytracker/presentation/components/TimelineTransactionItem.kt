package com.aol.moneytracker.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.CurrencyFormatter
import com.aol.moneytracker.util.DateFormatter

/**
 * High-end financial timeline activity item with connecting vertical lines,
 * category status nodes, and structured statement cards.
 */
@Composable
fun TimelineTransactionItem(
    transaction: Transaction,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val incomeColor = MoneyTrackerTheme.colors.income
    val category = Category.findCategory(transaction.categoryId, transaction.type)
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
    ) {
        // 1. Timeline Column (Continuous vertical track + circular category node)
        Box(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Upper Connecting Line
            if (!isFirstInGroup) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .align(Alignment.TopCenter)
                        .background(lineColor)
                )
            }

            // Lower Connecting Line
            if (!isLastInGroup) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(top = 20.dp)
                        .align(Alignment.TopCenter)
                        .background(lineColor)
                )
            }

            // Central Timeline Node Badge
            Surface(
                shape = CircleShape,
                color = if (isIncome) {
                    incomeColor.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.background
                ),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.iconKey),
                        contentDescription = category.name,
                        tint = if (isIncome) incomeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 2. Transaction Card / Info Box
        Surface(
            onClick = { onDeleteClick(transaction.id) },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = DateFormatter.formatTime(transaction.dateMillis),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    if (transaction.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = transaction.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                val prefix = if (isIncome) "+ " else "- "
                Text(
                    text = prefix + CurrencyFormatter.formatRupiah(transaction.amount),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = if (isIncome) incomeColor else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
