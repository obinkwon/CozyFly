package com.game.cozyfly

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.game.cozyfly.constants.LeaderBoardConstants
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.enums.ViewState
import com.game.cozyfly.listener.GameEventListener
import com.game.cozyfly.view.GameView
import com.game.cozyfly.view.SettingPopupView
import com.game.cozyfly.view.ShopView
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.leaderboard.LeaderboardVariant


class MainActivity : ComponentActivity(), GameEventListener {
    private lateinit var bgmPlayer: MediaPlayer // bgm 플레이어
    private lateinit var prefs: SharedPreferences
    private lateinit var gameConfig: GameConfig // 게임 설정 변수
    private lateinit var gamesSignInClient: GamesSignInClient // 게임 설정 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 상태바 숨기기
        // 구글 리더보드 초기값 설정
        PlayGamesSdk.initialize(this)
        gamesSignInClient = PlayGames.getGamesSignInClient(this)
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
        // 상점 뷰 추가
        val shopView = ShopView(this, this, gameConfig)
        gameView.settingPopupView = settingPopupView   // settingPopupView 넘겨주기
        gameView.shopView = shopView   // shopView 넘겨주기

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
        autoGameSignIn {}
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
        if (hasFocus) {
            // 상태바 숨기기
            val controller = WindowInsetsControllerCompat(window, window.decorView)

            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    // 자동 로그인 시도하는 함수
    private fun autoGameSignIn(onSuccess: () -> Unit) {
        gamesSignInClient.signIn().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.isAuthenticated) {
                onSuccess()
            } else {
                Log.e("Login", "fail", task.exception)
            }
        }.addOnFailureListener {
            Log.e("PlayGames", "autoGameSignIn 실패", it)
        }
    }
    // 리더보드 점수 저장
    override fun onSubmitScore(score: Int) {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            val isAuthenticated = task.isSuccessful && task.result.isAuthenticated
            // 구글 로그인 된사람만 리더보드 점수 저장
            if (isAuthenticated) {
                val leaderboardsClient = PlayGames.getLeaderboardsClient(this)
                leaderboardsClient.submitScore(LeaderBoardConstants.ID, score.toLong())
            }
        }.addOnFailureListener {
            Log.e("PlayGames", "onSubmitScore 실패", it)
        }
    }
    // 리더보드 열기
    override fun showLeaderboard() {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            val isAuthenticated = task.isSuccessful && task.result.isAuthenticated
            val leaderboardsClient = PlayGames.getLeaderboardsClient(this)
            if (isAuthenticated) {
                leaderboardsClient.getLeaderboardIntent(LeaderBoardConstants.ID).addOnSuccessListener { intent -> startActivityForResult(intent, 1001) }
            } else {
                // 구글 로그인
                autoGameSignIn {
                    // 로그인 완료 후 다시 호출
                    leaderboardsClient.getLeaderboardIntent(LeaderBoardConstants.ID).addOnSuccessListener { intent -> startActivityForResult(intent, 1001) }
                }
            }
        }.addOnFailureListener {
            Log.e("PlayGames", "showLeaderboard 실패", it)
        }
    }
    // 리더보드 점수 가져오기
    override fun getLeaderboardScore(callback: (Int) -> Unit) {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
            val isAuthenticated = task.isSuccessful && task.result.isAuthenticated
            if (isAuthenticated) {
                loadScore(callback)
            } else {
                autoGameSignIn {
                    // 로그인 완료 후 다시 호출
                    loadScore(callback)
                }
            }
        }.addOnFailureListener {
            Log.e("PlayGames", "getLeaderboardScore 실패", it)
        }
    }
    // 점수 불러오기
    private fun loadScore(callback: (score: Int) -> Unit, retry: Int = 0) {
        val leaderboardsClient = PlayGames.getLeaderboardsClient(this)

        leaderboardsClient.loadCurrentPlayerLeaderboardScore(
            LeaderBoardConstants.ID,
            LeaderboardVariant.TIME_SPAN_ALL_TIME,
            LeaderboardVariant.COLLECTION_PUBLIC
        )
            .addOnSuccessListener { result ->
                val scoreData = result.get()

                if (scoreData != null) {
                    val score = scoreData.rawScore.toInt()
                    callback(score)
                } else {
                    callback(0)
                }
            }
            .addOnFailureListener { e ->
                if (retry < 2) {
                    gamesSignInClient.signIn().addOnCompleteListener {
                        loadScore(callback, retry + 1)
                    }.addOnFailureListener {
                        Log.e("PlayGames", "loadScore 실패", it)
                    }
                } else {
                    callback(0)
                }
            }
    }
}