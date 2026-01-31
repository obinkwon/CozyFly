package com.game.cozyfly.listener

import com.game.cozyfly.enums.GameState
import com.game.cozyfly.enums.ViewState

interface GameEventListener {
    fun onBgmToggle()
    fun onClickModeToggle()
    fun onGameStateToggle(gameState: GameState)
    fun onViewStateToggle(viewState: ViewState)
}