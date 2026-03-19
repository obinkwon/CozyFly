package com.game.cozyfly

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.edit
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.game.cozyfly.compose.ShopScreen
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.enums.ViewState
import com.game.cozyfly.listener.GameEventListener
import com.game.cozyfly.view.GameView
import com.game.cozyfly.view.SettingPopupView

class MainActivity : ComponentActivity(), GameEventListener {
    private lateinit var bgmPlayer: MediaPlayer // bgm 플레이어
    private lateinit var prefs: SharedPreferences
    private lateinit var gameConfig: GameConfig // 게임 설정 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 상태바 숨기기
        // 초기값 설정
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        val clickMode = ClickMode.getMode(prefs.getString("CLICK_MODE", ClickMode.CLICK_TAP.type)) ?: ClickMode.CLICK_TAP
        val bgmState = ClickMode.getMode(prefs.getString("BGM_MODE", ClickMode.BGM_ON.type)) ?: ClickMode.BGM_ON
        val gameState = GameState.PLAY
        val viewState = ViewState.MENU
        val bestScore = prefs.getInt("BEST_SCORE", 0)
        val coinScore = prefs.getInt("COIN_SCORE", 0)
        gameConfig = GameConfig(bgmState, clickMode, gameState, viewState, bestScore, coinScore)

        // 게임 뷰 추가
        val gameView = GameView(this, this, gameConfig)
        // 팝업 뷰 추가
        val settingPopupView = SettingPopupView(this, this, gameConfig)
        gameView.settingPopupView = settingPopupView   // settingPopupView 넘겨주기
        // 상점 뷰 추가
        val shopView = ComposeView(this).apply {
            visibility = View.GONE
            setContent {
                ShopScreen(
                    onClose = {
                        visibility = View.GONE
                        onViewStateToggle(ViewState.MENU)
                    }
                )
            }
        }

        // 컨테이너 생성
        val container = FrameLayout(this)
        container.addView(gameView)
        container.addView(settingPopupView)
        container.addView(shopView)

        setContentView(container)
        // 배경음 초기화
        bgmPlayer = MediaPlayer.create(this, R.raw.bgm)
        bgmPlayer.isLooping = true
        bgmPlayer.start()
        if (bgmState != ClickMode.BGM_ON) bgmPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        // 배경음악 상태 체크
        if (gameConfig.bgmState == ClickMode.BGM_ON) bgmPlayer.start() else bgmPlayer.pause()
    }

    override fun onPause() {
        super.onPause()
        bgmPlayer.pause()
    }
    
    // 배경음 조절 토글
    override fun onBgmToggle() {
        if (gameConfig.bgmState == ClickMode.BGM_ON) {
            gameConfig.bgmState = ClickMode.BGM_OFF
            bgmPlayer.pause()
        }
        else {
            gameConfig.bgmState = ClickMode.BGM_ON
            bgmPlayer.start()
        }

        prefs.edit { putString("BGM_MODE", gameConfig.bgmState.type) }
    }
    // 모드 조절 토글
    override fun onClickModeToggle() {
        gameConfig.clickMode =
            if (gameConfig.clickMode == ClickMode.CLICK_TAP)
                ClickMode.CLICK_HOLD
            else
                ClickMode.CLICK_TAP

        prefs.edit { putString("CLICK_MODE", gameConfig.clickMode.type) }
    }
    // 게임 상태 조절 토글
    override fun onGameStateToggle(gameState: GameState) {
        gameConfig.gameState = gameState
    }
    // view 상태 조절 토글
    override fun onViewStateToggle(viewState: ViewState) {
        gameConfig.viewState = viewState
    }
    // 현재 앱 창이 포커스를 얻거나 잃을 때 호출
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }
    // 상태바 숨기기
    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}