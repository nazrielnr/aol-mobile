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
import com.aol.moneytracker.presentation.screens.wallet.AddWalletScreen
import com.aol.moneytracker.presentation.screens.wallet.WalletStateHolder
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.ProvideAppLanguage
import com.aol.moneytracker.util.ThemeManager

class AddWalletActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WalletStateHolder.ensureInitialized(this)

        val editWalletId = intent.getStringExtra(EXTRA_WALLET_ID)
        val existingWallet = editWalletId?.let { id ->
            WalletStateHolder.wallets.firstOrNull { it.id == id }
        }

        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            ProvideAppLanguage {
                MoneyTrackerTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AddWalletScreen(
                            initialWallet = existingWallet,
                            canDelete = existingWallet != null && WalletStateHolder.wallets.size > 1,
                            onBackClick = { finish() },
                            onSaveWallet = { savedWallet ->
                                if (existingWallet != null) {
                                    WalletStateHolder.updateWallet(savedWallet)
                                } else {
                                    WalletStateHolder.addWallet(savedWallet)
                                }
                                finish()
                            },
                            onDeleteWallet = { walletId ->
                                WalletStateHolder.deleteWallet(walletId)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_WALLET_ID = "extra_wallet_id"

        fun start(context: Context) {
            context.startActivity(Intent(context, AddWalletActivity::class.java))
        }

        fun startEdit(context: Context, walletId: String) {
            context.startActivity(
                Intent(context, AddWalletActivity::class.java).apply {
                    putExtra(EXTRA_WALLET_ID, walletId)
                }
            )
        }
    }
}
