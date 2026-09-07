package com.example.domain.ocr

object CategoryClassifier {

    const val CATEGORY_RECEIPTS = "Receipts"
    const val CATEGORY_SHOPPING = "Shopping"
    const val CATEGORY_BILLS = "Bills"
    const val CATEGORY_TICKETS = "Tickets"
    const val CATEGORY_TRAVEL = "Travel"
    const val CATEGORY_STUDY = "Study"
    const val CATEGORY_NOTES = "Notes"
    const val CATEGORY_DOCUMENTS = "Documents"
    const val CATEGORY_SOCIAL = "Social"
    const val CATEGORY_ENTERTAINMENT = "Entertainment"
    const val CATEGORY_MAPS = "Maps"
    const val CATEGORY_FINANCE = "Finance"
    const val CATEGORY_FOOD = "Food"
    const val CATEGORY_OTHER = "Other"

    val ALL_CATEGORIES = listOf(
        CATEGORY_RECEIPTS,
        CATEGORY_SHOPPING,
        CATEGORY_BILLS,
        CATEGORY_TICKETS,
        CATEGORY_TRAVEL,
        CATEGORY_STUDY,
        CATEGORY_NOTES,
        CATEGORY_DOCUMENTS,
        CATEGORY_SOCIAL,
        CATEGORY_ENTERTAINMENT,
        CATEGORY_MAPS,
        CATEGORY_FINANCE,
        CATEGORY_FOOD,
        CATEGORY_OTHER
    )

    private val categoryKeywords: Map<String, List<String>> = mapOf(
        CATEGORY_RECEIPTS to listOf(
            "receipt", "subtotal", "tax", "cashier", "order total", "pos sale", "payment method",
            "tendered", "change due", "merchant", "store #", "vat invoice", "tip", "terminal"
        ),
        CATEGORY_SHOPPING to listOf(
            "amazon", "ebay", "cart", "checkout", "add to cart", "shipping address", "delivery fee",
            "tracking number", "walmart", "target", "aliexpress", "order placed", "item total", "buy now"
        ),
        CATEGORY_BILLS to listOf(
            "bill", "due date", "amount due", "utility", "electricity", "electric bill", "water bill",
            "gas bill", "telecom", "broadband", "billing statement", "invoice date", "late fee"
        ),
        CATEGORY_TICKETS to listOf(
            "ticket", "boarding pass", "gate", "seat", "pnr", "flight", "terminal", "boarding time",
            "departure", "arrival", "cinema", "movie ticket", "concert", "booking id", "eticket", "e-ticket"
        ),
        CATEGORY_TRAVEL to listOf(
            "hotel", "airbnb", "resort", "check-in", "checkout", "itinerary", "airline", "expedia",
            "booking.com", "hostel", "passport", "visa", "reservation confirmed", "trip"
        ),
        CATEGORY_STUDY to listOf(
            "lecture", "assignment", "quiz", "exam", "syllabus", "homework", "chapter", "professor",
            "textbook", "university", "college", "course", "grade", "gpa", "thesis", "research paper"
        ),
        CATEGORY_NOTES to listOf(
            "note", "todo", "to-do", "reminder", "checklist", "bullet", "meeting notes", "brainstorm",
            "memo", "ideas", "tasks", "keep notes"
        ),
        CATEGORY_DOCUMENTS to listOf(
            "agreement", "contract", "license", "certificate", "identification", "affidavit",
            "terms and conditions", "policy", "signed", "notary", "confidential", "legal", "official"
        ),
        CATEGORY_SOCIAL to listOf(
            "instagram", "twitter", "tweet", "facebook", "tiktok", "whatsapp", "reddit", "telegram",
            "snapchat", "followers", "retweet", "post", "dm", "message", "chat", "threads"
        ),
        CATEGORY_ENTERTAINMENT to listOf(
            "spotify", "netflix", "youtube", "hulu", "disney+", "podcast", "album", "artist",
            "playlist", "trailer", "gameplay", "stream", "series", "season", "episode"
        ),
        CATEGORY_MAPS to listOf(
            "google maps", "waze", "navigation", "turn right", "turn left", "head north", "exit",
            "highway", "route", "traffic", "destination", "km", "miles", "estimated arrival"
        ),
        CATEGORY_FINANCE to listOf(
            "bank", "transfer", "account balance", "credit card", "debit card", "transaction",
            "wire transfer", "iban", "swift", "routing number", "paypal", "crypto", "bitcoin", "wallet"
        ),
        CATEGORY_FOOD to listOf(
            "menu", "restaurant", "doordash", "ubereats", "grubhub", "swiggy", "zomato", "delivery fee",
            "appetizer", "entree", "calories", "recipe", "ingredients", "nutrition"
        )
    )

    private val temporaryRemovableKeywords = listOf(
        "otp", "verification code", "one-time password", "security code", "valid for 5 minutes",
        "do not share this code", "confirm login", "auth code", "sms code", "temporary code"
    )

    fun classify(text: String, filename: String = ""): String {
        val lowerText = (text + " " + filename).lowercase()
        var bestCategory = CATEGORY_OTHER
        var highestScore = 0

        for ((category, keywords) in categoryKeywords) {
            var score = 0
            for (keyword in keywords) {
                if (lowerText.contains(keyword)) {
                    score += keyword.length // give slightly more weight to longer phrases
                }
            }
            if (score > highestScore && score >= 4) {
                highestScore = score
                bestCategory = category
            }
        }

        return bestCategory
    }

    fun isPotentiallyRemovable(text: String): Boolean {
        val lower = text.lowercase()
        return temporaryRemovableKeywords.any { lower.contains(it) }
    }
}
