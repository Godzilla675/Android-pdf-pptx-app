package com.officesuite.app.writing

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Writing Tools for Medium Priority Features Phase 2
 * Provides:
 * - Word count tracking and goals
 * - Reading time estimates
 * - Thesaurus integration (offline synonyms)
 * - Dictionary lookup (offline definitions)
 * - Table of Contents generation
 * - Readability analysis
 */
class WritingTools(private val context: Context) {

    companion object {
        private const val AVERAGE_READING_SPEED_WPM = 200
        private const val AVERAGE_SPEAKING_SPEED_WPM = 150
        private const val WORDS_PER_PAGE = 250
    }

    /**
     * Data class for document writing statistics
     */
    data class WritingStats(
        val wordCount: Int,
        val characterCount: Int,
        val characterCountNoSpaces: Int,
        val sentenceCount: Int,
        val paragraphCount: Int,
        val pageCount: Int,
        val readingTimeMinutes: Int,
        val speakingTimeMinutes: Int,
        val averageWordLength: Double,
        val averageSentenceLength: Double,
        val readabilityScore: Double,
        val readabilityGrade: String,
        val vocabularyRichness: Double
    )

    /**
     * Data class for word count goal tracking
     */
    data class WordCountGoal(
        val targetWords: Int,
        val currentWords: Int,
        val progressPercent: Int,
        val remainingWords: Int,
        val isComplete: Boolean,
        val estimatedMinutesToComplete: Int
    )

    /**
     * Data class for table of contents entry
     */
    data class TocEntry(
        val level: Int,
        val title: String,
        val lineNumber: Int
    )

    /**
     * Calculate comprehensive writing statistics
     */
    suspend fun analyzeWriting(text: String): WritingStats = withContext(Dispatchers.Default) {
        val cleanText = text.trim()
        
        val wordCount = countWords(cleanText)
        val characterCount = cleanText.length
        val characterCountNoSpaces = cleanText.replace("\\s".toRegex(), "").length
        val sentenceCount = countSentences(cleanText)
        val paragraphCount = countParagraphs(cleanText)
        val pageCount = (wordCount / WORDS_PER_PAGE.toDouble()).let { 
            if (it < 1) 1 else kotlin.math.ceil(it).toInt() 
        }
        
        val readingTimeMinutes = calculateReadingTime(wordCount)
        val speakingTimeMinutes = calculateSpeakingTime(wordCount)
        
        val averageWordLength = if (wordCount > 0) {
            characterCountNoSpaces.toDouble() / wordCount
        } else 0.0
        
        val averageSentenceLength = if (sentenceCount > 0) {
            wordCount.toDouble() / sentenceCount
        } else 0.0
        
        val readabilityScore = calculateFleschReadingEase(cleanText, wordCount, sentenceCount)
        val readabilityGrade = getReadabilityGrade(readabilityScore)
        val vocabularyRichness = calculateVocabularyRichness(cleanText)
        
        WritingStats(
            wordCount = wordCount,
            characterCount = characterCount,
            characterCountNoSpaces = characterCountNoSpaces,
            sentenceCount = sentenceCount,
            paragraphCount = paragraphCount,
            pageCount = pageCount,
            readingTimeMinutes = readingTimeMinutes,
            speakingTimeMinutes = speakingTimeMinutes,
            averageWordLength = averageWordLength,
            averageSentenceLength = averageSentenceLength,
            readabilityScore = readabilityScore,
            readabilityGrade = readabilityGrade,
            vocabularyRichness = vocabularyRichness
        )
    }

    /**
     * Get word count goal progress
     */
    fun getWordCountGoalProgress(currentWordCount: Int, targetWordCount: Int): WordCountGoal {
        val remainingWords = (targetWordCount - currentWordCount).coerceAtLeast(0)
        val progressPercent = if (targetWordCount > 0) {
            ((currentWordCount.toDouble() / targetWordCount) * 100).toInt().coerceIn(0, 100)
        } else 0
        
        // Estimate time to complete at average typing speed of 40 WPM
        val estimatedMinutes = remainingWords / 40
        
        return WordCountGoal(
            targetWords = targetWordCount,
            currentWords = currentWordCount,
            progressPercent = progressPercent,
            remainingWords = remainingWords,
            isComplete = currentWordCount >= targetWordCount,
            estimatedMinutesToComplete = estimatedMinutes
        )
    }

    /**
     * Get synonyms for a word (offline thesaurus)
     */
    suspend fun getSynonyms(word: String): List<String> = withContext(Dispatchers.Default) {
        val lowercaseWord = word.lowercase().trim()
        thesaurusMap[lowercaseWord] ?: emptyList()
    }

    /**
     * Get definition for a word (offline dictionary)
     */
    suspend fun getDefinition(word: String): String? = withContext(Dispatchers.Default) {
        val lowercaseWord = word.lowercase().trim()
        dictionaryMap[lowercaseWord]
    }

