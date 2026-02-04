package com.game.cozyfly.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.game.cozyfly.data.TextStyle
import com.game.cozyfly.enums.ClickMode

// 캔버스 관련 Util
object CanvasUtil {
    private val paint = Paint()

    private val buttonPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val buttonBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        style: TextStyle
    ) {
        paint.textSize = style.size
        paint.color = style.color
        paint.textAlign = style.align
        canvas.drawText(text, x, y, paint)
    }

    fun drawButton(
        canvas: Canvas,
        clickMode: ClickMode,
        rect: RectF
    ) {
        // 버튼 배경
        canvas.drawRoundRect(rect, 25f, 25f, buttonPaint)
        // 테두리
        canvas.drawRoundRect(rect, 25f, 25f, buttonBorderPaint)

        // 텍스트 중앙 정렬 계산
        val centerX = rect.centerX()
        val centerY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2

        canvas.drawText(clickMode.desc, centerX, centerY, textPaint)
    }
}