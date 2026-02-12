package com.game.cozyfly.ui.share

import android.content.Context
import android.graphics.*
import com.game.cozyfly.R
import kotlin.text.iterator
import androidx.core.graphics.createBitmap
import com.game.cozyfly.constants.SizeConstants
import com.game.cozyfly.util.ButtonUtil

// 공유 이미지 렌더링
class ShareImageRenderer(context: Context) {

    private val background = BitmapFactory.decodeResource(context.resources, R.drawable.popup_background)
    private val playerImg = BitmapFactory.decodeResource(context.resources, R.drawable.fly1)
    private val scoreBtn = BitmapFactory.decodeResource(context.resources, R.drawable.score)

    private val numberTexts = arrayOf(
        BitmapFactory.decodeResource(context.resources, R.drawable.num_0),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_1),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_2),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_3),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_4),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_5),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_6),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_7),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_8),
        BitmapFactory.decodeResource(context.resources, R.drawable.num_9)
    )

    private val playerRect = RectF()
    private val scoreBtnRect = RectF()
    private val scoreTextRect = RectF()
    private val backgroundRect = RectF()

    // 공유 이미지 생성
    fun render(bestScore: Int): Bitmap {
        // 해상도
        val width = 1080
        val height = 1350

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        // 배경 그리기
        backgroundRect.set(ButtonUtil.getButtonSize(width / 2f, height / 2f, width.toFloat(), height.toFloat()))
        canvas.drawBitmap(background, null, backgroundRect, null)

        // SCORE 버튼 그리기
        scoreBtnRect.set(ButtonUtil.getButtonSize(width / 2f, 100f, SizeConstants.MIDDLE_BTN_WIDTH, SizeConstants.MIDDLE_BTN_HEIGHT))
        canvas.drawBitmap(scoreBtn, null, scoreBtnRect, null)

        // 점수 텍스트 그리기
        scoreTextRect.set(ButtonUtil.getButtonSize(width / 2f, scoreBtnRect.bottom + 150f, SizeConstants.SMALL_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        drawScore(canvas, bestScore, scoreTextRect)

        // 플레이어 그리기
        playerRect.set(ButtonUtil.getButtonSize(width / 2f, height / 2f, SizeConstants.PLAYER_BIG_WIDTH, SizeConstants.PLAYER_BIG_HEIGHT))
        canvas.drawBitmap(playerImg, null, playerRect, null)

        return bitmap
    }

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
}
