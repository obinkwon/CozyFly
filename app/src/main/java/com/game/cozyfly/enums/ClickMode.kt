package com.game.cozyfly.enums

enum class ClickMode(val desc: String, val type: String) {
    BACK("BACK", ""),
    TAP("TAP MODE", "T"),
    HOLD("HOLD MODE", "H");

    companion object {
        fun getMode(type: String?): ClickMode? = entries.firstOrNull { it.type == type }
    }
}