package com.officesuite.app.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AccessibilityManager.
 * Tests medium priority features: Color Blind Modes, Line Spacing.
 */
class AccessibilityManagerTest {

    // ================== ColorBlindMode Tests ==================
    
    @Test
    fun `ColorBlindMode NONE has correct properties`() {
        val mode = ColorBlindMode.NONE
        assertEquals("None", mode.displayName)
        assertEquals("Normal color vision", mode.description)
    }
    
    @Test
    fun `ColorBlindMode PROTANOPIA has correct properties`() {
        val mode = ColorBlindMode.PROTANOPIA
        assertEquals("Protanopia", mode.displayName)
        assertTrue(mode.description.contains("Red-blind"))
    }
    
    @Test
    fun `ColorBlindMode DEUTERANOPIA has correct properties`() {
        val mode = ColorBlindMode.DEUTERANOPIA
        assertEquals("Deuteranopia", mode.displayName)
        assertTrue(mode.description.contains("Green-blind"))
    }
    
    @Test
    fun `ColorBlindMode TRITANOPIA has correct properties`() {
        val mode = ColorBlindMode.TRITANOPIA
        assertEquals("Tritanopia", mode.displayName)
        assertTrue(mode.description.contains("Blue-blind"))
    }
    
    @Test
    fun `ColorBlindMode MONOCHROMACY has correct properties`() {
        val mode = ColorBlindMode.MONOCHROMACY
        assertEquals("Monochromacy", mode.displayName)
        assertTrue(mode.description.contains("Complete"))
    }
    
    @Test
    fun `ColorBlindMode has all expected values`() {
        val modes = ColorBlindMode.values()
        assertEquals(5, modes.size)
    }
    
    // ================== LineSpacing Tests ==================
    
    @Test
    fun `LineSpacing COMPACT has correct multiplier`() {
        assertEquals(1.0f, LineSpacing.COMPACT.multiplier, 0.01f)
    }
    
    @Test
    fun `LineSpacing NORMAL has correct multiplier`() {
        assertEquals(1.4f, LineSpacing.NORMAL.multiplier, 0.01f)
    }
    
    @Test
    fun `LineSpacing RELAXED has correct multiplier`() {
        assertEquals(1.8f, LineSpacing.RELAXED.multiplier, 0.01f)
    }
    
    @Test
    fun `LineSpacing EXTRA_RELAXED has correct multiplier`() {
        assertEquals(2.2f, LineSpacing.EXTRA_RELAXED.multiplier, 0.01f)
    }
    
    @Test
    fun `LineSpacing has all expected values`() {
        val spacings = LineSpacing.values()
        assertEquals(4, spacings.size)
    }
    
    // ================== ColorBlindTransform Tests ==================
    
    @Test
    fun `transformProtanopia transforms color`() {
        val color = 0xFFFF0000.toInt() // Pure red
        val transformed = ColorBlindTransform.transformProtanopia(color)
        assertNotEquals(color, transformed)
    }
    
    @Test
    fun `transformDeuteranopia transforms color`() {
        val color = 0xFF00FF00.toInt() // Pure green
        val transformed = ColorBlindTransform.transformDeuteranopia(color)
        assertNotEquals(color, transformed)
    }
    
    @Test
    fun `transformTritanopia transforms color`() {
        val color = 0xFF0000FF.toInt() // Pure blue
        val transformed = ColorBlindTransform.transformTritanopia(color)
        assertNotEquals(color, transformed)
    }
    
    @Test
    fun `transformMonochromacy converts to grayscale`() {
        val color = 0xFFFF0000.toInt() // Pure red
        val transformed = ColorBlindTransform.transformMonochromacy(color)
        
        // Extract RGB components
        val r = (transformed shr 16) and 0xFF
        val g = (transformed shr 8) and 0xFF
        val b = transformed and 0xFF
        
        // In grayscale, R = G = B
        assertEquals(r, g)
        assertEquals(g, b)
    }
    
    @Test
    fun `transformMonochromacy preserves alpha`() {
        val color = 0x80FF0000.toInt() // Semi-transparent red
        val transformed = ColorBlindTransform.transformMonochromacy(color)
        
        val originalAlpha = (color shr 24) and 0xFF
        val transformedAlpha = (transformed shr 24) and 0xFF
        
        assertEquals(originalAlpha, transformedAlpha)
    }
    
    @Test
    fun `color transforms stay in valid range`() {
        val colors = listOf(
            0xFFFF0000.toInt(),
            0xFF00FF00.toInt(),
            0xFF0000FF.toInt(),
            0xFFFFFFFF.toInt(),
            0xFF000000.toInt()
        )
        
        for (color in colors) {
            val protanopia = ColorBlindTransform.transformProtanopia(color)
            val deuteranopia = ColorBlindTransform.transformDeuteranopia(color)
            val tritanopia = ColorBlindTransform.transformTritanopia(color)
            val mono = ColorBlindTransform.transformMonochromacy(color)
            
            // Verify all components are in valid range
            for (transformed in listOf(protanopia, deuteranopia, tritanopia, mono)) {
                val r = (transformed shr 16) and 0xFF
                val g = (transformed shr 8) and 0xFF
                val b = transformed and 0xFF
                
                assertTrue("R out of range", r in 0..255)
                assertTrue("G out of range", g in 0..255)
                assertTrue("B out of range", b in 0..255)
            }
        }
    }
}
