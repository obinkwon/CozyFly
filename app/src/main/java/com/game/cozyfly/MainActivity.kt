package com.game.cozyfly

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.enums.ViewState
import com.game.cozyfly.listener.GameEventListener

class MainActivity : ComponentActivity(), GameEventListener {
    private lateinit var bgmPlayer: MediaPlayer // bgm 플레이어
    private lateinit var prefs: SharedPreferences
    private lateinit var gameConfig: GameConfig // 게임 설정 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 상태바 영역까지 화면을 확장
        WindowCompat.setDecorFitsSystemWindows(window, false)
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        // 초기값 설정
        val clickMode = ClickMode.getMode(prefs.getString("CLICK_MODE", ClickMode.CLICK_TAP.type)) ?: ClickMode.CLICK_TAP
        val bgmState = ClickMode.getMode(prefs.getString("BGM_MODE", ClickMode.BGM_ON.type)) ?: ClickMode.BGM_ON
        val gameState = GameState.READY
        val viewState = ViewState.MENU
        gameConfig = GameConfig(bgmState, clickMode, gameState, viewState)

        // 게임 뷰 추가
        val gameView = GameView(this, this, gameConfig)
        // 팝업 뷰 추가
        val settingPopupView = SettingPopupView(this, this, gameConfig)
        val sharePopupView = SharePopupView(this, this, gameConfig)
        gameView.settingPopupView = settingPopupView   // settingPopupView 넘겨주기
        gameView.sharePopupView = sharePopupView    // sharePopupView 넘겨주기

        // 컨테이너 생성
        val container = FrameLayout(this)
        container.addView(gameView)
        container.addView(settingPopupView)
        container.addView(sharePopupView)

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
}