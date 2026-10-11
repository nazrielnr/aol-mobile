package com.aol.moneytracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.domain.model.TransactionType
import com.aol.moneytracker.presentation.screens.wallet.TransferWalletScreen
import com.aol.moneytracker.presentation.screens.wallet.WalletStateHolder
import com.aol.moneytracker.presentation.screens.wallet.bankWithCustomBracket
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.ProvideAppLanguage
import com.aol.moneytracker.util.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransferWalletActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WalletStateHolder.ensureInitialized(this)

        val wallets = WalletStateHolder.wallets
        if (wallets.size < 2) {
            finish()
            return
        }

        val app = applicationContext as MoneyTrackerApp

        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            ProvideAppLanguage {
                MoneyTrackerTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TransferWalletScreen(
                            wallets = wallets,
                            onBackClick = { finish() },
                            onConfirmTransfer = { fromWallet, toWallet, amount, note ->
                                val fromName = fromWallet.bankWithCustomBracket(this@TransferWalletActivity)
                                val toName = toWallet.bankWithCustomBracket(this@TransferWalletActivity)

                                // 1. Update wallet balances in state holder
                                WalletStateHolder.transferBalance(
                                    fromId = fromWallet.id,
                                    toId = toWallet.id,
                                    amount = amount
                                )

                                // 2. Record transfer automatically into Transaction History (Room DB)
                                val transferTitle = if (note.isNotBlank()) {
                                    "Transfer ke $toName ($note)"
                                } else {
                                    "Transfer ke $toName"
                                }

                                val transferTx = Transaction(
                                    title = transferTitle,
                                    amount = amount,
                                    type = TransactionType.EXPENSE,
                                    categoryId = "transfer",
                                    categoryName = com.aol.moneytracker.presentation.screens.wallet.TransactionWalletMeta.encodeTransfer(
                                        fromWallet = fromWallet,
                                        toWallet = toWallet,
                                        fromName = fromName,
                                        toName = toName
                                    ),
                                    dateMillis = System.currentTimeMillis(),
                                    note = note
                                )

                                com.aol.moneytracker.presentation.screens.notification.NotificationStateHolder.onTransactionRecorded(
                                    context = this@TransferWalletActivity,
                                    transaction = transferTx
                                )

                                app.applicationScope.launch(Dispatchers.IO) {
                                    app.repository.insertTransaction(transferTx)
                                }

                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, TransferWalletActivity::class.java))
        }
    }
}
