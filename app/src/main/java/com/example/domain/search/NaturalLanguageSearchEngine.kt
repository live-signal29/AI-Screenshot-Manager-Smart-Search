package com.example.domain.search

import com.example.data.local.ScreenshotEntity
import com.example.domain.ocr.CategoryClassifier
import java.util.Calendar

data class SearchIntent(
    val rawQuery: String,
    val textKeywords: List<String> = emptyList(),
    val targetCategory: String? = null,
    val requirePhone: Boolean = false,
    val requireEmail: Boolean = false,
    val requireUrl: Boolean = false,
    val requirePrice: Boolean = false,
    val targetPriceAmount: Double? = null,
    val targetDateStart: Long? = null,
    val targetDateEnd: Long? = null,
    val requireFavorite: Boolean = false,
    val explanation: String = ""
)

object NaturalLanguageSearchEngine {

    private val STOP_WORDS = setOf(
        "show", "find", "search", "get", "my", "me", "screenshots", "screenshot", "screen",
        "shots", "images", "photos", "pics", "containing", "contains", "about", "with", "from",
        "wali", "wala", "wale", "meri", "mere", "dhundo", "batao", "karo", "having", "please"
    )

    private val MONTH_MAP = mapOf(
        "january" to 0, "jan" to 0,
        "february" to 1, "feb" to 1,
        "march" to 2, "mar" to 2,
        "april" to 3, "apr" to 3,
        "may" to 4,
        "june" to 5, "jun" to 5,
        "july" to 6, "jul" to 6,
        "august" to 7, "aug" to 7,
        "september" to 8, "sep" to 8, "sept" to 8,
        "october" to 9, "oct" to 9,
        "november" to 10, "nov" to 10,
        "december" to 11, "dec" to 11
    )

