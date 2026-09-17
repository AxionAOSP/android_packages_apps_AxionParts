package com.android.axion.axionparts.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Path
import android.graphics.Path.FillType.EVEN_ODD
import android.graphics.RectF
import android.graphics.Shader.TileMode.CLAMP
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.view.View
import java.lang.Math
import kotlin.jvm.JvmOverloads
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SemiCircleFillView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private companion object {
        const val BORDER_WIDTH_DP = 1f
        const val POINTER_DIAMETER_DP = 8f
        const val RING_THICKNESS_DP = 20f
        const val DEFAULT_ANIMATION_DURATION = 500L
        const val SHORT_ANIMATION_DURATION = 800L
        const val LONG_ANIMATION_DURATION = 1000L
    }

    private val density = resources.displayMetrics.density
    private val ringThickness = RING_THICKNESS_DP * density
    private val pointerRadius = POINTER_DIAMETER_DP * density / 2f
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = FILL
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isNightMode()) Color.parseColor("#ff262626") else Color.parseColor("#ffe0e0e0")
        style = FILL
    }
    private val pointerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f * density
        isAntiAlias = true
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        style = STROKE
        strokeWidth = BORDER_WIDTH_DP * density
        setShadowLayer(10f * density, 0f, 5f * density, Color.argb(0x19, 0x00, 0x00, 0x00))
    }
    private val dashEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
    private val gradientColors = intArrayOf(
        Color.parseColor("#ffff2600"),
        Color.parseColor("#ffea0027"),
        Color.parseColor("#ffff5500"),
        Color.parseColor("#ff333333"),
        Color.TRANSPARENT,
    )

    private var innerRadius = 0f
    private var outerRadius = 0f
    private var animatedProgress = 0f
    private var currentProgress = 0f
    private var targetProgress = 0f
    private var animationDuration = DEFAULT_ANIMATION_DURATION
    private var animator: ValueAnimator? = null

    init {
        updateRadii()
    }

    fun setAnimationDurationType(type: Int) {
        animationDuration = when (type) {
            1 -> LONG_ANIMATION_DURATION
            2 -> SHORT_ANIMATION_DURATION
            else -> DEFAULT_ANIMATION_DURATION
        }
    }

    fun setProgressWithNoAnim(progress: Float) {
        val value = progress.coerceIn(0f, 1f)
        animator?.cancel()
        animatedProgress = value
        currentProgress = value
        targetProgress = value
        invalidate()
    }

    fun animateToProgress(progress: Float) {
        val value = progress.coerceIn(0f, 1f)
        if (value == targetProgress && animator?.isRunning == true) {
            return
        }
        if (value == currentProgress && animator?.isRunning != true) {
            targetProgress = value
            return
        }
        animator?.cancel()
        targetProgress = value
        animator = ValueAnimator.ofFloat(currentProgress, value).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animatedProgress = it.animatedValue as Float
                currentProgress = animatedProgress
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateRadii()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (outerRadius == 0f) {
            updateRadii()
        }
        val centerX = width / 2f
        val centerY = height.toFloat()
        val sweepAngle = animatedProgress * 180f
        val rtl = layoutDirection == LAYOUT_DIRECTION_RTL
        val pointerAngle = Math.toRadians(if (rtl) sweepAngle.toDouble() else (sweepAngle + 180f).toDouble())
        val midRadius = innerRadius + ringThickness / 2f
        val pointerX = centerX + midRadius * cos(pointerAngle).toFloat()
        val pointerY = centerY + midRadius * sin(Math.toRadians((sweepAngle + 180f).toDouble())).toFloat()

        pointerLinePaint.pathEffect = dashEffect
        pointerLinePaint.shader = LinearGradient(
            centerX,
            centerY,
            pointerX,
            pointerY,
            gradientColors,
            null,
            CLAMP,
        )
        canvas.drawLine(centerX, centerY, pointerX, pointerY, pointerLinePaint)

        val outerRect = RectF(
            centerX - outerRadius,
            centerY - outerRadius,
            centerX + outerRadius,
            centerY + outerRadius,
        )
        val innerRect = RectF(
            centerX - innerRadius,
            centerY - innerRadius,
            centerX + innerRadius,
            centerY + innerRadius,
        )
        val donutPath = Path().apply {
            addArc(outerRect, 180f, 180f)
            addArc(innerRect, 180f, 180f)
            fillType = EVEN_ODD
        }

        val drawStart = if (rtl) 0f else 180f
        val drawSweep = if (rtl) -sweepAngle else sweepAngle
        canvas.save()
        canvas.clipPath(donutPath)
        canvas.drawArc(outerRect, 180f, 180f, true, backgroundPaint)
        canvas.drawArc(outerRect, drawStart, drawSweep, true, fillPaint)
        canvas.restore()
        canvas.drawArc(outerRect, drawStart, drawSweep, false, borderPaint)
        canvas.drawArc(innerRect, drawStart, drawSweep, false, borderPaint)

        canvas.drawCircle(pointerX, pointerY, ringThickness / 2f, fillPaint)
        canvas.drawCircle(pointerX, pointerY, pointerRadius, pointerPaint)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isNightMode()) Color.rgb(0xe9, 0x3e, 0x47) else Color.rgb(0xd7, 0x19, 0x21)
        style = FILL
    }

    private fun isNightMode(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private fun updateRadii() {
        val w = if (width > 0) width.toFloat() else resources.displayMetrics.widthPixels.toFloat()
        val h = if (height > 0) height.toFloat() else (180f * density)
        val maxFromHeight = h - 16f * density
        val maxFromWidth = (w / 2f) - 20f * density
        outerRadius = min(maxFromHeight, maxFromWidth).coerceAtLeast(density * 40f)
        innerRadius = (outerRadius - ringThickness).coerceAtLeast(0f)
    }
}
