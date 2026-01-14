package com.game.cozyfly

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    private lateinit var bgmPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 상태바 영역까지 화면을 확장
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(GameView(this))
        // 배경음
        bgmPlayer = MediaPlayer.create(this, R.raw.bgm)
        bgmPlayer.isLooping = true
        bgmPlayer.start()
    }

    override fun onResume() {
        super.onResume()
        bgmPlayer.start()
    }

    override fun onPause() {
        super.onPause()
        bgmPlayer.pause()
    }
}