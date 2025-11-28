package com.officesuite.app.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.officesuite.app.R

/**
 * Reading Ruler View for accessibility.
 * Implements Medium Priority Feature from Phase 2 Section 12:
 * - Reading Ruler: Visual guide for line-by-line reading
 * 
 * This overlay view provides a translucent strip that helps users
 * focus on one line at a time while reading.
 */
class ReadingRulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlayPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    
    private val rulerPaint = Paint().apply {
        color = Color.TRANSPARENT
        style = Paint.Style.FILL
    }
    
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    // Ruler position (center Y of the visible strip)
    private var rulerY: Float = 0f
    
    // Height of the visible reading strip in pixels
    var rulerHeight: Float = 48f * resources.displayMetrics.density
        set(value) {
            field = value
            invalidate()
        }
    
    // Opacity of the overlay (0.0 - 1.0)
    var overlayOpacity: Float = 0.85f
        set(value) {
            field = value.coerceIn(0.5f, 1.0f)
            overlayPaint.alpha = (field * 255).toInt()
            invalidate()
        }
    
    // Whether the ruler can be dragged
    var isDraggable: Boolean = true
    
    // Callback when ruler position changes
    var onPositionChanged: ((Float) -> Unit)? = null
    
    // Show border around the reading strip
    var showBorder: Boolean = true
        set(value) {
            field = value
            invalidate()
        }
    
    // Border color
    var borderColor: Int = Color.parseColor("#4CAF50")
        set(value) {
            field = value
            borderPaint.color = value
            invalidate()
        }

    private var lastTouchY: Float = 0f
    private var isDragging: Boolean = false

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ReadingRulerView, 0, 0).apply {
            try {
                rulerHeight = getDimension(R.styleable.ReadingRulerView_rulerHeight, rulerHeight)
                overlayOpacity = getFloat(R.styleable.ReadingRulerView_overlayOpacity, overlayOpacity)
                showBorder = getBoolean(R.styleable.ReadingRulerView_showBorder, showBorder)
                borderColor = getColor(R.styleable.ReadingRulerView_borderColor, borderColor)
            } finally {
                recycle()
            }
        }
        
        overlayPaint.alpha = (overlayOpacity * 255).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Initialize ruler position to center
        if (rulerY == 0f) {
            rulerY = h / 2f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val halfRulerHeight = rulerHeight / 2
        val topEdge = (rulerY - halfRulerHeight).coerceAtLeast(0f)
        val bottomEdge = (rulerY + halfRulerHeight).coerceAtMost(height.toFloat())
        
        // Draw top overlay (darkened area above reading strip)
        canvas.drawRect(0f, 0f, width.toFloat(), topEdge, overlayPaint)
        
        // Draw bottom overlay (darkened area below reading strip)
        canvas.drawRect(0f, bottomEdge, width.toFloat(), height.toFloat(), overlayPaint)
        
        // Draw border around the reading strip
        if (showBorder) {
            canvas.drawRect(0f, topEdge, width.toFloat(), bottomEdge, borderPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDraggable) return super.onTouchEvent(event)
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val halfRulerHeight = rulerHeight / 2
                val topEdge = rulerY - halfRulerHeight
                val bottomEdge = rulerY + halfRulerHeight
                
                // Check if touch is within the ruler strip
                if (event.y >= topEdge && event.y <= bottomEdge) {
                    isDragging = true
                    lastTouchY = event.y
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
                
                // If touched outside, move ruler to that position
                rulerY = event.y.coerceIn(rulerHeight / 2, height - rulerHeight / 2)
                invalidate()
                onPositionChanged?.invoke(rulerY)
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val delta = event.y - lastTouchY
                    rulerY = (rulerY + delta).coerceIn(rulerHeight / 2, height - rulerHeight / 2)
                    lastTouchY = event.y
                    invalidate()
                    onPositionChanged?.invoke(rulerY)
                    return true
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        
        return super.onTouchEvent(event)
    }

    /**
     * Set ruler position as a percentage of the view height (0.0 - 1.0)
     */
    fun setPositionPercent(percent: Float) {
        rulerY = (height * percent.coerceIn(0f, 1f))
        invalidate()
    }

    /**
     * Get ruler position as a percentage of the view height
     */
    fun getPositionPercent(): Float {
        return if (height > 0) rulerY / height else 0.5f
    }

    /**
     * Move ruler up by one line height
     */
    fun moveUp() {
        rulerY = (rulerY - rulerHeight).coerceAtLeast(rulerHeight / 2)
        invalidate()
        onPositionChanged?.invoke(rulerY)
    }

    /**
     * Move ruler down by one line height
     */
    fun moveDown() {
        rulerY = (rulerY + rulerHeight).coerceAtMost(height - rulerHeight / 2)
        invalidate()
        onPositionChanged?.invoke(rulerY)
    }

    /**
     * Reset ruler to center
     */
    fun reset() {
        rulerY = height / 2f
        invalidate()
        onPositionChanged?.invoke(rulerY)
    }
}
