package com.liveness.sdk.corev4.facedetector

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.StyleableRes
import com.liveness.sdk.corev4.R
import kotlin.math.min


/**
 * Created by Hieudt43 on 12/1/21.
 */
internal class EllipseView : View {

    private companion object {
        const val OVAL_ASPECT_RATIO = 1.5f          // height/width — luôn ép ellipse
        const val NORMAL_WIDTH_FRACTION = 0.82f       // điện thoại thường: oval = 82% chiều rộng
        const val TABLET_SCALE_FACTOR = 0.60f         // tablet/foldable mở: thu nhỏ oval (tương tự ref 0.52)
        const val MAX_HEIGHT_FRACTION = 0.75f         // oval tối đa 75% chiều cao view
    }

    private val NOT_PRESENT: Int = Int.MIN_VALUE

    private var bm: Bitmap? = null
    private var cv: Canvas? = null
    private var eraser: Paint? = null
    private var paddingVertical = 0f
    private var paddingHorizontal = 0f
    private var xmlPaddingVertical = 0f
    private var xmlPaddingHorizontal = 0f
    private var dynamicPaddingApplied = false
    private var strokeWidth = 0f
    private var colorNormal = Color.WHITE
    private var colorWarning = Color.YELLOW
    private var colorActive = Color.GREEN
    private var colorStroke: Int = Color.WHITE
    private lateinit var strokePaint: Paint

    var onOvalChangedListener: ((RectF) -> Unit)? = null

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
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(
                attrs, R.styleable.EllipseView, defStyleAttr, 0
            )
            try {
                paddingVertical = getDimension(
                    ta,
                    R.styleable.EllipseView_paddingVertical,
                    R.dimen.default_padding_vertical
                ).toFloat()
                paddingHorizontal = getDimension(
                    ta,
                    R.styleable.EllipseView_paddingHorizontal,
                    R.dimen.default_padding_horizontal
                ).toFloat()
                strokeWidth = getDimension(
                    ta,
                    R.styleable.EllipseView_strokeWidth,
                    R.dimen.default_stroke_width
                ).toFloat()
                colorNormal = ta.getColor(R.styleable.EllipseView_colorNormal, Color.WHITE)
                colorWarning = ta.getColor(R.styleable.EllipseView_colorWarning, Color.YELLOW)
                colorActive = ta.getColor(R.styleable.EllipseView_colorActive, Color.GREEN)
                colorStroke = colorNormal

                xmlPaddingHorizontal = paddingHorizontal
                xmlPaddingVertical = paddingVertical
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
        recalculateDynamicPadding(w, h)
    }

    /**
     * Safety net: khi Z Fold gập/mở, configChanges trong manifest ngăn Activity recreate,
     * nhưng cần recalculate lại oval vì configuration thay đổi (isTablet() có thể thay đổi).
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        // Post để đợi view layout xong với kích thước mới
        post {
            if (width > 0 && height > 0) {
                bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                cv = Canvas(bm!!)
                recalculateDynamicPadding(width, height)
                invalidate()
            }
        }
    }

    /**
     * Kiểm tra thiết bị là tablet thật
     */
    private fun isTablet(): Boolean {
        val screenLayout = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isLargeScreen = screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE
        val smallestWidthDp = context.resources.configuration.smallestScreenWidthDp
        val isSmallestWidthTablet = smallestWidthDp >= 600
        return isLargeScreen || isSmallestWidthTablet
    }

    /**
     * Hệ số scale cho tablet/foldable mở — giảm kích thước oval trên màn hình lớn.
     *
     * Quy tắc:
     * - ratio > 0.75: chắc chắn wide screen (Z Fold mở, tablet landscape) → scale nhỏ
     * - isTablet() VÀ ratio > 0.55: tablet portrait thật (ratio ~0.625)
     *   → Z Fold gập (ratio ~0.37-0.44) KHÔNG vào đây → oval giữ ellipse
     * - Còn lại: điện thoại thường → không scale
     */
    private fun getScaleFactor(viewWidth: Int, viewHeight: Int): Float {
        val ratio = viewWidth.toFloat() / viewHeight.toFloat()
        // Z Fold mở hoặc tablet landscape
        if (ratio > 0.75f) return TABLET_SCALE_FACTOR
        // Tablet portrait thật (ratio ~0.625), nhưng KHÔNG phải Z Fold gập (ratio ~0.37-0.44)
        if (isTablet() && ratio > 0.55f) return TABLET_SCALE_FACTOR
        // Điện thoại thường hoặc Z Fold gập
        return 1.0f
    }

    /**
     * Tính oval theo pattern tabletScaleFactor (giống LivenessMaskView reference).
     * - Base: oval chiếm NORMAL_WIDTH_FRACTION (82%) chiều rộng, tỷ lệ OVAL_ASPECT_RATIO (1.4x)
     * - Tablet/Foldable mở: nhân thêm TABLET_SCALE_FACTOR để thu nhỏ
     * - Luôn ép aspect ratio → KHÔNG BAO GIỜ thành hình tròn
     */
    private fun recalculateDynamicPadding(w: Int, h: Int) {
        if (w == 0 || h == 0) return

        val wf = w.toFloat()
        val hf = h.toFloat()

        // Kích thước oval gốc (giống ref: viewW * 0.85)
        val baseW = wf * NORMAL_WIDTH_FRACTION
        val baseH = baseW * OVAL_ASPECT_RATIO

        // Áp dụng scale cho tablet (giống ref: tabletScaleFactor)
        val scaleFactor = getScaleFactor(w, h)
        var ovalW = baseW * scaleFactor
        var ovalH = baseH * scaleFactor

        // Giới hạn chiều cao tối đa
        val maxH = hf * MAX_HEIGHT_FRACTION
        if (ovalH > maxH) {
            ovalH = maxH
            ovalW = ovalH / OVAL_ASPECT_RATIO
        }

        paddingHorizontal = (wf - ovalW) / 2f
        paddingVertical = (hf - ovalH) / 2f
        dynamicPaddingApplied = scaleFactor < 1.0f

        val ovalRect = RectF(paddingHorizontal, paddingVertical,
            wf - paddingHorizontal, hf - paddingVertical)
        onOvalChangedListener?.invoke(ovalRect)
    }

    fun getOvalRect(): RectF = RectF(
        paddingHorizontal, paddingVertical,
        width - paddingHorizontal, height - paddingVertical
    )

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
            paddingHorizontal,
            paddingVertical,
            width - paddingHorizontal,
            height - paddingVertical
        )
        bm!!.eraseColor(Color.TRANSPARENT)
        cv?.drawColor(context.getColor(R.color.fm_black_60))
        cv?.drawOval(strokeRect, strokePaint)
        cv?.drawOval(rect, eraser!!)
        canvas.drawBitmap(bm!!, 0f, 0f, null)
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

    fun activeView() {
        colorStroke = colorActive
        invalidateEllipseBounds()
    }

    fun defaultView() {
        colorStroke = colorNormal
        invalidateEllipseBounds()
    }

    fun warningView() {
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
            colorStroke= targetColor
            invalidateEllipseBounds()
        }
        animator.start()
    }

}