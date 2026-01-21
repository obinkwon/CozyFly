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
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import com.game.cozyfly.constants.SizeConstants
import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.enums.ViewState
import com.game.cozyfly.util.ButtonUtil
import com.game.cozyfly.util.CanvasUtil

@SuppressLint("ViewConstructor")
class PopupView(context: Context, val gameView: GameView) : View(context) {

    private var btnW = 500f // 버튼 가로
    private var btnH = 300f // 버튼 세로
    private var showing = false
    private val background = BitmapFactory.decodeResource(resources, R.drawable.popup_background)
    private val settingBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.setting)
    private val settingsTextRect = RectF()
    private var closeBtnRect = RectF()
    private val popupRect = RectF()
    private val modeButtonRect = RectF()
    private val bgmButtonRect = RectF()
    private var bgmState = gameView.bgmState
    private var clickMode = gameView.clickMode

    // 팝업 표시
    fun showPopup() {
        showing = true
        invalidate() // 화면 렌더링
    }

    // 팝업 닫기
    private fun closePopup() {
        showing = false
        gameView.gameResume()
        gameView.holding = false
        gameView.popupYn = false
        invalidate() // 화면 렌더링
    }

    // 팝업 그리기
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (!showing) return

        // 팝업 배경 그리기
        drawBackground(canvas)
        // 팝업 버튼 그리기
        drawBtn(canvas)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!showing) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 닫기 버튼 클릭
                if (closeBtnRect.contains(event.x, event.y)) {
                    closePopup()
                    return true
                }
                // 모드 버튼 클릭
                else if (modeButtonRect.contains(event.x, event.y)) {
                    if(clickMode == ClickMode.CLICK_TAP) {
                        gameView.clickMode = ClickMode.CLICK_HOLD
                        clickMode = ClickMode.CLICK_HOLD
                    } else {
                        gameView.clickMode = ClickMode.CLICK_TAP
                        clickMode = ClickMode.CLICK_TAP
                    }
                    gameView.prefs.edit { putString("CLICK_MODE", clickMode.type) }
                    invalidate() // 화면 렌더링
                }
                // BGM 버튼 클릭
                else if (bgmButtonRect.contains(event.x, event.y)) {
                    bgmState = if(bgmState == ClickMode.BGM_ON) ClickMode.BGM_OFF else ClickMode.BGM_ON
                    if(bgmState == ClickMode.BGM_ON) gameView.bgmListener.onBgmOn() else gameView.bgmListener.onBgmOff()
                    gameView.prefs.edit { putString("BGM_MODE", bgmState.type) }
                    invalidate() // 화면 렌더링
                }
            }
        }
        return true
    }
    
    // 팝업 배경 그리기
    private fun drawBackground(canvas: Canvas) {
        // 반투명 배경
        val bgPaint = Paint().apply {
            color = "#AA000000".toColorInt()
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 배경 이미지 전체를 View 크기로 맞춰서 그리기
        popupRect.set(ButtonUtil.getButtonSize(width.toFloat() / 2, height.toFloat() / 2, width * 0.8f, height * 0.7f))
        // 모서리 라운드 처리
        val path = android.graphics.Path().apply {
            addRoundRect(popupRect, 40f, 40f, android.graphics.Path.Direction.CW)
        }
        // 클리핑
        canvas.withClip(path) {
            canvas.drawBitmap(background, null, popupRect, null)
        }
    }

    // 팝업 버튼 그리기
    private fun drawBtn(canvas: Canvas) {
        // 세팅 글자
        settingsTextRect.set(ButtonUtil.getButtonSize(width.toFloat() / 2, popupRect.top + 100f, SizeConstants.SETTING_BTN_WIDTH, SizeConstants.SETTING_BTN_HEIGHT))
        canvas.drawBitmap(settingBtn, null, settingsTextRect, null)

        // 닫기 버튼 영역 계산
        val btnSize = 70f
        closeBtnRect.set(
            popupRect.right - btnSize - 20f,
            popupRect.top + 20f,
            popupRect.right - 20f,
            popupRect.top + btnSize + 20f
        )
        // 닫기 버튼 그리기
        val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 8f
        }
        canvas.drawLine(closeBtnRect.left, closeBtnRect.top, closeBtnRect.right, closeBtnRect.bottom, xPaint)
        canvas.drawLine(closeBtnRect.right, closeBtnRect.top, closeBtnRect.left, closeBtnRect.bottom, xPaint)

        // 모드 버튼
        modeButtonRect.set(ButtonUtil.getButtonSize(width.toFloat() / 2, height.toFloat() / 2, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, clickMode, modeButtonRect)
        // 배경음 조절 버튼
        bgmButtonRect.set(ButtonUtil.getButtonSize(width.toFloat() / 2, modeButtonRect.top + 200f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, bgmState, bgmButtonRect)
    }
}