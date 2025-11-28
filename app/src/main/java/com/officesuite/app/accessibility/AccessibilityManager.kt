package com.officesuite.app.accessibility

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.officesuite.app.data.repository.ColorBlindMode

/**
 * Reading Ruler View for accessibility support
 * Part of Medium Priority Features Phase 2: Accessibility Enhancements
 * 
 * Provides a visual guide for line-by-line reading
 */
class ReadingRulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var lineHeight = 40f
    private var currentY = 0f
    private var rulerColor = Color.parseColor("#FFE082")
    private var overlayOpacity = 0.6f
    
    var isRulerEnabled = false
        set(value) {
            field = value
            visibility = if (value) VISIBLE else GONE
            invalidate()
        }

    init {
        paint.style = Paint.Style.FILL
        overlayPaint.style = Paint.Style.FILL
        visibility = GONE
    }

    /**
     * Set the reading line height
     */
    fun setLineHeight(height: Float) {
        lineHeight = height
        invalidate()
    }

    /**
     * Set the ruler highlight color
     */
    fun setRulerColor(color: Int) {
        rulerColor = color
        invalidate()
    }

    /**
     * Set overlay opacity (0.0 - 1.0)
     */
    fun setOverlayOpacity(opacity: Float) {
        overlayOpacity = opacity.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (!isRulerEnabled) return

        // Draw semi-transparent overlay above the reading line
        overlayPaint.color = Color.argb((255 * overlayOpacity).toInt(), 0, 0, 0)
        canvas.drawRect(0f, 0f, width.toFloat(), currentY, overlayPaint)

        // Draw the highlighted reading line
        paint.color = rulerColor
        canvas.drawRect(0f, currentY, width.toFloat(), currentY + lineHeight, paint)

        // Draw semi-transparent overlay below the reading line
        canvas.drawRect(0f, currentY + lineHeight, width.toFloat(), height.toFloat(), overlayPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isRulerEnabled) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                currentY = (event.y - lineHeight / 2).coerceIn(0f, height - lineHeight)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Move ruler to next line
     */
    fun moveToNextLine() {
        currentY = (currentY + lineHeight).coerceAtMost(height - lineHeight)
        invalidate()
    }

    /**
     * Move ruler to previous line
     */
    fun moveToPreviousLine() {
        currentY = (currentY - lineHeight).coerceAtLeast(0f)
        invalidate()
    }

    /**
     * Reset ruler to top
     */
    fun resetToTop() {
        currentY = 0f
        invalidate()
    }
}

/**
 * Accessibility Manager for handling all accessibility features
 */
class AccessibilityManager(private val context: Context) {

    /**
     * Get a color filter for color blind modes
     */
    fun getColorBlindFilter(mode: ColorBlindMode): ColorMatrixColorFilter? {
        return when (mode) {
            ColorBlindMode.NONE -> null
            ColorBlindMode.PROTANOPIA -> ColorMatrixColorFilter(protanopiaMatrix)
            ColorBlindMode.DEUTERANOPIA -> ColorMatrixColorFilter(deuteranopiaMatrix)
            ColorBlindMode.TRITANOPIA -> ColorMatrixColorFilter(tritanopiaMatrix)
        }
    }

    /**
     * Apply color blind mode filter to a view
     */
    fun applyColorBlindFilter(view: View, mode: ColorBlindMode) {
        val filter = getColorBlindFilter(mode)
        if (view is ViewGroup) {
            applyFilterRecursively(view, filter)
        } else {
            view.background?.colorFilter = filter
        }
    }

    private fun applyFilterRecursively(viewGroup: ViewGroup, filter: ColorMatrixColorFilter?) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                applyFilterRecursively(child, filter)
            } else {
                child.background?.colorFilter = filter
            }
        }
        viewGroup.background?.colorFilter = filter
    }

    /**
     * Get OpenDyslexic-style font adjustments
     * Note: For actual OpenDyslexic font, the TTF file needs to be added to assets
     */
    fun getDyslexiaFriendlyTypeface(): Typeface? {
        return try {
            // Try to load OpenDyslexic font if available
            Typeface.createFromAsset(context.assets, "fonts/OpenDyslexic-Regular.ttf")
        } catch (e: Exception) {
            // Fall back to sans-serif if OpenDyslexic is not available
            null
        }
    }

    /**
     * Get recommended text settings for dyslexia support
     */
    fun getDyslexiaFriendlySettings(): DyslexiaSettings {
        return DyslexiaSettings(
            lineSpacing = 1.5f,
            letterSpacing = 0.1f,
            wordSpacing = 0.3f,
            fontSize = 18f,
            useAlternateBackground = true,
            backgroundColor = Color.parseColor("#FFFBF0"),
            textColor = Color.parseColor("#333333")
        )
    }

    data class DyslexiaSettings(
        val lineSpacing: Float,
        val letterSpacing: Float,
        val wordSpacing: Float,
        val fontSize: Float,
        val useAlternateBackground: Boolean,
        val backgroundColor: Int,
        val textColor: Int
    )

    /**
     * Get high contrast color scheme
     */
    fun getHighContrastColors(): Pair<Int, Int> {
        return Pair(Color.BLACK, Color.WHITE)
    }

    /**
     * Create a readable line guide overlay
     */
    fun createReadingRulerOverlay(parent: FrameLayout): ReadingRulerView {
        val ruler = ReadingRulerView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        parent.addView(ruler)
        return ruler
    }

    companion object {
        // Color matrices for color blind simulation/correction
        
        // Protanopia (red-blind) correction matrix
        private val protanopiaMatrix = ColorMatrix(floatArrayOf(
            0.567f, 0.433f, 0f, 0f, 0f,
            0.558f, 0.442f, 0f, 0f, 0f,
            0f, 0.242f, 0.758f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        // Deuteranopia (green-blind) correction matrix
        private val deuteranopiaMatrix = ColorMatrix(floatArrayOf(
            0.625f, 0.375f, 0f, 0f, 0f,
            0.7f, 0.3f, 0f, 0f, 0f,
            0f, 0.3f, 0.7f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        // Tritanopia (blue-blind) correction matrix
        private val tritanopiaMatrix = ColorMatrix(floatArrayOf(
            0.95f, 0.05f, 0f, 0f, 0f,
            0f, 0.433f, 0.567f, 0f, 0f,
            0f, 0.475f, 0.525f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
    }
}
