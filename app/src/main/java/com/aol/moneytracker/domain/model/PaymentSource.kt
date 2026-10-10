package com.aol.moneytracker.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector
import com.aol.moneytracker.R

enum class PaymentSource(
    val id: String,
    val displayName: String,
    @StringRes val displayNameRes: Int
) {
    GOPAY("gopay", "GoPay", R.string.payment_gopay),
    OVO("ovo", "OVO", R.string.payment_ovo),
    BNI("bni", "Bank BNI", R.string.payment_bni),
    BCA("bca", "Bank BCA", R.string.payment_bca),
    MANDIRI("mandiri", "Bank Mandiri", R.string.payment_mandiri),
    CASH("cash", "Tunai", R.string.payment_cash);

    val icon: ImageVector
        get() = when (this) {
            GOPAY -> Icons.Outlined.AccountBalanceWallet
            OVO -> Icons.Outlined.PhoneAndroid
            BNI, MANDIRI -> Icons.Outlined.AccountBalance
            BCA -> Icons.Outlined.CreditCard
            CASH -> Icons.Outlined.Payments
        }

    companion object {
        fun fromId(id: String?): PaymentSource {
            if (id.isNullOrBlank()) return BNI
            return entries.firstOrNull {
                it.id.equals(id, ignoreCase = true) || it.displayName.equals(id, ignoreCase = true)
            } ?: BNI
        }

        /**
         * Resolves the fund source for a transaction.
         * Maps existing and new transactions intelligently to GoPay, OVO, Bank BNI, Bank BCA, etc.
         */
        fun determineSource(transaction: Transaction): PaymentSource {
            val titleLower = transaction.title.lowercase()
            return when {
                titleLower.contains("gopay") || titleLower.contains("kopi") || titleLower.contains("makan") || titleLower.contains("snack") -> GOPAY
                titleLower.contains("ovo") || titleLower.contains("bioskop") || titleLower.contains("pulsa") -> OVO
                titleLower.contains("bca") || titleLower.contains("freelance") || titleLower.contains("belanja") -> BCA
                titleLower.contains("mandiri") || titleLower.contains("bensin") || titleLower.contains("bahan bakar") || titleLower.contains("motor") -> MANDIRI
                titleLower.contains("tunai") || titleLower.contains("cash") || titleLower.contains("parkir") -> CASH
                else -> {
                    // Stable deterministic rotation based on transaction ID
                    val list = listOf(BNI, BCA, GOPAY, MANDIRI, OVO)
                    val index = ((transaction.id.toInt() xor (transaction.title.hashCode())) and 0x7FFFFFFF) % list.size
                    list[index]
                }
            }
        }
    }
}
