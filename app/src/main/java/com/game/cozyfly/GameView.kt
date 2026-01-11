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
import com.game.cozyfly.enums.GameState

class GameView(context: Context) : SurfaceView(context), Runnable, SurfaceHolder.Callback {

    // 게임 관련 변수
    private var flapTimer = 0
    private val flapDuration = 10   // 프레임 수 (약 0.15초)
    private var difficultyLevel = 1
    private val baseScrollSpeed = 8f
    private val baseSpawnInterval = 100
    private var scrollSpeed = baseScrollSpeed
    private var gameThread = Thread(this)
    private var running = false
    private var isGameOver = false
    private var spawnTimer = 0
    private var spawnInterval = baseSpawnInterval
    private var gameState = GameState.MENU
    private var startX = 0f
    private var startY = 0f
    private val startBtn: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.start)
    private var startW = 500f   // 버튼 가로
    private var startH = 300f   // 버튼 세로

    // 파리 관련 변수
    private val playerBitmap1 = BitmapFactory.decodeResource(resources, R.drawable.fly1)
    private val playerBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.fly2)
    // 현재 사용할 이미지
    private var currentPlayerBitmap = playerBitmap1
    private var x = 300f
    private var y = 500f
    private var velocityY = 0f
    private val gravity = 1.2f
    private val playerSize = 100f // 파리 크기 (이미지 크기 조절용)
    private val playerRadius = playerSize / 2
    
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
    private val gameOverStyle = TextStyle(100f, Color.RED)
    private val scoreStyle = TextStyle(60f, Color.WHITE)
    private val bestScoreStyle = TextStyle(40f, Color.WHITE)

    init {
        // 최고 점수 설정
        bestScore = prefs.getInt("BEST_SCORE", 0)
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        gameThread = Thread(this)
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // 필요 없으면 비워둬도 OK
    }

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
                    drawGame(canvas) // canvas를 전달
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

    private fun update() {
        if (isGameOver) return

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

        // 플레이어 중력
        velocityY += gravity
        y += velocityY

        // 화면 제한
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
                isGameOver = true

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
                currentPlayerBitmap = playerBitmap1
            }
        }
        // 난이도 조절
        updateDifficulty()
    }

    // 요소 그리기
    private fun drawGame(canvas: Canvas) {
        canvas.drawBitmap(background, bgX1, 0f, null)
        canvas.drawBitmap(background, bgX2, 0f, null)

        if (gameState == GameState.MENU) {
            drawMenu(canvas)
            return
        }

        // 플레이어
        val left = x - playerSize / 2
        val top = y - playerSize / 2

        val destRect = RectF(
            left,
            top,
            left + playerSize,
            top + playerSize
        )

        canvas.drawBitmap(currentPlayerBitmap, null, destRect, null)

        // 장애물 그리기
        for (obs in obstacles) {
            obs.draw(canvas)
        }

        // 게임오버 텍스트
        if (isGameOver) {
            CanvasUtil.drawText(canvas, "GAME OVER", width / 4f, height / 2f, gameOverStyle)
        }

        // 점수 표시
        CanvasUtil.drawText(canvas, "Score: $score", 50f, 80f, scoreStyle)
        // 최고 점수 표시
        CanvasUtil.drawText(canvas, "Best: $bestScore", 50f, 130f, bestScoreStyle)
    }

    private fun drawMenu(canvas: Canvas) {
        // 버튼 위치 계산
        startX = (width - startW) / 2f
        startY = (height - startH) / 2f

        val rect = RectF(startX, startY, startX + startW, startY + startH)
        canvas.drawBitmap(startBtn, null, rect, null)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (gameState == GameState.MENU) {
                // 메뉴 상태일 때 버튼 체크
                if (event.x >= startX && event.x <= startX + startW &&
                    event.y >= startY && event.y <= startY + startH) {
                    gameState = GameState.PLAY
                    resetGame()   // 기존 플레이어 위치/점수/장애물 초기화
                }
            } else if (gameState == GameState.PLAY) {
                if(isGameOver) resetGame();
                velocityY = -20f   // 점프

                currentPlayerBitmap = playerBitmap2
                flapTimer = flapDuration
            }

        }

        return true
    }

    // 시작
    fun start() {
        running = true
    }

    // 정지
    fun stop() {
        running = false
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
        isGameOver = false

        difficultyLevel = 1
        scrollSpeed = baseScrollSpeed
        spawnInterval = baseSpawnInterval

        bgX1 = 0f
        bgX2 = background.width.toFloat()
    }

    // 난이도 조절
    private fun updateDifficulty() {
        difficultyLevel = score / 5 + 1

        scrollSpeed = baseScrollSpeed + (difficultyLevel - 1) * 2f
        spawnInterval = (baseSpawnInterval - (difficultyLevel - 1) * 10)
            .coerceAtLeast(40)
    }
}
