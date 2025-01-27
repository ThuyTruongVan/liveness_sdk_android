package com.liveness.sdk.corev4.facedetector

import android.R.attr
import android.R.attr.radius
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.DimenRes
import androidx.annotation.StyleableRes
import com.liveness.sdk.corev4.R


/**
 * Created by Hieudt43 on 12/1/21.
 */
internal class CircleView : View {

    private val NOT_PRESENT: Int = Int.MIN_VALUE

    private var bm: Bitmap? = null
    private var cv: Canvas? = null
    private var eraser: Paint? = null
    private var linePaint: Paint? = null
    private var paddingVertical = 0f
    private var paddingHorizontal = 0f
    private var paddingLine = 3f
    private var lineWidth = 20f
    private var lineHeight = 5f
    private var colorLine = Color.GRAY
    private var colorLineNormal = Color.GRAY
    private var colorBackground = Color.WHITE

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyleAttr: Int
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

        linePaint = Paint()
        linePaint!!.isAntiAlias = true
        linePaint!!.strokeCap = Paint.Cap.ROUND
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(
                attrs, R.styleable.CircleView, defStyleAttr, 0
            )
            try {
                paddingVertical = getDimension(
                    ta,
                    R.styleable.CircleView_circle_paddingVertical,
                    R.dimen.default_padding_vertical
                ).toFloat()
                paddingHorizontal = getDimension(
                    ta,
                    R.styleable.CircleView_circle_paddingHorizontal,
                    R.dimen.default_padding_horizontal
                ).toFloat()

                lineWidth = getDimension(
                    ta,
                    R.styleable.CircleView_circle_lineWidth,
                    R.dimen.default_line_width
                ).toFloat()

                lineHeight = getDimension(
                    ta,
                    R.styleable.CircleView_circle_lineHeight,
                    R.dimen.default_line_height
                ).toFloat()

                paddingLine = getDimension(
                    ta,
                    R.styleable.CircleView_circle_paddingLine,
                    R.dimen.default_padding_line
                ).toFloat()

                colorLine = ta.getColor(R.styleable.CircleView_circle_colorLine, Color.GRAY)
                colorLineNormal = colorLine

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
        linePaint!!.color = colorLineNormal
        linePaint!!.strokeWidth = lineHeight
        val circleRadius = (width - paddingHorizontal * 2) / 2
        val centerX = circleRadius + paddingHorizontal
        val centerY = circleRadius + paddingVertical
        bm!!.eraseColor(Color.TRANSPARENT)
//        cv?.drawPaint(Paint().apply {
//            shader = LinearGradient(
//                0f, gradientOffset, 0f, gradientOffset + height * 0.1f,
//                startColor, endColor, Shader.TileMode.CLAMP
//            )
//        })
        //top to bottom
        cv?.drawPaint(Paint().apply {
            shader = LinearGradient(
                0f, gradientOffset, 0f, gradientOffset - height * 0.01f,
                startColor, endColor, Shader.TileMode.CLAMP
            )
        })
//        cv?.drawColor(colorBackground)
        cv?.drawCircle(centerX, centerY, circleRadius - lineWidth - paddingLine, eraser!!)
        canvas.drawBitmap(bm!!, 0f, 0f, null)
        canvas.translate(centerX, centerY)
        for (i in 0..71) {
            val startY = -circleRadius
            val endY = -circleRadius + lineWidth
            canvas.drawLine(0f, startY, 0f, endY, linePaint!!)
            canvas.rotate(5f)
        }
        canvas.translate(0f, 0f)
        super.onDraw(canvas)

    }




    private fun dpToPx(dp: Int): Int {
        val r: Resources = resources
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.displayMetrics
            )
        )
    }

    private fun getDimension(
        a: TypedArray, @StyleableRes styleableId: Int,
        @DimenRes defaultDimension: Int
    ): Int {
        var result = a.getDimensionPixelSize(
            styleableId,
            NOT_PRESENT
        )
        if (result == NOT_PRESENT) {
            result = resources.getDimensionPixelSize(defaultDimension)
        }
        return result
    }

    fun flashView(color: Int) {
        colorBackground = color
        colorLineNormal = Color.WHITE
        postInvalidate()
    }

    fun resetView() {
        colorBackground = Color.WHITE
        colorLineNormal = colorLine
        startColor = colorBackground
        endColor = colorBackground
        postInvalidate()
    }

    private var gradientOffset: Float = 0f
    private var startColor: Int = Color.WHITE
    private var endColor: Int = Color.WHITE
    private var gradientAnimator: ValueAnimator? = null

    fun animateBackgroundSlide(toColor: Int, duration: Long = 600L) {
        startColor = colorBackground
        endColor = toColor
        colorLineNormal = Color.WHITE

        gradientAnimator?.cancel()
        //bottom to top
//        gradientAnimator = ValueAnimator.ofFloat(height.toFloat(), 0f).apply {
//            this.duration = duration
//            interpolator = LinearInterpolator()
//            addUpdateListener { animator ->
//                gradientOffset = animator.animatedValue as Float
//                invalidate()
//            }
//            start()
//        }
        //top to bottom
        gradientAnimator = ValueAnimator.ofFloat(0f, height.toFloat()).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                gradientOffset = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }


//
//    fun defaultView() {
//        colorStroke = colorNormal
//        invalidateEllipseBounds()
//    }
//
//    fun warningView() {
//        colorStroke = colorWarning
//        invalidateEllipseBounds()
//    }
//
//    private fun invalidateEllipseBounds() {
//        postInvalidate(
//            (paddingHorizontal - strokeWidth / 2).toInt(),
//            (paddingVertical - strokeWidth / 2).toInt(),
//            (width - paddingHorizontal + strokeWidth / 2).toInt(),
//            (height - paddingVertical + strokeWidth / 2).toInt()
//        )
//    }
//
//    private fun animateColorChange(targetColor: Int) {
//        val animator = ValueAnimator.ofArgb(colorStroke, targetColor)
//        animator.duration = 300
//        animator.addUpdateListener { animation ->
//            colorStroke = animation.animatedValue as Int
//            colorStroke = targetColor
//            invalidateEllipseBounds()
//        }
//        animator.start()
//    }
}