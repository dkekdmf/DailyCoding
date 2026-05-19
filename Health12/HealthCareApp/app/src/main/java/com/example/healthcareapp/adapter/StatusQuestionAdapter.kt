package com.example.healthcareapp.adapter

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.ItemConditionQuestionBinding

class StatusQuestionAdapter(private val questions: List<StatusQuestion>) :
    RecyclerView.Adapter<StatusQuestionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemConditionQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConditionQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questions[position]
        val context = holder.binding.root.context

        holder.binding.apply {
            tvStepCount.text = "${item.id}/5"
            tvQuestionTitle.text = item.title
            tvMinLabel.text = item.minLabel
            tvMaxLabel.text = item.maxLabel

            val seekBar = root.findViewById<SeekBar>(R.id.slider)
            seekBar.max = 10

            // 🎯 뷰 재사용 시 상태 초기화
            seekBar.progress = item.score
            tvSliderGuide.text = item.guides[item.score] ?: "${item.score} - 선택됨"

            // 🎯 썸네일 갱신 (중요: progress 설정 직후 호출)
            seekBar.thumb = createThumbWithText(context, item.score.toString())

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    // 사용자가 값을 변경할 때 0 이하로 안 떨어지게 방어
                    val finalScore = if (progress < 1) 1 else progress

                    item.score = finalScore
                    tvSliderGuide.text = item.guides[finalScore] ?: "$finalScore - 선택됨"

                    // 🎯 핸들 텍스트 업데이트
                    p0?.thumb = createThumbWithText(context, finalScore.toString())
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
        }
    }

    private fun createThumbWithText(context: Context, text: String): Drawable {
        val size = 120 // 핸들 크기
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. 은은한 그림자 효과
        paint.color = Color.parseColor("#10000000")
        canvas.drawCircle(size / 2f, size / 2f, size / 2.1f, paint)

        // 2. 메인 흰색 원
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, size / 2.4f, paint)

        // 3. 테두리 (이미지처럼 아주 연한 회색)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawCircle(size / 2f, size / 2f, size / 2.4f, paint)

        // 4. 숫자 텍스트 (이미지의 연한 회색/하늘색 톤)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 46f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val yPos = (canvas.height / 2f) - (textBounds.centerY())
        canvas.drawText(text, size / 2f, yPos, paint)

        return BitmapDrawable(context.resources, bitmap)
    }

    override fun getItemCount(): Int = questions.size
}