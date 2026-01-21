package com.game.cozyfly.util

import android.graphics.RectF

// 버튼 관련 Util
object ButtonUtil {

    private val rectF = RectF()

    // 버튼 사이즈 가져오기
    fun getButtonSize(
        defaultX: Float, // 기준 X좌표
        defaultY: Float, // 기준 Y좌표
        width: Float, // 버튼 width
        height: Float, // 버튼 height
    ): RectF {
        val left = defaultX - (width / 2)
        val right = defaultX + (width / 2)
        val top = defaultY - (height / 2)
        val bottom = defaultY + (height / 2)

        rectF.set(left, top, right, bottom)

        return rectF
    }
}