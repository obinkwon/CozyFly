package com.game.cozyfly.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import com.game.cozyfly.R
import com.game.cozyfly.constants.SizeConstants
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.listener.GameEventListener
import com.game.cozyfly.util.ButtonUtil
import com.game.cozyfly.util.CanvasUtil


@SuppressLint("ViewConstructor")
class SettingPopupView(
    context: Context,
    private val eventListener: GameEventListener,
    private val gameConfig: GameConfig
) : View(context) {

    // 화면 관련 변수
    private var showing = false
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private val background = BitmapFactory.decodeResource(resources, R.drawable.popup_background)
    private val settingBtn = BitmapFactory.decodeResource(resources, R.drawable.setting)
    private val closeBtn = BitmapFactory.decodeResource(resources, R.drawable.close)
    private val settingsTextRect = RectF()
    private var closeBtnRect = RectF()
    private val popupRect = RectF()
    private val modeButtonRect = RectF()
    private val bgmButtonRect = RectF()
    private val gmsLoginButtonRect = RectF()
    private val gmsScroeButtonRect = RectF()

    // 팝업 표시
    fun showPopup() {
        showing = true
        invalidate() // 화면 렌더링
    }

    // 팝업 닫기
    private fun closePopup() {
        showing = false
        eventListener.onGameStateToggle(GameState.PLAY) // 게임 상태 전환
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

        when {
            // 닫기 버튼 클릭
            closeBtnRect.contains(event.x, event.y) -> closePopup()
            // 모드 버튼 클릭
            modeButtonRect.contains(event.x, event.y) -> {
                eventListener.onClickModeToggle()
                invalidate() // 화면 렌더링
            }
            // BGM 버튼 클릭
            bgmButtonRect.contains(event.x, event.y) -> {
                eventListener.onBgmToggle()
                invalidate() // 화면 렌더링
            }
            // 리더보드 열기 클릭
            gmsLoginButtonRect.contains(event.x, event.y) -> eventListener.showLeaderboard()
            // 리더보드 점수 불러오기 클릭
            gmsScroeButtonRect.contains(event.x, event.y) -> {
                // 리더보드 열기
                eventListener.getLeaderboardScore() { score ->
                    // 사용
                    val finalScore = score.coerceAtLeast(0)
                    if(finalScore > 0){
                        if(gameConfig.bestScore < finalScore){
                            prefs.edit { putInt("BEST_SCORE", finalScore) }
                            gameConfig.bestScore = finalScore
                        }
                        Toast.makeText(context, "점수를 불러 왔습니다", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "불러올 점수가 없습니다", Toast.LENGTH_SHORT).show()
                    }
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
        // 세팅 글자
        settingsTextRect.set(ButtonUtil.getButtonSize(width.toFloat() / 2, popupRect.top + 100f, SizeConstants.SETTING_BTN_WIDTH, SizeConstants.SETTING_BTN_HEIGHT))
        canvas.drawBitmap(settingBtn, null, settingsTextRect, null)

        // 닫기 버튼
        closeBtnRect.set(ButtonUtil.getButtonSize(popupRect.right - 50f, popupRect.top + 50f, SizeConstants.SMALL_BTN_WIDTH, SizeConstants.SMALL_BTN_HEIGHT))
        canvas.drawBitmap(closeBtn, null, closeBtnRect, null)

        // 모드 버튼
        modeButtonRect.set(ButtonUtil.getButtonSize(width / 2f, height / 2f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, gameConfig.clickMode, modeButtonRect)

        // 배경음 조절 버튼
        bgmButtonRect.set(ButtonUtil.getButtonSize(width / 2f, modeButtonRect.top + 200f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, gameConfig.bgmState, bgmButtonRect)

        // 리더보드 열기 버튼
        gmsLoginButtonRect.set(ButtonUtil.getButtonSize(width / 2f, bgmButtonRect.top + 200f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, "LEADERBOARD", gmsLoginButtonRect)

        // 리더보드 점수 불러오기 버튼
        gmsScroeButtonRect.set(ButtonUtil.getButtonSize(width / 2f, gmsLoginButtonRect.top + 200f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, "LOAD SCORE", gmsScroeButtonRect)
    }
}