package com.aol.moneytracker.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// 1. GOOGLE MATERIAL 3 LIGHT PALETTE (Clean, High-Contrast Banking Standard)
// ============================================================================
val PrimaryLight = Color(0xFF0B57D0)               // Google Signature Blue
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD3E3FD)      // Soft Blue Pill Container
val OnPrimaryContainerLight = Color(0xFF041E49)

val SecondaryLight = Color(0xFF00639B)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFC2E7FF)
val OnSecondaryContainerLight = Color(0xFF001D35)

val TertiaryLight = Color(0xFF006874)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFF97F0FF)
val OnTertiaryContainerLight = Color(0xFF001F24)

val ErrorLight = Color(0xFFDC2626)                 // Crisp Outflow / Alert Red
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF7F1D1D)

val BackgroundLight = Color(0xFFF1F5F9)            // Cool Alabaster Slate Canvas (Cards pop in #FFFFFF)
val OnBackgroundLight = Color(0xFF0F172A)          // Deep Slate Ink
val SurfaceLight = Color(0xFFF5F8FC)               // Crisp Porcelain Sheet Surface
val OnSurfaceLight = Color(0xFF0F172A)             // Deep Slate Ink
val SurfaceVariantLight = Color(0xFFE2E8F0)
val OnSurfaceVariantLight = Color(0xFF475569)      // Balanced Slate Subtitle Grey
val OutlineLight = Color(0xFF64748B)
val OutlineVariantLight = Color(0xFF94A3B8)        // Slate 400 (Produces crisp hairline borders with alpha)
val ScrimLight = Color(0xFF0F172A)
val InverseSurfaceLight = Color(0xFF1E293B)
val InverseOnSurfaceLight = Color(0xFFF8FAFC)

// Tonal Surface Containers (Light)
val SurfaceBrightLight = Color(0xFFFFFFFF)         // Active Pill Capsule Indicator
val SurfaceDimLight = Color(0xFFE2E8F0)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFFFFFFF)   // Pure White Elevated Cards
val SurfaceContainerLight = Color(0xFFFFFFFF)      // Pure White Bento & Transaction Cards
val SurfaceContainerHighLight = Color(0xFFE7EEF8)  // Floating Nav Capsule, Switch Track, Menus
val SurfaceContainerHighestLight = Color(0xFFDCE5F2) // TopBar Pills, Icon Badges, Progress Tracks

// ============================================================================
// 2. GOOGLE MATERIAL 3 DARK PALETTE
// ============================================================================
val PrimaryDark = Color(0xFFA8C7FA)                // Google M3 Light Blue
val OnPrimaryDark = Color(0xFF062E6F)
val PrimaryContainerDark = Color(0xFF0842A0)
val OnPrimaryContainerDark = Color(0xFFD3E3FD)

val SecondaryDark = Color(0xFF80D0FF)
val OnSecondaryDark = Color(0xFF003453)
val SecondaryContainerDark = Color(0xFF004C77)
val OnSecondaryContainerDark = Color(0xFFC2E7FF)

val TertiaryDark = Color(0xFF4FD8EB)
val OnTertiaryDark = Color(0xFF00363D)
val TertiaryContainerDark = Color(0xFF004F58)
val OnTertiaryContainerDark = Color(0xFF97F0FF)

val ErrorDark = Color(0xFFF2B8B5)
val OnErrorDark = Color(0xFF601410)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFF9DEDC)

val BackgroundDark = Color(0xFF121316)             // Deep Neutral Dark Slate
val OnBackgroundDark = Color(0xFFE2E2E5)
val SurfaceDark = Color(0xFF121316)
val OnSurfaceDark = Color(0xFFE2E2E5)
val SurfaceVariantDark = Color(0xFF444746)
val OnSurfaceVariantDark = Color(0xFFC4C7C5)
val OutlineDark = Color(0xFF8E918F)
val OutlineVariantDark = Color(0xFF444746)
val ScrimDark = Color(0xFF000000)
val InverseSurfaceDark = Color(0xFFE2E2E5)
val InverseOnSurfaceDark = Color(0xFF1A1C1E)

// Tonal Surface Containers (Dark)
val SurfaceBrightDark = Color(0xFF383B40)
val SurfaceDimDark = Color(0xFF121316)
val SurfaceContainerLowestDark = Color(0xFF0D0E11)
val SurfaceContainerLowDark = Color(0xFF1A1C1E)
val SurfaceContainerDark = Color(0xFF1E2023)
val SurfaceContainerHighDark = Color(0xFF282A2E)
val SurfaceContainerHighestDark = Color(0xFF333539)

// ============================================================================
// 3. EXTENDED GLOBAL SEMANTIC TOKENS (Light & Dark Mapped)
// ============================================================================

// Banking & Finance Inflow / Outflow Semantic Colors
val FinanceGreenLight = Color(0xFF15803D)          // Rich Legible Emerald for Light Mode
val FinanceGreenContainerLight = Color(0xFFDCFCE7)
val OnFinanceGreenContainerLight = Color(0xFF052E16)

val FinanceGreenDark = Color(0xFF4ADE80)           // Luminous Mint-Emerald for Dark Mode
val FinanceGreenContainerDark = Color(0xFF14532D)
val OnFinanceGreenContainerDark = Color(0xFFBBF7D0)

val FinanceRedLight = Color(0xFFDC2626)
val FinanceRedContainerLight = Color(0xFFFEE2E2)

val FinanceRedDark = Color(0xFFF87171)
val FinanceRedContainerDark = Color(0xFF7F1D1D)

// Legacy aliases kept mapped to theme tokens for backward compatibility
val FinanceGreen = FinanceGreenLight
val FinanceRed = FinanceRedLight

