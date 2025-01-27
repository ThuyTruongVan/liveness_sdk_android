package com.liveness.sdk.corev4.facedetector

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.StyleableRes
import com.liveness.sdk.corev4.R


/**
 * Created by Hieudt43 on 12/1/21.
 */
internal class EllipseView : View {

    private val NOT_PRESENT: Int = Int.MIN_VALUE

    private var bm: Bitmap? = null
    private var cv: Canvas? = null
    private var eraser: Paint? = null
    private var paddingVertical = 0f
    private var paddingHorizontal = 0f
    private var strokeWidth = 0f
    private var colorNormal = Color.GRAY
    private var colorWarning = Color.RED
    private var colorActive = Color.WHITE
    private var colorStroke: Int = Color.WHITE
    private lateinit var strokePaint: Paint
    private var isLoading = false
    private var sweepAngle = 0f

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        eraser = Paint()
        eraser!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        eraser!!.isAntiAlias = true
        if (isHardwareAccelerated) {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(
                attrs, R.styleable.EllipseView, defStyleAttr, 0
            )
            try {
                paddingVertical = getDimension(
                    ta, R.styleable.EllipseView_paddingVertical, R.dimen.default_padding_vertical
                ).toFloat()
                paddingHorizontal = getDimension(
                    ta,
                    R.styleable.EllipseView_paddingHorizontal,
                    R.dimen.default_padding_horizontal
                ).toFloat()
                strokeWidth = getDimension(
                    ta, R.styleable.EllipseView_strokeWidth, R.dimen.default_stroke_width
                ).toFloat()
                colorNormal = ta.getColor(R.styleable.EllipseView_colorNormal, Color.WHITE)
                colorWarning = ta.getColor(R.styleable.EllipseView_colorWarning, Color.YELLOW)
                colorActive = ta.getColor(R.styleable.EllipseView_colorActive, Color.GREEN)
                colorStroke = colorNormal

            } finally {
                ta.recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            cv = Canvas(bm!!)
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = colorStroke
            strokeWidth = this@EllipseView.strokeWidth
        }
        val strokeRect = RectF(
            paddingHorizontal - strokeWidth / 2,
            paddingVertical - strokeWidth / 2,
            width - paddingHorizontal + strokeWidth / 2,
            height - paddingVertical + strokeWidth / 2
        )

        val rect = RectF(
            paddingHorizontal, paddingVertical, width - paddingHorizontal, height - paddingVertical
        )
        bm!!.eraseColor(Color.TRANSPARENT)
        cv?.drawColor(context.getColor(R.color.fm_white))
        if (!isLoading) {
            cv?.drawOval(strokeRect, strokePaint)
        } else {
            cv?.drawArc(
                strokeRect, -90f,
                sweepAngle, false,
                strokePaint
            )
        }
        cv?.drawOval(rect, eraser!!)
        canvas.drawBitmap(bm!!, 0f, 0f, null)
        super.onDraw(canvas)
    }


    private fun dpToPx(dp: Int): Int {
        val r: Resources = resources
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics
            )
        )
    }

    private fun getDimension(
        a: TypedArray, @StyleableRes styleableId: Int, @DimenRes defaultDimension: Int
    ): Int {
        var result = a.getDimensionPixelSize(
            styleableId, NOT_PRESENT
        )
        if (result == NOT_PRESENT) {
            result = resources.getDimensionPixelSize(defaultDimension)
        }
        return result
    }

    fun loadingViewSemi(time: Long = 1000) {
        isLoading = true
        colorStroke = colorActive
        val animator = ValueAnimator.ofFloat(0f, 180f).apply {
            duration = time
            addUpdateListener {
                sweepAngle = it.animatedValue as Float
                invalidateEllipseBounds()
            }
            start()
        }

    }
    fun loadingViewPrepare(time: Long = 5000, endAngle: Float) {
        isLoading = true
        colorStroke = colorActive
        val animator = ValueAnimator.ofFloat(180f, endAngle).apply {
            duration = time
            addUpdateListener {
                sweepAngle = it.animatedValue as Float
                invalidateEllipseBounds()
            }
            start()
        }

    }

    fun loadingViewFull(startAngle: Float) {
        isLoading = true
        colorStroke = colorActive
        val animator = ValueAnimator.ofFloat(startAngle, 360f).apply {
            duration = 300
            addUpdateListener {
                sweepAngle = it.animatedValue as Float
                invalidateEllipseBounds()
            }
            start()
        }

    }

    fun defaultView() {
        isLoading = false
        colorStroke = colorNormal
        invalidateEllipseBounds()
    }

    fun errorView() {
        isLoading = false
        colorStroke = colorWarning
        invalidateEllipseBounds()
    }

    private fun invalidateEllipseBounds() {
        postInvalidate(
            (paddingHorizontal - strokeWidth / 2).toInt(),
            (paddingVertical - strokeWidth / 2).toInt(),
            (width - paddingHorizontal + strokeWidth / 2).toInt(),
            (height - paddingVertical + strokeWidth / 2).toInt()
        )
    }

    private fun animateColorChange(targetColor: Int) {
        val animator = ValueAnimator.ofArgb(colorStroke, targetColor)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            colorStroke = animation.animatedValue as Int
            colorStroke = targetColor
            invalidateEllipseBounds()
        }
        animator.start()
    }

}