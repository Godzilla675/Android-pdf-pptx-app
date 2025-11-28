package com.officesuite.app.utils

/**
 * Document statistics and analysis utility.
 * Implements Medium Priority Features from Phase 2:
 * - Document Statistics (Section 8)
 * - Reading Time Estimate (Section 6)
 * - Readability Scores (Section 8)
 * - Word Count Goals (Section 6)
 */
object DocumentStatistics {
    
    // Average reading speed in words per minute
    private const val AVERAGE_READING_SPEED_WPM = 200
    private const val FAST_READING_SPEED_WPM = 300
    private const val SLOW_READING_SPEED_WPM = 150
    
    /**
     * Calculate comprehensive document statistics
     */
    fun calculateStatistics(text: String): DocumentStats {
        val cleanText = text.trim()
        
        if (cleanText.isEmpty()) {
            return DocumentStats.EMPTY
        }
        
        val characters = cleanText.length
        val charactersNoSpaces = cleanText.replace("\\s".toRegex(), "").length
        val words = countWords(cleanText)
        val sentences = countSentences(cleanText)
        val paragraphs = countParagraphs(cleanText)
        val lines = countLines(cleanText)
        
        val readingTimeMinutes = calculateReadingTime(words, AVERAGE_READING_SPEED_WPM)
        val speakingTimeMinutes = calculateSpeakingTime(words)
        
        val averageWordsPerSentence = if (sentences > 0) words.toDouble() / sentences else 0.0
        val averageSyllablesPerWord = calculateAverageSyllablesPerWord(cleanText)
        
        val fleschReadingEase = calculateFleschReadingEase(
            totalWords = words,
            totalSentences = sentences,
            totalSyllables = (words * averageSyllablesPerWord).toInt()
        )
        
        val fleschKincaidGrade = calculateFleschKincaidGrade(
            totalWords = words,
            totalSentences = sentences,
            totalSyllables = (words * averageSyllablesPerWord).toInt()
        )
        
        return DocumentStats(
            characters = characters,
            charactersNoSpaces = charactersNoSpaces,
            words = words,
            sentences = sentences,
            paragraphs = paragraphs,
            lines = lines,
            readingTimeMinutes = readingTimeMinutes,
            speakingTimeMinutes = speakingTimeMinutes,
            averageWordsPerSentence = averageWordsPerSentence,
            averageSyllablesPerWord = averageSyllablesPerWord,
            fleschReadingEase = fleschReadingEase,
            fleschKincaidGrade = fleschKincaidGrade,
            readabilityLevel = getReadabilityLevel(fleschReadingEase)
        )
    }
    
    /**
     * Count words in text
     */
    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    }
    
    /**
     * Count sentences in text (ends with . ! ?)
     */
    fun countSentences(text: String): Int {
        if (text.isBlank()) return 0
        // Match sentence-ending punctuation followed by space or end of string
        val pattern = "[.!?]+(?=\\s|$)".toRegex()
        val count = pattern.findAll(text.trim()).count()
        return if (count == 0 && text.isNotBlank()) 1 else count
    }
    
    /**
     * Count paragraphs (text blocks separated by blank lines)
     */
    fun countParagraphs(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\n\\s*\\n".toRegex()).filter { it.isNotBlank() }.size
    }
    
    /**
     * Count lines in text
     */
    fun countLines(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\n".toRegex()).size
    }
    
    /**
     * Calculate reading time in minutes
     */
    fun calculateReadingTime(wordCount: Int, wpm: Int = AVERAGE_READING_SPEED_WPM): Double {
        if (wordCount <= 0) return 0.0
        return wordCount.toDouble() / wpm
    }
    
    /**
     * Calculate speaking/presentation time (average speaking speed is ~150 wpm)
     */
    fun calculateSpeakingTime(wordCount: Int): Double {
        if (wordCount <= 0) return 0.0
        return wordCount.toDouble() / 150.0
    }
    
    /**
     * Format reading time as a human-readable string
     */
    fun formatReadingTime(minutes: Double): String {
        return when {
            minutes < 1 -> "< 1 min read"
            minutes < 60 -> "${minutes.toInt()} min read"
            else -> {
                val hours = (minutes / 60).toInt()
                val mins = (minutes % 60).toInt()
                if (mins == 0) "$hours hr read" else "$hours hr $mins min read"
            }
        }
    }
    
    /**
     * Calculate average syllables per word
     */
    fun calculateAverageSyllablesPerWord(text: String): Double {
        val words = text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.isEmpty()) return 0.0
        
        val totalSyllables = words.sumOf { countSyllables(it) }
        return totalSyllables.toDouble() / words.size
    }
    
    /**
     * Count syllables in a word (approximate)
     */
    fun countSyllables(word: String): Int {
        if (word.isBlank()) return 0
        
        val cleanWord = word.lowercase().replace("[^a-z]".toRegex(), "")
        if (cleanWord.isEmpty()) return 0
        if (cleanWord.length <= 3) return 1
        
        var count = 0
        var prevIsVowel = false
        val vowels = "aeiouy"
        
        for (char in cleanWord) {
            val isVowel = char in vowels
            if (isVowel && !prevIsVowel) {
                count++
            }
            prevIsVowel = isVowel
        }
        
        // Handle silent 'e' at end
        if (cleanWord.endsWith("e") && count > 1) {
            count--
        }
        
        // Every word has at least one syllable
        return maxOf(1, count)
    }
    
    /**
     * Calculate Flesch Reading Ease score
     * Higher scores = easier to read (0-100 scale)
     */
    fun calculateFleschReadingEase(
        totalWords: Int,
        totalSentences: Int,
        totalSyllables: Int
    ): Double {
        if (totalWords == 0 || totalSentences == 0) return 0.0
        
        val avgSentenceLength = totalWords.toDouble() / totalSentences
        val avgSyllablesPerWord = totalSyllables.toDouble() / totalWords
        
        val score = 206.835 - (1.015 * avgSentenceLength) - (84.6 * avgSyllablesPerWord)
        return score.coerceIn(0.0, 100.0)
    }
    
    /**
     * Calculate Flesch-Kincaid Grade Level
     * Returns approximate US grade level needed to understand the text
     */
    fun calculateFleschKincaidGrade(
        totalWords: Int,
        totalSentences: Int,
        totalSyllables: Int
    ): Double {
        if (totalWords == 0 || totalSentences == 0) return 0.0
        
        val avgSentenceLength = totalWords.toDouble() / totalSentences
        val avgSyllablesPerWord = totalSyllables.toDouble() / totalWords
        
        val grade = (0.39 * avgSentenceLength) + (11.8 * avgSyllablesPerWord) - 15.59
        return grade.coerceIn(0.0, 20.0)
    }
    
    /**
     * Get human-readable readability level from Flesch score
     */
    fun getReadabilityLevel(fleschScore: Double): ReadabilityLevel {
        return when {
            fleschScore >= 90 -> ReadabilityLevel.VERY_EASY
            fleschScore >= 80 -> ReadabilityLevel.EASY
            fleschScore >= 70 -> ReadabilityLevel.FAIRLY_EASY
            fleschScore >= 60 -> ReadabilityLevel.STANDARD
            fleschScore >= 50 -> ReadabilityLevel.FAIRLY_DIFFICULT
            fleschScore >= 30 -> ReadabilityLevel.DIFFICULT
            else -> ReadabilityLevel.VERY_DIFFICULT
        }
    }
    
    /**
     * Calculate progress toward word count goal
     */
    fun calculateGoalProgress(currentWords: Int, goalWords: Int): GoalProgress {
        if (goalWords <= 0) return GoalProgress(0, 0, 0.0, false)
        
        val remaining = maxOf(0, goalWords - currentWords)
        val percentage = (currentWords.toDouble() / goalWords * 100).coerceIn(0.0, 100.0)
        val isComplete = currentWords >= goalWords
        
        return GoalProgress(
            currentWords = currentWords,
            remainingWords = remaining,
            percentComplete = percentage,
            isComplete = isComplete
        )
    }
}

