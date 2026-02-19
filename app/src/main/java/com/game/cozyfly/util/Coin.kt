package com.game.cozyfly.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class Coin(
    startX: Float,
    startY: Float,
    private val width: Float,
    private val height: Float,
    private val frames: Array<Bitmap>
) {

    var x = startX
    var y = startY

    private var frameIndex = 0
    private var frameCounter = 0
    private val frameChangeInterval = 3 // 60fps 기준 20fps

    private val rect = RectF()

    fun update(scrollSpeed: Float) {
        // 왼쪽으로 이동 (장애물처럼)
        x -= scrollSpeed

        // 애니메이션
        frameCounter++
        if (frameCounter >= frameChangeInterval) {
            frameIndex = (frameIndex + 1) % frames.size
            frameCounter = 0
        }
    }

    fun draw(canvas: Canvas) {
        rect.set(x, y, x + width, y + height)
        canvas.drawBitmap(frames[frameIndex], null, rect, null)
    }

    fun isOffScreen(): Boolean {
        return x + width < 0
    }

    fun collidesWith(playerX: Float, playerY: Float, playerRadius: Float): Boolean {
        val coinRect = RectF(x, y, x + width, y + height)

        val closestX = playerX.coerceIn(coinRect.left, coinRect.right)
        val closestY = playerY.coerceIn(coinRect.top, coinRect.bottom)

        val dx = playerX - closestX
        val dy = playerY - closestY

        return dx * dx + dy * dy < playerRadius * playerRadius
    }

    fun getRect(): RectF {
        return RectF(x, y, x + width, y + height)
    }

}
