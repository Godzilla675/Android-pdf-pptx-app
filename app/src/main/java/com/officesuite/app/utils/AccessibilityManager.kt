package com.officesuite.app.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Accessibility Manager for handling accessibility features.
 * Implements Medium Priority Features from Phase 2 Section 12:
 * - Dyslexia-Friendly Font option
 * - Color Blind Modes
 * - Reading Ruler
 * - Font scaling
 */
class AccessibilityManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // ================== Dyslexia Font ==================
    
    var isDyslexiaFontEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYSLEXIA_FONT, false)
        set(value) = prefs.edit().putBoolean(KEY_DYSLEXIA_FONT, value).apply()
    
    // ================== Color Blind Mode ==================
    
    var colorBlindMode: ColorBlindMode
        get() {
            val ordinal = prefs.getInt(KEY_COLOR_BLIND_MODE, ColorBlindMode.NONE.ordinal)
            return ColorBlindMode.values().getOrElse(ordinal) { ColorBlindMode.NONE }
        }
        set(value) = prefs.edit().putInt(KEY_COLOR_BLIND_MODE, value.ordinal).apply()
    
    // ================== Reading Ruler ==================
    
    var isReadingRulerEnabled: Boolean
        get() = prefs.getBoolean(KEY_READING_RULER, false)
        set(value) = prefs.edit().putBoolean(KEY_READING_RULER, value).apply()
    
    var readingRulerHeight: Int
        get() = prefs.getInt(KEY_READING_RULER_HEIGHT, 48) // default 48dp
        set(value) = prefs.edit().putInt(KEY_READING_RULER_HEIGHT, value.coerceIn(24, 96)).apply()
    
    var readingRulerOpacity: Float
        get() = prefs.getFloat(KEY_READING_RULER_OPACITY, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_READING_RULER_OPACITY, value.coerceIn(0.5f, 1.0f)).apply()
    
    // ================== High Contrast ==================
    
    var isHighContrastEnabled: Boolean
        get() = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        set(value) = prefs.edit().putBoolean(KEY_HIGH_CONTRAST, value).apply()
    
    // ================== Reduce Motion ==================
    
    var isReduceMotionEnabled: Boolean
        get() = prefs.getBoolean(KEY_REDUCE_MOTION, false)
        set(value) = prefs.edit().putBoolean(KEY_REDUCE_MOTION, value).apply()
    
    // ================== Large Touch Targets ==================
    
    var isLargeTouchTargetsEnabled: Boolean
        get() = prefs.getBoolean(KEY_LARGE_TOUCH_TARGETS, false)
        set(value) = prefs.edit().putBoolean(KEY_LARGE_TOUCH_TARGETS, value).apply()
    
    // ================== Line Spacing ==================
    
    var lineSpacing: LineSpacing
        get() {
            val ordinal = prefs.getInt(KEY_LINE_SPACING, LineSpacing.NORMAL.ordinal)
            return LineSpacing.values().getOrElse(ordinal) { LineSpacing.NORMAL }
        }
        set(value) = prefs.edit().putInt(KEY_LINE_SPACING, value.ordinal).apply()
    
    // ================== Letter Spacing ==================
    
    var letterSpacing: Float
        get() = prefs.getFloat(KEY_LETTER_SPACING, 0f)
        set(value) = prefs.edit().putFloat(KEY_LETTER_SPACING, value.coerceIn(0f, 0.3f)).apply()
    
    /**
     * Get the font family to use based on accessibility settings
     */
    fun getFontFamily(): String {
        return if (isDyslexiaFontEnabled) {
            FONT_OPENDYSLEXIC
        } else {
            FONT_DEFAULT
        }
    }
    
    /**
     * Apply color transformation for color blind mode
     */
    fun transformColor(color: Int): Int {
        return when (colorBlindMode) {
            ColorBlindMode.NONE -> color
            ColorBlindMode.PROTANOPIA -> ColorBlindTransform.transformProtanopia(color)
            ColorBlindMode.DEUTERANOPIA -> ColorBlindTransform.transformDeuteranopia(color)
            ColorBlindMode.TRITANOPIA -> ColorBlindTransform.transformTritanopia(color)
            ColorBlindMode.MONOCHROMACY -> ColorBlindTransform.transformMonochromacy(color)
        }
    }
    
    /**
     * Reset all accessibility settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit().apply {
            putBoolean(KEY_DYSLEXIA_FONT, false)
            putInt(KEY_COLOR_BLIND_MODE, ColorBlindMode.NONE.ordinal)
            putBoolean(KEY_READING_RULER, false)
            putInt(KEY_READING_RULER_HEIGHT, 48)
            putFloat(KEY_READING_RULER_OPACITY, 0.85f)
            putBoolean(KEY_HIGH_CONTRAST, false)
            putBoolean(KEY_REDUCE_MOTION, false)
            putBoolean(KEY_LARGE_TOUCH_TARGETS, false)
            putInt(KEY_LINE_SPACING, LineSpacing.NORMAL.ordinal)
            putFloat(KEY_LETTER_SPACING, 0f)
        }.apply()
    }
    
    companion object {
        private const val PREFS_NAME = "accessibility_prefs"
        
        private const val KEY_DYSLEXIA_FONT = "dyslexia_font"
        private const val KEY_COLOR_BLIND_MODE = "color_blind_mode"
        private const val KEY_READING_RULER = "reading_ruler"
        private const val KEY_READING_RULER_HEIGHT = "reading_ruler_height"
        private const val KEY_READING_RULER_OPACITY = "reading_ruler_opacity"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_LARGE_TOUCH_TARGETS = "large_touch_targets"
        private const val KEY_LINE_SPACING = "line_spacing"
        private const val KEY_LETTER_SPACING = "letter_spacing"
        
        const val FONT_OPENDYSLEXIC = "opendyslexic"
        const val FONT_DEFAULT = "sans-serif"
    }
}

/**
 * Color blind mode types
 */
