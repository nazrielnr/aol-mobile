package com.aol.moneytracker.presentation.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import com.aol.moneytracker.R
import com.aol.moneytracker.presentation.theme.MoneyTrackerTheme
import com.hafidznoor.idnfinlogos.IdnFinLogos
import com.hafidznoor.idnfinlogos.LogoMeta
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Universal Category Classification mapped to `hafidznoor/idn-finlogos`.
 */
enum class FinCategory(
    val idnCategorySlug: String?,
    @StringRes val labelRes: Int
) {
    BANK("bank-logo", R.string.wallet_bca_type),
    EWALLET("e-wallet", R.string.wallet_gopay_type),
    CASH(null, R.string.wallet_cash_type)
}

/**
 * Dynamic Financial Institution model backed universally by `IdnFinLogos` metadata.
 */
data class FinInstitution(
    val id: String,
    val displayName: String,
    val category: FinCategory,
    val assetPath: String?,
    val isPopular: Boolean = false,
    val popularRank: Int = Int.MAX_VALUE
) {
    val fallbackIcon: ImageVector
        get() = when (category) {
            FinCategory.BANK -> Icons.Default.AccountBalance
            FinCategory.EWALLET -> Icons.Default.Payments
            FinCategory.CASH -> Icons.Default.AccountBalanceWallet
        }
}

/**
 * Universally extracted 1:1 Icon-Mark representation of an `idn-finlogos` SVG asset.
 */
data class ExtractedFinIcon(
    val badgeBackgroundColor: Color,
    val primaryBrandColor: Color,
    val iconBounds: RectF,
    val layers: List<IconPathLayer>,
    val isWordmarkOnly: Boolean,
    val needsLightBorderInLightMode: Boolean
) {
    fun brandColor(isDark: Boolean): Color {
        val lum = primaryBrandColor.luminance()
        return if (isDark) {
            if (lum < 0.25f) lerp(primaryBrandColor, Color.White, 0.45f) else primaryBrandColor
        } else {
            if (lum > 0.65f) lerp(primaryBrandColor, Color.Black, 0.25f) else primaryBrandColor
        }
    }

    fun cardGradient(isDark: Boolean): List<Color> {
        val base = primaryBrandColor
        val lum = base.luminance()
        val start = when {
            lum > 0.45f -> lerp(base, Color(0xFF1E293B), if (isDark) 0.45f else 0.30f)
            lum < 0.08f -> lerp(base, Color(0xFF1E3A8A), 0.20f)
            else -> if (isDark) lerp(base, Color.Black, 0.22f) else base
        }
        val end = lerp(start, Color.White, if (isDark) 0.14f else 0.22f)
        return listOf(start, end)
    }
}

data class IconPathLayer(
    val path: Path,
    val colorInt: Int
)

/**
 * Universal SVG Analyzer & Icon-Mark Extractor (`SvgIconExtractor`).
 * Works dynamically on ANY SVG from `hafidznoor/idn-finlogos` without hardcoding coordinates:
 * 1. Parses `<path>` elements and resolves `<linearGradient>` / `<radialGradient>` `url(#...)` fills.
 * 2. Strips small multi-letter bottom tagline rows (e.g. "BANK RAKYAT INDONESIA", "CIMB NIAGA", "astrapay").
 * 3. Isolates the standalone 1:1 Icon Emblem (removing long wordmark text) or clean acronym.
 * 4. Computes full-bleed Badge Background Color & contrasting foreground layers for Light & Dark themes.
 */
object SvgIconExtractor {

    private val cache = ConcurrentHashMap<String, ExtractedFinIcon>()

    private data class UnresolvedSvgPath(
        val d: String,
        val rawFill: String?,
        val isEvenOdd: Boolean
    )

    private data class RawSvgPath(
        val d: String,
        val colorInt: Int,
        val isEvenOdd: Boolean
    )

    private data class ContourData(
        val path: Path,
        val bounds: RectF,
        val colorInt: Int,
        val isEvenOdd: Boolean,
        val sourcePathIndex: Int
    ) {
        val area: Float get() = max(0f, bounds.width()) * max(0f, bounds.height())
    }

