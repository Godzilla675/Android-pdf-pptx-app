package com.officesuite.app.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Focus Mode Manager for distraction-free writing.
 * Implements Medium Priority Feature from Phase 2 Section 6:
 * - Focus Mode: Distraction-free writing environment
 * - Word Count Goals: Set and track word count targets
 */
class FocusModeManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // ================== Focus Mode ==================
    
    var isFocusModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_FOCUS_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_FOCUS_MODE, value).apply()
    
    var focusModeStyle: FocusModeStyle
        get() {
            val ordinal = prefs.getInt(KEY_FOCUS_STYLE, FocusModeStyle.MINIMAL.ordinal)
            return FocusModeStyle.values().getOrElse(ordinal) { FocusModeStyle.MINIMAL }
        }
        set(value) = prefs.edit().putInt(KEY_FOCUS_STYLE, value.ordinal).apply()
    
    var typewriterModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_TYPEWRITER_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_TYPEWRITER_MODE, value).apply()
    
    var highlightCurrentLine: Boolean
        get() = prefs.getBoolean(KEY_HIGHLIGHT_CURRENT_LINE, true)
        set(value) = prefs.edit().putBoolean(KEY_HIGHLIGHT_CURRENT_LINE, value).apply()
    
    var dimInactiveText: Boolean
        get() = prefs.getBoolean(KEY_DIM_INACTIVE_TEXT, false)
        set(value) = prefs.edit().putBoolean(KEY_DIM_INACTIVE_TEXT, value).apply()
    
    // ================== Word Count Goals ==================
    
    var dailyWordGoal: Int
        get() = prefs.getInt(KEY_DAILY_WORD_GOAL, 0)
        set(value) = prefs.edit().putInt(KEY_DAILY_WORD_GOAL, value.coerceAtLeast(0)).apply()
    
    var sessionWordGoal: Int
        get() = prefs.getInt(KEY_SESSION_WORD_GOAL, 0)
        set(value) = prefs.edit().putInt(KEY_SESSION_WORD_GOAL, value.coerceAtLeast(0)).apply()
    
    var documentWordGoal: Int
        get() = prefs.getInt(KEY_DOCUMENT_WORD_GOAL, 0)
        set(value) = prefs.edit().putInt(KEY_DOCUMENT_WORD_GOAL, value.coerceAtLeast(0)).apply()
    
    // Track daily progress
    var todayWordCount: Int
        get() {
            val savedDate = prefs.getString(KEY_TODAY_DATE, "")
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(java.util.Date())
            
            // Reset if it's a new day
            if (savedDate != today) {
                prefs.edit()
                    .putString(KEY_TODAY_DATE, today)
                    .putInt(KEY_TODAY_WORD_COUNT, 0)
                    .apply()
                return 0
            }
            
            return prefs.getInt(KEY_TODAY_WORD_COUNT, 0)
        }
        set(value) {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(java.util.Date())
            prefs.edit()
                .putString(KEY_TODAY_DATE, today)
                .putInt(KEY_TODAY_WORD_COUNT, value.coerceAtLeast(0))
                .apply()
        }
    
    // Track session progress (resets each time the app opens)
    private var sessionStartWordCount: Int = 0
    private var sessionCurrentWordCount: Int = 0
    
    /**
     * Start a new writing session
     */
    fun startSession(currentWordCount: Int = 0) {
        sessionStartWordCount = currentWordCount
        sessionCurrentWordCount = currentWordCount
    }
    
    /**
     * Update the current word count in the session
     */
    fun updateSessionWordCount(currentWordCount: Int) {
        val newWordsWritten = currentWordCount - sessionCurrentWordCount
        if (newWordsWritten > 0) {
            todayWordCount += newWordsWritten
        }
        sessionCurrentWordCount = currentWordCount
    }
    
    /**
     * Get words written in the current session
     */
    fun getSessionWordsWritten(): Int {
        return (sessionCurrentWordCount - sessionStartWordCount).coerceAtLeast(0)
    }
    
    /**
     * Calculate progress toward daily goal
     */
    fun getDailyGoalProgress(): GoalProgress {
        return DocumentStatistics.calculateGoalProgress(todayWordCount, dailyWordGoal)
    }
    
    /**
     * Calculate progress toward session goal
     */
    fun getSessionGoalProgress(): GoalProgress {
        return DocumentStatistics.calculateGoalProgress(getSessionWordsWritten(), sessionWordGoal)
    }
    
    /**
     * Calculate progress toward document goal
     */
    fun getDocumentGoalProgress(documentWordCount: Int): GoalProgress {
        return DocumentStatistics.calculateGoalProgress(documentWordCount, documentWordGoal)
    }
    
    /**
     * Check if any goal has been set
     */
    fun hasActiveGoal(): Boolean {
        return dailyWordGoal > 0 || sessionWordGoal > 0 || documentWordGoal > 0
    }
    
    /**
     * Get writing streak (consecutive days with goal met)
     */
    fun getWritingStreak(): Int {
        return prefs.getInt(KEY_WRITING_STREAK, 0)
    }
    
    /**
     * Update writing streak based on today's progress
     */
    fun updateWritingStreak() {
        if (dailyWordGoal > 0 && todayWordCount >= dailyWordGoal) {
            val lastStreakDate = prefs.getString(KEY_LAST_STREAK_DATE, "")
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(java.util.Date())
            val yesterday = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(java.util.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
            
            val currentStreak = getWritingStreak()
            
            when {
                lastStreakDate == today -> {
                    // Already updated today, do nothing
                }
                lastStreakDate == yesterday -> {
                    // Continuing streak
                    prefs.edit()
                        .putInt(KEY_WRITING_STREAK, currentStreak + 1)
                        .putString(KEY_LAST_STREAK_DATE, today)
                        .apply()
                }
                else -> {
                    // Starting new streak
                    prefs.edit()
                        .putInt(KEY_WRITING_STREAK, 1)
                        .putString(KEY_LAST_STREAK_DATE, today)
                        .apply()
                }
            }
        }
    }
    
    /**
     * Reset all focus mode settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit().apply {
            putBoolean(KEY_FOCUS_MODE, false)
            putInt(KEY_FOCUS_STYLE, FocusModeStyle.MINIMAL.ordinal)
            putBoolean(KEY_TYPEWRITER_MODE, false)
            putBoolean(KEY_HIGHLIGHT_CURRENT_LINE, true)
            putBoolean(KEY_DIM_INACTIVE_TEXT, false)
            putInt(KEY_DAILY_WORD_GOAL, 0)
            putInt(KEY_SESSION_WORD_GOAL, 0)
            putInt(KEY_DOCUMENT_WORD_GOAL, 0)
        }.apply()
    }
    
    companion object {
        private const val PREFS_NAME = "focus_mode_prefs"
        
        private const val KEY_FOCUS_MODE = "focus_mode"
        private const val KEY_FOCUS_STYLE = "focus_style"
        private const val KEY_TYPEWRITER_MODE = "typewriter_mode"
        private const val KEY_HIGHLIGHT_CURRENT_LINE = "highlight_current_line"
        private const val KEY_DIM_INACTIVE_TEXT = "dim_inactive_text"
        
        private const val KEY_DAILY_WORD_GOAL = "daily_word_goal"
        private const val KEY_SESSION_WORD_GOAL = "session_word_goal"
        private const val KEY_DOCUMENT_WORD_GOAL = "document_word_goal"
        private const val KEY_TODAY_WORD_COUNT = "today_word_count"
        private const val KEY_TODAY_DATE = "today_date"
        
        private const val KEY_WRITING_STREAK = "writing_streak"
        private const val KEY_LAST_STREAK_DATE = "last_streak_date"
    }
}

/**
 * Focus mode visual styles
 */
enum class FocusModeStyle(val displayName: String, val description: String) {
    MINIMAL("Minimal", "Hide toolbar and navigation"),
    FULL("Full", "Hide all UI elements"),
    SEPIA("Sepia", "Warm sepia background for comfortable reading"),
    NIGHT("Night", "Dark background for low-light writing"),
    CUSTOM("Custom", "Customize your own focus mode")
}
