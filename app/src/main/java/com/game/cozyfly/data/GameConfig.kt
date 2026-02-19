package com.game.cozyfly.data

import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.enums.ViewState

data class GameConfig(
    var bgmState: ClickMode,
    var clickMode: ClickMode,
    var gameState: GameState,
    var viewState: ViewState,
    var bestScore: Int,
    var coinScore: Int,
)