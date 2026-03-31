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
    private val background: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.shop_background)

    private val backBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.back)
    private val backBtnRect = RectF()

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

        // 배경 화면 그리기
        drawBackground(canvas)
        drawShop(canvas)
    }

    // 상점 화면 그리기
    private fun drawShop(canvas: Canvas) {
        // back 버튼
        backBtnRect.set(ButtonUtil.getButtonSize(width - 100f, 150f, SizeConstants.SETTING_ICON_WIDTH, SizeConstants.SETTING_ICON_HEIGHT))
        canvas.drawBitmap(backBtn, null, backBtnRect, null)
    }

    // 배경 화면 그리기
    private fun drawBackground(canvas: Canvas) {
        val backgroundRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawBitmap(background, null, backgroundRect, null)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!showing || gameConfig.viewState != ViewState.SHOP || event.action != MotionEvent.ACTION_DOWN) return false

        if(gameConfig.viewState == ViewState.SHOP){
            handleShopTouch(event)
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