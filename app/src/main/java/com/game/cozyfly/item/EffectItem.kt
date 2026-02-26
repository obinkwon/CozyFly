package com.game.cozyfly.item;

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

    fun collidesWith(px: Float, py: Float, radius: Float): Boolean {
        val playerRect = RectF(px - radius, py - radius, px + radius, py + radius)
        return RectF.intersects(playerRect, getRect())
    }
}
