package com.game.cozyfly.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.game.cozyfly.R
import com.game.cozyfly.constants.SizeConstants
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.enums.ViewState
import com.game.cozyfly.listener.GameEventListener
import com.game.cozyfly.util.ButtonUtil

@SuppressLint("ViewConstructor")
class ShopView(
    context: Context,
    private val eventListener: GameEventListener,
    private val gameConfig: GameConfig,
) : View(context) {

    // 화면 관련 변수
    private var showing = false
    private var centerX = 0f // 중앙 X좌표 값
    private var centerY = 0f // 중앙 Y좌표 값
    private var startX = 0f
    private val swipeThreshold = 100 // 스와이프 판정 거리(px)
    private val backBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.back)
    private val characterImgs: Array<Array<Bitmap>> = arrayOf(
        arrayOf(
            BitmapFactory.decodeResource(resources, R.drawable.fly1),
            BitmapFactory.decodeResource(resources, R.drawable.fly2)
        ),
        arrayOf(
            BitmapFactory.decodeResource(resources, R.drawable.sprite1),
            BitmapFactory.decodeResource(resources, R.drawable.sprite2)
        )
    )
    // 현재 선택된 스킨
    private var currentSkin = characterImgs[0][0]
    private var isCycling = false
    private var currentCharacterIndex = 0 // 어떤 캐릭터
    private var currentImageIndex = 0     // 그 캐릭터의 몇 번째 이미지

    private val backBtnRect = RectF()
    private val characterRect = RectF()

    // 화면 표시
    fun showView() {
        showing = true
        invalidate() // 화면 렌더링
    }

    // 화면 숨기기
    fun hideView() {
        showing = false
        eventListener.onViewStateToggle(ViewState.MENU) // 메뉴 화면 표시
        invalidate() // 화면 렌더링
    }

    // 팝업 그리기
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (!showing || gameConfig.viewState != ViewState.SHOP) return

        centerX = width / 2f
        centerY = height / 2f
        // 배경 화면 그리기
        drawShop(canvas)
    }

    // 상점 화면 그리기
    private fun drawShop(canvas: Canvas) {
        // back 버튼
        backBtnRect.set(ButtonUtil.getButtonSize(width - 100f, 150f, SizeConstants.SETTING_ICON_WIDTH, SizeConstants.SETTING_ICON_HEIGHT))
        canvas.drawBitmap(backBtn, null, backBtnRect, null)

        // 캐릭터 버튼
        characterRect.set(ButtonUtil.getButtonSize(centerX, centerY, SizeConstants.PLAYER_BIG_WIDTH, SizeConstants.PLAYER_BIG_HEIGHT))
        canvas.drawBitmap(currentSkin, null, characterRect, null)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!showing || gameConfig.viewState != ViewState.SHOP) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
            }

            MotionEvent.ACTION_UP -> {
                val diffX = event.x - startX

                if (kotlin.math.abs(diffX) > swipeThreshold) {
                    handleSwipe(diffX)
                } else {
                    handleShopTouch(event)
                }
            }
        }

        return true
    }

    // shop 터치 이벤트
    private fun handleShopTouch(event: MotionEvent) {
        when {
            backBtnRect.contains(event.x, event.y) -> hideView()
            characterRect.contains(event.x, event.y) -> handleCharacterTouch()
        }
    }

    // 캐릭터 변경
    private fun handleSwipe(diffX: Float) {
        if (characterImgs.size <= 1) return

        // 자동 애니메이션 중이면 정지
        if (isCycling) {
            isCycling = false
            removeCallbacks(runnable)
        }

        if (diffX > 0) {
            // 👉 오른쪽 스와이프 → 이전 캐릭터
            currentCharacterIndex =
                (currentCharacterIndex - 1 + characterImgs.size) % characterImgs.size
        } else {
            // 👉 왼쪽 스와이프 → 다음 캐릭터
            currentCharacterIndex =
                (currentCharacterIndex + 1) % characterImgs.size
        }

        // 👉 캐릭터 바뀌면 이미지 초기화
        currentImageIndex = 0

        currentSkin = characterImgs[currentCharacterIndex][currentImageIndex]
        invalidate()
    }

    // 애니메이션 실행
    private val runnable = object : Runnable {
        override fun run() {
            if (!isCycling) return

            val currentImgs = characterImgs[currentCharacterIndex]

            if (currentImgs.isEmpty()) return

            currentImageIndex = (currentImageIndex + 1) % currentImgs.size
            currentSkin = currentImgs[currentImageIndex]

            invalidate()

            postDelayed(this, 1000)
        }
    }

    // 캐릭터 터치 이벤트
    private fun handleCharacterTouch() {
        if (isCycling) {
            // 멈춤
            isCycling = false
            removeCallbacks(runnable)
        } else {
            // 시작
            isCycling = true
            post(runnable)
        }
    }
}