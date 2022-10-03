package com.example.uroflowmetryapp.activity

import android.graphics.*
import android.graphics.drawable.Drawable


class ColorBarDrawable(private val themeColors: List<String>) : Drawable() {
    override fun draw(canvas: Canvas) {

        // get drawable dimensions
        val bounds: Rect = bounds
        val width: Int = bounds.right - bounds.left
        val height: Int = bounds.bottom - bounds.top

        // draw background gradient
        val backgroundPaint = Paint()
        val barWidth = width / themeColors.size
        val barWidthRemainder = width % themeColors.size
        for (i in themeColors.indices) {
            backgroundPaint.setColor(Color.parseColor(themeColors[i]))
            canvas.drawRect((i * barWidth).toFloat(),
                (height.toFloat()*2)/6, ((i + 1) * barWidth).toFloat(), (height.toFloat()*4)/6, backgroundPaint)
        }

        // draw remainder, if exists
        if (barWidthRemainder > 0) {
            canvas.drawRect(
                (themeColors.size * barWidth).toFloat(),
                (height.toFloat()*2)/6,
                (themeColors.size * barWidth + barWidthRemainder).toFloat(),
                (height.toFloat()*4)/6,
                backgroundPaint
            )
        }
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }
}