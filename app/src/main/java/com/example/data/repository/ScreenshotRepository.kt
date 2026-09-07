package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryStat
import com.example.data.local.ScreenshotDao
import com.example.data.local.ScreenshotEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.ai.AiAssistantService
import com.example.domain.ai.AiInsight
import com.example.domain.duplicate.DuplicateDetector
import com.example.domain.duplicate.DuplicateGroup
import com.example.domain.ocr.CategoryClassifier
import com.example.domain.ocr.EntityExtractor
import com.example.domain.ocr.OcrProcessor
import com.example.domain.scanner.ScreenshotScanner
import com.example.domain.search.NaturalLanguageSearchEngine
import com.example.domain.search.SearchIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class ScanProgress(
    val isScanning: Boolean = false,
    val currentItem: Int = 0,
    val totalItems: Int = 0,
    val currentStepText: String = ""
)

class ScreenshotRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context)
) {
    private val dao: ScreenshotDao = database.screenshotDao()
    private val scanner = ScreenshotScanner(context)
    private val ocrProcessor = OcrProcessor(context)
    private val aiService = AiAssistantService()

    val allScreenshotsFlow: Flow<List<ScreenshotEntity>> = dao.getAllFlow()
    val favoritesFlow: Flow<List<ScreenshotEntity>> = dao.getFavoritesFlow()
    val duplicatesFlow: Flow<List<ScreenshotEntity>> = dao.getDuplicatesFlow()
    val totalCountFlow: Flow<Int> = dao.getTotalCountFlow()
    val totalStorageFlow: Flow<Long> = dao.getTotalStorageFlow()
    val duplicateCountFlow: Flow<Int> = dao.getDuplicateCountFlow()
    val duplicateStorageFlow: Flow<Long> = dao.getDuplicateStorageFlow()
    val categoryStatsFlow: Flow<List<CategoryStat>> = dao.getCategoryStats()
    val largeScreenshotsFlow: Flow<List<ScreenshotEntity>> = dao.getLargeScreenshotsFlow()
    val removableScreenshotsFlow: Flow<List<ScreenshotEntity>> = dao.getRemovableScreenshotsFlow()

    fun getByCategoryFlow(category: String): Flow<List<ScreenshotEntity>> = dao.getByCategoryFlow(category)
    fun getByIdFlow(id: Long): Flow<ScreenshotEntity?> = dao.getByIdFlow(id)

    suspend fun getById(id: Long): ScreenshotEntity? = dao.getById(id)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        dao.setFavorite(id, isFavorite)
    }

    suspend fun updateCategory(id: Long, category: String) {
        dao.updateCategory(id, category)
    }

    suspend fun deleteScreenshot(id: Long) {
        dao.deleteById(id)
    }

    suspend fun deleteScreenshots(ids: List<Long>) {
        dao.deleteByIds(ids)
    }

    suspend fun clearAllData() {
        dao.clearAll()
    }

    /**
     * Scans the device MediaStore for screenshots and updates local database.
     */
    suspend fun scanMediaLibrary(onProgress: (ScanProgress) -> Unit = {}): Int = withContext(Dispatchers.IO) {
        onProgress(ScanProgress(isScanning = true, currentItem = 0, totalItems = 0, currentStepText = "Scanning device storage..."))
        
        val lastScan = preferencesRepository.lastScanTimestamp.first()
        val foundMedia = scanner.scanScreenshots(lastScan)

        if (foundMedia.isNotEmpty()) {
            dao.insertAll(foundMedia)
        }

        preferencesRepository.setLastScanTimestamp(System.currentTimeMillis())

        // If library is empty (e.g. fresh emulator), seed curated demo screenshots for instant productivity testing
        val count = dao.getAllList().size
        if (count == 0) {
            seedDemoScreenshots()
        }

        // Process any unanalyzed screenshots (OCR, entities, categories, perceptual hashing)
        processUnprocessedScreenshots(onProgress)

        // Update duplicate clusters
        updateDuplicateDetection()

        onProgress(ScanProgress(isScanning = false, currentItem = 0, totalItems = 0, currentStepText = "Scan complete"))
        dao.getAllList().size
    }

    suspend fun processUnprocessedScreenshots(onProgress: (ScanProgress) -> Unit = {}) = withContext(Dispatchers.IO) {
        val unprocessed = dao.getUnprocessed(limit = 100)
        val total = unprocessed.size
        if (total == 0) return@withContext

        val ocrEnabled = preferencesRepository.isOcrEnabled.first()

        for ((index, item) in unprocessed.withIndex()) {
            onProgress(
                ScanProgress(
                    isScanning = true,
                    currentItem = index + 1,
                    totalItems = total,
                    currentStepText = "Analyzing screenshot ${index + 1} of $total..."
                )
            )

            var ocrText = item.ocrText
            if (ocrEnabled && ocrText.isBlank() && item.uri.startsWith("content://")) {
                ocrText = ocrProcessor.processImageUri(item.uri)
            }

            val entities = EntityExtractor.extract(ocrText)
            val category = if (item.category == "Other" || item.category.isBlank()) {
                CategoryClassifier.classify(ocrText, item.displayName)
            } else {
                item.category
            }

            val isRemovable = CategoryClassifier.isPotentiallyRemovable(ocrText)
            val pHash = if (item.perceptualHash == 0L && item.uri.startsWith("content://")) {
                DuplicateDetector.computeDHash(context, item.uri)
            } else {
                item.perceptualHash
            }

            val updated = item.copy(
                ocrText = ocrText,
                category = category,
                isProcessed = true,
                isPotentiallyRemovable = isRemovable,
                perceptualHash = pHash,
                extractedPrices = EntityExtractor.serializeList(entities.prices),
                extractedDates = EntityExtractor.serializeList(entities.dates),
                extractedEmails = EntityExtractor.serializeList(entities.emails),
                extractedPhones = EntityExtractor.serializeList(entities.phones),
                extractedUrls = EntityExtractor.serializeList(entities.urls),
                extractedOrderNumbers = EntityExtractor.serializeList(entities.orderNumbers)
            )

            dao.update(updated)
        }
    }

    suspend fun updateDuplicateDetection() = withContext(Dispatchers.IO) {
        val all = dao.getAllList()
        val groups = DuplicateDetector.findDuplicateClusters(all)
        val toUpdate = mutableListOf<ScreenshotEntity>()

        for (group in groups) {
            for (dup in group.duplicateScreenshots) {
                toUpdate.add(dup.copy(isDuplicate = true, duplicateGroupId = group.groupId))
            }
        }

        if (toUpdate.isNotEmpty()) {
            dao.updateAll(toUpdate)
        }
    }

    suspend fun getDuplicateClusters(): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val all = dao.getAllList()
        DuplicateDetector.findDuplicateClusters(all)
    }

    suspend fun searchNaturalLanguage(query: String): Pair<SearchIntent, List<Pair<ScreenshotEntity, Int>>> = withContext(Dispatchers.IO) {
        val intent = NaturalLanguageSearchEngine.parseQuery(query)
        val all = dao.getAllList()
        val scored = NaturalLanguageSearchEngine.filterAndScore(all, intent)
        Pair(intent, scored)
    }

    suspend fun getAiInsight(screenshot: ScreenshotEntity): AiInsight = withContext(Dispatchers.IO) {
        val isCloudAi = preferencesRepository.isAiEnabled.first()
        val apiKey = preferencesRepository.aiApiKey.first()
        val endpoint = preferencesRepository.aiApiEndpoint.first()
        aiService.generateInsight(screenshot, isCloudAi, apiKey, endpoint)
    }

    suspend fun askAiQuestion(screenshot: ScreenshotEntity, question: String): String = withContext(Dispatchers.IO) {
        val isCloudAi = preferencesRepository.isAiEnabled.first()
        val apiKey = preferencesRepository.aiApiKey.first()
        val endpoint = preferencesRepository.aiApiEndpoint.first()
        aiService.answerQuestion(screenshot, question, isCloudAi, apiKey, endpoint)
    }

    private suspend fun seedDemoScreenshots() {
        val now = System.currentTimeMillis()
        val day = 86_400_000L

        val demoItems = listOf(
            ScreenshotEntity(
                id = 1001L,
                uri = "android.resource://${context.packageName}/drawable/ic_launcher_background",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_Flight_Ticket.png",
                displayName = "Screenshot_2026_Flight_AA284.png",
                dateTaken = now - (day * 2),
                dateModified = now - (day * 2),
                size = 1_840_000L,
                width = 1080,
                height = 2400,
                ocrText = "Boarding Pass - American Airlines\nFlight AA 284\nSeat 14B Gate B22\nDeparture: JFK 14:30\nArrival: SFO 18:10\nPNR: W7X9KP\nPassenger: John Doe\nPrice: $349.50\nBooking Date: 12/08/2026\nsupport@aa.com",
                category = CategoryClassifier.CATEGORY_TICKETS,
                isFavorite = true,
                isProcessed = true,
                perceptualHash = 0x123456789ABCDEFL,
                extractedPrices = "$349.50",
                extractedDates = "12/08/2026",
                extractedEmails = "support@aa.com",
                extractedPhones = "+1-800-433-7300",
                extractedUrls = "https://aa.com/checkin",
                extractedOrderNumbers = "AA 284;;PNR: W7X9KP"
            ),
            ScreenshotEntity(
                id = 1002L,
                uri = "android.resource://${context.packageName}/drawable/ic_launcher_background",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_Amazon_Order.png",
                displayName = "Screenshot_Amazon_Order_Details.png",
                dateTaken = now - (day * 3),
                dateModified = now - (day * 3),
                size = 2_150_000L,
                width = 1080,
                height = 2340,
                ocrText = "Amazon.com Checkout Confirmation\nOrder # 114-8392019-4829104\nItem: Sony WH-1000XM5 Wireless Headphones\nTotal: $398.00 (Tax: $32.00)\nEstimated Delivery: Tomorrow\nTracking ID: 1Z9999999999999999\nCustomer Support: 1-888-280-4331\nhelp@amazon.com",
                category = CategoryClassifier.CATEGORY_SHOPPING,
                isFavorite = false,
                isProcessed = true,
                perceptualHash = 0x223456789ABCDEFL,
                extractedPrices = "$398.00;;$32.00",
                extractedDates = "Tomorrow",
                extractedEmails = "help@amazon.com",
                extractedPhones = "1-888-280-4331",
                extractedUrls = "https://amazon.com/orders",
                extractedOrderNumbers = "114-8392019-4829104"
            ),
            ScreenshotEntity(
                id = 1003L,
                uri = "android.resource://${context.packageName}/drawable/ic_launcher_background",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_Cafe_Receipt.png",
                displayName = "Screenshot_Bistro_Receipt.png",
                dateTaken = now - (day * 1),
                dateModified = now - (day * 1),
                size = 1_420_000L,
                width = 1080,
                height = 2400,
                ocrText = "Blue Bottle Coffee & Bakery\n123 Market St, San Francisco\nDate: 04/09/2026 09:15 AM\nCashier: Sarah M.\n1x Oat Latte - $6.50\n1x Avocado Toast - $12.00\nSubtotal: $18.50\nTax: $1.75\nTip: $3.50\nTotal Amount: $23.75\nCard Ending in *4821",
                category = CategoryClassifier.CATEGORY_RECEIPTS,
                isFavorite = false,
                isProcessed = true,
                perceptualHash = 0x323456789ABCDEFL,
                extractedPrices = "$6.50;;$12.00;;$18.50;;$1.75;;$3.50;;$23.75",
                extractedDates = "04/09/2026",
                extractedEmails = "info@bluebottlecoffee.com",
                extractedPhones = "(415) 555-0199",
                extractedUrls = "https://bluebottlecoffee.com",
                extractedOrderNumbers = "INV-92841"
            ),
            ScreenshotEntity(
                id = 1004L,
                uri = "android.resource://${context.packageName}/drawable/ic_launcher_background",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_Electricity_Bill.png",
                displayName = "Screenshot_Utility_Statement.png",
                dateTaken = now - (day * 8),
                dateModified = now - (day * 8),
                size = 1_680_000L,
                width = 1080,
                height = 2400,
                ocrText = "Pacific Gas & Electric Utility Statement\nAccount No: 9482-1049-28\nStatement Date: August 28, 2026\nPayment Due Date: September 18, 2026\nTotal Amount Due: $148.65\nCustomer Hotline: 1-800-743-5000\nbilling@pge.com\nPay online at pge.com/paynow",
                category = CategoryClassifier.CATEGORY_BILLS,
                isFavorite = false,
                isProcessed = true,
                perceptualHash = 0x423456789ABCDEFL,
                extractedPrices = "$148.65",
                extractedDates = "August 28, 2026;;September 18, 2026",
                extractedEmails = "billing@pge.com",
                extractedPhones = "1-800-743-5000",
                extractedUrls = "https://pge.com/paynow",
                extractedOrderNumbers = "9482-1049-28"
            ),
            ScreenshotEntity(
                id = 1005L,
                uri = "android.resource://${context.packageName}/drawable/ic_launcher_background",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_Study_Notes.png",
                displayName = "Screenshot_CS_Lecture_Notes.png",
                dateTaken = now - (day * 5),
                dateModified = now - (day * 5),
                size = 2_890_000L,
                width = 1080,
                height = 2400,
                ocrText = "CS 301: Advanced Algorithms - Lecture 14\nDynamic Programming & Graph Traversal\nImportant: Midterm Quiz on 15/09/2026 covering chapters 4-8.\nAssignment 3 due Friday midnight.\nProfessor contact: dr.alvarez@university.edu\nLecture slides link: https://cs.university.edu/301/slides",
                category = CategoryClassifier.CATEGORY_STUDY,
                isFavorite = true,
                isProcessed = true,
                perceptualHash = 0x523456789ABCDEFL,
                extractedPrices = "",
                extractedDates = "15/09/2026",
                extractedEmails = "dr.alvarez@university.edu",
                extractedPhones = "+1 (555) 948-2819",
                extractedUrls = "https://cs.university.edu/301/slides",
                extractedOrderNumbers = "CS 301"
            ),
            ScreenshotEntity(
                id = 1006L,
                uri = "android.resource://${context.packageName}/drawable/ic_launcher_background",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_Duplicate_Cafe_1.png",
                displayName = "Screenshot_Bistro_Receipt_Duplicate.png",
                dateTaken = now - (day * 1) + 12000,
                dateModified = now - (day * 1) + 12000,
                size = 1_420_000L,
                width = 1080,
                height = 2400,
                ocrText = "Blue Bottle Coffee & Bakery\n123 Market St, San Francisco\nDate: 04/09/2026 09:15 AM\nCashier: Sarah M.\n1x Oat Latte - $6.50\n1x Avocado Toast - $12.00\nSubtotal: $18.50\nTax: $1.75\nTip: $3.50\nTotal Amount: $23.75",
                category = CategoryClassifier.CATEGORY_RECEIPTS,
                isFavorite = false,
                isProcessed = true,
                isDuplicate = true,
                duplicateGroupId = "dup_group_1003",
                perceptualHash = 0x323456789ABCDEFL, // identical pHash for near duplicate demonstration
                extractedPrices = "$6.50;;$12.00;;$18.50;;$23.75",
                extractedDates = "04/09/2026",
                extractedEmails = "info@bluebottlecoffee.com",
                extractedPhones = "(415) 555-0199",
                extractedUrls = "https://bluebottlecoffee.com",
                extractedOrderNumbers = "INV-92841"
            ),
            ScreenshotEntity(
                id = 1007L,
                uri = "android.resource://${context.packageName}/drawable/ic_launcher_background",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_OTP_Verification.png",
                displayName = "Screenshot_Verification_Code.png",
                dateTaken = now - (day * 12),
                dateModified = now - (day * 12),
                size = 890_000L,
                width = 1080,
                height = 2400,
                ocrText = "Security Alert: Your verification code is 849-204.\nThis one-time password (OTP) is valid for 5 minutes.\nDo not share this code with anyone.\nIf you did not request this, contact security@bank.com immediately.",
                category = CategoryClassifier.CATEGORY_FINANCE,
                isFavorite = false,
                isProcessed = true,
                isPotentiallyRemovable = true, // Detected as removable temporary OTP!
                perceptualHash = 0x723456789ABCDEFL,
                extractedPrices = "",
                extractedDates = "",
                extractedEmails = "security@bank.com",
                extractedPhones = "+1-800-432-1000",
                extractedUrls = "https://bank.com/security",
                extractedOrderNumbers = "849-204"
            ),
            ScreenshotEntity(
                id = 1008L,
                uri = "android.resource://${context.packageName}/drawable/ic_launcher_background",
                filePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_Large_Design_Mockup.png",
                displayName = "Screenshot_App_UI_HighRes.png",
                dateTaken = now - (day * 14),
                dateModified = now - (day * 14),
                size = 4_520_000L, // > 3MB Large file!
                width = 1440,
                height = 3200,
                ocrText = "FX Signal Lab Design System v2.0\nDashboard, Live Charts, Signal Alerts, Navigation Drawer\nHigh Resolution Export Canvas 1440x3200\nAuthor: Lead Designer\nReview: September 2026",
                category = CategoryClassifier.CATEGORY_DOCUMENTS,
                isFavorite = false,
                isProcessed = true,
                perceptualHash = 0x823456789ABCDEFL,
                extractedPrices = "",
                extractedDates = "September 2026",
                extractedEmails = "design@fxsignallab.com",
                extractedPhones = "",
                extractedUrls = "https://fxsignallab.com/design",
                extractedOrderNumbers = "v2.0"
            )
        )

        dao.insertAll(demoItems)
    }
}
