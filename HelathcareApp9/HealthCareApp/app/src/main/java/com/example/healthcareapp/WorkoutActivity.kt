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

        Log.d("JaehoonTest", "--- WorkoutActivity 데이터 수신 확인 ---")
        Log.d("JaehoonTest", "수신된 날짜: $diaryDate, 폴더 ID: $intentFolderId, 일지ID: $diaryIdStr")

        // 수정/완료 토글 리스너 연동
        tvEditBtn.setOnClickListener {
            if (!isEditMode) {
                // 🔓 [수정 모드 진입]
                isEditMode = true

                // 💡 0번 탭(운동 기록)과 1번 탭(컨디션) 모두에게 락 해제 신호 전송!
                getVisibleExerciseFragment()?.setEditable(true)
                getVisibleConditionFragment()?.setEditable(true)
                Log.d("JaehoonEdit", "🔓 상단 버튼 클릭 -> 운동 기록 & 컨디션 모두 락 해제")
            } else {
                // 🔒 [수정 완료 및 재잠금]
                isEditMode = false

                // 1. 각각 수정 완료된 최종 데이터(세트 수, kg, 회수 / 컨디션 점수 등) 안전하게 commit 저장
                getVisibleExerciseFragment()?.saveUpdatedExerciseData() // 👈 필요시 저장 메서드 호출
                getVisibleConditionFragment()?.saveUpdatedConditionData()

                // 2. 저장이 끝났으니 다시 터치 불가 철통 잠금 지시
                getVisibleExerciseFragment()?.setEditable(false)
                getVisibleConditionFragment()?.setEditable(false)
                Log.d("JaehoonEdit", "🔒 상단 버튼 클릭 -> 운동 기록 & 컨디션 모두 재잠금 완료")
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
        val fragment = supportFragmentManager.findFragmentByTag("f1") // 👈 "f2"에서 "f1"로 전격 수정하여 널 포인터 원천 봉쇄
        return fragment as? com.example.healthcareapp.fragment.ConditionCheckFragment
    }
    private fun getVisibleExerciseFragment(): com.example.healthcareapp.fragment.WorkoutRecordFragment? {
        val fragment = supportFragmentManager.findFragmentByTag("f0")
        return fragment as? com.example.healthcareapp.fragment.WorkoutRecordFragment // 🎯 여기에 클래스명 일치 확인!
    }
}