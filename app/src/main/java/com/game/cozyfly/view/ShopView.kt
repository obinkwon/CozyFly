package com.game.cozyfly.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import com.game.cozyfly.R
import com.game.cozyfly.constants.CharacterImgConstants.characterResIds
import com.game.cozyfly.constants.SizeConstants
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.enums.SkinType
import com.game.cozyfly.enums.ViewState
import com.game.cozyfly.listener.GameEventListener
import com.game.cozyfly.util.ButtonUtil
import com.game.cozyfly.util.CanvasUtil

@SuppressLint("ViewConstructor")
class ShopView(
    context: Context,
    private val eventListener: GameEventListener,
    private val gameConfig: GameConfig,
) : View(context) {

    // 화면 관련 변수
    private var showing = false
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private var centerX = 0f // 중앙 X좌표 값
    private var centerY = 0f // 중앙 Y좌표 값
    private val backBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.back)
    private val characterImgs = characterResIds.map { row ->
        row.map { resId ->
            BitmapFactory.decodeResource(resources, resId)
        }.toTypedArray()
    }.toTypedArray()
    private val characterSkins: Array<SkinType> = SkinType.entries.toTypedArray()
    private val leftBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.left)
    private val rightBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.right)
    // 현재 선택된 스킨
    private var currentCharacterIndex = gameConfig.selectSkin.ordinal // 어떤 캐릭터
    private var currentSkin = characterImgs[gameConfig.selectSkin.ordinal][0]
    private var currentName = characterImgs[gameConfig.selectSkin.ordinal][2]
    private var currentSkinType = gameConfig.selectSkin
    private var currentSkinPrice = characterSkins[gameConfig.selectSkin.ordinal].price
    private var isCycling = false
    private var currentImageIndex = 0 // 그 캐릭터의 몇 번째 이미지

    private val backBtnRect = RectF()
    private val characterRect = RectF()
    private val characterNameRect = RectF()
    private val leftBtnRect = RectF()
    private val rightBtnRect = RectF()
    private val purchaseButtonRect = RectF()

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
        characterRect.set(ButtonUtil.getButtonSize(centerX, centerY - 200f, SizeConstants.PLAYER_BIG_WIDTH, SizeConstants.PLAYER_BIG_HEIGHT))
        canvas.drawBitmap(currentSkin, null, characterRect, null)

        // 왼쪽 방향표 버튼
        leftBtnRect.set(ButtonUtil.getButtonSize(100f, centerY, SizeConstants.SETTING_ICON_WIDTH, SizeConstants.SETTING_ICON_HEIGHT))
        canvas.drawBitmap(leftBtn, null, leftBtnRect, null)

        // 오른쪽 방향표 버튼
        rightBtnRect.set(ButtonUtil.getButtonSize(width - 100f, centerY, SizeConstants.SETTING_ICON_WIDTH, SizeConstants.SETTING_ICON_HEIGHT))
        canvas.drawBitmap(rightBtn, null, rightBtnRect, null)

        // 현재 캐릭터 이름
        characterNameRect.set(ButtonUtil.getButtonSize(centerX, characterRect.bottom + 200f, SizeConstants.START_BTN_WIDTH, SizeConstants.SETTING_BTN_HEIGHT))
        canvas.drawBitmap(currentName, null, characterNameRect, null)

        // 모드 버튼
        purchaseButtonRect.set(ButtonUtil.getButtonSize(centerX, characterNameRect.bottom + 200f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, getSkinClickMode(currentSkinType), purchaseButtonRect, getSkinClickState(currentSkinType))
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!showing || gameConfig.viewState != ViewState.SHOP) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            handleShopTouch(event)
        }

        return true
    }

    // shop 터치 이벤트
    private fun handleShopTouch(event: MotionEvent) {
        when {
            backBtnRect.contains(event.x, event.y) -> hideView()
            characterRect.contains(event.x, event.y) -> handleCharacterTouch()
            leftBtnRect.contains(event.x, event.y) -> handleCharacterChange("L")
            rightBtnRect.contains(event.x, event.y) -> handleCharacterChange("R")
            purchaseButtonRect.contains(event.x, event.y) -> handlePurchaseClick()
        }
    }

    // 캐릭터 변경
    private fun handleCharacterChange(direction: String) {
        if (characterImgs.size <= 1) return

        isCycling = false
        // 왼쪽 클릭
        if (direction == "L") {
            currentCharacterIndex = (currentCharacterIndex + 1) % characterImgs.size
        }
        // 오른쪽 클릭
        else {
            currentCharacterIndex = (currentCharacterIndex - 1 + characterImgs.size) % characterImgs.size
        }

        currentSkin = characterImgs[currentCharacterIndex][0]
        currentName = characterImgs[currentCharacterIndex][2]
        currentSkinType = characterSkins[currentCharacterIndex]
        currentSkinPrice = characterSkins[currentCharacterIndex].price
        invalidate()
    }

    // 애니메이션 실행
    private val runnable = object : Runnable {
        override fun run() {
            if (!isCycling) return

            val currentImgs = characterImgs[currentCharacterIndex]

            if (currentImgs.isEmpty()) return

            currentImageIndex = if (currentImageIndex == 0) 1 else 0
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
    
    // 해당 스킨 버튼 텍스트 조회
    private fun getSkinClickMode(skinType: SkinType): String {
        // 구매한 스킨일 경우
        if(gameConfig.purchasedSkins.contains(skinType)) {
            // 사용중인 스킨일 경우
            if(gameConfig.selectSkin == skinType){
                return ClickMode.SELECT_Y.desc
            } else {
                return ClickMode.SELECT_N.desc
            }
        }

        return "$currentSkinPrice Coin"
    }

    // 해당 스킨 버튼 상태 조회
    private fun getSkinClickState(skinType: SkinType): Boolean {
        // 구매한 스킨일 경우
        if(gameConfig.purchasedSkins.contains(skinType)) {
            // 사용중인 스킨일 경우
            if(gameConfig.selectSkin == skinType){
                return true
            } else {
                return false
            }
        }

        return false
    }

    // 스킨 구매
    private fun handlePurchaseClick() {
        // 구매한 스킨일 경우
        if(gameConfig.purchasedSkins.contains(currentSkinType)) {
            // 사용중인 스킨이 아닐 경우
            if(gameConfig.selectSkin != currentSkinType){
                // 스킨 교체
                gameConfig.selectSkin = currentSkinType
                prefs.edit { putString("SELECT_SKIN", currentSkinType.name) }
            }
        }
        // 구매 안했을때
        else {
            // 코인 충분한지 확인
            if (gameConfig.coinScore >= currentSkinPrice) {
                // 코인 차감
                gameConfig.coinScore -= currentSkinPrice
                prefs.edit { putInt("COIN_SCORE", gameConfig.coinScore) }

                // 구매 목록에 추가
                gameConfig.purchasedSkins.add(currentSkinType)
                prefs.edit { putStringSet("PURCHASE_SKIN", gameConfig.purchasedSkins.map { it.name }.toSet()) }

                // 스킨 사용
                gameConfig.selectSkin = currentSkinType
            } else {
                // 코인 부족 처리 (토스트 등)
                Toast.makeText(context, "코인이 부족합니다", Toast.LENGTH_SHORT).show()
            }
        }

        invalidate()
    }
}