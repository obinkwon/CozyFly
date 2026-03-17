package com.game.cozyfly.view

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import com.game.cozyfly.R
import com.game.cozyfly.constants.SizeConstants
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.listener.GameEventListener
import com.game.cozyfly.ui.share.ShareImageRenderer
import com.game.cozyfly.util.ButtonUtil
import kotlin.text.iterator

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
    private val saveBtn = BitmapFactory.decodeResource(resources, R.drawable.save)
    private val numberTexts = arrayOf(
        BitmapFactory.decodeResource(resources, R.drawable.num_0),
        BitmapFactory.decodeResource(resources, R.drawable.num_1),
        BitmapFactory.decodeResource(resources, R.drawable.num_2),
        BitmapFactory.decodeResource(resources, R.drawable.num_3),
        BitmapFactory.decodeResource(resources, R.drawable.num_4),
        BitmapFactory.decodeResource(resources, R.drawable.num_5),
        BitmapFactory.decodeResource(resources, R.drawable.num_6),
        BitmapFactory.decodeResource(resources, R.drawable.num_7),
        BitmapFactory.decodeResource(resources, R.drawable.num_8),
        BitmapFactory.decodeResource(resources, R.drawable.num_9)
    )
    private val playerRect = RectF()
    private var closeBtnRect = RectF()
    private val popupRect = RectF()
    private val scoreBtnRect = RectF()
    private val scoreTextRect = RectF()
    private val saveBtnRect = RectF()

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
                // 저장 버튼 클릭
                else if (saveBtnRect.contains(event.x, event.y)) {
                    val renderer = ShareImageRenderer(context)
                    val bitmap = renderer.render(gameConfig.bestScore)
                    saveBitmapToGallery(bitmap)
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
        // 닫기 버튼 그리기
        closeBtnRect.set(ButtonUtil.getButtonSize(popupRect.right - 50f, popupRect.top + 50f, SizeConstants.SMALL_BTN_WIDTH, SizeConstants.SMALL_BTN_HEIGHT))
        canvas.drawBitmap(closeBtn, null, closeBtnRect, null)

        // score 버튼 그리기
        scoreBtnRect.set(ButtonUtil.getButtonSize(width / 2f, popupRect.top + 100f, SizeConstants.MIDDLE_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        canvas.drawBitmap(scoreBtn, null, scoreBtnRect, null)

        // 점수 텍스트 그리기
        scoreTextRect.set(ButtonUtil.getButtonSize(width / 2f, scoreBtnRect.bottom + 150f, SizeConstants.SMALL_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        // 최고 점수 표시
        drawScore(canvas, gameConfig.bestScore, scoreTextRect)

        // 플레이어 그리기
        playerRect.set(ButtonUtil.getButtonSize(width / 2f, height / 2f + 100f, SizeConstants.PLAYER_WIDTH, SizeConstants.PLAYER_HEIGHT))
        canvas.drawBitmap(playerImg, null, playerRect, null)

        // 저장 버튼 그리기
        saveBtnRect.set(ButtonUtil.getButtonSize(width / 2f, popupRect.bottom - 100f, SizeConstants.MIDDLE_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        canvas.drawBitmap(saveBtn, null, saveBtnRect, null)
    }

    // 점수 그리기
    private fun drawScore(canvas: Canvas, score: Int, rect: RectF) {
        val scoreStr = score.toString()
        val digitWidth = rect.width()
        val digitHeight = rect.height()

        // 전체 점수 폭
        val totalWidth = digitWidth * scoreStr.length

        // 중앙 정렬
        var x = rect.centerX() - totalWidth / 2
        val y = rect.top

        for (ch in scoreStr) {
            val num = ch - '0'
            val bmp = numberTexts[num]

            val dst = RectF(
                x,
                y,
                x + digitWidth,
                y + digitHeight
            )

            canvas.drawBitmap(bmp, null, dst, null)
            x += digitWidth
        }
    }

    // 화면 캡쳐
    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "share_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CozyFly")
        }

        val imageUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        imageUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            Toast.makeText(context, "이미지가 저장되었습니다", Toast.LENGTH_SHORT).show()
        }
    }



}