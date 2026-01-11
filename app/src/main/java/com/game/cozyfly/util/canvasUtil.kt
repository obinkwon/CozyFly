package com.game.cozyfly.util

import android.graphics.Canvas
import android.graphics.Paint
import com.game.cozyfly.data.TextStyle

// canvas object
object CanvasUtil {
    private val paint = Paint()

    fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        style: TextStyle
    ) {
        paint.textSize = style.size
        paint.color = style.color
        canvas.drawText(text, x, y, paint)
    }
}