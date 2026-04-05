package com.game.cozyfly.enums

enum class SkinType {
    FLY,
    SPRITE,
    ;

    companion object {
        fun getSkin(type: String?): SkinType? = entries.firstOrNull { it.name == type }
    }

}