    fun extract(context: Context, assetPath: String?): ExtractedFinIcon? {
        if (assetPath.isNullOrBlank()) return null
        cache[assetPath]?.let { return it }

        val extracted = runCatching {
            parseAndExtractIcon(context, assetPath)
        }.getOrNull()

        if (extracted != null) {
            cache[assetPath] = extracted
        }
        return extracted
    }

    private fun parseAndExtractIcon(context: Context, assetPath: String): ExtractedFinIcon? {
        var svgW = 80f
        var svgH = 26f
        val unresolvedPaths = mutableListOf<UnresolvedSvgPath>()
        val gradientColors = mutableMapOf<String, Int>()

        context.assets.open(assetPath).use { inputStream ->
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(inputStream, "UTF-8")

            val fillStack = ArrayDeque<String?>()
            val ruleStack = ArrayDeque<String?>()
            var ignoreDepth = 0
            var activeGradientId: String? = null

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val name = parser.name.lowercase()
                        if (name == "lineargradient" || name == "radialgradient") {
                            activeGradientId = parser.getAttributeValue(null, "id")
                            ignoreDepth++
                        } else if (name == "stop" && activeGradientId != null) {
                            val stopColor = parser.getAttributeValue(null, "stop-color")
                            val parsedStop = parseDirectColor(stopColor)
                            if (parsedStop != null && !gradientColors.containsKey(activeGradientId)) {
                                gradientColors[activeGradientId!!] = parsedStop
                            }
                            ignoreDepth++
                        } else if (name == "defs" || name == "clippath" || name == "mask") {
                            ignoreDepth++
                        } else if (ignoreDepth == 0) {
                            val ownFill = parser.getAttributeValue(null, "fill")
                            val ownRule = parser.getAttributeValue(null, "fill-rule")
                                ?: parser.getAttributeValue(null, "clip-rule")

                            val effectiveFill = ownFill ?: fillStack.lastOrNull()
                            val effectiveRule = ownRule ?: ruleStack.lastOrNull()
                            fillStack.addLast(effectiveFill)
                            ruleStack.addLast(effectiveRule)

                            if (name == "svg") {
                                val vb = parser.getAttributeValue(null, "viewBox")
                                if (!vb.isNullOrBlank()) {
                                    val parts = vb.trim().split(Regex("[\\s,]+")).mapNotNull { it.toFloatOrNull() }
                                    if (parts.size == 4) {
                                        svgW = parts[2].coerceAtLeast(1f)
                                        svgH = parts[3].coerceAtLeast(1f)
                                    }
                                }
                            } else if (name == "path") {
                                val d = parser.getAttributeValue(null, "d")
                                if (!d.isNullOrBlank()) {
                                    val isEvenOdd = effectiveRule?.equals("evenodd", ignoreCase = true) == true
                                    unresolvedPaths.add(UnresolvedSvgPath(d, effectiveFill, isEvenOdd))
                                }
                            }
                        } else {
                            ignoreDepth++
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val name = parser.name.lowercase()
                        if (name == "lineargradient" || name == "radialgradient") {
                            activeGradientId = null
                        }
                        if (ignoreDepth > 0) {
                            ignoreDepth--
                        } else {
                            if (fillStack.isNotEmpty()) fillStack.removeLast()
                            if (ruleStack.isNotEmpty()) ruleStack.removeLast()
                        }
                    }
                }
                eventType = parser.next()
            }
        }

        val rawPaths = unresolvedPaths.mapNotNull { u ->
            val colorInt = resolveSvgFillColor(u.rawFill, gradientColors) ?: return@mapNotNull null
            RawSvgPath(u.d, colorInt, u.isEvenOdd)
        }

        if (rawPaths.isEmpty()) return null

        // Convert raw paths into individual measured contours
        val allContours = mutableListOf<ContourData>()
        rawPaths.forEachIndexed { index, raw ->
            val fullPath = runCatching { PathParser.createPathFromPathData(raw.d) }.getOrNull() ?: return@forEachIndexed
            val pm = PathMeasure(fullPath, false)
            do {
                val len = pm.length
                if (len > 0.05f) {
                    val cPath = Path()
                    if (pm.getSegment(0f, len, cPath, true)) {
                        cPath.close()
                        val b = RectF()
                        cPath.computeBounds(b, true)
                        if (b.width() > 0.05f || b.height() > 0.05f) {
                            allContours.add(
                                ContourData(
                                    path = cPath,
                                    bounds = b,
                                    colorInt = raw.colorInt,
                                    isEvenOdd = raw.isEvenOdd,
                                    sourcePathIndex = index
                                )
                            )
                        }
                    }
                }
            } while (pm.nextContour())
        }

        if (allContours.isEmpty()) return null

        // 1. Filter out bottom tagline or stacked bottom wordmark rows (e.g., "PermataBank", "astrapay", "BANK RAKYAT INDONESIA")
        val bottomSmallCandidates = allContours.filter {
            it.bounds.top > svgH * 0.60f &&
                it.bounds.height() < svgH * 0.36f &&
                it.bounds.width() < svgW * 0.25f
        }
        val upperContours = allContours.filterNot { it in bottomSmallCandidates }
        val upperBounds = if (upperContours.isNotEmpty()) unionBounds(upperContours.map { it.bounds }) else RectF()
        val hasSubstantialUpperContent = upperContours.isNotEmpty() && upperBounds.height() > svgH * 0.38f

        val filteredContours = if (hasSubstantialUpperContent && bottomSmallCandidates.size >= 4) {
            val minX = bottomSmallCandidates.minOf { it.bounds.left }
            val maxX = bottomSmallCandidates.maxOf { it.bounds.right }
            if (maxX - minX > svgW * 0.38f) {
                upperContours
            } else {
                allContours
            }
        } else {
            allContours
        }.ifEmpty { allContours }

        val totalBounds = unionBounds(filteredContours.map { it.bounds })
        val totalW = totalBounds.width().coerceAtLeast(1f)
        val totalH = totalBounds.height().coerceAtLeast(1f)

        // 2. Universal Icon-Mark Isolation for horizontal logos (aspectRatio > 1.35)
        var selectedContours = filteredContours
        var isWordmarkOnly = false

        if (totalW / totalH > 1.35f) {
            val minSplitX = totalBounds.left + totalH * 0.50f
            val maxSplitX = totalBounds.left + totalH * 1.65f

            val xClusters = clusterHorizontally(filteredContours, tolerance = totalH * 0.03f)
            val leftClusters = mutableListOf<List<ContourData>>()
            val rightClusters = mutableListOf<List<ContourData>>()

            for (cluster in xClusters) {
                val cBounds = unionBounds(cluster.map { it.bounds })
                if (cBounds.right <= maxSplitX && (leftClusters.isEmpty() || cBounds.left < totalBounds.left + totalH * 1.25f)) {
                    leftClusters.add(cluster)
                } else {
                    rightClusters.add(cluster)
                }
            }

            val leftGroup = leftClusters.flatten()
            val rightGroup = rightClusters.flatten()

            if (leftGroup.isNotEmpty() && rightGroup.isNotEmpty()) {
                val leftB = unionBounds(leftGroup.map { it.bounds })
                val rightB = unionBounds(rightGroup.map { it.bounds })
                val leftAspect = leftB.width() / leftB.height().coerceAtLeast(1f)

                val leftColors = leftGroup.map { normalizeColor(it.colorInt) }.toSet()
                val rightColors = rightGroup.map { normalizeColor(it.colorInt) }.toSet()

                // Check if leftGroup has embedded microscopic text (like "GRUP BCA" inside bca.svg's crest)
                // while rightGroup is a clean bold 3-letter acronym ("BCA")
                val leftTinyCount = leftGroup.count { it.bounds.height() < totalH * 0.14f }
                val rightLargeCount = rightGroup.count { it.bounds.height() > totalH * 0.45f }

                // Check if the entire SVG is already a short single-color 3-letter wordmark/acronym (e.g., "blu", "OVO")
                val isShortThreeLetterAcronym = xClusters.size <= 3 &&
                    filteredContours.size <= 8 &&
                    leftColors == rightColors

                if (leftTinyCount >= 5 && rightGroup.size <= 8 && rightLargeCount >= 3) {
                    selectedContours = rightGroup
                    isWordmarkOnly = true
                } else if (isShortThreeLetterAcronym) {
                    selectedContours = filteredContours
                    isWordmarkOnly = true
                } else {
                    val hasColorSeparation = leftColors != rightColors || leftColors.size >= 2
                    val hasHeightDominance = leftB.height() >= rightB.height() * 1.14f
                    val hasSquareContainer = leftGroup.size >= 3 && leftGroup.any {
                        it.bounds.width() >= totalH * 0.70f &&
                            it.bounds.height() >= totalH * 0.70f &&
                            (it.bounds.width() / it.bounds.height()) in 0.72f..1.38f
                    }

                    val isStandaloneIcon = rightClusters.size >= 2 &&
                        leftAspect in 0.48f..1.48f &&
                        leftB.height() >= totalH * 0.68f &&
                        (minSplitX <= leftB.right) &&
                        (hasColorSeparation || hasHeightDominance || hasSquareContainer)

                    if (isStandaloneIcon) {
                        selectedContours = leftGroup
                    } else {
                        isWordmarkOnly = true
                    }
                }
            } else {
                isWordmarkOnly = true
            }
        }

        var iconBounds = unionBounds(selectedContours.map { it.bounds })
        val iconW = iconBounds.width().coerceAtLeast(1f)
        val iconH = iconBounds.height().coerceAtLeast(1f)
        val iconArea = iconW * iconH

        val primaryBrandColorInt = pickPrimaryBrandColor(selectedContours, filteredContours)
        val primaryBrandColor = Color(primaryBrandColorInt)

        val uniqueNormalizedColors = selectedContours.map { normalizeColor(it.colorInt) }.distinct()

        // Check if there is a full-bleed container contour covering >= 75% of iconBounds
        val containerContours = selectedContours.filter {
            it.bounds.width() >= iconW * 0.86f &&
                it.bounds.height() >= iconH * 0.86f &&
                it.area >= iconArea * 0.75f &&
                !isNearWhite(it.colorInt)
        }
        val primaryContainer = containerContours.firstOrNull()

        val layers = mutableListOf<IconPathLayer>()
        var badgeBgColor: Color
        val needsLightBorder = false

        if (uniqueNormalizedColors.size == 1) {
            // Single-color icon mark (e.g., Jago-app, OVO concentric rings, BRI monogram, BCA, blu, LinkAja)
            val singleColorInt = selectedContours.first().colorInt
            val singleColor = Color(singleColorInt)
            badgeBgColor = singleColor
            val fgColorInt = if (singleColor.luminance() > 0.62f) {
                Color(0xFF18181B).toArgb()
            } else {
                android.graphics.Color.WHITE
            }

            selectedContours.groupBy { it.sourcePathIndex }.values.forEach { contours ->
                val merged = Path().apply {
                    // Use EVEN_ODD when a single path has nested cutouts (like OVO concentric rings or BRI cutouts)
                    fillType = if (contours.first().isEvenOdd || contours.size >= 2) {
                        Path.FillType.EVEN_ODD
                    } else {
                        Path.FillType.WINDING
                    }
                    contours.forEach { addPath(it.path) }
                }
                layers.add(IconPathLayer(merged, fgColorInt))
            }
        } else if (primaryContainer != null) {
            // Multi-color icon mark with its own solid container (e.g., GoPay, DANA, ShopeePay, CIMB Niaga, AstraPay)
            val containerColorInt = primaryContainer.colorInt
            badgeBgColor = Color(containerColorInt)

            val innerContours = selectedContours.filterNot {
                normalizeColor(it.colorInt) == normalizeColor(containerColorInt) &&
                    it.bounds.width() >= iconW * 0.86f &&
                    it.bounds.height() >= iconH * 0.86f
            }

            if (innerContours.isNotEmpty()) {
                val innerB = unionBounds(innerContours.map { it.bounds })
                val centerOffsetX = abs(innerB.centerX() - iconBounds.centerX()) / iconW
                val centerOffsetY = abs(innerB.centerY() - iconBounds.centerY()) / iconH
                // True symmetric circle/square frame (GoPay, DANA, CIMB, AstraPay) has centerOffsetY < 0.06f,
                // whereas ShopeePay's wallet with top flap has centerOffsetY ~ 0.115f so it keeps its wallet silhouette!
                val isCenteredFrame = centerOffsetX < 0.06f && centerOffsetY < 0.06f

                if (isCenteredFrame) {
                    // Strip the redundant outer circle/square/triangle so the inner symbol scales up boldly!
                    iconBounds = innerB
                    innerContours.groupBy { it.sourcePathIndex }.values.forEach { contours ->
                        val rawColor = contours.first().colorInt
                        val drawColor = if (normalizeColor(rawColor) == normalizeColor(containerColorInt)) {
                            android.graphics.Color.WHITE
                        } else {
                            rawColor
                        }
                        val merged = Path().apply {
                            fillType = if (contours.first().isEvenOdd) Path.FillType.EVEN_ODD else Path.FillType.WINDING
                            contours.forEach { addPath(it.path) }
                        }
                        layers.add(IconPathLayer(merged, drawColor))
                    }
                } else {
                    // Asymmetric shaped silhouette (like ShopeePay's wallet with top flap + inner 'S'):
                    // Draw wallet silhouette in White and inner 'S' in brandColor on the brandColor badge!
                    selectedContours.groupBy { it.sourcePathIndex }.values.forEach { contours ->
                        val rawColor = contours.first().colorInt
                        val drawColor = if (normalizeColor(rawColor) == normalizeColor(containerColorInt)) {
                            android.graphics.Color.WHITE
                        } else if (isNearWhite(rawColor)) {
                            containerColorInt
                        } else {
                            rawColor
                        }
                        val merged = Path().apply {
                            fillType = if (contours.first().isEvenOdd) Path.FillType.EVEN_ODD else Path.FillType.WINDING
                            contours.forEach { addPath(it.path) }
                        }
                        layers.add(IconPathLayer(merged, drawColor))
                    }
                }
            } else {
                selectedContours.groupBy { it.sourcePathIndex }.values.forEach { contours ->
                    val merged = Path().apply {
                        fillType = if (contours.first().isEvenOdd) Path.FillType.EVEN_ODD else Path.FillType.WINDING
                        contours.forEach { addPath(it.path) }
                    }
                    layers.add(IconPathLayer(merged, contours.first().colorInt))
                }
            }
        } else {
            // Multi-color icon mark without a solid background container (e.g., SeaBank, Mandiri, BSI, BNI, Permata, Jago)
            val colorAreas = mutableMapOf<Int, Float>()
            selectedContours.forEach { c ->
                val norm = normalizeColor(c.colorInt)
                colorAreas[norm] = (colorAreas[norm] ?: 0f) + c.area
            }
            val dominantColorInt = colorAreas.maxByOrNull { it.value }?.key ?: primaryBrandColorInt
            badgeBgColor = Color(dominantColorInt)

            selectedContours.groupBy { it.sourcePathIndex }.values.forEach { contours ->
                val rawColor = contours.first().colorInt
                val normColor = normalizeColor(rawColor)
                val drawColor = if (normColor == dominantColorInt || Color(rawColor).luminance() < 0.18f) {
                    android.graphics.Color.WHITE
                } else {
                    rawColor
                }
                val merged = Path().apply {
                    fillType = if (contours.first().isEvenOdd) Path.FillType.EVEN_ODD else Path.FillType.WINDING
                    contours.forEach { addPath(it.path) }
                }
                layers.add(IconPathLayer(merged, drawColor))
            }
        }

        return ExtractedFinIcon(
            badgeBackgroundColor = badgeBgColor,
            primaryBrandColor = primaryBrandColor,
            iconBounds = iconBounds,
            layers = layers,
            isWordmarkOnly = isWordmarkOnly,
            needsLightBorderInLightMode = needsLightBorder
        )
    }

    private fun clusterHorizontally(contours: List<ContourData>, tolerance: Float): List<List<ContourData>> {
        val sorted = contours.sortedBy { it.bounds.left }
        val clusters = mutableListOf<MutableList<ContourData>>()
        var currentEnd = -Float.MAX_VALUE

        for (c in sorted) {
            if (clusters.isEmpty() || c.bounds.left > currentEnd + tolerance) {
                clusters.add(mutableListOf(c))
                currentEnd = c.bounds.right
            } else {
                clusters.last().add(c)
                currentEnd = max(currentEnd, c.bounds.right)
            }
        }
        return clusters
    }

    private fun pickPrimaryBrandColor(
        selectedContours: List<ContourData>,
        allContours: List<ContourData>
    ): Int {
        val pool = (selectedContours + allContours).filterNot { isNearWhite(it.colorInt) }
        if (pool.isEmpty()) return android.graphics.Color.parseColor("#0B57D0")

        val scores = mutableMapOf<Int, Float>()
        pool.forEach { c ->
            val norm = normalizeColor(c.colorInt)
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(norm, hsv)
            val saturationBonus = 1f + hsv[1] * 1.5f
            val darkPenalty = if (hsv[2] < 0.15f && hsv[1] < 0.2f) 0.15f else 1f
            val selectedBonus = if (c in selectedContours) 2.5f else 1f
            scores[norm] = (scores[norm] ?: 0f) + c.area * saturationBonus * darkPenalty * selectedBonus
        }
        return scores.maxByOrNull { it.value }?.key ?: pool.first().colorInt
    }

    private fun unionBounds(rects: List<RectF>): RectF {
        val res = RectF()
        rects.forEachIndexed { idx, r ->
            if (idx == 0) res.set(r) else res.union(r)
        }
        return res
    }

    private fun isNearWhite(colorInt: Int): Boolean {
        val r = android.graphics.Color.red(colorInt)
        val g = android.graphics.Color.green(colorInt)
        val b = android.graphics.Color.blue(colorInt)
        return r > 235 && g > 235 && b > 235
    }

    private fun normalizeColor(colorInt: Int): Int {
        if (isNearWhite(colorInt)) return android.graphics.Color.WHITE
        val r = (android.graphics.Color.red(colorInt) / 4) * 4
        val g = (android.graphics.Color.green(colorInt) / 4) * 4
        val b = (android.graphics.Color.blue(colorInt) / 4) * 4
        return android.graphics.Color.rgb(r, g, b)
    }

    private fun resolveSvgFillColor(raw: String?, gradients: Map<String, Int>): Int? {
        if (raw.isNullOrBlank()) return android.graphics.Color.BLACK
        val trimmed = raw.trim()
        if (trimmed.startsWith("url(#", ignoreCase = true)) {
            val id = trimmed.substringAfter("(#").substringBefore(")")
            return gradients[id]
        }
        return parseDirectColor(trimmed)
    }

    private fun parseDirectColor(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim().lowercase()
        if (trimmed == "none" || trimmed == "transparent") return null
        if (trimmed == "white") return android.graphics.Color.WHITE
        if (trimmed == "black") return android.graphics.Color.BLACK
        if (trimmed.startsWith("#")) {
            val hex = trimmed.removePrefix("#")
            val expanded = when (hex.length) {
                3 -> "${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}"
                6, 8 -> hex
                else -> return null
            }
            return runCatching { android.graphics.Color.parseColor("#$expanded") }.getOrNull()
        }
        return null
    }
}