    fun parseQuery(raw: String): SearchIntent {
        val query = raw.trim()
        val lower = query.lowercase()

        var targetCategory: String? = null
        var requirePhone = false
        var requireEmail = false
        var requireUrl = false
        var requirePrice = false
        var targetPriceAmount: Double? = null
        var dateStart: Long? = null
        var dateEnd: Long? = null
        var requireFavorite = false
        val explanations = mutableListOf<String>()

        // 1. Detect Category
        for (cat in CategoryClassifier.ALL_CATEGORIES) {
            val catLower = cat.lowercase()
            if (lower.contains(catLower) ||
                (cat == CategoryClassifier.CATEGORY_RECEIPTS && (lower.contains("receipt") || lower.contains("bill") || lower.contains("invoice"))) ||
                (cat == CategoryClassifier.CATEGORY_TICKETS && (lower.contains("ticket") || lower.contains("flight") || lower.contains("boarding pass") || lower.contains("cinema"))) ||
                (cat == CategoryClassifier.CATEGORY_SHOPPING && (lower.contains("shopping") || lower.contains("amazon") || lower.contains("cart") || lower.contains("order"))) ||
                (cat == CategoryClassifier.CATEGORY_TRAVEL && (lower.contains("travel") || lower.contains("hotel") || lower.contains("trip"))) ||
                (cat == CategoryClassifier.CATEGORY_STUDY && (lower.contains("study") || lower.contains("exam") || lower.contains("homework") || lower.contains("lecture"))) ||
                (cat == CategoryClassifier.CATEGORY_FINANCE && (lower.contains("bank") || lower.contains("finance") || lower.contains("transfer") || lower.contains("payment")))
            ) {
                targetCategory = cat
                explanations.add("Category: $cat")
                break
            }
        }

        // 2. Detect Entity requirements
        if (lower.contains("phone number") || lower.contains("phone") || lower.contains("contact") || lower.contains("mobile number")) {
            requirePhone = true
            explanations.add("Has phone number")
        }
        if (lower.contains("email address") || lower.contains("email") || lower.contains("e-mail") || lower.contains("mail")) {
            requireEmail = true
            explanations.add("Has email address")
        }
        if (lower.contains("url") || lower.contains("link") || lower.contains("website") || lower.contains("http")) {
            requireUrl = true
            explanations.add("Has web link")
        }
        if (lower.contains("favorite") || lower.contains("starred") || lower.contains("important")) {
            requireFavorite = true
            explanations.add("Marked favorite")
        }

        // 3. Detect Price / Amount
        val priceRegex = Regex("""(?:\$|€|£|₹|pkr|usd|eur)?\s*(\d+(?:\.\d{1,2})?)\s*(?:\$|€|£|₹|dollars?|rupees?|euros?)?""")
        val priceMatches = priceRegex.findAll(lower)
        for (m in priceMatches) {
            val numStr = m.groups[1]?.value
            if (numStr != null && numStr.isNotBlank()) {
                val num = numStr.toDoubleOrNull()
                if (num != null && num > 0 && num < 1000000) {
                    requirePrice = true
                    targetPriceAmount = num
                    explanations.add("Price contains $num")
                    break
                }
            }
        }

        // 4. Detect Date / Time Filters
        val now = Calendar.getInstance()
        if (lower.contains("today")) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            dateStart = cal.timeInMillis
            dateEnd = System.currentTimeMillis()
            explanations.add("Date: Today")
        } else if (lower.contains("yesterday")) {
            val calStart = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calEnd = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            dateStart = calStart.timeInMillis
            dateEnd = calEnd.timeInMillis
            explanations.add("Date: Yesterday")
        } else if (lower.contains("this week")) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            dateStart = cal.timeInMillis
            dateEnd = System.currentTimeMillis()
            explanations.add("Date: This week")
        } else if (lower.contains("this month")) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            dateStart = cal.timeInMillis
            dateEnd = System.currentTimeMillis()
            explanations.add("Date: This month")
        } else {
            // Check month names like "from January"
            for ((monthName, monthIndex) in MONTH_MAP) {
                if (lower.contains(monthName)) {
                    val calStart = Calendar.getInstance().apply {
                        set(Calendar.MONTH, monthIndex)
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }
                    val calEnd = Calendar.getInstance().apply {
                        set(Calendar.MONTH, monthIndex)
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }
                    dateStart = calStart.timeInMillis
                    dateEnd = calEnd.timeInMillis
                    explanations.add("Month: ${monthName.replaceFirstChar { it.uppercase() }}")
                    break
                }
            }
        }

        // 5. Extract remaining meaningful keywords
        val tokens = lower.split(Regex("""[\s,?!;:.#+]+"""))
            .filter { it.length > 2 && it !in STOP_WORDS }

        val textKeywords = tokens.filter { token ->
            val isMonth = MONTH_MAP.containsKey(token)
            val isDateWord = token in listOf("today", "yesterday", "week", "month", "year")
            val isEntityWord = token in listOf("phone", "number", "email", "link", "url", "price", "dollar", "rupee")
            !isMonth && !isDateWord && !isEntityWord
        }

        if (textKeywords.isNotEmpty()) {
            explanations.add("Keywords: " + textKeywords.joinToString(", "))
        }

        val explanationText = if (explanations.isNotEmpty()) explanations.joinToString(" • ") else "All screenshots"

        return SearchIntent(
            rawQuery = query,
            textKeywords = textKeywords,
            targetCategory = targetCategory,
            requirePhone = requirePhone,
            requireEmail = requireEmail,
            requireUrl = requireUrl,
            requirePrice = requirePrice,
            targetPriceAmount = targetPriceAmount,
            targetDateStart = dateStart,
            targetDateEnd = dateEnd,
            requireFavorite = requireFavorite,
            explanation = explanationText
        )
    }

    /**
     * Filters and scores a list of screenshots against the parsed natural language intent.
     */
    fun filterAndScore(screenshots: List<ScreenshotEntity>, intent: SearchIntent): List<Pair<ScreenshotEntity, Int>> {
        val results = mutableListOf<Pair<ScreenshotEntity, Int>>()

        for (item in screenshots) {
            var score = 0

            // Category match
            if (intent.targetCategory != null) {
                if (item.category.equals(intent.targetCategory, ignoreCase = true)) {
                    score += 50
                } else if (intent.targetCategory == CategoryClassifier.CATEGORY_OTHER) {
                    score += 10
                } else {
                    // Mismatch category penalizes or excludes if category was explicitly asked
                    continue
                }
            }

            // Entity matches
            if (intent.requirePhone) {
                if (item.extractedPhones.isNotBlank()) score += 40 else continue
            }
            if (intent.requireEmail) {
                if (item.extractedEmails.isNotBlank()) score += 40 else continue
            }
            if (intent.requireUrl) {
                if (item.extractedUrls.isNotBlank()) score += 30 else continue
            }
            if (intent.requirePrice) {
                if (item.extractedPrices.isNotBlank()) {
                    score += 30
                    if (intent.targetPriceAmount != null) {
                        val priceTarget = intent.targetPriceAmount.toInt().toString()
                        if (item.extractedPrices.contains(priceTarget) || item.ocrText.contains(priceTarget)) {
                            score += 50
                        }
                    }
                } else if (intent.targetPriceAmount != null) {
                    val priceTarget = intent.targetPriceAmount.toInt().toString()
                    if (item.ocrText.contains(priceTarget)) {
                        score += 30
                    } else {
                        continue
                    }
                }
            }

            // Date match
            if (intent.targetDateStart != null && intent.targetDateEnd != null) {
                if (item.dateTaken in intent.targetDateStart..intent.targetDateEnd) {
                    score += 40
                } else {
                    continue
                }
            }

            // Favorite match
            if (intent.requireFavorite) {
                if (item.isFavorite) score += 20 else continue
            }

            // Keyword text match across OCR text, filename, entities
            val searchableContent = (item.ocrText + " " + item.displayName + " " + item.category).lowercase()
            var matchedKeywordsCount = 0

            for (kw in intent.textKeywords) {
                if (searchableContent.contains(kw)) {
                    matchedKeywordsCount++
                    score += 30
                }
            }

            // If keywords were specified, require at least one match unless satisfied by high category/entity match
            if (intent.textKeywords.isNotEmpty() && matchedKeywordsCount == 0 && score < 40) {
                continue
            }

            // Add baseline score
            score += 10
            results.add(Pair(item, score))
        }

        // Sort descending by score, then by newest
        return results.sortedWith(compareByDescending<Pair<ScreenshotEntity, Int>> { it.second }.thenByDescending { it.first.dateTaken })
    }
}
