package com.game.cozyfly.enums

enum class SkinType(val price: Int) {
    FLY(0),
    SPRITE(100),
    GOOBER(1000),
    ;

    companion object {
        fun getSkin(type: String?): SkinType? = entries.firstOrNull { it.name == type }
    }

}