package com.aol.moneytracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aol.moneytracker.presentation.navigation.MainScaffold
import com.aol.moneytracker.presentation.screens.home.HomeViewModel
import com.aol.moneytracker.presentation.screens.stats.StatsViewModel
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.aol.moneytracker.util.LanguageManager
import com.aol.moneytracker.util.ProvideAppLanguage
import com.aol.moneytracker.util.ThemeManager

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    private val homeViewModel: HomeViewModel by viewModels {
        val app = application as MoneyTrackerApp
        HomeViewModel.provideFactory(app.repository)
    }

    private val statsViewModel: StatsViewModel by viewModels {
        val app = application as MoneyTrackerApp
        StatsViewModel.provideFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by ThemeManager.isDarkMode.collectAsState()

            ProvideAppLanguage {
                MoneyTrackerTheme(darkTheme = isDarkMode) {
                    MainScaffold(
                        homeViewModel = homeViewModel,
                        statsViewModel = statsViewModel,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = { enabled ->
                            ThemeManager.setDarkMode(this@MainActivity, enabled)
                        }
                    )
                }
            }
        }
    }
}
