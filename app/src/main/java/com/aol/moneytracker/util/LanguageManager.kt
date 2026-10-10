package com.aol.moneytracker.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.aol.moneytracker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AppLanguage(
    val code: String,
    @StringRes val labelRes: Int,
    @StringRes val subtitleRes: Int,
    val flagEmoji: String
) {
    INDONESIAN(
        code = "in",
        labelRes = R.string.settings_language_id,
        subtitleRes = R.string.settings_language_id_sub,
        flagEmoji = "🇮🇩"
    ),
    ENGLISH(
        code = "en",
        labelRes = R.string.settings_language_en,
        subtitleRes = R.string.settings_language_en_sub,
        flagEmoji = "🇺🇸"
    );

    val locale: Locale
        get() = when (this) {
            INDONESIAN -> Locale("in", "ID")
            ENGLISH -> Locale.US
        }

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return when (code?.lowercase()) {
                "en", "en-us", "en_us" -> ENGLISH
                else -> INDONESIAN
            }
        }
    }
}

object LanguageManager {
    private const val PREFS_NAME = "artha_language_prefs"
    private const val KEY_LANGUAGE = "selected_language_code"

    private val _currentLanguage = MutableStateFlow(AppLanguage.INDONESIAN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.INDONESIAN.code)
        val language = AppLanguage.fromCode(savedCode)
        _currentLanguage.value = language
        Locale.setDefault(language.locale)
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        Locale.setDefault(language.locale)
        _currentLanguage.value = language
    }

    fun getActiveLocale(): Locale {
        return _currentLanguage.value.locale
    }

    fun wrapContext(base: Context): Context {
        val prefs = base.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.INDONESIAN.code)
        val language = AppLanguage.fromCode(savedCode)
        _currentLanguage.value = language
        val locale = language.locale
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}

private class LocalizedActivityContextWrapper(
    base: Context,
    private val localizedContext: Context
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedContext.resources
}

@Composable
fun ProvideAppLanguage(
    content: @Composable () -> Unit
) {
    val language by LanguageManager.currentLanguage.collectAsState()
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current

    val (localizedConfiguration, localizedContext) = remember(language, baseContext, baseConfiguration) {
        val locale = language.locale
        Locale.setDefault(locale)
        val config = Configuration(baseConfiguration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        val configContext = baseContext.createConfigurationContext(config)
        val wrappedContext = LocalizedActivityContextWrapper(baseContext, configContext)
        config to wrappedContext
    }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfiguration,
        LocalContext provides localizedContext
    ) {
        content()
    }
}