/**
 * Comprehensive document statistics
 */
data class DocumentStats(
    val characters: Int,
    val charactersNoSpaces: Int,
    val words: Int,
    val sentences: Int,
    val paragraphs: Int,
    val lines: Int,
    val readingTimeMinutes: Double,
    val speakingTimeMinutes: Double,
    val averageWordsPerSentence: Double,
    val averageSyllablesPerWord: Double,
    val fleschReadingEase: Double,
    val fleschKincaidGrade: Double,
    val readabilityLevel: ReadabilityLevel
) {
    companion object {
        val EMPTY = DocumentStats(
            characters = 0,
            charactersNoSpaces = 0,
            words = 0,
            sentences = 0,
            paragraphs = 0,
            lines = 0,
            readingTimeMinutes = 0.0,
            speakingTimeMinutes = 0.0,
            averageWordsPerSentence = 0.0,
            averageSyllablesPerWord = 0.0,
            fleschReadingEase = 0.0,
            fleschKincaidGrade = 0.0,
            readabilityLevel = ReadabilityLevel.STANDARD
        )
    }
    
    /**
     * Format reading time as human-readable string
     */
    fun formattedReadingTime(): String = DocumentStatistics.formatReadingTime(readingTimeMinutes)
    
    /**
     * Format speaking time as human-readable string
     */
    fun formattedSpeakingTime(): String {
        return when {
            speakingTimeMinutes < 1 -> "< 1 min"
            speakingTimeMinutes < 60 -> "${speakingTimeMinutes.toInt()} min"
            else -> {
                val hours = (speakingTimeMinutes / 60).toInt()
                val mins = (speakingTimeMinutes % 60).toInt()
                if (mins == 0) "$hours hr" else "$hours hr $mins min"
            }
        }
    }
    
    /**
     * Format grade level as human-readable string
     */
    fun formattedGradeLevel(): String {
        val grade = fleschKincaidGrade.toInt()
        return when {
            grade <= 0 -> "Kindergarten"
            grade <= 12 -> "Grade $grade"
            else -> "College level"
        }
    }
}

/**
 * Readability level descriptions
 */
enum class ReadabilityLevel(val description: String, val audience: String) {
    VERY_EASY("Very Easy", "5th graders"),
    EASY("Easy", "6th graders"),
    FAIRLY_EASY("Fairly Easy", "7th graders"),
    STANDARD("Standard", "8th-9th graders"),
    FAIRLY_DIFFICULT("Fairly Difficult", "High school students"),
    DIFFICULT("Difficult", "College students"),
    VERY_DIFFICULT("Very Difficult", "College graduates")
}

/**
 * Word count goal progress tracking
 */
data class GoalProgress(
    val currentWords: Int,
    val remainingWords: Int,
    val percentComplete: Double,
    val isComplete: Boolean
) {
    fun formattedProgress(): String {
        return when {
            isComplete -> "Goal reached! ✓"
            remainingWords > 0 -> "$remainingWords words to go"
            else -> "Start writing..."
        }
    }
}
