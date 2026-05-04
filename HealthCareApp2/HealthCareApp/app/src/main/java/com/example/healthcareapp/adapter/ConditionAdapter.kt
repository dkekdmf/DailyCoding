package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.ConditionRecord
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.ItemCondition1Binding
import com.example.healthcareapp.databinding.ItemConditionQuestionBinding
import com.example.healthcareapp.databinding.ItemBodyPartSelectionBinding

class ConditionAdapter(private val items: List<ConditionRecord>) :
    RecyclerView.Adapter<ConditionAdapter.ViewHolder>() {

    private val chipIdToKey = mapOf(
        R.id.chip_head to "머리/목",
        R.id.chip_upper to "상체",
        R.id.chip_arm to "팔/손",
        R.id.chip_lower to "하체",
        R.id.chip_foot to "발"
    )

    private val bodyDataMap = mapOf(
        "앞면" to mapOf(
            "머리/목" to listOf("머리", "이마", "얼굴", "목"),
            "상체" to listOf("어깨", "가슴", "윗배", "아랫배", "옆구리"),
            "팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
            "하체" to listOf("고관절", "사타구니", "생식기", "허벅지", "무릎", "정강이"),
            "발" to listOf("발목", "발등", "발가락")
        ),
        "뒷면" to mapOf(
            "머리/목" to listOf("경추 (목뼈 부위)"),
            "상체" to listOf("등", "어깨", "날개(견갑골)", "허리", "꼬리뼈"),
            "팔/손" to listOf("윗팔", "팔꿈치", "아랫팔", "손목", "손바닥", "손가락"),
            "하체" to listOf("엉덩이", "뒷허벅지", "오금", "종아리"),
            "발" to listOf("아킬레스건", "발바닥")
        )
    )

    private var currentDirection = "앞면"

    inner class ViewHolder(val binding: ItemCondition1Binding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCondition1Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        // --- [1] 헤더 로직 ---
        binding.tvConditionTitle.text = item.title
        binding.layoutDetail.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
        binding.ivArrow.rotation = if (item.isExpanded) 180f else 0f

        binding.layoutHeader.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }

        // --- [2] 1번 질문 (통증) ---
        if (item.questions.isNotEmpty()) {
            val firstQ = item.questions[0]
            binding.layoutFirstQuestion.apply {
                tvStepCount.text = "1/5"
                tvQuestionTitle.text = "평소와 다른 관절이나 근육 통증이 있나요?"
                tvMinLabel.text = "매우 심함"
                tvMaxLabel.text = "통증 없음"

                slider.valueFrom = 1f
                slider.valueTo = 10f
                slider.stepSize = 1f
                slider.value = if (firstQ.score < 1f) 10f else firstQ.score

                updateSliderGuideByQuestion(tvSliderGuide, 0, slider.value.toInt())

                slider.clearOnChangeListeners()
                slider.addOnChangeListener { _, value, _ ->
                    firstQ.score = value
                    updateSliderGuideByQuestion(tvSliderGuide, 0, value.toInt())
                }
            }
        }

        // 서브 질문 리스트 (2~5번)
        if (item.isShowAllQuestions) {
            binding.btnShowAll.visibility = View.GONE
            binding.rvRestQuestions.visibility = View.VISIBLE

            val restQuestions = item.questions.drop(1)
            binding.rvRestQuestions.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = QuestionsSubAdapter(restQuestions)
                isNestedScrollingEnabled = false
            }
        } else {
            binding.btnShowAll.visibility = View.VISIBLE
            binding.rvRestQuestions.visibility = View.GONE
        }

        binding.btnShowAll.setOnClickListener {
            item.isShowAllQuestions = true
            notifyItemChanged(position)
        }

        setupBodySelection(binding)
        binding.etFeedbackMemo.setText(item.memo)
    }

    // --- 질문별 통합 가이드 로직 ---
    private fun updateSliderGuideByQuestion(textView: TextView, questionIndex: Int, score: Int) {
        val description = when (questionIndex) {
            0 -> getPainGuide(score)
            1 -> getSleepTimeGuide(score)
            2 -> getSleepQualityGuide(score)
            3 -> getFatigueGuide(score)
            4 -> getOverallConditionGuide(score)
            else -> "$score - 단계"
        }
        textView.text = "$score - $description"
    }

    private fun getPainGuide(score: Int): String = when (score) {
        1 -> "매우 심함 / 운동이 어려움"
        2 -> "일상 움직임도 불편함"
        3 -> "운동 시 불편이 큼"
        4 -> "움직일 때 거슬리는 수준"
        5 -> "통증이 분명히 느껴짐"
        6 -> "신경은 쓰이지만 운동 가능"
        7 -> "약간 불편한 정도"
        8 -> "아주 약하게 느껴짐"
        9 -> "거의 느껴지지 않음"
        10 -> "통증 없음"
        else -> ""
    }

    private fun getSleepTimeGuide(score: Int): String = when (score) {
        1 -> "1시간 수준 / 거의 못 잠"
        2 -> "2시간 / 매우 부족"
        3 -> "3시간 / 많이 부족"
        4 -> "4시간 / 부족"
        5 -> "5시간 / 약간 부족"
        6 -> "6시간 / 다소 부족"
        7 -> "7시간 / 보통"
        8 -> "8시간 / 적절"
        9 -> "9시간 / 충분"
        10 -> "10시간 / 매우 충분"
        else -> ""
    }

    private fun getSleepQualityGuide(score: Int): String = when (score) {
        1 -> "거의 못 잠"
        2 -> "자주 깨고 매우 피곤함"
        3 -> "여러 번 깨고 피로함"
        4 -> "뒤척임 많고 개운하지 않음"
        5 -> "잤지만 개운하지 않음"
        6 -> "보통"
        7 -> "비교적 잘 잠"
        8 -> "깊게 잔 편"
        9 -> "거의 안 깨고 개운함"
        10 -> "푹 자고 매우 개운함"
        else -> ""
    }

    private fun getFatigueGuide(score: Int): String = when (score) {
        1 -> "매우 많이 남아있음"
        2 -> "많이 남아있음"
        3 -> "꽤 남아있음"
        4 -> "남아있는 편"
        5 -> "어느 정도 남아있음"
        6 -> "조금 남아있음"
        7 -> "약간 남아있음"
        8 -> "거의 없음"
        9 -> "아주 미세함"
        10 -> "전혀 없음"
        else -> ""
    }

    private fun getOverallConditionGuide(score: Int): String = when (score) {
        1 -> "매우 안 좋음 / 많이 지치고 힘든 상태"
        2 -> "많이 안 좋음 / 몸과 마음이 무거운 상태"
        3 -> "안 좋은 편 / 피로감이 큰 상태"
        4 -> "다소 안 좋음 / 불편하고 무거운 느낌"
        5 -> "보통 이하 / 썩 좋지는 않은 상태"
        6 -> "무난함 / 크게 나쁘지 않은 상태"
        7 -> "괜찮은 편 / 비교적 안정된 상태"
        8 -> "좋은 편 / 몸과 마음이 비교적 가벼움"
        9 -> "매우 좋음 / 활력이 있고 안정적임"
        10 -> "최상 / 몸과 마음이 매우 가볍고 개운함"
        else -> ""
    }

    private fun setupBodySelection(binding: ItemCondition1Binding) {
        binding.btnFront.setOnClickListener {
            currentDirection = "앞면"
            updateDirectionUI(binding)
            updateBodyPartsList(binding)
        }
        binding.btnBack.setOnClickListener {
            currentDirection = "뒷면"
            updateDirectionUI(binding)
            updateBodyPartsList(binding)
        }
        binding.chipGroupBody.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) updateBodyPartsList(binding)
        }
        updateDirectionUI(binding)
        updateBodyPartsList(binding)
    }

    private fun updateDirectionUI(binding: ItemCondition1Binding) {
        val context = binding.root.context
        val activeColor = ContextCompat.getColor(context, R.color.front_black)
        val inactiveColor = ContextCompat.getColor(context, R.color.back_gray)

        if (currentDirection == "앞면") {
            binding.btnFront.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnFront.setTextColor(activeColor)
            binding.btnBack.setBackgroundResource(android.R.color.transparent)
            binding.btnBack.setTextColor(inactiveColor)
        } else {
            binding.btnBack.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.btnBack.setTextColor(activeColor)
            binding.btnFront.setBackgroundResource(android.R.color.transparent)
            binding.btnFront.setTextColor(inactiveColor)
        }
    }

    private fun updateBodyPartsList(binding: ItemCondition1Binding) {
        val selectedChipId = binding.chipGroupBody.checkedChipId
        val bodyKey = chipIdToKey[selectedChipId] ?: "머리/목"
        val detailList = bodyDataMap[currentDirection]?.get(bodyKey) ?: emptyList()

        binding.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BodyPartDetailAdapter(detailList)
        }
    }

    override fun getItemCount(): Int = items.size

    private inner class QuestionsSubAdapter(private val qList: List<StatusQuestion1>) :
        RecyclerView.Adapter<QuestionsSubAdapter.QViewHolder>() {

        inner class QViewHolder(val binding: ItemConditionQuestionBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QViewHolder {
            val binding = ItemConditionQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return QViewHolder(binding)
        }

        override fun onBindViewHolder(holder: QViewHolder, position: Int) {
            val q = qList[position]
            val actualIndex = position + 1

            holder.binding.apply {
                tvStepCount.text = "${actualIndex + 1}/5"
                tvQuestionTitle.text = when(actualIndex) {
                    1 -> "오늘 수면 시간이 어떻게 되시나요?"
                    2 -> "수면의 질은 어떠셨나요?"
                    3 -> "전 날 운동의 피로가 남아있나요?"
                    4 -> "현재 몸과 마음의 컨디션은 어떤가요?"
                    else -> q.title
                }

                when(actualIndex) {
                    1, 2 -> {
                        tvMinLabel.text = "매우 부족/낮음"
                        tvMaxLabel.text = "매우 충분/좋음"
                    }
                    3 -> {
                        tvMinLabel.text = "매우 많이 남음"
                        tvMaxLabel.text = "전혀 없음"
                    }
                    4 -> {
                        tvMinLabel.text = "매우 안 좋음"
                        tvMaxLabel.text = "최상의 컨디션"
                    }
                    else -> {
                        tvMinLabel.text = "매우 심함"
                        tvMaxLabel.text = "통증 없음"
                    }
                }

                slider.valueFrom = 1f
                slider.valueTo = 10f
                slider.stepSize = 1f
                // 초기값 설정: 4, 5번 질문은 긍정적인 쪽(높은 점수)으로 기본값 세팅
                slider.value = if (q.score < 1f) (if (actualIndex >= 3) 7f else 10f) else q.score

                updateSliderGuideByQuestion(tvSliderGuide, actualIndex, slider.value.toInt())

                slider.clearOnChangeListeners()
                slider.addOnChangeListener { _, value, _ ->
                    q.score = value
                    updateSliderGuideByQuestion(tvSliderGuide, actualIndex, value.toInt())
                }
            }
        }
        override fun getItemCount(): Int = qList.size
    }

    private class BodyPartDetailAdapter(private val parts: List<String>) :
        RecyclerView.Adapter<BodyPartDetailAdapter.BodyViewHolder>() {
        inner class BodyViewHolder(val binding: ItemBodyPartSelectionBinding) : RecyclerView.ViewHolder(binding.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BodyViewHolder {
            val binding = ItemBodyPartSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BodyViewHolder(binding)
        }
        override fun onBindViewHolder(holder: BodyViewHolder, position: Int) {
            holder.binding.tvPartName.text = parts[position]
        }
        override fun getItemCount(): Int = parts.size
    }
}