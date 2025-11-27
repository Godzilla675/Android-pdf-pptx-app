package com.officesuite.app.ui.scanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.officesuite.app.utils.DocumentBorderDetector

/**
 * Custom view overlay that displays the detected document borders
 * on top of the camera preview.
 */
class BorderOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var corners: DocumentBorderDetector.DetectedCorners? = null
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f
    private var sourceWidth: Int = 0
    private var sourceHeight: Int = 0

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        pathEffect = CornerPathEffect(10f)
    }

    private val cornerPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = Color.parseColor("#204CAF50")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val borderPath = Path()

    /**
     * Sets the detected corners to display.
     * @param detectedCorners The corners detected from the image
     * @param imageWidth Original image width
     * @param imageHeight Original image height
     */
    fun setCorners(
        detectedCorners: DocumentBorderDetector.DetectedCorners?,
        imageWidth: Int,
        imageHeight: Int
    ) {
        this.corners = detectedCorners
        this.sourceWidth = imageWidth
        this.sourceHeight = imageHeight
        updateScaleFactors()
        invalidate()
    }

    /**
     * Clears the displayed corners.
     */
    fun clearCorners() {
        corners = null
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateScaleFactors()
    }

    private fun updateScaleFactors() {
        if (sourceWidth > 0 && sourceHeight > 0 && width > 0 && height > 0) {
            // Calculate scale to fit image in view while maintaining aspect ratio
            val viewAspect = width.toFloat() / height
            val imageAspect = sourceWidth.toFloat() / sourceHeight

            if (viewAspect > imageAspect) {
                // View is wider than image - fit by height
                scaleY = height.toFloat() / sourceHeight
                scaleX = scaleY
            } else {
                // View is taller than image - fit by width
                scaleX = width.toFloat() / sourceWidth
                scaleY = scaleX
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val detectedCorners = corners ?: return

        // Calculate offset to center the scaled image in the view
        val scaledWidth = sourceWidth * scaleX
        val scaledHeight = sourceHeight * scaleY
        val offsetX = (width - scaledWidth) / 2
        val offsetY = (height - scaledHeight) / 2

        // Transform corners to view coordinates
        val points = detectedCorners.toList().map { point ->
            PointF(
                point.x * scaleX + offsetX,
                point.y * scaleY + offsetY
            )
        }

        // Draw filled polygon
        borderPath.reset()
        borderPath.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            borderPath.lineTo(points[i].x, points[i].y)
        }
        borderPath.close()
        canvas.drawPath(borderPath, fillPaint)

        // Draw border
        canvas.drawPath(borderPath, borderPaint)

        // Draw corner circles
        val cornerRadius = 12f
        for (point in points) {
            canvas.drawCircle(point.x, point.y, cornerRadius, cornerPaint)
        }
    }

    /**
     * Sets the border color based on detection confidence.
     * Green for high confidence, yellow for medium, red for low.
     */
    fun updateConfidenceColor(confidence: Float) {
        val color = when {
            confidence >= 0.7f -> Color.parseColor("#4CAF50") // Green
            confidence >= 0.4f -> Color.parseColor("#FFC107") // Yellow
            else -> Color.parseColor("#F44336") // Red
        }
        borderPaint.color = color
        cornerPaint.color = color
        fillPaint.color = Color.argb(32, Color.red(color), Color.green(color), Color.blue(color))
        invalidate()
    }
}