// Accents, Badges & Overlays
val NotificationBadgeLight = Color(0xFFEF4444)
val NotificationBadgeDark = Color(0xFFFF5252)

val InsightAccentLight = Color(0xFF0B57D0)
val InsightAccentDark = Color(0xFFA8C7FA)

val BackdropScrimLight = Color(0x590F172A)
val BackdropScrimDark = Color(0x73000000)

// Chart Floating Percentage Tooltip
val ChartBadgeContainerLight = Color(0xFF0F172A)
val ChartBadgeContentLight = Color(0xFFFFFFFF)
val ChartBadgeBorderLight = Color(0x4DFFFFFF)

val ChartBadgeContainerDark = Color(0xFF0F1115)
val ChartBadgeContentDark = Color(0xFFFFFFFF)
val ChartBadgeBorderDark = Color(0x59FFFFFF)

// Home Hero Ambient Flowing Gradient Stops (Light Mode - Airy Periwinkle & Pearl Sky)
val HeroGradientLight1 = Color(0xFFD6E4FA)
val HeroGradientLight2 = Color(0xFFE6EFFC)
val HeroGradientLight3 = Color(0xFFC8DCFA)
val HeroGradientLight4 = Color(0xFFEDF3FC)
val HeroGradientLight5 = Color(0xFFD2E2F9)

val HeroWaveLight1 = Color(0x8CFFFFFF)
val HeroWaveLight2 = Color(0x2E60A5FA)
val HeroWaveLight3 = Color(0x73FFFFFF)

val HeroVerticalLight1 = Color(0x59C2D9FA)
val HeroVerticalLight2 = Color(0x73F1F5F9)

// Home Hero Ambient Flowing Gradient Stops (Dark Mode - Deep Monochrome Slate)
val HeroGradientDark1 = Color(0xFF1E2126)
val HeroGradientDark2 = Color(0xFF15171A)
val HeroGradientDark3 = Color(0xFF282D33)
val HeroGradientDark4 = Color(0xFF121316)
val HeroGradientDark5 = Color(0xFF1F2228)

val HeroWaveDark1 = Color(0x12FFFFFF)
val HeroWaveDark2 = Color(0x14B5BAC2)
val HeroWaveDark3 = Color(0x0FFFFFFF)

val HeroVerticalDark1 = Color(0x5924282F)
val HeroVerticalDark2 = Color(0x59181A1D)

// Sticky Header Gradients
val StickyHeaderGradientLight1 = Color(0xFFD6E4FA)
val StickyHeaderGradientLight2 = Color(0xFFE8F0FC)

val StickyHeaderGradientDark1 = Color(0xFF24203B)
val StickyHeaderGradientDark2 = Color(0xFF1B182B)

// Wallet Account Card Gradients (Light & Dark)
val WalletBcaStart = Color(0xFF0D47A1)
val WalletBcaEnd = Color(0xFF1976D2)

val WalletMandiriStart = Color(0xFF004D40)
val WalletMandiriEnd = Color(0xFF00897B)

val WalletGopayStart = Color(0xFF4A148C)
val WalletGopayEnd = Color(0xFF7B1FA2)

val WalletCashStart = Color(0xFFE65100)
val WalletCashEnd = Color(0xFFF57C00)

val OnWalletCardColor = Color(0xFFFFFFFF)

// Category Semantic Palette (Light Mode - Rich & Legible on White Surfaces)
val CategoryFoodLight = Color(0xFF0284C7)          // Rich Sky Blue
val CategoryTransportLight = Color(0xFFEA580C)     // Deep Vibrant Orange
val CategoryShoppingLight = Color(0xFF1A73E8)      // Google Signature Blue
val CategoryBillsLight = Color(0xFFD97706)         // Rich Amber Gold
val CategoryEntertainmentLight = Color(0xFFE11D48) // Rose Crimson
val CategoryHealthLight = Color(0xFF059669)        // Deep Emerald
val CategoryEducationLight = Color(0xFF0891B2)     // Deep Cyan
val CategorySalaryLight = Color(0xFF059669)        // Deep Emerald
val CategoryBonusLight = Color(0xFFD97706)         // Rich Amber
val CategoryInvestmentLight = Color(0xFF0369A1)    // Ocean Blue
val CategoryFreelanceLight = Color(0xFF2563EB)     // Royal Blue
val CategoryBusinessLight = Color(0xFF0284C7)      // Sky Blue
val CategorySavingsLight = Color(0xFF0D9488)       // Deep Teal
val CategoryOtherLight = Color(0xFF64748B)         // Slate

// Category Semantic Palette (Dark Mode - Luminous & Vibrant on Dark Surfaces)
val CategoryFoodDark = Color(0xFF38BDF8)           // Sky Blue
val CategoryTransportDark = Color(0xFFF97316)      // Vibrant Orange
val CategoryShoppingDark = Color(0xFF60A5FA)       // Luminous Blue
val CategoryBillsDark = Color(0xFFFBBF24)          // Amber
val CategoryEntertainmentDark = Color(0xFFFB7185)  // Coral Rose
val CategoryHealthDark = Color(0xFF10B981)         // Emerald Green
val CategoryEducationDark = Color(0xFF06B6D4)      // Cyan
val CategorySalaryDark = Color(0xFF10B981)         // Emerald Green
val CategoryBonusDark = Color(0xFFF59E0B)          // Amber
val CategoryInvestmentDark = Color(0xFF38BDF8)     // Deep Sky Blue
val CategoryFreelanceDark = Color(0xFF60A5FA)      // Royal Blue
val CategoryBusinessDark = Color(0xFF0EA5E9)       // Ocean Blue
val CategorySavingsDark = Color(0xFF14B8A6)        // Teal
val CategoryOtherDark = Color(0xFF94A3B8)          // Slate
