package com.aol.moneytracker.presentation.screens.wallet

import android.content.Context
import com.aol.moneytracker.domain.model.PaymentSource
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.presentation.components.FinLogoMapping

/**
 * Resolves the clean, recognizable official Bank / E-Wallet / Cash name for a providerKey.
 */
fun resolveOfficialBankName(providerKey: String, fallbackName: String? = null): String {
    return when (providerKey.trim().lowercase().substringBefore("_")) {
        "bca" -> "Bank BCA"
        "mandiri" -> "Bank Mandiri"
        "bri" -> "Bank BRI"
        "bni" -> "Bank BNI"
        "bsi" -> "Bank BSI"
        "jago" -> "Bank Jago"
        "seabank" -> "SeaBank"
        "blu-bca", "blu" -> "blu by BCA"
        "cimb-niaga", "cimb" -> "CIMB Niaga"
        "permata" -> "Bank Permata"
        "gopay" -> "GoPay"
        "ovo" -> "OVO"
        "dana" -> "DANA"
        "shopee-pay", "shopeepay" -> "ShopeePay"
        "linkaja" -> "LinkAja"
        "astra-pay", "astrapay" -> "AstraPay"
        "cash", "tunai" -> "Dompet Tunai"
        else -> fallbackName?.takeIf { it.isNotBlank() }
            ?: FinLogoMapping.resolveInstitution(providerKey).displayName
    }
}

fun WalletItem.officialBankName(context: Context): String {
    return resolveOfficialBankName(providerKey, context.getString(nameRes))
}

fun WalletItem.customLabelOrNull(context: Context): String? {
    val bank = officialBankName(context)
    val defaultResName = context.getString(nameRes)
    val instDisplayName = FinLogoMapping.resolveInstitution(providerKey).displayName
    val rawCustom = customName?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (
        rawCustom.equals(bank, ignoreCase = true) ||
        rawCustom.equals(defaultResName, ignoreCase = true) ||
        rawCustom.equals(instDisplayName, ignoreCase = true)
    ) {
        null
    } else {
        rawCustom
    }
}

/**
 * Formats as "Bank Mandiri (Nama Custom)" when a custom wallet name exists,
 * or simply "Bank Mandiri" when using the default name.
 */
fun WalletItem.bankWithCustomBracket(context: Context): String {
    val bank = officialBankName(context)
    val custom = customLabelOrNull(context)
    return if (custom != null) "$bank ($custom)" else bank
}

/**
 * Top info label for stacked wallet views:
 * Returns custom wallet name if set, or the wallet's category/type label (e.g. "Rekening Utama").
 */
fun WalletItem.walletInfoTopLabel(context: Context): String {
    val custom = customLabelOrNull(context)
    val category = (customType ?: context.getString(typeRes)).trim()
    return custom ?: category.ifBlank { "Rekening Utama" }
}

data class ResolvedWalletInfo(
    val walletId: String,
    val providerKey: String,
    val bankName: String,
    val customName: String?,
    val categoryLabel: String
) {
    /**
     * For Transaction List ("Bank Mandiri (Custom)" or "Bank Mandiri")
     */
    val bankWithCustomLabel: String
        get() = if (!customName.isNullOrBlank()) "$bankName ($customName)" else bankName

    /**
     * Top line in Detail ("Atas: Kategori Wallet, Bawah: Icon Bank + Nama Bank (Custom)")
     */
    val topWalletInfoLabel: String
        get() = categoryLabel.ifBlank { "Rekening Utama" }

    val displayName: String
        get() = bankWithCustomLabel
}

data class ResolvedTransferInfo(
    val fromWalletId: String,
    val toWalletId: String,
    val fromProviderKey: String,
    val toProviderKey: String,
    val fromBankName: String,
    val toBankName: String,
    val fromCustomName: String? = null,
    val toCustomName: String? = null,
    val fromCategoryLabel: String = "Rekening Sumber",
    val toCategoryLabel: String = "Rekening Tujuan"
) {
    val fromBankWithCustom: String
        get() = if (!fromCustomName.isNullOrBlank()) "$fromBankName ($fromCustomName)" else fromBankName

    val toBankWithCustom: String
        get() = if (!toCustomName.isNullOrBlank()) "$toBankName ($toCustomName)" else toBankName

    val fromTopWalletInfo: String
        get() = fromCategoryLabel.ifBlank { "Rekening Sumber" }

    val toTopWalletInfo: String
        get() = toCategoryLabel.ifBlank { "Rekening Tujuan" }

    val fromName: String
        get() = fromBankWithCustom

    val toName: String
        get() = toBankWithCustom

    val routeLabel: String
        get() = "$fromBankWithCustom → $toBankWithCustom"
}

