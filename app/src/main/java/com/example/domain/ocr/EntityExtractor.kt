package com.example.domain.ocr

data class ExtractedEntities(
    val prices: List<String> = emptyList(),
    val dates: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val phones: List<String> = emptyList(),
    val urls: List<String> = emptyList(),
    val orderNumbers: List<String> = emptyList()
)

object EntityExtractor {

    // Prices: $49.99, ₹2,500, PKR 5,000, €20, £10.99, 50 USD, etc.
    private val priceRegex = Regex(
        """(?:[\$€£₹¥]|(?:PKR|USD|EUR|GBP|INR|CAD|AUD)\s?)\s*(\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{2})?|\d+(?:[.,]\d{2})?)|\b(\d+(?:[.,]\d{2})?)\s*(?:[\$€£₹¥]|PKR|USD|EUR|GBP|INR)""",
        RegexOption.IGNORE_CASE
    )

    // Dates: 12/08/2026, 2026-08-12, August 12, 12 Aug 2026, etc.
    private val dateRegex = Regex(
        """\b\d{1,4}[-/. ]\d{1,2}[-/. ]\d{2,4}\b|\b(?:Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?)\s+\d{1,2}(?:st|nd|rd|th)?(?:,\s*\d{4})?\b""",
        RegexOption.IGNORE_CASE
    )

    // Emails
    private val emailRegex = Regex(
        """[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}"""
    )

    // Phone numbers: +1-800-555-0199, (555) 123-4567, etc.
    private val phoneRegex = Regex(
        """(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}|\b\+?\d{10,14}\b"""
    )

    // URLs
    private val urlRegex = Regex(
        """\b(?:https?://|www\.)[A-Za-z0-9.-]+(?::\d+)?(?:/[^\s<>"{}|\^`\\]*)?""",
        RegexOption.IGNORE_CASE
    )

    // Order, Invoice, Tracking, Flight numbers
    private val orderOrFlightRegex = Regex(
        """(?i)\b(?:Order|Invoice|Booking|Tracking|Receipt|Flight|PNR|Reference|Ref|Ticket)(?:\s*(?:#|No\.?|Num|ID|Code))?\s*[:#-]?\s*([A-Z0-9-]{4,20})\b|\b([A-Z]{2}\s?\d{3,4})\b"""
    )

    fun extract(text: String): ExtractedEntities {
        if (text.isBlank()) return ExtractedEntities()

        val prices = priceRegex.findAll(text)
            .map { it.value.trim() }
            .distinct()
            .take(10)
            .toList()

        val dates = dateRegex.findAll(text)
            .map { it.value.trim() }
            .distinct()
            .take(10)
            .toList()

        val emails = emailRegex.findAll(text)
            .map { it.value.trim() }
            .distinct()
            .take(10)
            .toList()

        val phones = phoneRegex.findAll(text)
            .map { it.value.trim() }
            .filter { it.replace(Regex("""\D"""), "").length in 10..15 }
            .distinct()
            .take(10)
            .toList()

        val urls = urlRegex.findAll(text)
            .map { it.value.trim() }
            .distinct()
            .take(10)
            .toList()

        val orderNumbers = orderOrFlightRegex.findAll(text)
            .map { it.value.trim() }
            .distinct()
            .take(10)
            .toList()

        return ExtractedEntities(
            prices = prices,
            dates = dates,
            emails = emails,
            phones = phones,
            urls = urls,
            orderNumbers = orderNumbers
        )
    }

    fun serializeList(items: List<String>): String = items.joinToString(";;")
    fun deserializeList(raw: String): List<String> = if (raw.isBlank()) emptyList() else raw.split(";;").filter { it.isNotBlank() }
}
