package com.game.cozyfly.enums

enum class ClickMode(val desc: String, val type: String) {
    BACK("BACK", ""),
    CLICK_TAP("TAP MODE", "T"),
    CLICK_HOLD("HOLD MODE", "H"),
    BGM_ON("BGM ON", "ON"),
    BGM_OFF("BGM OFF", "OFF"),
    ;

    companion object {
        fun getMode(type: String?): ClickMode? = entries.firstOrNull { it.type == type }
    }
}