object TransactionWalletMeta {
    private const val WALLET_PREFIX = "@wallet:"
    private const val TRANSFER_PREFIX = "@transfer:"

    fun encodeNormal(
        wallet: WalletItem,
        walletDisplayName: String,
        localizedCategoryName: String
    ): String {
        return "$WALLET_PREFIX${wallet.id}|${wallet.providerKey}|$walletDisplayName|$localizedCategoryName"
    }

    fun encodeTransfer(
        fromWallet: WalletItem,
        toWallet: WalletItem,
        fromName: String,
        toName: String
    ): String {
        return "$TRANSFER_PREFIX${fromWallet.id}|${toWallet.id}|${fromWallet.providerKey}|${toWallet.providerKey}|$fromName|$toName"
    }

    fun resolveNormal(context: Context, transaction: Transaction): ResolvedWalletInfo {
        WalletStateHolder.ensureInitialized(context)
        val wallets = WalletStateHolder.wallets
        val raw = transaction.categoryName

        if (raw.startsWith(WALLET_PREFIX)) {
            val parts = raw.removePrefix(WALLET_PREFIX).split("|")
            val walletId = parts.getOrNull(0).orEmpty()
            val fallbackProvider = parts.getOrNull(1).orEmpty().ifBlank { "bca" }
            val fallbackName = parts.getOrNull(2).orEmpty()

            val liveWallet = wallets.firstOrNull { it.id == walletId }
                ?: wallets.firstOrNull { it.providerKey.equals(fallbackProvider, ignoreCase = true) }

            return if (liveWallet != null) {
                ResolvedWalletInfo(
                    walletId = liveWallet.id,
                    providerKey = liveWallet.providerKey,
                    bankName = liveWallet.officialBankName(context),
                    customName = liveWallet.customLabelOrNull(context),
                    categoryLabel = liveWallet.customType ?: context.getString(liveWallet.typeRes)
                )
            } else {
                val bank = resolveOfficialBankName(fallbackProvider, fallbackName)
                val custom = fallbackName.takeIf {
                    it.isNotBlank() && !it.equals(bank, ignoreCase = true)
                }
                ResolvedWalletInfo(
                    walletId = walletId,
                    providerKey = fallbackProvider,
                    bankName = bank,
                    customName = custom,
                    categoryLabel = "Rekening"
                )
            }
        }

        // Legacy/seeded transactions fallback: map PaymentSource to live WalletStateHolder item if available
        val source = PaymentSource.determineSource(transaction)
        val matchedWallet = wallets.firstOrNull {
            it.providerKey.equals(source.id, ignoreCase = true) ||
                it.id.equals(source.id, ignoreCase = true)
        }
        return if (matchedWallet != null) {
            ResolvedWalletInfo(
                walletId = matchedWallet.id,
                providerKey = matchedWallet.providerKey,
                bankName = matchedWallet.officialBankName(context),
                customName = matchedWallet.customLabelOrNull(context),
                categoryLabel = matchedWallet.customType ?: context.getString(matchedWallet.typeRes)
            )
        } else {
            val bank = resolveOfficialBankName(source.id, context.getString(source.displayNameRes))
            ResolvedWalletInfo(
                walletId = source.id,
                providerKey = source.id,
                bankName = bank,
                customName = null,
                categoryLabel = "Rekening Utama"
            )
        }
    }

