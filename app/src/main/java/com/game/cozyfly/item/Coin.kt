package com.game.cozyfly.item

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
    private val frameChangeInterval = 6 // 60fps 기준 10fps

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

    fun getRect(): RectF {
        return RectF(x, y, x + width, y + height)
    }

    fun isOffScreen(): Boolean {
        return x + width < 0
    }

    fun collidesWith(playerX: Float, playerY: Float, playerRadius: Float): Boolean {
        // 사각형 영역 (코인 또는 장애물)
        val rect = RectF(x, y, x + width, y + height)

        // 원의 중심에서 사각형 안의 가장 가까운 점 찾기
        val closestX = playerX.coerceIn(rect.left, rect.right)
        val closestY = playerY.coerceIn(rect.top, rect.bottom)

        // 거리 계산
        val dx = playerX - closestX
        val dy = playerY - closestY

        // 거리² <= 반지름² 이면 충돌
        return dx * dx + dy * dy <= playerRadius * playerRadius
    }
}