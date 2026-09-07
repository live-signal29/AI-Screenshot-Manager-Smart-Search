package com.example

import com.example.data.local.ScreenshotEntity
import com.example.domain.duplicate.DuplicateDetector
import com.example.domain.ocr.CategoryClassifier
import com.example.domain.ocr.EntityExtractor
import com.example.domain.search.NaturalLanguageSearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainUnitTests {

    @Test
    fun testEntityExtractor_extractsPricesDatesEmailsPhonesUrls() {
        val sampleText = """
            Receipt total is $49.99 on 12/08/2026.
            Contact support at help@amazon.com or call +1-800-555-0199.
            Visit https://amazon.com/orders. Order # 114-8392019-4829104.
        """.trimIndent()

        val entities = EntityExtractor.extract(sampleText)

        assertTrue("Should extract price", entities.prices.any { it.contains("49.99") })
        assertTrue("Should extract date", entities.dates.any { it.contains("12/08/2026") })
        assertTrue("Should extract email", entities.emails.contains("help@amazon.com"))
        assertTrue("Should extract phone", entities.phones.any { it.contains("800") })
        assertTrue("Should extract URL", entities.urls.any { it.contains("amazon.com") })
        assertTrue("Should extract Order", entities.orderNumbers.any { it.contains("114-8392019") })
    }

    @Test
    fun testCategoryClassifier_correctlyIdentifiesCategories() {
        val flightText = "American Airlines Boarding Pass Flight AA 284 Gate B22 Seat 14B Departure JFK"
        val flightCat = CategoryClassifier.classify(flightText)
        assertEquals(CategoryClassifier.CATEGORY_TICKETS, flightCat)

        val receiptText = "Market Cafe Pos Sale Cashier Sarah Subtotal $18.50 Tax $1.75 Tip $3.50 Total $23.75"
        val receiptCat = CategoryClassifier.classify(receiptText)
        assertEquals(CategoryClassifier.CATEGORY_RECEIPTS, receiptCat)

        val studyText = "CS 301 Lecture 14 Dynamic Programming Midterm Quiz Homework Assignment Syllabus"
        val studyCat = CategoryClassifier.classify(studyText)
        assertEquals(CategoryClassifier.CATEGORY_STUDY, studyCat)
    }

    @Test
    fun testCategoryClassifier_identifiesRemovableOtp() {
        val otpText = "Your verification code is 849-204. This one-time password (OTP) is valid for 5 minutes."
        assertTrue("Should detect temporary OTP as potentially removable", CategoryClassifier.isPotentiallyRemovable(otpText))
    }

    @Test
    fun testNaturalLanguageSearchEngine_parsesHinglishAndQueries() {
        val intent1 = NaturalLanguageSearchEngine.parseQuery("Meri flight ticket wali screenshots")
        assertEquals(CategoryClassifier.CATEGORY_TICKETS, intent1.targetCategory)

        val intent2 = NaturalLanguageSearchEngine.parseQuery("Show screenshots containing $50")
        assertTrue(intent2.requirePrice)
        assertEquals(50.0, intent2.targetPriceAmount ?: 0.0, 0.01)

        val intent3 = NaturalLanguageSearchEngine.parseQuery("Find screenshots containing a phone number")
        assertTrue(intent3.requirePhone)

        val intent4 = NaturalLanguageSearchEngine.parseQuery("Find receipts")
        assertEquals(CategoryClassifier.CATEGORY_RECEIPTS, intent4.targetCategory)
    }

    @Test
    fun testDuplicateDetector_clustersMatchingScreenshots() {
        val item1 = ScreenshotEntity(
            id = 1L,
            uri = "content://1",
            filePath = "/path/1",
            displayName = "shot1.png",
            dateTaken = 1000L,
            dateModified = 1000L,
            size = 5000L,
            width = 1080,
            height = 1920,
            ocrText = "Duplicate text",
            fileHash = "hash123",
            perceptualHash = 0x1111222233334444L
        )
        val item2 = ScreenshotEntity(
            id = 2L,
            uri = "content://2",
            filePath = "/path/2",
            displayName = "shot2.png",
            dateTaken = 2000L,
            dateModified = 2000L,
            size = 5000L,
            width = 1080,
            height = 1920,
            ocrText = "Duplicate text",
            fileHash = "hash123",
            perceptualHash = 0x1111222233334444L
        )

        val clusters = DuplicateDetector.findDuplicateClusters(listOf(item1, item2))
        assertEquals(1, clusters.size)
        assertEquals(1, clusters[0].duplicateScreenshots.size)
        assertEquals(2L, clusters[0].duplicateScreenshots[0].id)
    }
}
