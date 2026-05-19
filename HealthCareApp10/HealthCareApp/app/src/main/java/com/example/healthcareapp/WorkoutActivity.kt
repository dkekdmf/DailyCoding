package com.example.healthcareapp

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapp.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class WorkoutActivity : AppCompatActivity() {

    private var isExpanded = false

    // 🎯 뷰페이저 내부 프래그먼트들이 긁어갈 수 있도록 캐싱 데이터 변수 선언
    var savedExerciseList: List<com.example.healthcareapp.data.ExerciseRecord> = emptyList()

    // 🎯 [신규 추가 - 파괴 유실 방지 가드] 하위 프래그먼트가 이 마스터 저널을 가져가서 파싱하고 아코디언을 그리도록 가교를 개설합니다!
    // 기존 기능들은 단 하나도 건드리지 않기 위한 가장 정교하고 안전한 설계입니다.
    var matchedJournalData: com.example.healthcareapp.data.JournalSummaryDto? = null

    // 🎯 [교정 완료 1] 마스터 플래그를 전역 변수로 승격시켜 버튼을 눌러 화면이 갱신되어도 상태가 100% 유지되도록 보장합니다!
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_start)

        val tvEditBtn = findViewById<FrameLayout>(R.id.btn_edit)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val arrowBtn = findViewById<View>(R.id.arrow_btn)
        val tvMainTimer = findViewById<TextView>(R.id.tv_main_timer)
        val tvStartTime = findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = findViewById<TextView>(R.id.tv_end_time)

        val tvHeaderDate = findViewById<TextView>(R.id.tv_date_header)
        val ivHeaderEmoji = findViewById<ImageView>(R.id.condition_emoticon)

        val conditionname = "컨디션 체크"

        val diaryDate = intent.getStringExtra("DIARY_DATE") ?: "날짜 없음"
        val startTab = intent.getIntExtra("SELECT_TAB", 0)
        val diaryIdStr = intent.getStringExtra("DIARY_ID") ?: "-1"

        val intentFolderId = intent.getStringExtra("FOLDER_ID")?.toLongOrNull()
            ?: intent.getLongExtra("FOLDER_ID", -1L)

        var emojiResId = intent.getIntExtra("EMOJI_RES_ID", -1)
        val tvEditBtnText = tvEditBtn.findViewById<TextView>(R.id.tv_edit_text)
        Log.d("JaehoonTest", "--- WorkoutActivity 데이터 수신 확인 ---")
        Log.d("JaehoonTest", "수신된 날짜: $diaryDate, 폴더 ID: $intentFolderId, 일지ID: $diaryIdStr")

        // 수정/완료 토글 리스너 연동
        tvEditBtn.setOnClickListener {
            // 1. 현재 수정 모드가 아니면 -> 수정 모드 진입 (락 해제)
            if (!isEditMode) {
                isEditMode = true

                // f0, f1 태그로 프래그먼트를 찾아 락 해제
                supportFragmentManager.findFragmentByTag("f0")?.let { (it as? com.example.healthcareapp.fragment.WorkoutRecordFragment)?.setEditable(true) }
                supportFragmentManager.findFragmentByTag("f1")?.let { (it as? com.example.healthcareapp.fragment.ConditionCheckFragment)?.setEditable(true) }

                tvEditBtnText.text = "완료"
                Log.d("JaehoonEdit", "🔓 수정 모드 진입 -> 락 해제")

            } else {
                // 2. 현재 수정 모드면 -> 팝업 호출 (저장은 팝업 안에서 수행)
                showCompletionDialog(tvEditBtnText)
            }
        }

        if (intentFolderId != -1L && diaryDate != "날짜 없음") {
            val sharedPrefs = getSharedPreferences("CalendarEmojiPrefs", Context.MODE_PRIVATE)
            val folderIsolatedKey = "${diaryDate}_$intentFolderId"
            val cachedEmojiId = sharedPrefs.getInt(folderIsolatedKey, -1)

            if (cachedEmojiId != -1) {
                emojiResId = cachedEmojiId
            }
        }

        val formattedDate = if (diaryDate.contains("-")) diaryDate.replace("-", ".") else diaryDate
        tvHeaderDate.text = formattedDate

        if (emojiResId != -1) {
            ivHeaderEmoji.setImageResource(emojiResId)
            ivHeaderEmoji.visibility = View.VISIBLE
        } else {
            ivHeaderEmoji.setImageResource(R.drawable.emoticon1)
            ivHeaderEmoji.visibility = View.VISIBLE
        }

        // ==========================================
        // 🎯 [순정 보존] 정밀 로그 추적 및 데이터 바인딩 시스템
        // ==========================================
        try {
            val journals = DiaryPrefsManager.getJournals(this, intentFolderId)
            Log.d("JaehoonDetail", "📂 [1단계] 현재 폴더($intentFolderId)에서 불러온 총 일지 개수: ${journals.size}개")

            val matchedJournal = journals.find { it.id.toString() == diaryIdStr }
            Log.d("JaehoonDetail", "🔍 [2단계] 인텐트로 넘어온 DIARY_ID($diaryIdStr) 매칭 결과: ${matchedJournal != null}")

            if (matchedJournal != null) {
                Log.d("JaehoonDetail", "✅ [3단계] 매칭 성공!")

                // 🎯 [순정 보존 연동] 하위 프래그먼트 딜리버리용 객체 백업 슛!
                matchedJournalData = matchedJournal

                val displayStartTime = if (matchedJournal.startTime.isNullOrEmpty() || matchedJournal.startTime == "null") "00:00" else matchedJournal.startTime
                val displayEndTime = if (matchedJournal.endTime.isNullOrEmpty() || matchedJournal.endTime == "null") "00:00" else matchedJournal.endTime
                val displayTotalTime = if (matchedJournal.totalTime.isNullOrEmpty()) "00:00:00" else matchedJournal.totalTime

                // UI 결합
                tvMainTimer.text = displayTotalTime
                tvStartTime.text = "시작\n$displayStartTime"
                tvEndTime.text = "종료\n$displayEndTime"

                savedExerciseList = matchedJournal.exerciseList ?: emptyList()
                Log.d("JaehoonDetail", "   └ 🏋️ 안전 수송 완료된 세트 데이터 개수: ${savedExerciseList.size}개")

            } else {
                tvMainTimer.text = "01:12:32"
                tvStartTime.text = "시작\n16:16"
                tvEndTime.text = "종료\n17:30"
            }
        } catch (e: Exception) {
            Log.e("JaehoonDetail", "💥 [에러 방어성공] 복원 중 예외 발생했으나 크래시 차단완료: ${e.message}")
            tvMainTimer.text = "01:12:32"
            tvStartTime.text = "시작\n16:16"
            tvEndTime.text = "종료\n17:30"
        }

        // 5. 뷰페이저 어댑터 연결
        viewPager.adapter = ViewPagerAdapter(this)

        // 6. TabLayout과 ViewPager2 결합
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = layoutInflater.inflate(R.layout.custom_tab, tabLayout, false) as TextView
            customView.text = if (position == 0) "운동 기록" else conditionname

            if (position == 0) {
                customView.setTextColor(ContextCompat.getColor(this, R.color.black))
                customView.setTypeface(null, Typeface.BOLD)
            } else {
                customView.setTextColor(ContextCompat.getColor(this, R.color.chip_selected))
                customView.setTypeface(null, Typeface.NORMAL)
            }
            tab.customView = customView
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.black))
                    setTypeface(null, Typeface.BOLD)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.chip_selected))
                    setTypeface(null, Typeface.NORMAL)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewPager.post {
            viewPager.setCurrentItem(startTab, false)
        }

        arrowBtn.setOnClickListener {
            finish()
        }
    }

    // ==================================================================
    // 🎯 [교정 완료 2] 0번이 운동, 1번이 컨디션이므로 태그 주소를 "f1"로 칼같이 맞춤!!
    // ==================================================================
    private fun getVisibleConditionFragment(): com.example.healthcareapp.fragment.ConditionCheckFragment? {
        val fragment = supportFragmentManager.findFragmentByTag("f1")
        return fragment as? com.example.healthcareapp.fragment.ConditionCheckFragment
    }
    private fun showCompletionDialog(btnTextView: TextView?) {
        // 1. 다이얼로그 뷰 inflate
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_complete, null)

        // 2. AlertDialog 생성
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        // 3. 배경을 투명하게 설정 (둥근 모서리 보이기 위함)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 4. 화면 중앙 배치 (기본값)
        dialog.show()

        // 5. 버튼 리스너 연결
        dialogView.findViewById<View>(R.id.btn_no).setOnClickListener {
            dialog.dismiss() // 팝업만 닫기
        }

        dialogView.findViewById<View>(R.id.btn_yes).setOnClickListener {
            // [네] 클릭 시 실제 저장 및 모드 전환
            isEditMode = false

            // 데이터 저장
            getVisibleExerciseFragment()?.saveUpdatedExerciseData()
            getVisibleConditionFragment()?.saveUpdatedConditionData()

            // 잠금 상태로 원복
            getVisibleExerciseFragment()?.setEditable(false)
            getVisibleConditionFragment()?.setEditable(false)

            // 버튼 텍스트 원복
            btnTextView?.text = "수정"

            dialog.dismiss() // 팝업 닫기
            Log.d("JaehoonEdit", "🔒 수정 완료 및 재잠금 저장 완료")
        }
    }
    private fun getVisibleExerciseFragment(): com.example.healthcareapp.fragment.WorkoutRecordFragment? {
        val fragment = supportFragmentManager.findFragmentByTag("f0")
        return fragment as? com.example.healthcareapp.fragment.WorkoutRecordFragment
    }
}