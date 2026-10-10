package com.aol.moneytracker.presentation.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class AppColors(
    val isDark: Boolean,
    val income: Color,
    val incomeContainer: Color,
    val onIncomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val notificationBadge: Color,
    val insightAccent: Color,
    val backdropScrim: Color,
    val chartBadgeContainer: Color,
    val chartBadgeContent: Color,
    val chartBadgeBorder: Color,
    val heroGradientStops: List<Color>,
    val heroWaveStops: List<Color>,
    val heroVerticalStops: List<Color>,
    val stickyHeaderGradientStops: List<Color>,
    val walletBcaGradient: List<Color>,
    val walletMandiriGradient: List<Color>,
    val walletGopayGradient: List<Color>,
    val walletCashGradient: List<Color>,
    val onWalletCard: Color,
    val categoryFood: Color,
    val categoryTransport: Color,
    val categoryShopping: Color,
    val categoryBills: Color,
    val categoryEntertainment: Color,
    val categoryHealth: Color,
    val categoryEducation: Color,
    val categorySalary: Color,
    val categoryBonus: Color,
    val categoryInvestment: Color,
    val categoryFreelance: Color,
    val categoryBusiness: Color,
    val categorySavings: Color,
    val categoryOther: Color
) {
    fun categoryColor(key: String): Color {
        return when (key.lowercase()) {
            "food" -> categoryFood
            "transport" -> categoryTransport
            "shopping" -> categoryShopping
            "bills" -> categoryBills
            "entertainment" -> categoryEntertainment
            "health" -> categoryHealth
            "education" -> categoryEducation
            "salary" -> categorySalary
            "bonus" -> categoryBonus
            "investment" -> categoryInvestment
            "freelance" -> categoryFreelance
            "business" -> categoryBusiness
            "other_income", "savings" -> categorySavings
            else -> categoryOther
        }
    }
}

private val LightAppColors = AppColors(
    isDark = false,
    income = FinanceGreenLight,
    incomeContainer = FinanceGreenContainerLight,
    onIncomeContainer = OnFinanceGreenContainerLight,
    expense = FinanceRedLight,
    expenseContainer = FinanceRedContainerLight,
    notificationBadge = NotificationBadgeLight,
    insightAccent = InsightAccentLight,
    backdropScrim = BackdropScrimLight,
    chartBadgeContainer = ChartBadgeContainerLight,
    chartBadgeContent = ChartBadgeContentLight,
    chartBadgeBorder = ChartBadgeBorderLight,
    heroGradientStops = listOf(
        HeroGradientLight1,
        HeroGradientLight2,
        HeroGradientLight3,
        HeroGradientLight4,
        HeroGradientLight5
    ),
    heroWaveStops = listOf(
        HeroWaveLight1,
        HeroWaveLight2,
        HeroWaveLight3
    ),
    heroVerticalStops = listOf(
        HeroVerticalLight1,
        HeroVerticalLight2
    ),
    stickyHeaderGradientStops = listOf(
        StickyHeaderGradientLight1,
        StickyHeaderGradientLight2
    ),
    walletBcaGradient = listOf(WalletBcaStart, WalletBcaEnd),
    walletMandiriGradient = listOf(WalletMandiriStart, WalletMandiriEnd),
    walletGopayGradient = listOf(WalletGopayStart, WalletGopayEnd),
    walletCashGradient = listOf(WalletCashStart, WalletCashEnd),
    onWalletCard = OnWalletCardColor,
    categoryFood = CategoryFoodLight,
    categoryTransport = CategoryTransportLight,
    categoryShopping = CategoryShoppingLight,
    categoryBills = CategoryBillsLight,
    categoryEntertainment = CategoryEntertainmentLight,
    categoryHealth = CategoryHealthLight,
    categoryEducation = CategoryEducationLight,
    categorySalary = CategorySalaryLight,
    categoryBonus = CategoryBonusLight,
    categoryInvestment = CategoryInvestmentLight,
    categoryFreelance = CategoryFreelanceLight,
    categoryBusiness = CategoryBusinessLight,
    categorySavings = CategorySavingsLight,
    categoryOther = CategoryOtherLight
)

private val DarkAppColors = AppColors(
    isDark = true,
    income = FinanceGreenDark,
    incomeContainer = FinanceGreenContainerDark,
    onIncomeContainer = OnFinanceGreenContainerDark,
    expense = FinanceRedDark,
    expenseContainer = FinanceRedContainerDark,
    notificationBadge = NotificationBadgeDark,
    insightAccent = InsightAccentDark,
    backdropScrim = BackdropScrimDark,
    chartBadgeContainer = ChartBadgeContainerDark,
    chartBadgeContent = ChartBadgeContentDark,
    chartBadgeBorder = ChartBadgeBorderDark,
    heroGradientStops = listOf(
        HeroGradientDark1,
        HeroGradientDark2,
        HeroGradientDark3,
        HeroGradientDark4,
        HeroGradientDark5
    ),
    heroWaveStops = listOf(
        HeroWaveDark1,
        HeroWaveDark2,
        HeroWaveDark3
    ),
    heroVerticalStops = listOf(
        HeroVerticalDark1,
        HeroVerticalDark2
    ),
    stickyHeaderGradientStops = listOf(
        StickyHeaderGradientDark1,
        StickyHeaderGradientDark2
    ),
    walletBcaGradient = listOf(WalletBcaStart, WalletBcaEnd),
    walletMandiriGradient = listOf(WalletMandiriStart, WalletMandiriEnd),
    walletGopayGradient = listOf(WalletGopayStart, WalletGopayEnd),
    walletCashGradient = listOf(WalletCashStart, WalletCashEnd),
    onWalletCard = OnWalletCardColor,
    categoryFood = CategoryFoodDark,
    categoryTransport = CategoryTransportDark,
    categoryShopping = CategoryShoppingDark,
    categoryBills = CategoryBillsDark,
    categoryEntertainment = CategoryEntertainmentDark,
    categoryHealth = CategoryHealthDark,
    categoryEducation = CategoryEducationDark,
    categorySalary = CategorySalaryDark,
    categoryBonus = CategoryBonusDark,
    categoryInvestment = CategoryInvestmentDark,
    categoryFreelance = CategoryFreelanceDark,
    categoryBusiness = CategoryBusinessDark,
    categorySavings = CategorySavingsDark,
    categoryOther = CategoryOtherDark
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object MoneyTrackerTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceBright = SurfaceBrightLight,
    surfaceDim = SurfaceDimLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = ScrimLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceBright = SurfaceBrightDark,
    surfaceDim = SurfaceDimDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark
)

@Composable
fun MoneyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Keep the actual window background in sync with Compose so predictive back
            // and activity transitions never fall through to the system's default black/white.
            window.setBackgroundDrawable(ColorDrawable(colorScheme.background.toArgb()))
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ExpressiveShapes,
            content = content
        )
    }
}
