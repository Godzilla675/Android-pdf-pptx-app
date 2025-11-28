package com.officesuite.app.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DocumentStatistics utility class.
 * Tests medium priority features: document statistics, reading time, readability scores.
 */
class DocumentStatisticsTest {

    // ================== Word Count Tests ==================
    
    @Test
    fun `countWords returns 0 for empty string`() {
        assertEquals(0, DocumentStatistics.countWords(""))
    }
    
    @Test
    fun `countWords returns 0 for blank string`() {
        assertEquals(0, DocumentStatistics.countWords("   "))
    }
    
    @Test
    fun `countWords counts single word`() {
        assertEquals(1, DocumentStatistics.countWords("Hello"))
    }
    
    @Test
    fun `countWords counts multiple words`() {
        assertEquals(5, DocumentStatistics.countWords("The quick brown fox jumps"))
    }
    
    @Test
    fun `countWords handles multiple spaces`() {
        assertEquals(3, DocumentStatistics.countWords("One  two   three"))
    }
    
    @Test
    fun `countWords handles newlines`() {
        assertEquals(4, DocumentStatistics.countWords("Hello\nworld\nfoo\nbar"))
    }
    
    // ================== Sentence Count Tests ==================
    
    @Test
    fun `countSentences returns 0 for empty string`() {
        assertEquals(0, DocumentStatistics.countSentences(""))
    }
    
    @Test
    fun `countSentences counts single sentence with period`() {
        assertEquals(1, DocumentStatistics.countSentences("Hello world."))
    }
    
    @Test
    fun `countSentences counts sentence with exclamation`() {
        assertEquals(1, DocumentStatistics.countSentences("Hello world!"))
    }
    
    @Test
    fun `countSentences counts sentence with question mark`() {
        assertEquals(1, DocumentStatistics.countSentences("How are you?"))
    }
    
    @Test
    fun `countSentences counts multiple sentences`() {
        assertEquals(3, DocumentStatistics.countSentences("First. Second! Third?"))
    }
    
    @Test
    fun `countSentences returns 1 for text without punctuation`() {
        assertEquals(1, DocumentStatistics.countSentences("Hello world"))
    }
    
    // ================== Paragraph Count Tests ==================
    
    @Test
    fun `countParagraphs returns 0 for empty string`() {
        assertEquals(0, DocumentStatistics.countParagraphs(""))
    }
    
    @Test
    fun `countParagraphs counts single paragraph`() {
        assertEquals(1, DocumentStatistics.countParagraphs("Hello world"))
    }
    
    @Test
    fun `countParagraphs counts multiple paragraphs`() {
        assertEquals(2, DocumentStatistics.countParagraphs("First paragraph.\n\nSecond paragraph."))
    }
    
    @Test
    fun `countParagraphs handles multiple blank lines`() {
        assertEquals(2, DocumentStatistics.countParagraphs("First.\n\n\nSecond."))
    }
    
    // ================== Line Count Tests ==================
    
    @Test
    fun `countLines returns 0 for empty string`() {
        assertEquals(0, DocumentStatistics.countLines(""))
    }
    
    @Test
    fun `countLines counts single line`() {
        assertEquals(1, DocumentStatistics.countLines("Hello world"))
    }
    
    @Test
    fun `countLines counts multiple lines`() {
        assertEquals(3, DocumentStatistics.countLines("Line 1\nLine 2\nLine 3"))
    }
    
    // ================== Reading Time Tests ==================
    
    @Test
    fun `calculateReadingTime returns 0 for 0 words`() {
        assertEquals(0.0, DocumentStatistics.calculateReadingTime(0), 0.01)
    }
    
    @Test
    fun `calculateReadingTime calculates correctly for 200 words`() {
        // 200 words at 200 wpm = 1 minute
        assertEquals(1.0, DocumentStatistics.calculateReadingTime(200), 0.01)
    }
    
    @Test
    fun `calculateReadingTime calculates correctly for 100 words`() {
        // 100 words at 200 wpm = 0.5 minutes
        assertEquals(0.5, DocumentStatistics.calculateReadingTime(100), 0.01)
    }
    
    @Test
    fun `formatReadingTime formats less than 1 minute`() {
        assertEquals("< 1 min read", DocumentStatistics.formatReadingTime(0.5))
    }
    
    @Test
    fun `formatReadingTime formats minutes`() {
        assertEquals("5 min read", DocumentStatistics.formatReadingTime(5.0))
    }
    
    @Test
    fun `formatReadingTime formats hours`() {
        assertEquals("1 hr 30 min read", DocumentStatistics.formatReadingTime(90.0))
    }
    
    // ================== Speaking Time Tests ==================
    
    @Test
    fun `calculateSpeakingTime returns 0 for 0 words`() {
        assertEquals(0.0, DocumentStatistics.calculateSpeakingTime(0), 0.01)
    }
    
    @Test
    fun `calculateSpeakingTime calculates correctly for 150 words`() {
        // 150 words at 150 wpm = 1 minute
        assertEquals(1.0, DocumentStatistics.calculateSpeakingTime(150), 0.01)
    }
    
    // ================== Syllable Count Tests ==================
    
    @Test
    fun `countSyllables counts single syllable word`() {
        assertEquals(1, DocumentStatistics.countSyllables("cat"))
    }
    
    @Test
    fun `countSyllables counts two syllable word`() {
        assertEquals(2, DocumentStatistics.countSyllables("hello"))
    }
    
    @Test
    fun `countSyllables counts three syllable word`() {
        assertEquals(3, DocumentStatistics.countSyllables("beautiful"))
    }
    
    @Test
    fun `countSyllables handles silent e`() {
        // "love" should be 1 syllable, not 2
        assertEquals(1, DocumentStatistics.countSyllables("love"))
    }
    
