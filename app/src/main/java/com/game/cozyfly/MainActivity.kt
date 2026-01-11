package com.game.cozyfly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 상태바 영역까지 화면을 확장
        WindowCompat.setDecorFitsSystemWindows(window, false)
        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.start()
    }

    override fun onPause() {
        super.onPause()
        gameView.stop()
    }
}