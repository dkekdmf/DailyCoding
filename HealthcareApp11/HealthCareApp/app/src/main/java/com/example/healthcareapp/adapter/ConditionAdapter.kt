package com.example.healthcareapp.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.ConditionRecord
import com.example.healthcareapp.data.StatusQuestion1
import com.example.healthcareapp.databinding.ItemCondition1Binding
import com.example.healthcareapp.databinding.ItemConditionQuestionBinding
import com.example.healthcareapp.databinding.ItemBodyPartSelectionBinding
import com.example.healthcareapp.sheet.PainBottomSheetFragment

class ConditionAdapter(private val items: List<ConditionRecord>) :
    RecyclerView.Adapter<ConditionAdapter.ViewHolder>() {

    private var editingPosition: Int = -1

    // 🎯 [부모-자식 원격 스위치] 외부 WorkoutActivity에서 밀어주는 수정 권한 플래그
    var isEditMode: Boolean = false

    private val chipIdToKey = mapOf(
        R.id.chip_head to "머리/목", R.id.chip_upper to "상체",
        R.id.chip_arm to "팔/손", R.id.chip_lower to "하체", R.id.chip_foot to "발"
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
        val context = binding.root.context
        val currentPos = holder.bindingAdapterPosition

        // ❌ 기존 코드: val isEditMode = (editingPosition == currentPos)
        // 🔓 [교정 완료] 이름을 완전히 분리하여 외부 마스터 스위치 플래그(this.isEditMode)를 완벽히 살려냅니다!
        val isRowEditing = (editingPosition == currentPos)

        // 🎯 마스터 원격 스위치와 행 자체 편집 권한을 결합합니다.
        val isFinalEditMode = this.isEditMode || isRowEditing
        binding.tvConditionTitle.text = item.title
        binding.layoutDetail.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
        binding.ivArrow.rotation = if (item.isExpanded) 180f else 0f

        binding.layoutHeader.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(currentPos)
        }

        val btnEditView = binding.root.findViewById<FrameLayout>(R.id.btn_edit)
        btnEditView?.setOnClickListener {
            val previousEditingPosition = editingPosition
            editingPosition = if (editingPosition == currentPos) -1 else currentPos
            if (previousEditingPosition != -1) notifyItemChanged(previousEditingPosition)
            notifyItemChanged(currentPos)
        }

        // 🎯 [터치 잠금 가드] 수정 모드일 때만 통증 바텀시트가 열리도록 방어합니다.
        binding.pain1.setOnClickListener {
            if (isFinalEditMode && context is FragmentActivity) {

                // 🔓 [교정 완료] 생성자에 기본 타이틀("부위")을 명시하고, 콜백 인자를 3개로 확장합니다.
                val bottomSheet = PainBottomSheetFragment("부위") { direction, stage, memo ->

                    // 피그마 출력 규격에 맞게 텍스트 조합
                    val finalResultText = "$direction 부위 | 통증정도: ${stage}단계"

                    // 재훈님이 짜두신 기존 뷰 바인딩 및 DTO 데이터 저장 로직 100% 그대로 연동
                    binding.pain1.findViewById<TextView>(R.id.tv_pain_tag_content)?.text = finalResultText
                    item.title = finalResultText // 수정한 통증 태그 데이터 임시 저장
                }
                bottomSheet.show(context.supportFragmentManager, "PainBottomSheet")
            }
        }
        // [실데이터 복원] 1번 질문 처리
        if (item.questions.isNotEmpty()) {
            val firstQ = item.questions[0]
            binding.layoutFirstQuestion.apply {
                tvStepCount.text = "1/5"
                tvQuestionTitle.text = "운동 후 평소와 다른 관절이나\n근육 통증이 있었나요?"
                tvMinLabel.text = "매우 심함"
                tvMaxLabel.text = "통증 없음"

                val seekBar = root.findViewById<SeekBar>(R.id.slider)
                seekBar.max = 10

                // 🎯 [실시간 락 제어] 원격 수정 모드 상태를 썸네일 스타일과 터치 권한에 실시간 바인딩!
                seekBar.isEnabled = isFinalEditMode
                updateSeekBarStyle(seekBar, isFinalEditMode)

                val initialScore = if (firstQ.score < 1f) 10 else firstQ.score.toInt()
                seekBar.progress = initialScore
                updateSliderGuideByQuestion(tvSliderGuide, 0, initialScore)
                seekBar.thumb = createThumbWithText(context, initialScore.toString(), isFinalEditMode)

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        val finalScore = if (progress < 1) 1 else progress
                        firstQ.score = finalScore.toFloat()
                        updateSliderGuideByQuestion(tvSliderGuide, 0, finalScore)
                        p0?.thumb = createThumbWithText(context, finalScore.toString(), isFinalEditMode)
                    }
                    override fun onStartTrackingTouch(p0: SeekBar?) {}
                    override fun onStopTrackingTouch(p0: SeekBar?) {}
                })
            }
        }

        if (item.isShowAllQuestions) {
            binding.btnShowAll.visibility = View.GONE
            binding.rvRestQuestions.visibility = View.VISIBLE
            binding.rvRestQuestions.apply {
                layoutManager = LinearLayoutManager(context)
                // 🎯 2~5번 하위 서브 문항에도 최종 통제 플래그(isFinalEditMode)를 완벽 전송합니다.
                adapter = QuestionsSubAdapter(item.questions.drop(1), isFinalEditMode)
                isNestedScrollingEnabled = false
            }
        } else {
            binding.btnShowAll.visibility = View.VISIBLE
            binding.rvRestQuestions.visibility = View.GONE
        }

        binding.btnShowAll.setOnClickListener {
            item.isShowAllQuestions = true
            notifyItemChanged(currentPos)
        }

        // 🎯 [터치 잠금 가드] 수정 모드일 때만 앞/뒷면 및 부위 칩 선택이 동작하도록 UI 바인딩 통제
        binding.btnFront.isEnabled = isFinalEditMode
        binding.btnBack.isEnabled = isFinalEditMode
        for (i in 0 until binding.chipGroupBody.childCount) {
            binding.chipGroupBody.getChildAt(i).isEnabled = isFinalEditMode
        }
        setupBodySelection(binding)

        // 🎯 [실데이터 복원 및 메모장 락]
        binding.etFeedbackMemo.setText(item.memo)
        binding.etFeedbackMemo.isEnabled = isFinalEditMode
        binding.etFeedbackMemo.isFocusableInTouchMode = isFinalEditMode

        // 유저가 텍스트를 고칠 때마다 바로 DTO(item)에 실시간 캐싱 동기화 처리
        binding.etFeedbackMemo.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item.memo = binding.etFeedbackMemo.text.toString()
            }
        }

        binding.pain1.findViewById<TextView>(R.id.tv_pain_tag_content)?.text = item.title
    }

    private fun updateSeekBarStyle(seekBar: SeekBar, isEditMode: Boolean) {
        val blueColor = Color.parseColor("#3B82F6")
        val grayColor = Color.parseColor("#94A3B8")
        seekBar.progressTintList = ColorStateList.valueOf(if (isEditMode) blueColor else grayColor)
    }

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
        1 -> "매우 심함 / 운동이 어려움"; 2 -> "일상 움직임도 불편함"; 3 -> "운동 시 불편이 큼"; 4 -> "움직일 때 거슬리는 수준"; 5 -> "통증이 분명히 느껴짐"
        6 -> "신경은 쓰이지만 운동 가능"; 7 -> "약간 불편한 정도"; 8 -> "아주 약하게 느껴짐"; 9 -> "거의 느껴지지 않음"; 10 -> "통증 없음"
        else -> ""
    }

    private fun getSleepTimeGuide(score: Int): String = when (score) {
        1 -> "1시간 수준 / 거의 못 잠"; 2 -> "2시간 / 매우 부족"; 3 -> "3시간 / 많이 부족"; 4 -> "4시간 / 부족"; 5 -> "5시간 / 약간 부족"
        6 -> "6시간 / 다소 부족"; 7 -> "7시간 / 보통"; 8 -> "8시간 / 적절"; 9 -> "9시간 / 충분"; 10 -> "10시간 / 매우 충분"
        else -> ""
    }

    private fun getSleepQualityGuide(score: Int): String = when (score) {
        1 -> "거의 못 잠"; 2 -> "자주 깨고 매우 피곤함"; 3 -> "여러 번 깨고 피로함"; 4 -> "뒤척임 많고 개운하지 않음"; 5 -> "잤지만 개운하지 않음"
        6 -> "보통"; 7 -> "비교적 잘 잠"; 8 -> "깊게 잔 편"; 9 -> "거의 안 깨고 개운함"; 10 -> "푹 자고 매우 개운함"
        else -> ""
    }

    private fun getFatigueGuide(score: Int): String = when (score) {
        1 -> "매우 많이 남아있음"; 2 -> "많이 남아있음"; 3 -> "꽤 남아있음"; 4 -> "남아있는 편"; 5 -> "어느 정도 남아있음"
        6 -> "조금 남아있음"; 7 -> "약간 남아있음"; 8 -> "거의 없음"; 9 -> "아주 미세함"; 10 -> "전혀 없음"
        else -> ""
    }

    private fun getOverallConditionGuide(score: Int): String = when (score) {
        1 -> "매우 안 좋음 / 많이 지치고 힘든 상태"; 2 -> "많이 안 좋음 / 몸과 마음이 무거운 상태"; 3 -> "안 좋은 편 / 피로감이 큰 상태"
        4 -> "다소 안 좋음 / 불편하고 무거운 느낌"; 5 -> "보통 이하 / 썩 좋지는 않은 상태"; 6 -> "무난함 / 크게 나쁘지 않은 상태"
        7 -> "괜찮은 편 / 비교적 안정된 상태"; 8 -> "좋은 편 / 몸과 마음이 비교적 가벼움"; 9 -> "매우 좋음 / 활력이 있고 안정적임"; 10 -> "최상 / 몸과 마음이 매우 가볍고 개운함"
        else -> ""
    }

    private fun setupBodySelection(binding: ItemCondition1Binding) {
        binding.btnFront.setOnClickListener { currentDirection = "앞면"; updateDirectionUI(binding); updateBodyPartsList(binding) }
        binding.btnBack.setOnClickListener { currentDirection = "뒷면"; updateDirectionUI(binding); updateBodyPartsList(binding) }
        binding.chipGroupBody.setOnCheckedStateChangeListener { _, checkedIds -> if (checkedIds.isNotEmpty()) updateBodyPartsList(binding) }
    }

    private fun updateDirectionUI(binding: ItemCondition1Binding) {
        val context = binding.root.context
        val activeColor = ContextCompat.getColor(context, R.color.front_black)
        val inactiveColor = ContextCompat.getColor(context, R.color.back_gray)
        if (currentDirection == "앞면") {
            binding.btnFront.setBackgroundResource(R.drawable.bg_tab_selected); binding.btnFront.setTextColor(activeColor)
            binding.btnBack.setBackgroundResource(android.R.color.transparent); binding.btnBack.setTextColor(inactiveColor)
        } else {
            binding.btnBack.setBackgroundResource(R.drawable.bg_tab_selected); binding.btnBack.setTextColor(activeColor)
            binding.btnFront.setBackgroundResource(android.R.color.transparent); binding.btnFront.setTextColor(inactiveColor)
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

    private fun createThumbWithText(context: Context, text: String, isEditMode: Boolean): Drawable {
        val size = 115
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE; canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 4f; paint.color = Color.parseColor("#E2E8F0"); canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
        paint.style = Paint.Style.FILL; paint.color = if (isEditMode) Color.parseColor("#3B82F6") else Color.parseColor("#64748B")
        paint.textSize = 44f; paint.textAlign = Paint.Align.CENTER; paint.isFakeBoldText = true
        val textBounds = Rect(); paint.getTextBounds(text, 0, text.length, textBounds)
        val yPos = (canvas.height / 2f) - (textBounds.centerY()); canvas.drawText(text, size / 2f, yPos, paint)
        return BitmapDrawable(context.resources, bitmap)
    }

    override fun getItemCount(): Int = items.size

    private inner class QuestionsSubAdapter(private val qList: List<StatusQuestion1>, private val isEditMode: Boolean) : RecyclerView.Adapter<QuestionsSubAdapter.QViewHolder>() {
        inner class QViewHolder(val binding: ItemConditionQuestionBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QViewHolder {
            val binding = ItemConditionQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return QViewHolder(binding)
        }

        override fun onBindViewHolder(holder: QViewHolder, position: Int) {
            val q = qList[position]
            val context = holder.binding.root.context
            val actualQuestionNum = position + 2

            val questionTitles = listOf("오늘 수면 시간이 어떻게 되시나요?", "수면의 질은 어떠셨나요?", "전 날 운동의 피로가 남아있나요?", "현재 몸과 마음의 컨디션은 어떤가요?")
            val minLabels = listOf("1시간", "거의 못 잠", "매우 많이 남아있음", "매우 안 좋음")
            val maxLabels = listOf("10시간", "매우 개운함", "전혀 없음", "최상")

            holder.binding.apply {
                tvStepCount.text = "$actualQuestionNum/5"
                tvQuestionTitle.text = questionTitles.getOrNull(position) ?: ""
                tvMinLabel.text = minLabels.getOrNull(position) ?: ""
                tvMaxLabel.text = maxLabels.getOrNull(position) ?: ""

                val seekBar = root.findViewById<SeekBar>(R.id.slider)
                seekBar.max = 10

                // 🎯 2~5번 하위 슬라이더도 원격 수정 모드에 연동 잠금 처리!
                seekBar.isEnabled = isEditMode
                updateSeekBarStyle(seekBar, isEditMode)

                val initialScore = if (q.score < 1f) (if (actualQuestionNum >= 4) 7 else 10) else q.score.toInt()
                seekBar.progress = initialScore
                updateSliderGuideByQuestion(tvSliderGuide, actualQuestionNum - 1, initialScore)
                seekBar.thumb = createThumbWithText(context, initialScore.toString(), isEditMode)

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        val finalScore = if (progress < 1) 1 else progress
                        q.score = finalScore.toFloat()
                        updateSliderGuideByQuestion(tvSliderGuide, actualQuestionNum - 1, finalScore)
                        p0?.thumb = createThumbWithText(context, finalScore.toString(), isEditMode)
                    }
                    override fun onStartTrackingTouch(p0: SeekBar?) {}
                    override fun onStopTrackingTouch(p0: SeekBar?) {}
                })
            }
        }

        override fun getItemCount(): Int = qList.size
    }

    private inner class BodyPartDetailAdapter(private val parts: List<String>) :
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