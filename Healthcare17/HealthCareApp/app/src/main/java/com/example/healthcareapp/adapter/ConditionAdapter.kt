package com.example.healthcareapp.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
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
import com.example.healthcareapp.widget.Slidercustom
import com.google.android.material.chip.Chip
import java.util.Locale

class ConditionAdapter(private val items: List<ConditionRecord>) :
    RecyclerView.Adapter<ConditionAdapter.ViewHolder>() {

    private var editingPosition: Int = -1
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
        val activeColor = Color.parseColor("#3B82F6") // 파란색
        val inactiveColor = Color.parseColor("#CBD5E1") // 회색
        val isRowEditing = (editingPosition == currentPos)
        val isFinalEditMode = this.isEditMode || isRowEditing
        val toggleListener = View.OnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(currentPos)
        }
        binding.ivArrow.setOnClickListener(toggleListener)
        binding.tvConditionTitle.setOnClickListener(toggleListener)

        binding.tvConditionTitle.text = item.title

        val displayTime = item.time
        binding.tvFinishStatus.text = "$displayTime 작성 완료"

        binding.layoutDetail.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
        binding.ivArrow.rotation = if (item.isExpanded) 180f else 0f

        if (item.questions.isNotEmpty()) {
            val firstQ = item.questions[0]
            val initialScore = if (firstQ.score > 0f) firstQ.score.toInt() else 10
            firstQ.score = initialScore.toFloat()

            binding.layoutFirstQuestion.apply {
                val dotSlider = root.findViewById<Slidercustom>(R.id.slider)
                updateSeekBarStyle(dotSlider, isFinalEditMode)
                // 1. 모드에 따라 색상 결정
                val active = if (isFinalEditMode) Color.parseColor("#3B82F6") else Color.parseColor("#CBD5E1")
                val inactive = Color.parseColor("#D8E1ED")

                // 2. setSliderColors 함수로 색상 전달 (내부에서 invalidate 호출됨)
                dotSlider.setSliderColors(active, inactive)


                // 3. 상태 적용
                dotSlider.isEditEnabled = isFinalEditMode
                dotSlider.max = 10
                dotSlider.progress = initialScore

                updateSliderGuideByQuestion(tvSliderGuide, 0, initialScore)

                dotSlider.onProgressChanged = { value ->
                    firstQ.score = value.toFloat()
                    updateSliderGuideByQuestion(tvSliderGuide, 0, value)
                }
            }
        }

        if (item.isShowAllQuestions) {
            binding.btnShowAll.visibility = View.GONE
            binding.rvRestQuestions.visibility = View.VISIBLE
            binding.rvRestQuestions.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = QuestionsSubAdapter(item.questions.drop(1), isFinalEditMode)
                isNestedScrollingEnabled = false
            }
        } else {
            binding.btnShowAll.visibility = View.VISIBLE
            binding.rvRestQuestions.visibility = View.GONE
        }
        binding.btnShowAll.setOnClickListener { item.isShowAllQuestions = true; notifyItemChanged(currentPos) }

        // ==================================================================
        // 🎯 [정밀 수술 구역] 상호 오염 방지 및 데이터 강제 제자리 배치 가드 엔진
        // ==================================================================
        // ==================================================================
        // 🎯 [정밀 수술 구역] 변수 불일치 에러 완벽 진압 및 교통정리 엔진
        // ==================================================================
        var frontCount = 0
        var backCount = 0
        var parsedPainDisplay: String? = null

        // 💡 [해결 핵심] 에러가 났던 rawMemo와 rawConditionMemo, rawPainTag의 이름을 명확하게 강제 매칭합니다.
        val rawMemo = item.memo ?: ""
        val rawConditionMemo = item.memo ?: "" // 둘 다 안전하게 item.memo를 바라보게 세팅
        val rawPainTag = item.painTag ?: ""

        // 💡 [Step 1] 통증 데이터 확보: 세 변수 중 "통증"이나 "단계" 문자열이 들어있으면 통증 부위 데이터로 원천 간주
        val realPainSource = when {
            rawPainTag.contains("통증") || rawPainTag.contains("단계") -> rawPainTag
            rawConditionMemo.contains("통증") || rawConditionMemo.contains("단계") -> rawConditionMemo
            rawMemo.contains("통증") || rawMemo.contains("단계") -> rawMemo
            else -> ""
        }

        if (realPainSource.isNotBlank() && realPainSource != "null") {
            val sessions = realPainSource.split("||").filter { it.isNotBlank() }
            val formattedParts = mutableListOf<String>()

            sessions.forEach { session ->
                val content = if (session.contains("#")) session.split("#").getOrNull(1) ?: "" else session
                if (content.isNotBlank() && !content.contains("기록된 통증이 없습니다")) {

                    // 1. 데이터 파싱: "우 가슴 : 통증정도 : 5단계" 형태를 고려하여 분리
                    // 앞면/뒷면은 보통 데이터 문자열의 가장 앞부분에 붙어있으므로
                    // "우" 또는 "좌" 또는 "앞" 또는 "뒤" 키워드로 명확히 판별합니다.
                    if (content.contains("앞") || content.contains("좌")) frontCount++
                    if (content.contains("뒤") || content.contains("우")) backCount++

                    // 2. 출력용 문자열 생성
                    val cleanContent = content.replace(":", " : ")
                        .replace("  ", " ")
                        .replace("통증정도 :", "통증정도 :")
                    formattedParts.add(cleanContent)
                }
            }
            parsedPainDisplay = formattedParts.joinToString(" | ")
        }
        val frontText = "앞면 $frontCount"
        val backText = "뒷면 $backCount"

        binding.btnFront.text = createColoredSpannable(frontText, frontCount)
        binding.btnBack.text = createColoredSpannable(backText, backCount)

        // 💡 [Step 2] 순수 메모 데이터 확보: "통증"이나 "단계"라는 글자가 섞이지 않은 알맹이 피드백만 메모로 인정
        val realMemoSource = when {
            !rawMemo.contains("통증정도") && !rawMemo.contains("단계") && rawMemo.isNotBlank() && rawMemo != "null" -> rawMemo
            !rawConditionMemo.contains("통증정도") && !rawConditionMemo.contains("단계") && rawConditionMemo.isNotBlank() && rawConditionMemo != "null" -> rawConditionMemo
            else -> ""
        }

        val memoBuilder = StringBuilder()
        if (realMemoSource.isNotBlank()) {
            if (realMemoSource.contains("||") || realMemoSource.contains("#")) {
                val sessions = realMemoSource.split("||").filter { it.isNotBlank() }
                sessions.forEach { session ->
                    val contentPart = session.split("#").getOrNull(1) ?: ""
                    val pureMemo = if (contentPart.contains("[SCORE]")) {
                        contentPart.split("[SCORE]").getOrNull(0) ?: ""
                    } else {
                        contentPart
                    }.trim()

                    if (pureMemo.isNotBlank() && pureMemo != "null" && !pureMemo.contains("통증")) {
                        if (memoBuilder.isNotEmpty()) memoBuilder.append("\n")
                        memoBuilder.append(pureMemo)
                    }
                }
            } else {
                memoBuilder.append(realMemoSource.trim())
            }
        }

        // ==================================================================
        // 🎯 [정상 도킹] 필터링이 완료된 독립 데이터를 각각의 UI 컴포넌트에 마킹
        // ==================================================================

        // 1. 메모/피드백 창 적용
        val finalDisplayMemo = memoBuilder.toString()
        binding.etFeedbackMemo.setText(finalDisplayMemo)
        binding.etFeedbackMemo.isEnabled = isFinalEditMode
        binding.etFeedbackMemo.isFocusableInTouchMode = isFinalEditMode
        binding.etFeedbackMemo.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                item.memo = binding.etFeedbackMemo.text.toString()
            }
        }

        // 2. 통증 부위 레이아웃 컨테이너 및 텍스트 뷰 바인딩
        val painLayout = binding.pain1
        val textView = binding.tvPainTagContent
        val deleteIcon = binding.root.findViewById<ImageView>(R.id.iv_delete_pain)

        if (!parsedPainDisplay.isNullOrEmpty() && parsedPainDisplay != "기록된 통증이 없습니다") {
            painLayout.visibility = View.VISIBLE
            textView.text = parsedPainDisplay
            textView.setTextColor(Color.parseColor("#2D3A4B"))
            deleteIcon?.visibility = if (isFinalEditMode) View.VISIBLE else View.GONE
        } else {
            painLayout.visibility = if (isFinalEditMode) View.VISIBLE else View.GONE
            textView.text = "기록된 통증이 없습니다"
            textView.setTextColor(Color.parseColor("#94A3B8"))
            deleteIcon?.visibility = View.GONE
        }

        // 3. 편집 모드 유무에 따른 조작 락 분리
        binding.btnFront.isEnabled = isFinalEditMode
        binding.btnBack.isEnabled = isFinalEditMode
        for (i in 0 until binding.chipGroupBody.childCount) {
            binding.chipGroupBody.getChildAt(i).isEnabled = isFinalEditMode
        }

        setupBodySelection(binding, currentPos)
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
    private fun updateSeekBarStyle(slider: Slidercustom, isEditMode: Boolean) {
        val blueColor = Color.parseColor("#3B82F6") // 수정 모드: 파란색
        val grayColor = Color.parseColor("#94A3B8") // 완료 상태: 회색

        // 모드에 따라 색상 적용
        slider.setSliderColors(
            if (isEditMode) blueColor else grayColor,
            Color.parseColor("#D8E1ED") // 비활성 트랙 색상
        )
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

    private fun setupBodySelection(binding: ItemCondition1Binding, parentPos: Int) {
        binding.btnFront.setOnClickListener { currentDirection = "앞면"; updateDirectionUI(binding); updateBodyPartsList(binding, parentPos) }
        binding.btnBack.setOnClickListener { currentDirection = "뒷면"; updateDirectionUI(binding); updateBodyPartsList(binding, parentPos) }
        binding.chipGroupBody.setOnCheckedStateChangeListener { _, checkedIds -> if (checkedIds.isNotEmpty()) updateBodyPartsList(binding, parentPos) }
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

    private fun updateBodyPartsList(binding: ItemCondition1Binding, parentPos: Int) {
        val selectedChipId = binding.chipGroupBody.checkedChipId
        val bodyKey = chipIdToKey[selectedChipId] ?: "머리/목"
        val detailList = bodyDataMap[currentDirection]?.get(bodyKey) ?: emptyList()

        binding.rvBodyParts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BodyPartDetailAdapter(detailList, isEditMode, parentPos)
        }
    }

    private fun createColoredSpannable(fullText: String, count: Int): SpannableString {
        val spannable = SpannableString(fullText)
        val startIndex = fullText.lastIndexOf(count.toString())
        val endIndex = startIndex + count.toString().length

        // 파란색 색상 적용 (#3B82F6)
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#3B82F6")),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
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
            val actualQuestionNum = position + 2

            val questionTitles = listOf("오늘 수면 시간이 어떻게 되시나요?", "수면의 질은 어떠셨나요?", "전 날 운동의 피로가 남아있나요?", "현재 몸과 마음의 컨디션은 어떤가요?")
            val minLabels = listOf("1시간", "거의 못 잠", "매우 많이 남아있음", "매우 안 좋음")
            val maxLabels = listOf("10시간", "매우 개운함", "전혀 없음", "최상")

            holder.binding.apply {
                tvStepCount.text = "$actualQuestionNum/5"
                tvQuestionTitle.text = questionTitles.getOrNull(position) ?: ""
                tvMinLabel.text = minLabels.getOrNull(position) ?: ""
                tvMaxLabel.text = maxLabels.getOrNull(position) ?: ""

                // 1. SeekBar 대신 Slidercustom 참조
                val dotSlider = root.findViewById<Slidercustom>(R.id.slider)

                // 2. 슬라이더 속성 설정 (색상은 Slidercustom 내부에서 자동 처리)
                dotSlider.max = 10
                dotSlider.isEditEnabled = isEditMode

                // 3. 초기값 설정
                val initialScore = if (q.score < 1f) (if (actualQuestionNum >= 4) 7 else 10) else q.score.toInt()
                dotSlider.progress = initialScore

                // 4. 가이드 업데이트
                updateSliderGuideByQuestion(tvSliderGuide, actualQuestionNum - 1, initialScore)

                // 5. 값 변경 리스너 (Thumb를 직접 그릴 필요 없음)
                dotSlider.onProgressChanged = { value ->
                    val finalScore = if (value < 1) 1 else value
                    q.score = finalScore.toFloat()
                    updateSliderGuideByQuestion(tvSliderGuide, actualQuestionNum - 1, finalScore)
                }
            }
        }

        override fun getItemCount(): Int = qList.size
    }


    private inner class BodyPartDetailAdapter(
        private val parts: List<String>,
        private val isEditMode: Boolean,
        private val parentPosition: Int
    ) : RecyclerView.Adapter<BodyPartDetailAdapter.BodyViewHolder>() {

        inner class BodyViewHolder(val binding: ItemBodyPartSelectionBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BodyViewHolder {
            val binding = ItemBodyPartSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BodyViewHolder(binding)
        }

        override fun onBindViewHolder(holder: BodyViewHolder, position: Int) {
            val partName = parts[position]
            holder.binding.tvPartName.text = partName

            holder.itemView.setOnClickListener {
                if (isEditMode) {
                    val activity = holder.itemView.context as? FragmentActivity
                    activity?.let { act ->
                        val sheet = PainBottomSheetFragment(partName) { direction, stage, memo ->
                            if (parentPosition in items.indices) {
                                val item = items[parentPosition]

                                // 🎯 [수정 포인트] ": "을 사용하여 통증정도 포맷을 변경했습니다.
                                val newPainString = "컨디션체크 01#$direction: $partName : 통증정도 : ${stage}단계"

                                item.painTag = newPainString
                                this@ConditionAdapter.notifyItemChanged(parentPosition)
                            }
                        }
                        sheet.show(act.supportFragmentManager, "PainBottomSheet")
                    }
                }
            }
        }

        override fun getItemCount(): Int = parts.size
    }
}