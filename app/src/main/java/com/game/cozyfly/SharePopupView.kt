package com.game.cozyfly

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import com.game.cozyfly.constants.SizeConstants
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.data.TextStyle
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.listener.GameEventListener
import com.game.cozyfly.util.ButtonUtil
import com.game.cozyfly.util.CanvasUtil

@SuppressLint("ViewConstructor")
class SharePopupView(context: Context,
    private val eventListener: GameEventListener,
    private val gameConfig: GameConfig,
) : View(context) {

    private var showing = false
    private val playerImg = BitmapFactory.decodeResource(resources, R.drawable.fly1)
    private val background = BitmapFactory.decodeResource(resources, R.drawable.popup_background)
    private val scoreBtn = BitmapFactory.decodeResource(resources, R.drawable.score)
    private val closeBtn = BitmapFactory.decodeResource(resources, R.drawable.close)
    private val playerRect = RectF()
    private var closeBtnRect = RectF()
    private val popupRect = RectF()
    private val scoreBtnRect = RectF()
    private val bgmButtonRect = RectF()
    private val scoreTextRect = RectF()

    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private var bestScore = prefs.getInt("BEST_SCORE", 0)

    // 팝업 표시
    fun showPopup() {
        showing = true
        invalidate() // 화면 렌더링
    }

    // 팝업 닫기
    private fun closePopup() {
        showing = false
        eventListener.onGameStateToggle(GameState.GAMEOVER) // 게임 상태 전환
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
        if (!showing || event.action != MotionEvent.ACTION_DOWN) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 닫기 버튼 클릭
                if (closeBtnRect.contains(event.x, event.y)) {
                    closePopup()
                }
                // BGM 버튼 클릭
                else if (bgmButtonRect.contains(event.x, event.y)) {
                    eventListener.onBgmToggle()
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
        popupRect.set(ButtonUtil.getButtonSize(width / 2f, height / 2f, width * 0.8f, height * 0.7f))
        // 모서리 라운드 처리
        val path = Path().apply {
            addRoundRect(popupRect, 40f, 40f, Path.Direction.CW)
        }
        // 클리핑
        canvas.withClip(path) {
            canvas.drawBitmap(background, null, popupRect, null)
        }
    }

    // 팝업 버튼 그리기
    private fun drawBtn(canvas: Canvas) {
        // 플레이어 그리기
        playerRect.set(ButtonUtil.getButtonSize(width / 2f, height / 2f, SizeConstants.PLAYER_WIDTH, SizeConstants.PLAYER_HEIGHT))
        canvas.drawBitmap(playerImg, null, playerRect, null)

        // 닫기 버튼
        closeBtnRect.set(ButtonUtil.getButtonSize(popupRect.right - 50f, popupRect.top + 50f, SizeConstants.SMALL_BTN_WIDTH, SizeConstants.SMALL_BTN_HEIGHT))
        canvas.drawBitmap(closeBtn, null, closeBtnRect, null)

        // score 버튼 그리기
        scoreBtnRect.set(ButtonUtil.getButtonSize(width / 2f, popupRect.top + 200f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.BIG_BTN_HEIGHT))
        canvas.drawBitmap(scoreBtn, null, scoreBtnRect, null)

        // 점수 텍스트 그리기
        scoreTextRect.set(ButtonUtil.getButtonSize(width / 2f, popupRect.top + 100f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.BIG_BTN_HEIGHT))
        // 최고 점수 표시
        CanvasUtil.drawText(canvas, "Best: $bestScore", width / 2f - 180f, height / 2f - 150f, TextStyle(100f, Color.BLACK))
    }
}