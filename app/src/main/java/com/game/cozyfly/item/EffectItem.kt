package com.game.cozyfly.item

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import com.game.cozyfly.enums.EffectType

class EffectItem(
    var x: Float,
    var y: Float,
    private val width: Float,
    private val height: Float,
    private val bitmap: Bitmap,
    val type: EffectType
) {

    fun update(speed: Float) {
        x -= speed
    }

    fun draw(canvas: Canvas) {
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawBitmap(bitmap, null, rect, null)
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
