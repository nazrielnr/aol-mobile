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
import com.aol.moneytracker.presentation.screens.settings.SettingsScreen
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.ProvideAppLanguage
import com.aol.moneytracker.util.ThemeManager

/**
 * Dedicated Android Activity for Settings screen, matching the architecture in Amaya reference.
 * Running as a separate OS Activity with android:enableOnBackInvokedCallback="true" allows
 * Android's WindowManager to execute the 100% native OS Cross-Activity Predictive Back gesture.
 */
class SettingsActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            ProvideAppLanguage {
                MoneyTrackerTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SettingsScreen(
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { enabled ->
                                ThemeManager.setDarkMode(this@SettingsActivity, enabled)
                            },
                            onBackClick = { finish() }
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }
}
