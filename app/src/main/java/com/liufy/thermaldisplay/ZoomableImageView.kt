package com.liufy.thermaldisplay

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import kotlin.math.min

/**
 * Native pinch-to-zoom + drag ImageView.
 * The transform belongs to the view, so changing thermal images does not stop carousel timing.
 */
class ZoomableImageView(
    context: Context,
    private val fitFraction: Float,
    private val maxUserScale: Float
) : ImageView(context), ScaleGestureDetector.OnScaleGestureListener {

    private val transform = Matrix()
    private val scaleDetector = ScaleGestureDetector(context, this)
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            resetTransform()
            return true
        }
    })

    private var userScale = 1f
    private var lastX = 0f
    private var lastY = 0f
    private var firstDrawable = true

    init {
        scaleType = ScaleType.MATRIX
        isClickable = true
        isFocusable = true
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val hadDrawable = this.drawable != null
        val saved = Matrix(imageMatrix)
        super.setImageDrawable(drawable)
        if (drawable == null) return
        post {
            if (!hadDrawable || firstDrawable) {
                firstDrawable = false
                resetTransform()
            } else {
                imageMatrix = saved
            }
        }
    }

    fun setImageDrawablePreserveTransform(drawable: Drawable?) {
        val saved = Matrix(imageMatrix)
        val initialized = this.drawable != null
        super.setImageDrawable(drawable)
        post {
            if (initialized) imageMatrix = saved else resetTransform()
        }
    }

    fun resetTransform() {
        val d = drawable ?: return
        if (width <= 0 || height <= 0 || d.intrinsicWidth <= 0 || d.intrinsicHeight <= 0) return
        val targetW = width * fitFraction
        val targetH = height * fitFraction
        val base = min(targetW / d.intrinsicWidth.toFloat(), targetH / d.intrinsicHeight.toFloat())
        val dw = d.intrinsicWidth * base
        val dh = d.intrinsicHeight * base
        val dx = (width - dw) / 2f
        val dy = (height - dh) / 2f
        transform.reset()
        transform.postScale(base, base)
        transform.postTranslate(dx, dy)
        userScale = 1f
        imageMatrix = transform
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && (oldw == 0 || oldh == 0)) post { resetTransform() }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && event.pointerCount == 1 && userScale > 1.001f) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    transform.set(imageMatrix)
                    transform.postTranslate(dx, dy)
                    imageMatrix = transform
                }
                lastX = event.x
                lastY = event.y
            }
        }
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val requested = (userScale * detector.scaleFactor).coerceIn(0.70f, maxUserScale)
        val factor = requested / userScale
        userScale = requested
        transform.set(imageMatrix)
        transform.postScale(factor, factor, detector.focusX, detector.focusY)
        imageMatrix = transform
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
    override fun onScaleEnd(detector: ScaleGestureDetector) = Unit
}
