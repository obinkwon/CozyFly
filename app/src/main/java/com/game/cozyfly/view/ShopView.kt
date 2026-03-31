package com.game.cozyfly.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
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
) : SurfaceView(context), Runnable, SurfaceHolder.Callback {

    // 쓰레드 변수
    private var shopThread = Thread(this)
    private var running = false

    // 배경 관련 변수
    private val background: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.shop_background)

    // 게임 관련 변수
    private var centerX = 0f // 중앙 X좌표 값
    private var centerY = 0f // 중앙 Y좌표 값
    private val backBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.back)
    private val backBtnRect = RectF()

    // 초기 설정
    init {
        holder.addCallback(this)
    }

    // surfaceView 변경
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 필요 없으면 비워둬도 OK
    }

    // surfaceView 생성
    override fun surfaceCreated(holder: SurfaceHolder) {
        play()
    }

    // surfaceView 제거
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    fun pause() {
        running = false
        try {
            shopThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun play() {
        if (running) return

        // 중앙 위치 계산
        centerX = width / 2f
        centerY = height / 2f
        // 쓰레드 실행
        running = true
        shopThread = Thread(this)
        shopThread.start()
    }

    // 실행
    override fun run() {
        while (running) {
            if (!holder.surface.isValid) continue

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                synchronized(holder) {
                    update() // 게임 동작 업데이트
                    drawCanvas(canvas) // canvas를 전달
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    // 상점 동작 업데이트
    private fun update() {
    }

    // 화면 상태별로 캔버스에 요소 그리기
    private fun drawCanvas(canvas: Canvas) {
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
                backBtnRect.contains(event.x, event.y) -> eventListener.onViewStateToggle(ViewState.MENU)
            }
        }
    }
}