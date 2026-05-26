package com.example.healthcareapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.ItemConditionQuestionBinding

class ConditionCheckAdapter(
    private val questions: List<StatusQuestion1>,
    private var isEditMode: Boolean = true
) : RecyclerView.Adapter<ConditionCheckAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemConditionQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConditionQuestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val q = questions[position]

        // 🎯 에러 방지: 멤버 변수인 'isEditMode'를 그대로 사용하세요!
        // 별도의 isFinalEditMode 선언 없이 바로 쓰면 됩니다.

        holder.binding.apply {
            tvStepCount.text = "${position + 1}/5"
            setupQuestionText(this, position + 1)

            val dotSlider = slider

            // 1. 슬라이더 활성화 여부
            dotSlider.isEditEnabled = this@ConditionCheckAdapter.isEditMode
            dotSlider.max = 10

            // 2. 색상 적용 (isEditMode가 true면 파란색, false면 회색)
            val activeColor = if (this@ConditionCheckAdapter.isEditMode)
                Color.parseColor("#3B82F6") else Color.parseColor("#94A3B8")

            dotSlider.setSliderColors(activeColor, Color.parseColor("#D8E1ED"))

            val initialScore = if (q.score < 1f) 8 else q.score.toInt()
            q.score = initialScore.toFloat()
            dotSlider.progress = initialScore

            updateSliderGuideByQuestion(tvSliderGuide, position, initialScore)

            dotSlider.onProgressChanged = { value ->
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    questions[currentPos].score = value.toFloat()
                    updateSliderGuideByQuestion(tvSliderGuide, currentPos, value)
                }
            }
        }
    }

    override fun getItemCount(): Int = questions.size

    private fun setupQuestionText(binding: ItemConditionQuestionBinding, num: Int) {
        when (num) {
            1 -> { binding.tvQuestionTitle.text = "평소와 다른 관절이나근육 통증이 있었나요?"; binding.tvMinLabel.text = "매우 심함"; binding.tvMaxLabel.text = "통증 없음" }
            2 -> { binding.tvQuestionTitle.text = "오늘 수면 시간이 어떻게 되시나요?"; binding.tvMinLabel.text = "1시간"; binding.tvMaxLabel.text = "10시간" }
            3 -> { binding.tvQuestionTitle.text = "수면의 질은 어떠셨나요?"; binding.tvMinLabel.text = "거의 못 잠"; binding.tvMaxLabel.text = "매우 개운함" }
            4 -> { binding.tvQuestionTitle.text = "전 날 운동의 피로가 남아있나요?"; binding.tvMinLabel.text = "매우 많이 남아있음"; binding.tvMaxLabel.text = "전혀 없음" }
            5 -> { binding.tvQuestionTitle.text = "현재 몸과 마음의 컨디션은 어떠신가요?"; binding.tvMinLabel.text = "매우 안 좋음"; binding.tvMaxLabel.text = "최상" }
        }
    }

    private fun updateSliderGuideByQuestion(textView: TextView, index: Int, score: Int) {
        val description = when (index) {
            0 -> getPainGuide(score)
            1 -> getSleepTimeGuide(score)
            2 -> getSleepQualityGuide(score)
            3 -> getFatigueGuide(score)
            4 -> getOverallConditionGuide(score)
            else -> "$score 단계"
        }
        textView.text = "$score - $description"
    }

    private fun getPainGuide(s: Int) = when(s) {
        1 -> "매우 심함 / 운동이 어려움"; 2 -> "일상 움직임도 불편함"; 3 -> "운동 시 불편이 큼"; 4 -> "움직일 때 거슬리는 수준"; 5 -> "통증이 분명히 느껴짐"; 6 -> "신경은 쓰이지만 운동 가능"; 7 -> "약간 불편한 정도"; 8 -> "아주 약하게 느껴짐"; 9 -> "거의 느껴지지 않음"; 10 -> "통증 없음"; else -> ""
    }
    private fun getSleepTimeGuide(s: Int) = when(s) {
        1 -> "1시간 수준 / 거의 못 잠"; 2 -> "2시간 / 매우 부족"; 3 -> "3시간 / 많이 부족"; 4 -> "4시간 / 부족"; 5 -> "5시간 / 약간 부족"; 6 -> "6시간 / 다소 부족"; 7 -> "7시간 / 보통"; 8 -> "8시간 / 적절"; 9 -> "9시간 / 충분"; 10 -> "10시간 / 매우 충분"; else -> ""
    }
    private fun getSleepQualityGuide(s: Int) = when(s) {
        1 -> "거의 못 잠"; 2 -> "자주 깨고 매우 피곤함"; 3 -> "여러 번 깨고 피로함"; 4 -> "뒤척임 많고 개운하지 않음"; 5 -> "잤지만 개운하지 않음"; 6 -> "보통"; 7 -> "비교적 잘 잠"; 8 -> "깊게 잔 편"; 9 -> "거의 안 깨고 개운함"; 10 -> "푹 자고 매우 개운함"; else -> ""
    }
    private fun getFatigueGuide(s: Int) = when(s) {
        1 -> "매우 많이 남아있음"; 2 -> "많이 남아있음"; 3 -> "꽤 남아있음"; 4 -> "남아있는 편"; 5 -> "어느 정도 남아있음"; 6 -> "조금 남아있음"; 7 -> "약간 남아있음"; 8 -> "거의 없음"; 9 -> "아주 미세함"; 10 -> "전혀 없음"; else -> ""
    }
    private fun getOverallConditionGuide(s: Int) = when(s) {
        1 -> "매우 안 좋음 / 많이 지치고 힘든 상태"; 2 -> "많이 안 좋음 / 몸과 마음이 무거운 상태"; 3 -> "안 좋은 편 / 피로감이 큰 상태"; 4 -> "다소 안 좋음 / 불편하고 무거운 느낌"; 5 -> "보통 이하 / 썩 좋지는 않은 상태"; 6 -> "무난함 / 크게 나쁘지 않은 상태"; 7 -> "괜찮은 편 / 비교적 안정된 상태"; 8 -> "좋은 편 / 몸과 마음이 비교적 가벼움"; 9 -> "매우 좋음 / 활력이 있고 안정적임"; 10 -> "최상 / 몸과 마음이 매우 가볍고 개운함"; else -> ""
    }

    fun setEditMode(enabled: Boolean) {
        this.isEditMode = enabled
        notifyDataSetChanged() // 🎯 [핵심] 이 줄이 있어야 화면이 다시 그려집니다!
    }
}