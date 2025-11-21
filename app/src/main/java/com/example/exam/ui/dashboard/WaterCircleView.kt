package com.example.exam.ui.dashboard

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.sin

class WaterCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B3E5FC")
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4DD0E1")
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4DD0E1")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A237E")
        textSize = 80f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var percentage = 0
    private var animatedPercentage = 0f
    private var waveOffset = 0f
    private val wavePath = Path()
    private val clipPath = Path()

    init {
        startWaveAnimation()
    }

    fun setPercentage(value: Int) {
        percentage = value.coerceIn(0, 100)
        animateToPercentage()
    }

    private fun animateToPercentage() {
        ValueAnimator.ofFloat(animatedPercentage, percentage.toFloat()).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animatedPercentage = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun startWaveAnimation() {
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                waveOffset = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) * 0.9f

        clipPath.reset()
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        canvas.clipPath(clipPath)

        canvas.drawCircle(centerX, centerY, radius, circlePaint)
        canvas.drawCircle(centerX, centerY, radius, strokePaint)

        val waterLevel = centerY + radius - (animatedPercentage / 100f * radius * 2)
        
        wavePath.reset()
        wavePath.moveTo(0f, waterLevel)

        val waveHeight = 15f
        val waveLength = width / 2f

        for (x in 0..width step 5) {
            val y = waterLevel + sin((x / waveLength + waveOffset / 60f) * Math.PI * 2).toFloat() * waveHeight
            wavePath.lineTo(x.toFloat(), y)
        }

        wavePath.lineTo(width.toFloat(), height.toFloat())
        wavePath.lineTo(0f, height.toFloat())
        wavePath.close()

        canvas.drawPath(wavePath, waterPaint)

        canvas.drawText("${animatedPercentage.toInt()}%", centerX, centerY + textPaint.textSize / 3, textPaint)
    }
}
