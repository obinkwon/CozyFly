package com.game.cozyfly

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.listener.BgmListener

class MainActivity : ComponentActivity(), BgmListener {
    private lateinit var bgmPlayer: MediaPlayer
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 상태바 영역까지 화면을 확장
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 게임 뷰 추가
        val gameView = GameView(this, this)
        // 팝업 뷰 추가
        val popupView = PopupView(this, gameView)
        gameView.popupView = popupView   // popupView 넘겨주기
        // 컨테이너 생성
        val container = FrameLayout(this)
        container.addView(gameView)
        container.addView(popupView) // 게임 위에 올라갈 UI

        setContentView(container)
        // 배경음 초기화
        bgmPlayer = MediaPlayer.create(this, R.raw.bgm)
        bgmPlayer.isLooping = true
        bgmPlayer.start()
        // 배경음 상태 따라서 ON/OFF 설정
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE)
        val bgmState = ClickMode.getMode(prefs.getString("BGM_MODE", ClickMode.BGM_ON.type)) ?: ClickMode.BGM_ON
        if (bgmState == ClickMode.BGM_ON) onBgmOn() else onBgmOff()
    }

    override fun onResume() {
        super.onResume()
        // 배경음악 상태 체크
        val bgmState = ClickMode.getMode(prefs.getString("BGM_MODE", ClickMode.BGM_ON.type)) ?: ClickMode.BGM_ON
        if (bgmState == ClickMode.BGM_ON) onBgmOn() else onBgmOff()
    }

    override fun onPause() {
        super.onPause()
        onBgmOff()
    }

    override fun onBgmOn() {
        bgmPlayer.start()
    }

    override fun onBgmOff() {
        bgmPlayer.pause()
    }
}