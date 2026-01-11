package com.game.cozyfly

import android.graphics.Canvas
import android.graphics.*

// 장애물 Class
class Obstacle(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    private val bitmap: Bitmap
) {
    fun update(speed: Float) {
        x -= speed   // 오른쪽 → 왼쪽 이동
    }

    fun draw(canvas: Canvas) {
        val destRect = RectF(
            x,
            y,
            x + width,
            y + height
        )
        canvas.drawBitmap(bitmap, null, destRect, null)
    }

    fun isOffScreen(): Boolean {
        return x + width < 0
    }

    fun collidesWith(
        playerX: Float,
        playerY: Float,
        radius: Float
    ): Boolean {

        // 원 중심에서 사각형까지의 가장 가까운 점
        val closestX = playerX.coerceIn(x, x + width)
        val closestY = playerY.coerceIn(y, y + height)

        val dx = playerX - closestX
        val dy = playerY - closestY

        return dx * dx + dy * dy < radius * radius
    }
}