package com.example.healthcareapp.widget


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class Slidercustom @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var max = 10
    var isEditEnabled = true

    var progress = 8
        set(value) {
            if (field == value) return
            field = value.coerceIn(1, max)
            invalidate()
            onProgressChanged?.invoke(field)
        }

    var onProgressChanged: ((Int) -> Unit)? = null

    private val dp = context.resources.displayMetrics.density

    // Figma 수치 기준
    // 전체 뷰 높이: 26dp, thumb 직경: 24dp (반지름 12dp)
    // 트랙은 뷰 위아래 19.23% 안쪽 = 5dp 위아래 여백
    private val thumbR = 12f * dp          // 24dp 직경
    private val activeDotR = 4.5f * dp    // active 점 반지름
    private val inactiveDotR = 2.5f * dp  // inactive 점 반지름
    private var activeColor = Color.parseColor("#53A1FF")
    private var inactiveColor = Color.parseColor("#D8E1ED")
    private val disabledColor = Color.parseColor("#94A3B8")

    private val activeLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 2.5f * dp
    }
    private val inactiveLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = inactiveColor
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 1.5f * dp
    }
    private val activeDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val inactiveDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = inactiveColor
        style = Paint.Style.FILL
    }
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    // Figma: drop-shadow(0px 0px 1.2px rgba(0,0,0,0.1)) → BlurMaskFilter로 구현
    private val thumbShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(26, 0, 0, 0) // rgba(0,0,0,0.1)
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(1.2f * dp, BlurMaskFilter.Blur.NORMAL)
    }
    private val thumbTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f * 0f  // onSizeChanged에서 설정
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var trackLeft = 0f
    private var trackRight = 0f
    private var trackY = 0f

    init {
        // BlurMaskFilter는 소프트웨어 레이어 필요
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        trackLeft = thumbR
        trackRight = w - thumbR
        trackY = h / 2f
        thumbTextPaint.textSize = 14f * dp
    }

    private fun progressToX(p: Int): Float {
        if (max <= 1) return trackLeft
        return trackLeft + (p - 1).toFloat() / (max - 1).toFloat() * (trackRight - trackLeft)
    }

    private fun xToProgress(x: Float): Int {
        if (trackRight <= trackLeft) return 1
        val ratio = (x - trackLeft) / (trackRight - trackLeft)
        return (ratio * (max - 1) + 1).roundToInt().coerceIn(1, max)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 🎯 모드에 따라 결정된 currentColor를 사용
        val currentColor = if (isEditEnabled) activeColor else disabledColor

        activeLinePaint.color = currentColor
        activeDotPaint.color = currentColor
        thumbTextPaint.color = currentColor
        val thumbX = progressToX(progress)


        // 1. 연결선
        for (i in 1 until max) {
            val x1 = progressToX(i)
            val x2 = progressToX(i + 1)
            val paint = if (i < progress) activeLinePaint else inactiveLinePaint
            canvas.drawLine(x1, trackY, x2, trackY, paint)
        }

        // 2. 스텝 점 (thumb 위치 제외)
        for (i in 1..max) {
            if (i == progress) continue
            val x = progressToX(i)
            if (i < progress) {
                canvas.drawCircle(x, trackY, activeDotR, activeDotPaint)
            } else {
                canvas.drawCircle(x, trackY, inactiveDotR, inactiveDotPaint)
            }
        }


        // 3. Thumb 그림자 (Figma: 0px 0px 1.2px rgba(0,0,0,0.1))
        canvas.drawCircle(thumbX, trackY, thumbR, thumbShadowPaint)

        // 4. Thumb 본체 - Figma gradient: -71.7deg, #F8FBFF → #F5F5F5
        val angleRad = Math.toRadians(-71.705)
        val dx = (cos(angleRad) * thumbR).toFloat()
        val dy = (sin(angleRad) * thumbR).toFloat()
        val gradient = LinearGradient(
            thumbX - dx, trackY - dy,
            thumbX + dx, trackY + dy,
            Color.parseColor("#F8FBFF"),
            Color.parseColor("#F5F5F5"),
            Shader.TileMode.CLAMP
        )
        thumbPaint.shader = gradient
        canvas.drawCircle(thumbX, trackY, thumbR, thumbPaint)

        // 5. Thumb 숫자
        val text = progress.toString()
        val bounds = Rect()
        thumbTextPaint.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(text, thumbX, trackY - bounds.centerY().toFloat(), thumbTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEditEnabled) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                progress = xToProgress(event.x)
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    fun setSliderColors(active: Int, inactive: Int) {
        this.activeColor = active
        this.inactiveColor = inactive
        invalidate() // 다시 그려라! (갈아 끼운 것 반영)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Figma 슬라이더 높이 26dp (thumb 24dp + 여백)
        val desiredH = (26f * dp).toInt()
        val h = resolveSize(desiredH, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY))
    }
}