package com.aol.moneytracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun TransactionCard(
    transaction: Transaction,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val category = Category.findCategory(transaction.categoryId, transaction.type)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDeleteClick(transaction.id) }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Neutral M3 Tonal Avatar (Google Wallet Style)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.iconKey),
                    contentDescription = category.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Merchant / Title & Category Detail
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

                val subtitle = buildString {
                    append(transaction.categoryName)
                    if (transaction.note.isNotBlank()) {
                        append(" • ")
                        append(transaction.note)
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Financial Amount (Tabular Standard)
            val prefix = if (isIncome) "+ " else "- "
            Text(
                text = prefix + CurrencyFormatter.formatRupiah(transaction.amount),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = if (isIncome) MoneyTrackerTheme.colors.income else MaterialTheme.colorScheme.onSurface
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp, end = 20.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

fun getCategoryIcon(key: String): ImageVector {
    return when (key) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_bag" -> Icons.Default.ShoppingBag
        "receipt_long" -> Icons.AutoMirrored.Filled.ReceiptLong
        "sports_esports" -> Icons.Default.SportsEsports
        "medical_services" -> Icons.Default.MedicalServices
        "school" -> Icons.Default.School
        "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
        "card_giftcard" -> Icons.Default.CardGiftcard
        "trending_up" -> Icons.AutoMirrored.Filled.TrendingUp
        "laptop_mac" -> Icons.Default.LaptopMac
        "storefront" -> Icons.Default.Storefront
        "savings" -> Icons.Default.Savings
        "swap_horiz" -> Icons.Default.SwapHoriz
        "more_horiz" -> Icons.Default.MoreHoriz
        else -> Icons.Default.AttachMoney
    }
}

@Composable
fun getCategoryColor(key: String): androidx.compose.ui.graphics.Color {
    if (key.equals("transfer", ignoreCase = true)) {
        return MaterialTheme.colorScheme.primary
    }
    return MoneyTrackerTheme.colors.categoryColor(key)
}