enum class ColorBlindMode(val displayName: String, val description: String) {
    NONE("None", "Normal color vision"),
    PROTANOPIA("Protanopia", "Red-blind (1% of men)"),
    DEUTERANOPIA("Deuteranopia", "Green-blind (1% of men)"),
    TRITANOPIA("Tritanopia", "Blue-blind (rare)"),
    MONOCHROMACY("Monochromacy", "Complete color blindness")
}

/**
 * Line spacing options
 */
enum class LineSpacing(val multiplier: Float, val displayName: String) {
    COMPACT(1.0f, "Compact"),
    NORMAL(1.4f, "Normal"),
    RELAXED(1.8f, "Relaxed"),
    EXTRA_RELAXED(2.2f, "Extra Relaxed")
}

/**
 * Color transformation utilities for color blind simulation.
 * 
 * Color transformation matrices are based on the Brettel, Viénot, and Mollon (1997) algorithm
 * for simulating color blindness. These are approximations of how colors appear to people
 * with various types of color vision deficiency.
 * 
 * Reference: "Computerized simulation of color appearance for dichromats"
 * Brettel H, Viénot F, Mollon JD (1997), J Opt Soc Am A 14:2647-2655
 */
object ColorBlindTransform {
    
    /**
     * Transform color for protanopia (red-blind).
     * Protanopia affects about 1% of males and results in inability to perceive red light.
     * The L-cone (long wavelength, red) is missing or defective.
     */
    fun transformProtanopia(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val a = (color shr 24) and 0xFF
        
        // Protanopia transformation matrix (Brettel et al. 1997)
        // Simulates missing L-cone by mapping red channel to other cones
        val newR = (0.567 * r + 0.433 * g + 0.0 * b).toInt().coerceIn(0, 255)
        val newG = (0.558 * r + 0.442 * g + 0.0 * b).toInt().coerceIn(0, 255)
        val newB = (0.0 * r + 0.242 * g + 0.758 * b).toInt().coerceIn(0, 255)
        
        return (a shl 24) or (newR shl 16) or (newG shl 8) or newB
    }
    
    /**
     * Transform color for deuteranopia (green-blind).
     * Deuteranopia affects about 1% of males and results in inability to perceive green light.
     * The M-cone (medium wavelength, green) is missing or defective.
     */
    fun transformDeuteranopia(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val a = (color shr 24) and 0xFF
        
        // Deuteranopia transformation matrix (Brettel et al. 1997)
        // Simulates missing M-cone by mapping green channel to other cones
        val newR = (0.625 * r + 0.375 * g + 0.0 * b).toInt().coerceIn(0, 255)
        val newG = (0.7 * r + 0.3 * g + 0.0 * b).toInt().coerceIn(0, 255)
        val newB = (0.0 * r + 0.3 * g + 0.7 * b).toInt().coerceIn(0, 255)
        
        return (a shl 24) or (newR shl 16) or (newG shl 8) or newB
    }
    
    /**
     * Transform color for tritanopia (blue-blind).
     * Tritanopia is rare (affects ~0.001% of population) and results in inability to perceive blue light.
     * The S-cone (short wavelength, blue) is missing or defective.
     */
    fun transformTritanopia(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val a = (color shr 24) and 0xFF
        
        // Tritanopia transformation matrix (Brettel et al. 1997)
        // Simulates missing S-cone by mapping blue channel to other cones
        val newR = (0.95 * r + 0.05 * g + 0.0 * b).toInt().coerceIn(0, 255)
        val newG = (0.0 * r + 0.433 * g + 0.567 * b).toInt().coerceIn(0, 255)
        val newB = (0.0 * r + 0.475 * g + 0.525 * b).toInt().coerceIn(0, 255)
        
        return (a shl 24) or (newR shl 16) or (newG shl 8) or newB
    }
    
    /**
     * Transform color to monochrome (grayscale).
     * Simulates complete color blindness (achromatopsia/monochromacy).
     * Uses ITU-R BT.601 luma coefficients (0.299R + 0.587G + 0.114B).
     */
    fun transformMonochromacy(color: Int): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val a = (color shr 24) and 0xFF
        
        // ITU-R BT.601 standard luminosity formula for perceived brightness
        // Weights account for human eye's varying sensitivity to different wavelengths
        val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt().coerceIn(0, 255)
        
        return (a shl 24) or (gray shl 16) or (gray shl 8) or gray
    }
}
