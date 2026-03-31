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
    private val background: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.shop_background)
    private val backBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.back)
    private val backBtnRect = RectF()

    // 화면 표시
    fun showView() {
        showing = true
        eventListener.onViewStateToggle(ViewState.SHOP)
        invalidate() // 화면 렌더링
    }

    // 화면 숨기기
    private fun hideView() {
        showing = false
        eventListener.onViewStateToggle(ViewState.MENU) // 메뉴 화면 전환
        invalidate() // 화면 렌더링
    }

    // 팝업 그리기
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (!showing) return

        // 배경 화면 그리기
        drawBackground(canvas)
        // 화면 상태별로 실행
        when (gameConfig.viewState) {
            ViewState.MENU -> {}
            ViewState.PLAY -> {}
            ViewState.SHOP -> drawShop(canvas)
        }
    }

    // 상점 화면 그리기
    private fun drawShop(canvas: Canvas) {
        // back 버튼
        backBtnRect.set(ButtonUtil.getButtonSize(width - 100f, 150f, SizeConstants.SETTING_ICON_WIDTH, SizeConstants.SETTING_ICON_HEIGHT))
        canvas.drawBitmap(backBtn, null, backBtnRect, null)
    }

    // 배경 화면 그리기
    private fun drawBackground(canvas: Canvas) {
        val scale = maxOf(
            width.toFloat() / background.width,
            height.toFloat() / background.height
        )

        val scaledWidth = background.width * scale
        val scaledHeight = background.height * scale

        val left = (width - scaledWidth) / 2
        val top = (height - scaledHeight) / 2

        val backgroundRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
        canvas.drawBitmap(background, null, backgroundRect, null)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!showing || event.action != MotionEvent.ACTION_DOWN) return false

        when (gameConfig.viewState) {
            ViewState.MENU -> {}
            ViewState.PLAY -> {}
            ViewState.SHOP -> handleShopTouch(event)
        }

        return true
    }

    // shop 터치 이벤트
    private fun handleShopTouch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when {
                backBtnRect.contains(event.x, event.y) -> hideView()
            }
        }
    }
}