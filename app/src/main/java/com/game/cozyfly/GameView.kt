package com.game.cozyfly

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.edit
import com.game.cozyfly.constants.SizeConstants
import com.game.cozyfly.data.GameConfig
import com.game.cozyfly.data.TextStyle
import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.enums.ViewState
import com.game.cozyfly.listener.GameEventListener
import com.game.cozyfly.util.ButtonUtil
import com.game.cozyfly.util.CanvasUtil
import kotlin.random.Random

@SuppressLint("ViewConstructor")
class GameView(
    context: Context,
    private val eventListener: GameEventListener,
    private val gameConfig: GameConfig,
) : SurfaceView(context), Runnable, SurfaceHolder.Callback {

    // 팝업 view 변수
    lateinit var settingPopupView: SettingPopupView
    lateinit var sharePopupView: SharePopupView

    // 쓰레드 변수
    private var gameThread = Thread(this)
    private var running = false

    // 게임 관련 변수
    private var flapTimer = 0
    private val flapDuration = 10   // 프레임 수 (약 0.15초)
    private var difficultyLevel = 1 // 난이도
    private val baseScrollSpeed = 8f
    private val baseSpawnInterval = 100
    private var scrollSpeed = baseScrollSpeed
    private var spawnTimer = 0
    private var spawnInterval = baseSpawnInterval
    private var centerX = 0f // 중앙 X좌표 값
    private var centerY = 0f // 중앙 Y좌표 값
    private val startBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.start)
    private val settingBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.setting)
    private val gameOverText: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.gameover)
    private val settingIconBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.setting_icon)
    private val restartBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.restart)
    private val shareBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.share)
    private val modeButtonRect = RectF()
    private val backButtonRect = RectF()
    private val startButtonRect = RectF()
    private val settingsButtonRect = RectF()
    private val bgmButtonRect = RectF()
    private val settingIconBtnRect = RectF()
    private val restartBtnRect = RectF()
    private val shareBtnRect = RectF()
    var holding = false

    // 파리 관련 변수
    private val playerImg1 = BitmapFactory.decodeResource(resources, R.drawable.fly1)
    private val playerImg2 = BitmapFactory.decodeResource(resources, R.drawable.fly2)
    // 현재 사용할 이미지
    private var currentPlayer = playerImg1
    private var playerX = 0f // 파리 X좌표
    private var playerY = 0f // 파리 Y좌표
    private var velocityY = 0f
    private val gravity = 1.2f
    private val playerRadius = SizeConstants.PLAYER_WIDTH / 2

    // 배경 관련 변수
    private val background: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.background)
    private var bgX1 = 0f
    private var bgX2 = background.width.toFloat()
    private val bgScrollSpeed = 4f

    // 장애물 관련 변수
    private val obstacles = mutableListOf<Obstacle>()
    private val obstacleBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.obstacle)
    private val obstacleWidth = 100f
    private val obstacleHeight = 100f
    private val obstacleMargin = 50f

    // 점수 관련 변수
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private var score = 0
    private var timeCounter = 0
    private val fps = 60 // 프레임 설정

    // 초기 설정
    init {
        holder.addCallback(this)
    }

    // surfaceView 생성
    override fun surfaceCreated(holder: SurfaceHolder) {
        // 중앙 위치 계산
        centerX = width / 2f
        centerY = height / 2f
        // 쓰레드 실행
        running = true
        gameThread = Thread(this)
        gameThread.start()
    }

    // surfaceView 변경
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 필요 없으면 비워둬도 OK
    }

    // surfaceView 제거
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // 실행
    override fun run() {
        while (running) {
            if (!holder.surface.isValid) continue

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                synchronized(holder) {
                    update() // 게임 동작 업데이트
                    updateBackground() // 배경 업데이트
                    drawCanvas(canvas) // canvas를 전달
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    // 가로/세로 전환시 호출
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 중앙 위치 계산
        centerX = w / 2f
        centerY = h / 2f
    }

    // 게임 동작 업데이트
    private fun update() {
        // 게임플레이 상태 일때만 동작
        if (gameConfig.gameState != GameState.PLAY) return

        // 홀드 모드일때
        if (gameConfig.clickMode == ClickMode.CLICK_HOLD && holding) {
            velocityY = -20f  // 원하는 상승 속도
            currentPlayer = playerImg2
            flapTimer = flapDuration
        } else {
            // 플레이어 중력
            velocityY += gravity
        }
        
        // 플레이어 이동
        playerY += velocityY

        // 플레이어 화면이동 제한
        if (playerY - playerRadius < 0f) {
            playerY = playerRadius
            velocityY = 0f
        }
        if (playerY + playerRadius > height) {
            playerY = height - playerRadius
            velocityY = 0f
        }

        // 장애물 생성 타이밍
        spawnTimer++
        if (spawnTimer > spawnInterval) {
            spawnTimer = 0
            spawnObstacle()
        }

        // 장애물 이동
        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obs = iterator.next()
            obs.update(scrollSpeed)

            if (obs.isOffScreen()) {
                iterator.remove()
            }
        }

        // 충돌 판정
        for (obs in obstacles) {
            if (obs.collidesWith(playerX, playerY, playerRadius)) {
                eventListener.onGameStateToggle(GameState.GAMEOVER)
                prefs.edit { putInt("BEST_SCORE", gameConfig.bestScore) }
                break
            }
        }

        // 시간 기반 점수 (1초 마다)
        timeCounter++
        if (timeCounter >= fps) {
            score++
            if (score > gameConfig.bestScore) gameConfig.bestScore = score
            timeCounter = 0
        }

        // 플레이어 flap 애니메이션 / 상태 복귀
        if (flapTimer > 0) {
            flapTimer--
            if (flapTimer == 0) {
                currentPlayer = playerImg1
            }
        }

        // 난이도 조절
        updateDifficulty()
    }

    // 배경 업데이트
    private fun updateBackground() {
        // 준비화면 상태이거나 게임플레이 상태일때 동작
        if (gameConfig.gameState == GameState.READY || gameConfig.gameState == GameState.PLAY) {
            // 배경 화면 속도
            bgX1 -= bgScrollSpeed
            bgX2 -= bgScrollSpeed

            // 배경 화면 밖으로 나가면 다시 오른쪽 으로
            if (bgX1 + background.width < 0) {
                bgX1 = bgX2 + background.width
            }
            if (bgX2 + background.width < 0) {
                bgX2 = bgX1 + background.width
            }
        }
    }

    // 화면 상태별로 캔버스에 요소 그리기
    private fun drawCanvas(canvas: Canvas) {
        // 배경 화면 그리기
        drawBackground(canvas)
        // 화면 상태별로 실행
        when (gameConfig.viewState) {
            ViewState.MENU -> drawMenu(canvas)
            ViewState.PLAY -> drawGame(canvas)
            ViewState.SETTINGS -> drawSettings(canvas)
        }
    }

    private fun drawGame(canvas: Canvas) {
        // 파리 그리기
        val playerRect = ButtonUtil.getButtonSize(playerX, playerY, SizeConstants.PLAYER_WIDTH, SizeConstants.PLAYER_HEIGHT)
        canvas.drawBitmap(currentPlayer, null, playerRect, null)

        // 장애물 그리기
        for (obs in obstacles) {
            obs.draw(canvas)
        }

        // 게임오버 화면 표시
        if (gameConfig.gameState == GameState.GAMEOVER) {
            val gameOverTextRect = ButtonUtil.getButtonSize(centerX, centerY - 300f, SizeConstants.GAMEOVER_BTN_WIDTH, SizeConstants.GAMEOVER_BTN_HEIGHT)
            canvas.drawBitmap(gameOverText, null, gameOverTextRect, null)

            restartBtnRect.set(ButtonUtil.getButtonSize(centerX, centerY, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.BIG_BTN_HEIGHT))
            canvas.drawBitmap(restartBtn, null, restartBtnRect, null)

            shareBtnRect.set(ButtonUtil.getButtonSize(centerX, centerY + 300f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.BIG_BTN_HEIGHT))
            canvas.drawBitmap(shareBtn, null, shareBtnRect, null)
        }

        // 플레이 화면 세팅 아이콘 그리기
        settingIconBtnRect.set(ButtonUtil.getButtonSize(width - 100f, 150f, SizeConstants.SETTING_ICON_WIDTH, SizeConstants.SETTING_ICON_HEIGHT))
        canvas.drawBitmap(settingIconBtn, null, settingIconBtnRect, null)

        // 점수 표시
        CanvasUtil.drawText(canvas, "Score: $score", 50f, 150f, TextStyle(60f, Color.DKGRAY))
        // 최고 점수 표시
        CanvasUtil.drawText(canvas, "Best: ${gameConfig.bestScore}", 50f, 200f, TextStyle(40f, Color.DKGRAY))
    }
    
    // 메뉴 화면 그리기
    private fun drawMenu(canvas: Canvas) {
        // start 버튼
        startButtonRect.set(ButtonUtil.getButtonSize(centerX, centerY, SizeConstants.START_BTN_WIDTH, SizeConstants.START_BTN_HEIGHT))
        canvas.drawBitmap(startBtn, null, startButtonRect, null)

        // setting 버튼
        settingsButtonRect.set(ButtonUtil.getButtonSize(centerX, centerY + 200f, SizeConstants.SETTING_BTN_WIDTH, SizeConstants.SETTING_BTN_HEIGHT))
        canvas.drawBitmap(settingBtn, null, settingsButtonRect, null)
    }

    // 세팅 화면 그리기
    private fun drawSettings(canvas: Canvas) {
        // 세팅 글자
        settingsButtonRect.set(ButtonUtil.getButtonSize(centerX, 200f, SizeConstants.SETTING_BTN_WIDTH, SizeConstants.SETTING_BTN_HEIGHT))
        canvas.drawBitmap(settingBtn, null, settingsButtonRect, null)
        // 모드 버튼
        modeButtonRect.set(ButtonUtil.getButtonSize(centerX, centerY, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, gameConfig.clickMode, modeButtonRect)
        // 배경음 조절 버튼
        bgmButtonRect.set(ButtonUtil.getButtonSize(centerX, centerY + 150f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, gameConfig.bgmState, bgmButtonRect)
        // 뒤로가기 버튼
        backButtonRect.set(ButtonUtil.getButtonSize(centerX, centerY + 400f, SizeConstants.DEFAULT_BTN_WIDTH, SizeConstants.DEFAULT_BTN_HEIGHT))
        CanvasUtil.drawButton(canvas, ClickMode.BACK, backButtonRect)
    }
    
    // 배경 화면 그리기
    private fun drawBackground (canvas: Canvas) {
        canvas.drawBitmap(background, bgX1, 0f, null)
        canvas.drawBitmap(background, bgX2, 0f, null)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (gameConfig.viewState) {
            ViewState.MENU -> handleMenuTouch(event)
            ViewState.SETTINGS -> handleSettingsTouch(event)
            ViewState.PLAY -> handleGameplayTouch(event)
        }

        return true
    }
    
    // 메뉴 터치 이벤트
    private fun handleMenuTouch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (startButtonRect.contains(event.x, event.y)) {
                eventListener.onViewStateToggle(ViewState.PLAY)
                resetGame()   // 기존 플레이어 위치/점수/장애물 초기화
            } else if (settingsButtonRect.contains(event.x, event.y)) {
                eventListener.onViewStateToggle(ViewState.SETTINGS)
            }
        }
    }

    // 세팅 터치 이벤트
    private fun handleSettingsTouch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when {
                modeButtonRect.contains(event.x, event.y) -> {
                    eventListener.onClickModeToggle()
                }
                bgmButtonRect.contains(event.x, event.y) -> {
                    eventListener.onBgmToggle()
                }
                backButtonRect.contains(event.x, event.y) -> {
                    eventListener.onViewStateToggle(ViewState.MENU)
                }
            }
        }
    }
    
    // 플레이 터치 이벤트
    private fun handleGameplayTouch(event: MotionEvent) {
        when (gameConfig.gameState) {
            // 게임 실행중 일때만 진행
            GameState.PLAY -> {
                if (gameConfig.clickMode == ClickMode.CLICK_TAP) {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        velocityY = -20f // 점프
                        currentPlayer = playerImg2
                        flapTimer = flapDuration
                    }
                } else {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> holding = true
                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> holding = false
                    }
                }

                // 플레이 화면 세팅 아이콘 클릭할때
                if (settingIconBtnRect.contains(event.x, event.y)) {
                    holding = false
                    eventListener.onGameStateToggle(GameState.PAUSE) // 일시정지
                    settingPopupView.showPopup() // 팝업 호출
                }
            }
            // 게임 오버시 진행
            GameState.GAMEOVER -> {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    // 재시작 버튼 클릭
                    if (restartBtnRect.contains(event.x, event.y)) {
                        resetGame() // 재시작
                    }
                    // 공유 버튼 클릭
                    else if (shareBtnRect.contains(event.x, event.y)) {
                        eventListener.onGameStateToggle(GameState.SHARE) // 공유
                        sharePopupView.showPopup() // 팝업 호출
                    }
                }
            }
            // 기타
            GameState.PAUSE,
            GameState.READY,
            GameState.SHARE-> {
                // 아무 입력도 안 받음
                holding = false
            }
        }
    }

    // 장애물 생성
    private fun spawnObstacle() {
        val minY = obstacleMargin
        val maxY = height - obstacleHeight - obstacleMargin

        val randomY = Random.nextInt(
            minY.toInt(),
            maxY.toInt()
        ).toFloat()

        obstacles.add(
            Obstacle(
                width.toFloat(),
                randomY,
                obstacleWidth,
                obstacleHeight,
                obstacleBitmap
            )
        )

        obstacles.add(
            Obstacle(
                width.toFloat(),
                randomY + obstacleHeight,
                obstacleWidth,
                obstacleHeight,
                obstacleBitmap
            )
        )
    }

    // 게임 초기화
    private fun resetGame() {
        // 초기 시작점
        playerX = 200f
        playerY = height / 2f
        velocityY = 0f

        obstacles.clear()
        spawnTimer = 0

        score = 0
        timeCounter = 0

        difficultyLevel = 1
        scrollSpeed = baseScrollSpeed
        spawnInterval = baseSpawnInterval

        bgX1 = 0f
        bgX2 = background.width.toFloat()
        eventListener.onGameStateToggle(GameState.PLAY)
    }

    // 난이도 조절
    private fun updateDifficulty() {
        difficultyLevel = score / 5 + 1

        scrollSpeed = baseScrollSpeed + (difficultyLevel - 1) * 2f
        spawnInterval = (baseSpawnInterval - (difficultyLevel - 1) * 10).coerceAtLeast(40)
    }
}