    fun resolveTransfer(context: Context, transaction: Transaction): ResolvedTransferInfo {
        WalletStateHolder.ensureInitialized(context)
        val wallets = WalletStateHolder.wallets
        val raw = transaction.categoryName

        if (raw.startsWith(TRANSFER_PREFIX)) {
            val parts = raw.removePrefix(TRANSFER_PREFIX).split("|")
            val fromId = parts.getOrNull(0).orEmpty()
            val toId = parts.getOrNull(1).orEmpty()
            val fromProvider = parts.getOrNull(2).orEmpty().ifBlank { "bca" }
            val toProvider = parts.getOrNull(3).orEmpty().ifBlank { "mandiri" }
            val fallbackFromName = parts.getOrNull(4).orEmpty()
            val fallbackToName = parts.getOrNull(5).orEmpty()

            val liveFrom = wallets.firstOrNull { it.id == fromId }
                ?: wallets.firstOrNull { it.providerKey.equals(fromProvider, ignoreCase = true) }
            val liveTo = wallets.firstOrNull { it.id == toId }
                ?: wallets.firstOrNull { it.providerKey.equals(toProvider, ignoreCase = true) }

            val fromBank = liveFrom?.officialBankName(context) ?: resolveOfficialBankName(fromProvider, fallbackFromName)
            val toBank = liveTo?.officialBankName(context) ?: resolveOfficialBankName(toProvider, fallbackToName)

            return ResolvedTransferInfo(
                fromWalletId = liveFrom?.id ?: fromId,
                toWalletId = liveTo?.id ?: toId,
                fromProviderKey = liveFrom?.providerKey ?: fromProvider,
                toProviderKey = liveTo?.providerKey ?: toProvider,
                fromBankName = fromBank,
                toBankName = toBank,
                fromCustomName = liveFrom?.customLabelOrNull(context),
                toCustomName = liveTo?.customLabelOrNull(context),
                fromCategoryLabel = liveFrom?.let { it.customType ?: context.getString(it.typeRes) } ?: "Rekening Sumber",
                toCategoryLabel = liveTo?.let { it.customType ?: context.getString(it.typeRes) } ?: "Rekening Tujuan"
            )
        }

        // Legacy format: "providerKey|fromName → toName"
        if (raw.contains("|")) {
            val fromProvider = raw.substringBefore("|").ifBlank { "bca" }
            val routePart = raw.substringAfter("|")
            val fromName = routePart.substringBefore("→").trim()
            val toName = routePart.substringAfter("→", "").trim()

            val matchedFrom = wallets.firstOrNull {
                (it.customName ?: context.getString(it.nameRes)).equals(fromName, ignoreCase = true) ||
                    it.providerKey.equals(fromProvider, ignoreCase = true)
            }
            val matchedTo = wallets.firstOrNull {
                (it.customName ?: context.getString(it.nameRes)).equals(toName, ignoreCase = true) ||
                    it.officialBankName(context).equals(toName, ignoreCase = true)
            }

            return ResolvedTransferInfo(
                fromWalletId = matchedFrom?.id ?: fromProvider,
                toWalletId = matchedTo?.id ?: "mandiri",
                fromProviderKey = matchedFrom?.providerKey ?: fromProvider,
                toProviderKey = matchedTo?.providerKey ?: "mandiri",
                fromBankName = matchedFrom?.officialBankName(context) ?: resolveOfficialBankName(fromProvider, fromName),
                toBankName = matchedTo?.officialBankName(context) ?: resolveOfficialBankName("mandiri", toName),
                fromCustomName = matchedFrom?.customLabelOrNull(context),
                toCustomName = matchedTo?.customLabelOrNull(context),
                fromCategoryLabel = matchedFrom?.let { it.customType ?: context.getString(it.typeRes) } ?: "Rekening Sumber",
                toCategoryLabel = matchedTo?.let { it.customType ?: context.getString(it.typeRes) } ?: "Rekening Tujuan"
            )
        }

        val firstWallet = wallets.firstOrNull()
        val secondWallet = wallets.getOrNull(1) ?: firstWallet
        return ResolvedTransferInfo(
            fromWalletId = firstWallet?.id ?: "bca",
            toWalletId = secondWallet?.id ?: "mandiri",
            fromProviderKey = firstWallet?.providerKey ?: "bca",
            toProviderKey = secondWallet?.providerKey ?: "mandiri",
            fromBankName = firstWallet?.officialBankName(context) ?: "Bank BCA",
            toBankName = secondWallet?.officialBankName(context) ?: "Bank Mandiri",
            fromCustomName = firstWallet?.customLabelOrNull(context),
            toCustomName = secondWallet?.customLabelOrNull(context),
            fromCategoryLabel = firstWallet?.let { it.customType ?: context.getString(it.typeRes) } ?: "Rekening Sumber",
            toCategoryLabel = secondWallet?.let { it.customType ?: context.getString(it.typeRes) } ?: "Rekening Tujuan"
        )
    }
}
