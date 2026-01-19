package com.game.cozyfly

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt

@SuppressLint("ViewConstructor")
class PopupView(context: Context, val gameView: GameView) : View(context) {

    var showing = false

    private val bgPaint = Paint().apply {
        color = "#AA000000".toColorInt() // 반투명 배경
    }

    private val boxPaint = Paint().apply {
        color = Color.WHITE
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 50f
        textAlign = Paint.Align.CENTER
    }

    // 팝업 표시
    fun showPopup() {
        showing = true
        invalidate() // 화면 렌더링
    }

    // 팝업 닫기
    fun closePopup() {
        showing = false
        gameView.gameResume()
        invalidate() // 화면 렌더링
    }

    // 팝업 그리기
    override fun onDraw(canvas: Canvas) {
        if (!showing) return

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val boxW = width * 0.6f
        val boxH = height * 0.3f
        val left = (width - boxW) / 2
        val top = (height - boxH) / 2

        canvas.drawRoundRect(left, top, left+boxW, top+boxH, 40f, 40f, boxPaint)
        canvas.drawText("일시정지 상태", width/2f, (top + boxH/2), textPaint)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!showing) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            closePopup()
        }
        return true // ★ 여기 매우 중요!
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return if (showing) true else super.dispatchTouchEvent(event)
    }
}