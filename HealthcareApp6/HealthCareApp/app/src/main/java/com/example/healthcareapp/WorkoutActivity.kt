package com.example.healthcareapp

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_start)

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
        // 🎯 [교체 완료] 여기서부터 정밀 로그 추적 시스템 가동!
        // ==========================================
        try {
            val journals = DiaryPrefsManager.getJournals(this, intentFolderId)
            Log.d("JaehoonDetail", "📂 [1단계] 현재 폴더($intentFolderId)에서 불러온 총 일지 개수: ${journals.size}개")

            val matchedJournal = journals.find { it.id.toString() == diaryIdStr }
            Log.d("JaehoonDetail", "🔍 [2단계] 인텐트로 넘어온 DIARY_ID($diaryIdStr) 매칭 결과: ${matchedJournal != null}")

            if (matchedJournal != null) {
                Log.d("JaehoonDetail", "✅ [3단계] 매칭 성공!")

                // 🎯 [안전 장치 추가] 만약 구버전 세이브 데이터라서 startTime이나 endTime이 null 혹은 비어있다면 디폴트 문구로 치환
                val displayStartTime = if (matchedJournal.startTime.isNullOrEmpty() || matchedJournal.startTime == "null") "00:00" else matchedJournal.startTime
                val displayEndTime = if (matchedJournal.endTime.isNullOrEmpty() || matchedJournal.endTime == "null") "00:00" else matchedJournal.endTime
                val displayTotalTime = if (matchedJournal.totalTime.isNullOrEmpty()) "00:00:00" else matchedJournal.totalTime

                // UI 결합
                tvMainTimer.text = displayTotalTime
                tvStartTime.text = "시작\n$displayStartTime"
                tvEndTime.text = "종료\n$displayEndTime"

                // 🎯 [1번째 줄 에러 영구 저격] exerciseList가 null 객체로 들어오더라도 빈 리스트로 받아주어 .size() 터짐을 원천 차단합니다.
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
}