    /**
     * Generate table of contents from markdown/document headings
     */
    suspend fun generateTableOfContents(text: String): List<TocEntry> = withContext(Dispatchers.Default) {
        val entries = mutableListOf<TocEntry>()
        val lines = text.split("\n")
        
        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()
            
            // Markdown heading detection
            when {
                trimmedLine.startsWith("######") -> {
                    entries.add(TocEntry(6, trimmedLine.removePrefix("######").trim(), index + 1))
                }
                trimmedLine.startsWith("#####") -> {
                    entries.add(TocEntry(5, trimmedLine.removePrefix("#####").trim(), index + 1))
                }
                trimmedLine.startsWith("####") -> {
                    entries.add(TocEntry(4, trimmedLine.removePrefix("####").trim(), index + 1))
                }
                trimmedLine.startsWith("###") -> {
                    entries.add(TocEntry(3, trimmedLine.removePrefix("###").trim(), index + 1))
                }
                trimmedLine.startsWith("##") -> {
                    entries.add(TocEntry(2, trimmedLine.removePrefix("##").trim(), index + 1))
                }
                trimmedLine.startsWith("#") -> {
                    entries.add(TocEntry(1, trimmedLine.removePrefix("#").trim(), index + 1))
                }
            }
        }
        
        entries
    }

    /**
     * Format table of contents as markdown
     */
    fun formatTableOfContentsAsMarkdown(entries: List<TocEntry>): String {
        val builder = StringBuilder()
        builder.appendLine("## Table of Contents\n")
        
        entries.forEach { entry ->
            val indent = "  ".repeat(entry.level - 1)
            builder.appendLine("$indent- [${entry.title}](#${entry.title.lowercase().replace(" ", "-")})")
        }
        
        return builder.toString()
    }

    /**
     * Calculate reading time in minutes
     */
    fun calculateReadingTime(wordCount: Int): Int {
        val minutes = wordCount / AVERAGE_READING_SPEED_WPM.toDouble()
        return if (minutes < 1) 1 else minutes.toInt()
    }

    /**
     * Calculate speaking time in minutes
     */
    fun calculateSpeakingTime(wordCount: Int): Int {
        val minutes = wordCount / AVERAGE_SPEAKING_SPEED_WPM.toDouble()
        return if (minutes < 1) 1 else minutes.toInt()
    }

    /**
     * Get reading time as formatted string
     */
    fun getReadingTimeFormatted(wordCount: Int): String {
        val minutes = calculateReadingTime(wordCount)
        return when {
            minutes < 1 -> "< 1 min read"
            minutes == 1 -> "1 min read"
            else -> "$minutes min read"
        }
    }

    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    }

    private fun countSentences(text: String): Int {
        if (text.isBlank()) return 0
        val pattern = "[.!?]+".toRegex()
        val count = pattern.findAll(text).count()
        return if (count == 0 && text.isNotBlank()) 1 else count
    }

    private fun countParagraphs(text: String): Int {
        if (text.isBlank()) return 0
        return text.split("\n\\s*\n".toRegex()).filter { it.isNotBlank() }.size
    }

    private fun countSyllables(word: String): Int {
        val cleanWord = word.lowercase().replace("[^a-z]".toRegex(), "")
        if (cleanWord.isEmpty()) return 0
        
        var count = 0
        var prevVowel = false
        
        for (char in cleanWord) {
            val isVowel = char in "aeiouy"
            if (isVowel && !prevVowel) count++
            prevVowel = isVowel
        }
        
        // Handle silent 'e'
        if (cleanWord.endsWith("e") && count > 1) count--
        
        return if (count == 0) 1 else count
    }

    private fun calculateFleschReadingEase(text: String, wordCount: Int, sentenceCount: Int): Double {
        if (wordCount == 0 || sentenceCount == 0) return 0.0
        
        val words = text.split("\\s+".toRegex())
        val totalSyllables = words.sumOf { countSyllables(it) }
        
        val avgSentenceLength = wordCount.toDouble() / sentenceCount
        val avgSyllablesPerWord = totalSyllables.toDouble() / wordCount
        
        // Flesch Reading Ease formula
        val score = 206.835 - (1.015 * avgSentenceLength) - (84.6 * avgSyllablesPerWord)
        return score.coerceIn(0.0, 100.0)
    }

    private fun getReadabilityGrade(score: Double): String {
        return when {
            score >= 90 -> "Very Easy (5th Grade)"
            score >= 80 -> "Easy (6th Grade)"
            score >= 70 -> "Fairly Easy (7th Grade)"
            score >= 60 -> "Standard (8th-9th Grade)"
            score >= 50 -> "Fairly Difficult (10th-12th Grade)"
            score >= 30 -> "Difficult (College)"
            else -> "Very Difficult (Graduate)"
        }
    }

    private fun calculateVocabularyRichness(text: String): Double {
        val words = text.lowercase()
            .split("[\\s\\p{Punct}]+".toRegex())
            .filter { it.isNotBlank() && it.length > 2 }
        
        if (words.isEmpty()) return 0.0
        
        val uniqueWords = words.toSet().size
        // Type-Token Ratio
        return (uniqueWords.toDouble() / words.size) * 100
    }

    /**
     * Check if today is a new day for word count tracking
     */
    fun isNewDay(lastDateString: String): Boolean {
        if (lastDateString.isBlank()) return true
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        return lastDateString != today
    }

    /**
     * Get today's date string for tracking
     */
    fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Offline thesaurus - common word synonyms
    private val thesaurusMap = mapOf(
        "good" to listOf("excellent", "great", "fine", "wonderful", "superb", "outstanding", "remarkable", "splendid"),
        "bad" to listOf("poor", "terrible", "awful", "dreadful", "horrible", "unpleasant", "inferior"),
        "big" to listOf("large", "huge", "enormous", "massive", "giant", "immense", "vast", "substantial"),
        "small" to listOf("tiny", "little", "miniature", "compact", "petite", "minute", "diminutive"),
        "happy" to listOf("joyful", "cheerful", "delighted", "pleased", "content", "elated", "thrilled"),
        "sad" to listOf("unhappy", "sorrowful", "dejected", "melancholy", "gloomy", "miserable", "depressed"),
        "fast" to listOf("quick", "rapid", "swift", "speedy", "hasty", "brisk", "prompt"),
        "slow" to listOf("gradual", "leisurely", "unhurried", "sluggish", "delayed", "steady"),
        "smart" to listOf("intelligent", "clever", "brilliant", "wise", "bright", "sharp", "astute"),
        "beautiful" to listOf("gorgeous", "stunning", "lovely", "attractive", "pretty", "elegant", "exquisite"),
        "ugly" to listOf("unattractive", "unsightly", "hideous", "grotesque", "plain"),
        "old" to listOf("ancient", "elderly", "aged", "antique", "vintage", "mature"),
        "new" to listOf("fresh", "modern", "recent", "contemporary", "novel", "latest"),
        "important" to listOf("significant", "crucial", "essential", "vital", "critical", "key", "major"),
        "easy" to listOf("simple", "straightforward", "effortless", "uncomplicated", "elementary"),
        "hard" to listOf("difficult", "challenging", "tough", "demanding", "strenuous", "arduous"),
        "strong" to listOf("powerful", "sturdy", "robust", "mighty", "potent", "forceful"),
        "weak" to listOf("feeble", "frail", "fragile", "delicate", "powerless"),
        "love" to listOf("adore", "cherish", "treasure", "appreciate", "admire", "care for"),
        "hate" to listOf("detest", "despise", "loathe", "abhor", "dislike"),
        "help" to listOf("assist", "aid", "support", "guide", "serve", "facilitate"),
        "think" to listOf("believe", "consider", "ponder", "contemplate", "reflect", "reason"),
        "say" to listOf("state", "declare", "mention", "express", "articulate", "communicate"),
        "walk" to listOf("stroll", "stride", "march", "wander", "roam", "amble"),
        "run" to listOf("sprint", "dash", "race", "rush", "hurry", "jog"),
        "look" to listOf("observe", "view", "watch", "examine", "inspect", "gaze"),
        "begin" to listOf("start", "commence", "initiate", "launch", "originate"),
        "end" to listOf("finish", "conclude", "terminate", "complete", "cease"),
        "make" to listOf("create", "produce", "build", "construct", "form", "generate"),
        "use" to listOf("utilize", "employ", "apply", "operate", "exercise")
    )

    // Offline dictionary - common word definitions
    private val dictionaryMap = mapOf(
        "algorithm" to "A step-by-step procedure for solving a problem or accomplishing a task.",
        "analyze" to "To examine methodically and in detail for purposes of explanation and interpretation.",
        "collaborate" to "To work jointly with others on a project or activity.",
        "comprehensive" to "Including or dealing with all or nearly all elements or aspects of something.",
        "demonstrate" to "To clearly show the existence or truth of something by giving proof or evidence.",
        "efficient" to "Achieving maximum productivity with minimum wasted effort or expense.",
        "facilitate" to "To make an action or process easier.",
        "generate" to "To produce or create something.",
        "hypothesis" to "A proposed explanation made on the basis of limited evidence.",
        "implement" to "To put a decision, plan, or agreement into effect.",
        "innovative" to "Introducing new ideas, original and creative in thinking.",
        "justify" to "To show or prove to be right or reasonable.",
        "leverage" to "To use something to maximum advantage.",
        "methodology" to "A system of methods used in a particular area of study or activity.",
        "negotiate" to "To try to reach an agreement through discussion.",
        "optimize" to "To make the best or most effective use of a situation or resource.",
        "paradigm" to "A typical example or pattern of something; a model.",
        "quantify" to "To express or measure the quantity of something.",
        "resilient" to "Able to withstand or recover quickly from difficult conditions.",
        "synthesize" to "To combine different ideas or things into a coherent whole.",
        "ubiquitous" to "Present, appearing, or found everywhere.",
        "validate" to "To check or prove the validity or accuracy of something.",
        "workflow" to "The sequence of industrial, administrative, or other processes.",
        "benchmark" to "A standard or point of reference against which things may be compared.",
        "concise" to "Giving a lot of information clearly and in few words; brief but comprehensive."
    )
}
