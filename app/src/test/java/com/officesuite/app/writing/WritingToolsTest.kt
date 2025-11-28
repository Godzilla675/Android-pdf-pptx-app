package com.officesuite.app.writing

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import android.content.Context
import kotlinx.coroutines.runBlocking

/**
 * Unit tests for WritingTools
 */
@RunWith(MockitoJUnitRunner::class)
class WritingToolsTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var writingTools: WritingTools

    @Before
    fun setup() {
        writingTools = WritingTools(mockContext)
    }

    @Test
    fun `calculateReadingTime returns correct time for short text`() {
        // Less than 200 words should return 1 minute
        val result = writingTools.calculateReadingTime(100)
        assertEquals(1, result)
    }

    @Test
    fun `calculateReadingTime returns correct time for longer text`() {
        // 400 words at 200 WPM should be 2 minutes
        val result = writingTools.calculateReadingTime(400)
        assertEquals(2, result)
    }

    @Test
    fun `calculateSpeakingTime returns correct time`() {
        // 300 words at 150 WPM should be 2 minutes
        val result = writingTools.calculateSpeakingTime(300)
        assertEquals(2, result)
    }

    @Test
    fun `getReadingTimeFormatted returns formatted string`() {
        val result = writingTools.getReadingTimeFormatted(400)
        assertEquals("2 min read", result)
    }

    @Test
    fun `getReadingTimeFormatted returns singular for 1 minute`() {
        val result = writingTools.getReadingTimeFormatted(200)
        assertEquals("1 min read", result)
    }

    @Test
    fun `getWordCountGoalProgress calculates progress correctly`() {
        val result = writingTools.getWordCountGoalProgress(50, 100)
        
        assertEquals(100, result.targetWords)
        assertEquals(50, result.currentWords)
        assertEquals(50, result.progressPercent)
        assertEquals(50, result.remainingWords)
        assertFalse(result.isComplete)
    }

    @Test
    fun `getWordCountGoalProgress marks complete when target reached`() {
        val result = writingTools.getWordCountGoalProgress(100, 100)
        
        assertTrue(result.isComplete)
        assertEquals(0, result.remainingWords)
        assertEquals(100, result.progressPercent)
    }

    @Test
    fun `getWordCountGoalProgress handles over-achievement`() {
        val result = writingTools.getWordCountGoalProgress(150, 100)
        
        assertTrue(result.isComplete)
        assertEquals(0, result.remainingWords)
        assertEquals(100, result.progressPercent) // Capped at 100
    }

    @Test
    fun `getWordCountGoalProgress handles zero target`() {
        val result = writingTools.getWordCountGoalProgress(50, 0)
        
        assertEquals(0, result.progressPercent)
    }

    @Test
    fun `getSynonyms returns synonyms for known words`() = runBlocking {
        val synonyms = writingTools.getSynonyms("good")
        
        assertTrue(synonyms.isNotEmpty())
        assertTrue(synonyms.contains("excellent"))
    }

    @Test
    fun `getSynonyms returns empty list for unknown words`() = runBlocking {
        val synonyms = writingTools.getSynonyms("xyzabc123")
        
        assertTrue(synonyms.isEmpty())
    }

    @Test
    fun `getSynonyms is case insensitive`() = runBlocking {
        val synonymsLower = writingTools.getSynonyms("good")
        val synonymsUpper = writingTools.getSynonyms("GOOD")
        val synonymsMixed = writingTools.getSynonyms("Good")
        
        assertEquals(synonymsLower, synonymsUpper)
        assertEquals(synonymsLower, synonymsMixed)
    }

    @Test
    fun `getDefinition returns definition for known words`() = runBlocking {
        val definition = writingTools.getDefinition("algorithm")
        
        assertNotNull(definition)
        assertTrue(definition!!.contains("step-by-step"))
    }

    @Test
    fun `getDefinition returns null for unknown words`() = runBlocking {
        val definition = writingTools.getDefinition("xyzabc123")
        
        assertNull(definition)
    }

    @Test
    fun `generateTableOfContents extracts headings correctly`() = runBlocking {
        val markdown = """
            # Main Title
            Some content here.
            ## Section One
            More content.
            ### Subsection
            Even more content.
            ## Section Two
            Final content.
        """.trimIndent()

        val toc = writingTools.generateTableOfContents(markdown)

        assertEquals(4, toc.size)
        assertEquals("Main Title", toc[0].title)
        assertEquals(1, toc[0].level)
        assertEquals("Section One", toc[1].title)
        assertEquals(2, toc[1].level)
        assertEquals("Subsection", toc[2].title)
        assertEquals(3, toc[2].level)
        assertEquals("Section Two", toc[3].title)
        assertEquals(2, toc[3].level)
    }

    @Test
    fun `generateTableOfContents returns empty for no headings`() = runBlocking {
        val text = "Just some plain text without any headings."
        val toc = writingTools.generateTableOfContents(text)
        
        assertTrue(toc.isEmpty())
    }

    @Test
    fun `formatTableOfContentsAsMarkdown creates valid markdown`() {
        val entries = listOf(
            WritingTools.TocEntry(1, "Introduction", 1),
            WritingTools.TocEntry(2, "Methods", 5),
            WritingTools.TocEntry(2, "Results", 10)
        )

        val markdown = writingTools.formatTableOfContentsAsMarkdown(entries)

        assertTrue(markdown.contains("## Table of Contents"))
        assertTrue(markdown.contains("- [Introduction]"))
        assertTrue(markdown.contains("  - [Methods]"))
        assertTrue(markdown.contains("  - [Results]"))
    }

    @Test
    fun `isNewDay returns true for empty date`() {
        val result = writingTools.isNewDay("")
        assertTrue(result)
    }

    @Test
    fun `getTodayDateString returns valid date format`() {
        val dateString = writingTools.getTodayDateString()
        
        // Should match yyyy-MM-dd format
        assertTrue(dateString.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `analyzeWriting calculates statistics correctly`() = runBlocking {
        val text = "This is a test sentence. This is another sentence. And one more for good measure."
        
        val stats = writingTools.analyzeWriting(text)
        
        assertTrue(stats.wordCount > 0)
        assertTrue(stats.characterCount > 0)
        assertTrue(stats.sentenceCount == 3)
        assertTrue(stats.readingTimeMinutes >= 1)
        assertTrue(stats.readabilityScore in 0.0..100.0)
    }

    @Test
    fun `analyzeWriting handles empty text`() = runBlocking {
        val stats = writingTools.analyzeWriting("")
        
        assertEquals(0, stats.wordCount)
        assertEquals(0, stats.characterCount)
    }
}
