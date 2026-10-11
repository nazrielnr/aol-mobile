package com.aol.moneytracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.aol.moneytracker.domain.model.Category
import com.aol.moneytracker.domain.model.Transaction
import com.aol.moneytracker.presentation.screens.camera.ReceiptCameraScreen
import com.aol.moneytracker.presentation.screens.wallet.TransactionWalletMeta
import com.aol.moneytracker.presentation.screens.wallet.WalletStateHolder
import com.aol.moneytracker.presentation.screens.wallet.bankWithCustomBracket
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.ProvideAppLanguage
import com.aol.moneytracker.util.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiptCameraActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WalletStateHolder.ensureInitialized(this)
        val app = applicationContext as MoneyTrackerApp

        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            ProvideAppLanguage {
                MoneyTrackerTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ReceiptCameraScreen(
                            onBackClick = { finish() },
                            onEditReceipt = { title, amount, type, categoryId, note ->
                                AddTransactionActivity.startCreate(
                                    context = this@ReceiptCameraActivity,
                                    initialTitle = title,
                                    initialAmount = amount,
                                    initialType = type,
                                    initialCategoryId = categoryId,
                                    initialNote = note
                                )
                                finish()
                            },
                            onSaveReceipt = { title, amount, type, categoryId, note ->
                                val primaryWallet = WalletStateHolder.wallets.firstOrNull()
                                val catObj = Category.findCategory(categoryId, type)
                                val localizedCatName = getString(catObj.nameRes)
                                val encodedCategoryName = if (primaryWallet != null) {
                                    TransactionWalletMeta.encodeNormal(
                                        wallet = primaryWallet,
                                        walletDisplayName = primaryWallet.bankWithCustomBracket(this@ReceiptCameraActivity),
                                        localizedCategoryName = localizedCatName
                                    )
                                } else {
                                    localizedCatName
                                }
                                val newTx = Transaction(
                                    title = title,
                                    amount = amount,
                                    type = type,
                                    categoryId = categoryId,
                                    categoryName = encodedCategoryName,
                                    dateMillis = System.currentTimeMillis(),
                                    note = note
                                )
                                WalletStateHolder.applyTransactionEffect(this@ReceiptCameraActivity, newTx)
                                app.applicationScope.launch(Dispatchers.IO) {
                                    app.repository.insertTransaction(newTx)
                                }
                                Toast.makeText(
                                    this@ReceiptCameraActivity,
                                    getString(R.string.snackbar_transaction_saved, newTx.title),
                                    Toast.LENGTH_SHORT
                                ).show()
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
            context.startActivity(Intent(context, ReceiptCameraActivity::class.java))
        }
    }
}