/**
 * Universal Financial Institution Repository powered dynamically by `IdnFinLogos`,
 * curated to famous Indonesian Banks and E-Wallets so there are no obscure regional icons.
 */
object FinLogoMapping {

    private val popularBankOrder = listOf(
        "bca",
        "mandiri",
        "bri",
        "bni",
        "bsi",
        "jago",
        "seabank",
        "blu-bca",
        "cimb-niaga",
        "permata"
    )

    private val popularEWalletOrder = listOf(
        "gopay",
        "ovo",
        "dana",
        "shopee-pay",
        "linkaja",
        "astra-pay"
    )

    val banks: List<FinInstitution> by lazy {
        buildCategoryList(
            category = FinCategory.BANK,
            rawList = runCatching { IdnFinLogos.byCategory("bank-logo") }.getOrDefault(emptyList()),
            popularSlugs = popularBankOrder
        )
    }

    val eWallets: List<FinInstitution> by lazy {
        buildCategoryList(
            category = FinCategory.EWALLET,
            rawList = runCatching { IdnFinLogos.byCategory("e-wallet") }.getOrDefault(emptyList()),
            popularSlugs = popularEWalletOrder
        )
    }

    val cashInstitution = FinInstitution(
        id = "cash",
        displayName = "Dompet Tunai",
        category = FinCategory.CASH,
        assetPath = null,
        isPopular = true,
        popularRank = 0
    )

