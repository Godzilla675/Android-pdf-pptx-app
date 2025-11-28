package com.officesuite.app.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.officesuite.app.utils.GoalProgress

/**
 * Circular Progress View for word count goals.
 * Implements Medium Priority Feature from Phase 2 Section 6:
 * - Word Count Goals: Set and track word count targets
 */
class GoalProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        color = Color.parseColor("#E0E0E0")
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#4CAF50")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#212121")
        textSize = 48f
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#757575")
        textSize = 24f
    }

    private val rect = RectF()

    private var progress: Float = 0f
    private var currentWords: Int = 0
    private var goalWords: Int = 0
    private var isComplete: Boolean = false

    /**
     * Set the goal progress
     */
    fun setProgress(goalProgress: GoalProgress) {
        progress = (goalProgress.percentComplete.toFloat() / 100f).coerceIn(0f, 1f)
        currentWords = goalProgress.currentWords
        goalWords = goalProgress.currentWords + goalProgress.remainingWords
        isComplete = goalProgress.isComplete
        
        // Change color based on progress
        progressPaint.color = when {
            isComplete -> Color.parseColor("#4CAF50") // Green
            progress >= 0.75f -> Color.parseColor("#8BC34A") // Light green
            progress >= 0.5f -> Color.parseColor("#FFC107") // Amber
            progress >= 0.25f -> Color.parseColor("#FF9800") // Orange
            else -> Color.parseColor("#F44336") // Red
        }
        
        invalidate()
    }

    /**
     * Set progress directly (0.0 - 1.0)
     */
    fun setProgressValue(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * Set stroke width
     */
    fun setStrokeWidth(width: Float) {
        backgroundPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        invalidate()
    }

    /**
     * Set progress color
     */
    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    /**
     * Set background color
     */
    fun setBackgroundTrackColor(color: Int) {
        backgroundPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) - progressPaint.strokeWidth

        // Set up the arc bounds
        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Draw background circle
        canvas.drawArc(rect, 0f, 360f, false, backgroundPaint)

        // Draw progress arc
        val sweepAngle = 360f * progress
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        // Draw text
        if (goalWords > 0) {
            // Draw word count
            val wordText = "$currentWords"
            textPaint.textSize = radius / 2f
            canvas.drawText(wordText, centerX, centerY, textPaint)

            // Draw "of X words" below
            val goalText = "of $goalWords"
            subTextPaint.textSize = radius / 4f
            canvas.drawText(goalText, centerX, centerY + radius / 3f, subTextPaint)

            // Draw completion indicator
            if (isComplete) {
                val completeText = "✓ Goal reached!"
                subTextPaint.textSize = radius / 5f
                canvas.drawText(completeText, centerX, centerY + radius / 1.8f, subTextPaint)
            }
        } else {
            // Draw percentage
            val percentText = "${(progress * 100).toInt()}%"
            textPaint.textSize = radius / 1.5f
            canvas.drawText(percentText, centerX, centerY + textPaint.textSize / 3f, textPaint)
        }
    }
}
