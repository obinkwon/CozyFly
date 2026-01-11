package com.game.cozyfly

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceView
import kotlin.random.Random
import androidx.core.content.edit

class GameView(context: Context) : SurfaceView(context), Runnable {

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
    
    // 게임 관련 변수
    private var flapTimer = 0
    private val flapDuration = 10   // 프레임 수 (약 0.15초)
    private var difficultyLevel = 1
    private val baseScrollSpeed = 8f
    private val baseSpawnInterval = 100
    private var scrollSpeed = baseScrollSpeed
    private var gameThread = Thread(this)
    private var running = false
    private val paint = Paint()
    private var isGameOver = false
    private var spawnTimer = 0
    private var spawnInterval = baseSpawnInterval

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

    // 최고 점수 설정
    init {
        bestScore = prefs.getInt("BEST_SCORE", 0)
    }

    // 실행
    override fun run() {
        while (running) {
            if (!holder.surface.isValid) continue
            update()
            drawGame()
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
    private fun drawGame() {

        val canvas = holder.lockCanvas()
        canvas.drawBitmap(background, bgX1, 0f, null)
        canvas.drawBitmap(background, bgX2, 0f, null)

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


        // 장애물
        for (obs in obstacles) {
            obs.draw(canvas)
        }

        // 게임오버 텍스트
        if (isGameOver) {
            paint.color = Color.RED
            paint.textSize = 100f
            canvas.drawText("GAME OVER", width / 4f, height / 2f, paint)
        }

        paint.color = Color.WHITE
        paint.textSize = 60f
        canvas.drawText("Score: $score", 50f, 80f, paint)

        paint.textSize = 40f
        canvas.drawText("Best: $bestScore", 50f, 130f, paint)

        holder.unlockCanvasAndPost(canvas)
    }

    // 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if(isGameOver) resetGame();
            velocityY = -20f   // 점프

            currentPlayerBitmap = playerBitmap2
            flapTimer = flapDuration
        }

        return true
    }

    // 시작
    fun start() {
        running = true
        gameThread = Thread(this)
        gameThread.start()
    }

    // 정지
    fun stop() {
        running = false
        gameThread.join()
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
