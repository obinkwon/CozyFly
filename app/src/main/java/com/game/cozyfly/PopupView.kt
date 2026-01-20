package com.game.cozyfly

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip

@SuppressLint("ViewConstructor")
class PopupView(context: Context, val gameView: GameView) : View(context) {

    private var showing = false
    private var closeRect = RectF()
    private val background = BitmapFactory.decodeResource(resources, R.drawable.popup_background)

    private val bgPaint = Paint().apply {
        color = "#AA000000".toColorInt() // 반투명 배경
    }

    // 팝업 표시
    fun showPopup() {
        showing = true
        invalidate() // 화면 렌더링
    }

    // 팝업 닫기
    private fun closePopup() {
        showing = false
        gameView.gameResume()
        invalidate() // 화면 렌더링
    }

    // 팝업 그리기
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (!showing) return

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val boxW = width * 0.8f
        val boxH = height * 0.7f
        val left = (width - boxW) / 2
        val top = (height - boxH) / 2

        // 배경 이미지 전체를 View 크기로 맞춰서 그리기
        val dstRect = RectF(left, top, left + boxW, top + boxH)
        val path = android.graphics.Path().apply {
            addRoundRect(dstRect, 40f, 40f, android.graphics.Path.Direction.CW)
        }
        // 클리핑
        canvas.withClip(path) {
            canvas.drawBitmap(background, null, dstRect, null)
        }

        // --- X 버튼 영역 계산 ---
        val btnSize = 70f
        closeRect.set(
            dstRect.right - btnSize - 20f,
            dstRect.top + 20f,
            dstRect.right - 20f,
            dstRect.top + btnSize + 20f
        )

        // --- X 버튼 그리기 ---
        val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 8f
        }

        canvas.drawLine(closeRect.left, closeRect.top, closeRect.right, closeRect.bottom, xPaint)
        canvas.drawLine(closeRect.right, closeRect.top, closeRect.left, closeRect.bottom, xPaint)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!showing) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // X 버튼 눌림 체크
                if (closeRect.contains(event.x, event.y)) {
                    closePopup()
                    return true
                }
            }
        }
        return true
    }
}