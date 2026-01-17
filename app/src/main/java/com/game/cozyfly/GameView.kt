package com.game.cozyfly

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random
import androidx.core.content.edit
import com.game.cozyfly.util.CanvasUtil
import com.game.cozyfly.data.TextStyle
import com.game.cozyfly.enums.ClickMode
import com.game.cozyfly.enums.GameState
import com.game.cozyfly.enums.ViewState

class GameView(context: Context) : SurfaceView(context), Runnable, SurfaceHolder.Callback {

    // 쓰레드 변수
    private var gameThread = Thread(this)
    private var running = false
    // 게임 관련 변수
    private var flapTimer = 0
    private val flapDuration = 10   // 프레임 수 (약 0.15초)
    private var difficultyLevel = 1
    private val baseScrollSpeed = 8f
    private val baseSpawnInterval = 100
    private var scrollSpeed = baseScrollSpeed
    private var spawnTimer = 0
    private var spawnInterval = baseSpawnInterval
    private var gameState = GameState.PLAY // 초기값 실행 상태
    private var viewState = ViewState.MENU // 초기값 메뉴 화면
    private var startW = 500f // 버튼 가로
    private var startH = 300f // 버튼 세로
    private var centerX = 0f // 중앙 X좌표 값
    private var centerY = 0f // 중앙 Y좌표 값
    private val startBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.start)
    private val settingBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.setting)
    private val gameOverText: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.gameover)
    private val tapButtonRect = RectF()
    private val holdButtonRect = RectF()
    private val backButtonRect = RectF()
    private val startButtonRect = RectF()
    private val settingsButtonRect = RectF()
    private val gameOverTextRect = RectF()
    private var clickMode = ClickMode.TAP // 초기값 탭 상태
    private var holding = false

    // 파리 관련 변수
    private val playerImg1 = BitmapFactory.decodeResource(resources, R.drawable.fly1)
    private val playerImg2 = BitmapFactory.decodeResource(resources, R.drawable.fly2)
    // 현재 사용할 이미지
    private var currentPlayer = playerImg1
    private var x = 300f
    private var y = 500f
    private var velocityY = 0f
    private val gravity = 1.2f
    private val playerSize = 100f // 파리 크기 (이미지 크기 조절용)
    private val playerRadius = playerSize / 2
    private val playerRect = RectF()
    
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
    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    private var score = 0
    private var bestScore = 0
    private var timeCounter = 0
    private val fps = 60 // 프레임 설정

    // Text style
    private val scoreStyle = TextStyle(60f, Color.DKGRAY)
    private val bestScoreStyle = TextStyle(40f, Color.DKGRAY)
    private val infoStyle = TextStyle(40f, Color.DKGRAY)

    // 초기 설정
    init {
        // 최고 점수 설정
        bestScore = prefs.getInt("BEST_SCORE", 0)
        // 설정한 클릭 모드 설정
        clickMode = ClickMode.getMode(prefs.getString("CLICK_MODE", ClickMode.TAP.type)) ?: ClickMode.TAP
        holder.addCallback(this)
    }

    // surfaceView 생성
    override fun surfaceCreated(holder: SurfaceHolder) {
        // 버튼 위치 계산
        centerX = (width - startW) / 2f
        centerY = (height - startH) / 2f
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
                    update()
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

        centerX = w / 2f - startW / 2f
        centerY = h / 2f - startH / 2f
    }

    private fun update() {
        if (viewState == ViewState.PLAY && gameState == GameState.GAMEOVER) return

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

        // 홀드 모드일때
        if (clickMode == ClickMode.HOLD && holding) {
            velocityY = -20f  // 원하는 상승 속도
            currentPlayer = playerImg2
            flapTimer = flapDuration
        } else {
            // 플레이어 중력
            velocityY += gravity
        }
        
        // 플레이어 이동
        y += velocityY

        // 플레이어 화면이동 제한
        if (y - playerRadius < 0f) {
            y = playerRadius
            velocityY = 0f
        }
        if (y + playerRadius > height.toFloat()) {
            y = height.toFloat() - playerRadius
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
            if (obs.collidesWith(x, y, playerRadius)) {
                gameState = GameState.GAMEOVER

                if (score > bestScore) {
                    bestScore = score
                    prefs.edit { putInt("BEST_SCORE", bestScore) }
                }
                break
            }
        }

        // 시간 기반 점수 (1초 마다)
        timeCounter++
        if (timeCounter >= fps) {
            score++
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

    // 화면 상태별로 캔버스에 요소 그리기
    private fun drawCanvas(canvas: Canvas) {
        // 배경 화면 그리기
        drawBackground(canvas)
        // 화면 상태별로 실행
        when (viewState) {
            ViewState.MENU -> drawMenu(canvas)
            ViewState.PLAY -> drawGame(canvas)
            ViewState.SETTINGS -> drawSettings(canvas)
        }
    }

    private fun drawGame(canvas: Canvas) {
        // 파리 그리기
        val left = x - playerSize / 2
        val top = y - playerSize / 2
        playerRect.set(left, top, left + playerSize, top + playerSize)
        canvas.drawBitmap(currentPlayer, null, playerRect, null)

        // 장애물 그리기
        for (obs in obstacles) {
            obs.draw(canvas)
        }

        // 게임오버 텍스트
        if (gameState == GameState.GAMEOVER) {
            gameOverTextRect.set(centerX, centerY, centerX + startW, centerY + startH)
            canvas.drawBitmap(gameOverText, null, gameOverTextRect, null)
        }

        // 점수 표시
        CanvasUtil.drawText(canvas, "Score: $score", 50f, 150f, scoreStyle)
        // 최고 점수 표시
        CanvasUtil.drawText(canvas, "Best: $bestScore", 50f, 200f, bestScoreStyle)
    }
    
    // 메뉴 화면 그리기
    private fun drawMenu(canvas: Canvas) {
        // start 버튼
        startButtonRect.set(centerX, centerY, centerX + startW, centerY + startH)
        canvas.drawBitmap(startBtn, null, startButtonRect, null)

        // setting 버튼
        settingsButtonRect.set(centerX-50f, centerY + 150f, centerX + startW + 50f, centerY + startH + 250f)
        canvas.drawBitmap(settingBtn, null, settingsButtonRect, null)
    }

    // 세팅 화면 그리기
    private fun drawSettings(canvas: Canvas) {
        // 세팅 글자
        settingsButtonRect.set(centerX-50f, 100f, centerX + startW+50f, 450f)
        canvas.drawBitmap(settingBtn, null, settingsButtonRect, null)
        // 탭 모드 버튼 
        tapButtonRect.set(width / 2f - 200, centerY, width / 2f + 200, centerY + 100f)
        CanvasUtil.drawButton(canvas, ClickMode.TAP, tapButtonRect)
        // 홀드 모드 버튼
        holdButtonRect.set(width / 2f - 200, centerY + 150f, width / 2f + 200, centerY + 250f)
        CanvasUtil.drawButton(canvas, ClickMode.HOLD, holdButtonRect)
        // 뒤로가기 버튼
        backButtonRect.set(width / 2f - 200, centerY + 700f, width / 2f + 200, centerY + 800f)
        CanvasUtil.drawButton(canvas, ClickMode.BACK, backButtonRect)

        // 현재 선택 표시
        val selectedText = "$clickMode Mode"
        CanvasUtil.drawText(canvas, selectedText, width / 2f - 200, 750f, infoStyle)
    }

    // 배경 화면 그리기
    private fun drawBackground (canvas: Canvas) {
        canvas.drawBitmap(background, bgX1, 0f, null)
        canvas.drawBitmap(background, bgX2, 0f, null)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (viewState) {
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
                viewState = ViewState.PLAY
                resetGame()   // 기존 플레이어 위치/점수/장애물 초기화
            } else if (settingsButtonRect.contains(event.x, event.y)) {
                viewState = ViewState.SETTINGS
            }
        }
    }

    // 세팅 터치 이벤트
    private fun handleSettingsTouch(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (tapButtonRect.contains(event.x, event.y)) {
                clickMode = ClickMode.TAP
            } else if (holdButtonRect.contains(event.x, event.y)) {
                clickMode = ClickMode.HOLD
            } else if (backButtonRect.contains(event.x, event.y)) {
                prefs.edit { putString("CLICK_MODE", clickMode.type) }
                viewState = ViewState.MENU
            }
        }
    }
    
    // 플레이 터치 이벤트
    private fun handleGameplayTouch(event: MotionEvent) {
        // 게임 실행중일때
        if(gameState == GameState.PLAY) {
            if (clickMode == ClickMode.TAP) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    velocityY = -20f   // 점프
                    currentPlayer = playerImg2
                    flapTimer = flapDuration
                }
            } else {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> holding = true
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> holding = false
                }
            }
        }
        // 게임 오버시
        else if(gameState == GameState.GAMEOVER) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 재시작
                resetGame()
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
                x = width.toFloat(),
                y = randomY,
                width = obstacleWidth,
                height = obstacleHeight,
                bitmap = obstacleBitmap
            )
        )
        
        obstacles.add(
            Obstacle(
                x = width.toFloat(),
                y = randomY + obstacleHeight,
                width = obstacleWidth,
                height = obstacleHeight,
                bitmap = obstacleBitmap
            )
        )
    }

    // 게임 초기화
    private fun resetGame() {
        x = 300f
        y = height / 2f
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
        gameState = GameState.PLAY
    }

    // 난이도 조절
    private fun updateDifficulty() {
        difficultyLevel = score / 5 + 1

        scrollSpeed = baseScrollSpeed + (difficultyLevel - 1) * 2f
        spawnInterval = (baseSpawnInterval - (difficultyLevel - 1) * 10)
            .coerceAtLeast(40)
    }
}