    val allInstitutions: List<FinInstitution> by lazy {
        banks + eWallets + listOf(cashInstitution)
    }

    private fun resolvePreferredAssetPath(meta: LogoMeta): String {
        val appVariant = runCatching { IdnFinLogos.get("${meta.slug}-app") }.getOrNull()
        return appVariant?.assetPath ?: meta.assetPath
    }

    private fun buildCategoryList(
        category: FinCategory,
        rawList: List<LogoMeta>,
        popularSlugs: List<String>
    ): List<FinInstitution> {
        return rawList
            .filter { meta -> meta.slug in popularSlugs }
            .map { meta ->
                val popIdx = popularSlugs.indexOf(meta.slug)
                FinInstitution(
                    id = meta.slug,
                    displayName = formatUniversalName(meta),
                    category = category,
                    assetPath = resolvePreferredAssetPath(meta),
                    isPopular = true,
                    popularRank = popIdx
                )
            }
            .sortedBy { it.popularRank }
    }

    private fun formatUniversalName(meta: LogoMeta): String {
        val raw = meta.name.trim()
        return raw
            .replace(Regex("^PT\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+Digital$", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    fun byCategory(category: FinCategory): List<FinInstitution> = when (category) {
        FinCategory.BANK -> banks
        FinCategory.EWALLET -> eWallets
        FinCategory.CASH -> listOf(cashInstitution)
    }

    /**
     * Universally resolves any wallet ID, display name, or slug via `IdnFinLogos`.
     */
    fun resolveInstitution(idOrName: String?): FinInstitution {
        if (idOrName.isNullOrBlank()) return banks.firstOrNull() ?: cashInstitution
        val query = idOrName.trim().lowercase()

        if (query.contains("tunai") || query.contains("cash")) {
            return cashInstitution
        }

        // 1. Direct match in loaded institutions
        allInstitutions.firstOrNull {
            it.id.equals(query, ignoreCase = true) ||
                it.displayName.equals(query, ignoreCase = true)
        }?.let { return it }

        // 2. Normalized slug / keyword match
        val stripped = query
            .removePrefix("bank ")
            .removePrefix("wallet_")
            .substringBefore("_")
            .trim()

        allInstitutions.firstOrNull {
            it.id.equals(stripped, ignoreCase = true) ||
                it.id.replace("-", "").equals(stripped.replace("-", "").replace(" ", ""), ignoreCase = true) ||
                it.displayName.lowercase().contains(stripped) ||
                stripped.contains(it.id)
        }?.let { return it }

        // 3. Universal fallback via IdnFinLogos.get() / IdnFinLogos.search()
        val meta = runCatching {
            IdnFinLogos.get(stripped) ?: IdnFinLogos.search(stripped).firstOrNull()
        }.getOrNull()

        if (meta != null) {
            val cat = if (meta.category == "e-wallet") FinCategory.EWALLET else FinCategory.BANK
            return FinInstitution(
                id = meta.slug,
                displayName = formatUniversalName(meta),
                category = cat,
                assetPath = resolvePreferredAssetPath(meta)
            )
        }

        return banks.firstOrNull() ?: cashInstitution
    }

    fun resolveCardGradient(context: Context, idOrName: String?, isDark: Boolean): List<Color> {
        val inst = resolveInstitution(idOrName)
        if (inst.category == FinCategory.CASH) {
            return if (isDark) {
                listOf(Color(0xFFC2410C), Color(0xFFEA580C))
            } else {
                listOf(Color(0xFFE65100), Color(0xFFF57C00))
            }
        }
        val extracted = SvgIconExtractor.extract(context, inst.assetPath)
        return extracted?.cardGradient(isDark) ?: listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
    }
}

/**
 * Core Universal Vector Badge that renders the isolated 1:1 Icon-Mark of ANY Bank or E-Wallet
 * in `idn-finlogos` inside a full-bleed colored Circle or Squircle.
 */
@Composable
fun FinLogoBadge(
    idOrName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    shape: Shape = RoundedCornerShape(13.dp),
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent
) {
    val context = LocalContext.current
    val isDark = MoneyTrackerTheme.colors.isDark
    val institution = remember(idOrName) { FinLogoMapping.resolveInstitution(idOrName) }
    val extracted = remember(institution.assetPath) {
        SvgIconExtractor.extract(context, institution.assetPath)
    }

    val borderModifier = if (borderWidth > 0.dp && borderColor != Color.Transparent) {
        Modifier.border(borderWidth, borderColor, shape)
    } else {
        Modifier
    }

    if (extracted == null || institution.category == FinCategory.CASH) {
        val cashColor = if (isDark) Color(0xFFFB923C) else Color(0xFFE65100)
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(cashColor)
                .then(borderModifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = institution.fallbackIcon,
                contentDescription = institution.displayName,
                tint = Color.White,
                modifier = Modifier.size(size * 0.52f)
            )
        }
        return
    }

    val contentFraction = if (extracted.isWordmarkOnly) 0.64f else 0.58f

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(extracted.badgeBackgroundColor)
            .then(borderModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val bounds = extracted.iconBounds
            val bW = bounds.width().coerceAtLeast(1f)
            val bH = bounds.height().coerceAtLeast(1f)

            val targetW = canvasW * contentFraction
            val targetH = canvasH * contentFraction
            val scale = min(targetW / bW, targetH / bH)

            val matrix = Matrix().apply {
                postTranslate(-bounds.left, -bounds.top)
                postScale(scale, scale)
                postTranslate((canvasW - bW * scale) / 2f, (canvasH - bH * scale) / 2f)
            }

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }

            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                extracted.layers.forEach { layer ->
                    val transformed = Path(layer.path)
                    transformed.transform(matrix)
                    paint.color = layer.colorInt
                    native.drawPath(transformed, paint)
                }
            }
        }
    }
}