    @Test
    fun `countSyllables returns 1 for short words`() {
        assertEquals(1, DocumentStatistics.countSyllables("a"))
        assertEquals(1, DocumentStatistics.countSyllables("an"))
        assertEquals(1, DocumentStatistics.countSyllables("the"))
    }
    
    // ================== Flesch Reading Ease Tests ==================
    
    @Test
    fun `calculateFleschReadingEase returns 0 for empty input`() {
        assertEquals(0.0, DocumentStatistics.calculateFleschReadingEase(0, 0, 0), 0.01)
    }
    
    @Test
    fun `calculateFleschReadingEase calculates score`() {
        // Simple sentence: 4 words, 1 sentence, 4 syllables
        // Score = 206.835 - 1.015 * (4/1) - 84.6 * (4/4)
        // Score = 206.835 - 4.06 - 84.6 = 118.175 (clamped to 100)
        val score = DocumentStatistics.calculateFleschReadingEase(4, 1, 4)
        assertTrue(score >= 0 && score <= 100)
    }
    
    @Test
    fun `calculateFleschReadingEase clamps to valid range`() {
        val score = DocumentStatistics.calculateFleschReadingEase(100, 1, 100)
        assertTrue(score >= 0 && score <= 100)
    }
    
    // ================== Flesch-Kincaid Grade Tests ==================
    
    @Test
    fun `calculateFleschKincaidGrade returns 0 for empty input`() {
        assertEquals(0.0, DocumentStatistics.calculateFleschKincaidGrade(0, 0, 0), 0.01)
    }
    
    @Test
    fun `calculateFleschKincaidGrade returns valid grade level`() {
        val grade = DocumentStatistics.calculateFleschKincaidGrade(100, 10, 150)
        assertTrue(grade >= 0 && grade <= 20)
    }
    
    // ================== Readability Level Tests ==================
    
    @Test
    fun `getReadabilityLevel returns VERY_EASY for high scores`() {
        assertEquals(ReadabilityLevel.VERY_EASY, DocumentStatistics.getReadabilityLevel(95.0))
    }
    
    @Test
    fun `getReadabilityLevel returns EASY for high-medium scores`() {
        assertEquals(ReadabilityLevel.EASY, DocumentStatistics.getReadabilityLevel(85.0))
    }
    
    @Test
    fun `getReadabilityLevel returns STANDARD for medium scores`() {
        assertEquals(ReadabilityLevel.STANDARD, DocumentStatistics.getReadabilityLevel(65.0))
    }
    
    @Test
    fun `getReadabilityLevel returns DIFFICULT for low scores`() {
        assertEquals(ReadabilityLevel.DIFFICULT, DocumentStatistics.getReadabilityLevel(40.0))
    }
    
    @Test
    fun `getReadabilityLevel returns VERY_DIFFICULT for very low scores`() {
        assertEquals(ReadabilityLevel.VERY_DIFFICULT, DocumentStatistics.getReadabilityLevel(20.0))
    }
    
    // ================== Goal Progress Tests ==================
    
    @Test
    fun `calculateGoalProgress returns 0 progress for 0 goal`() {
        val progress = DocumentStatistics.calculateGoalProgress(100, 0)
        assertEquals(0.0, progress.percentComplete, 0.01)
    }
    
    @Test
    fun `calculateGoalProgress calculates 50 percent`() {
        val progress = DocumentStatistics.calculateGoalProgress(50, 100)
        assertEquals(50.0, progress.percentComplete, 0.01)
        assertEquals(50, progress.remainingWords)
        assertFalse(progress.isComplete)
    }
    
    @Test
    fun `calculateGoalProgress marks complete at 100 percent`() {
        val progress = DocumentStatistics.calculateGoalProgress(100, 100)
        assertEquals(100.0, progress.percentComplete, 0.01)
        assertEquals(0, progress.remainingWords)
        assertTrue(progress.isComplete)
    }
    
    @Test
    fun `calculateGoalProgress handles exceeding goal`() {
        val progress = DocumentStatistics.calculateGoalProgress(150, 100)
        assertEquals(100.0, progress.percentComplete, 0.01)
        assertEquals(0, progress.remainingWords)
        assertTrue(progress.isComplete)
    }
    
    // ================== Full Statistics Tests ==================
    
    @Test
    fun `calculateStatistics returns empty for empty string`() {
        val stats = DocumentStatistics.calculateStatistics("")
        assertEquals(DocumentStats.EMPTY, stats)
    }
    
    @Test
    fun `calculateStatistics calculates comprehensive stats`() {
        val text = "Hello world. This is a test. How are you?"
        val stats = DocumentStatistics.calculateStatistics(text)
        
        assertTrue(stats.words > 0)
        assertTrue(stats.characters > 0)
        assertTrue(stats.sentences == 3)
        assertTrue(stats.readingTimeMinutes > 0)
    }
    
    @Test
    fun `DocumentStats formattedReadingTime works correctly`() {
        val stats = DocumentStatistics.calculateStatistics("Hello world. " + "word ".repeat(200))
        val formatted = stats.formattedReadingTime()
        assertTrue(formatted.contains("min"))
    }
    
    @Test
    fun `GoalProgress formattedProgress shows completion`() {
        val progress = DocumentStatistics.calculateGoalProgress(100, 100)
        assertTrue(progress.formattedProgress().contains("Goal reached"))
    }
    
    @Test
    fun `GoalProgress formattedProgress shows remaining words`() {
        val progress = DocumentStatistics.calculateGoalProgress(50, 100)
        assertTrue(progress.formattedProgress().contains("50 words to go"))
    }
}
