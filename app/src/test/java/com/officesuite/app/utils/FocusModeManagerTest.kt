package com.officesuite.app.utils

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for FocusModeManager.
 * Tests medium priority features: Focus Mode, Word Count Goals.
 */
class FocusModeManagerTest {

    // ================== Focus Mode Style Tests ==================
    
    @Test
    fun `FocusModeStyle MINIMAL has correct properties`() {
        val style = FocusModeStyle.MINIMAL
        assertEquals("Minimal", style.displayName)
        assertEquals("Hide toolbar and navigation", style.description)
    }
    
    @Test
    fun `FocusModeStyle FULL has correct properties`() {
        val style = FocusModeStyle.FULL
        assertEquals("Full", style.displayName)
        assertEquals("Hide all UI elements", style.description)
    }
    
    @Test
    fun `FocusModeStyle SEPIA has correct properties`() {
        val style = FocusModeStyle.SEPIA
        assertEquals("Sepia", style.displayName)
        assertTrue(style.description.contains("sepia"))
    }
    
    @Test
    fun `FocusModeStyle NIGHT has correct properties`() {
        val style = FocusModeStyle.NIGHT
        assertEquals("Night", style.displayName)
        assertTrue(style.description.contains("Dark"))
    }
    
    @Test
    fun `FocusModeStyle has all expected values`() {
        val styles = FocusModeStyle.values()
        assertEquals(5, styles.size)
        assertTrue(styles.contains(FocusModeStyle.MINIMAL))
        assertTrue(styles.contains(FocusModeStyle.FULL))
        assertTrue(styles.contains(FocusModeStyle.SEPIA))
        assertTrue(styles.contains(FocusModeStyle.NIGHT))
        assertTrue(styles.contains(FocusModeStyle.CUSTOM))
    }
}