/**
 * Squircle wrapper (`RoundedCornerShape`) for Account List Cards, Detail Sheets, and Carousel Cards.
 */
@Composable
fun FinLogoSquircleBadge(
    idOrName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    cornerRadius: Dp = 13.dp,
    horizontalPadding: Dp = 0.dp,
    verticalPadding: Dp = 0.dp
) {
    FinLogoBadge(
        idOrName = idOrName,
        size = size,
        shape = RoundedCornerShape(cornerRadius),
        modifier = modifier
    )
}

/**
 * Circular wrapper (`CircleShape`) matching the Bank & E-Wallet list design and inline transaction indicators.
 */
@Composable
fun FinLogoCircleBadge(
    idOrName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent
) {
    FinLogoBadge(
        idOrName = idOrName,
        size = size,
        shape = CircleShape,
        borderWidth = borderWidth,
        borderColor = borderColor,
        modifier = modifier
    )
}

/**
 * Emblem badge for top-right of `WalletCard` carousel and inline transaction payment source.
 */
@Composable
fun FinLogoCardBadge(
    idOrName: String?,
    modifier: Modifier = Modifier,
    width: Dp = 38.dp,
    height: Dp = 38.dp,
    cornerRadius: Dp = 12.dp
) {
    val badgeSize = min(width.value, height.value).dp
    FinLogoBadge(
        idOrName = idOrName,
        size = badgeSize,
        shape = if (cornerRadius >= badgeSize / 2) CircleShape else RoundedCornerShape(cornerRadius),
        modifier = modifier
    )
}
