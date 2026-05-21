package com.example.healthcareapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapp.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class WorkoutActivity : AppCompatActivity() {

    private var isExpanded = false
    var savedExerciseList: List<com.example.healthcareapp.data.ExerciseRecord> = emptyList()
    var matchedJournalData: com.example.healthcareapp.data.JournalSummaryDto? = null
    private var isEditMode = false
    private lateinit var globalPagerAdapter: ViewPagerAdapter

    // 🎯 레이아웃 뷰 컴포넌트 멤버 전역 변수화 (실시간 갱신 접근용)
    private lateinit var viewPager: ViewPager2
    private var intentFolderId: Long = -1L
    private var diaryDate: String = "날짜 없음"
    private var diaryIdStr: String = "-1"

    // ==================================================================
    // 🎯 [실시간 복귀 고속도로 개통] 컨디션 메모 실시간 감지 받아치기 런처
    // ==================================================================
    val conditionCheckLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val updatedConditionMemo = data.getStringExtra("UPDATED_CONDITION_MEMO")
            val updatedPainTag = data.getStringExtra("UPDATED_PAIN_TAG")

            Log.d("JaehoonSync", "✅ 컨디션 체크 화면으로부터 메모 실시간 데이터 복귀 캐치!")

            // 💾 1. SharedPreferences 영구 저장 데이터 최신화 다시 불러오기
            try {
                val journals = DiaryPrefsManager.getJournals(this, intentFolderId)
                val matchedJournal = journals.find { it.id.toString() == diaryIdStr }
                if (matchedJournal != null) {
                    matchedJournalData = matchedJournal
                    savedExerciseList = matchedJournal.exerciseList ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("JaehoonSync", "캐시 재로드 에러: ${e.message}")
            }

            // 🚀 2. 현재 떠 있는 뷰페이저 내부 컨디션 프래그먼트에 신호 및 데이터를 주입해 강제 새로고침 유도
            getVisibleConditionFragment()?.let { fragment ->
                // 프래그먼트 내부에 public 데이터 수신기가 있다면 찌르기,
                // 없더라도 프래그먼트 내부에서 SharedPreferences 데이터를 다시 온로드하도록 호출
                fragment.onResume()
                Log.d("JaehoonSync", "🔥 컨디션 프래그먼트 실시간 리프레시 슛 완료!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_start)

        val tvEditBtn = findViewById<FrameLayout>(R.id.btn_edit)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        viewPager = findViewById<ViewPager2>(R.id.view_pager) // 멤버 변수 연결
        val arrowBtn = findViewById<View>(R.id.arrow_btn)
        val tvMainTimer = findViewById<TextView>(R.id.tv_main_timer)
        val tvStartTime = findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = findViewById<TextView>(R.id.tv_end_time)

        val tvHeaderDate = findViewById<TextView>(R.id.tv_date_header)
        val ivHeaderEmoji = findViewById<ImageView>(R.id.condition_emoticon)

        val conditionname = "컨디션 체크"

        diaryDate = intent.getStringExtra("DIARY_DATE") ?: "날짜 없음"
        val startTab = intent.getIntExtra("SELECT_TAB", 0)
        diaryIdStr = intent.getStringExtra("DIARY_ID") ?: "-1"

        intentFolderId = intent.getStringExtra("FOLDER_ID")?.toLongOrNull()
            ?: intent.getLongExtra("FOLDER_ID", -1L)

        var emojiResId = intent.getIntExtra("EMOJI_RES_ID", -1)
        val tvEditBtnText = tvEditBtn.findViewById<TextView>(R.id.tv_edit_text)

        tvEditBtn.translationZ = 10f

        tvEditBtn.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true

                getVisibleExerciseFragment()?.setEditable(true)
                getVisibleConditionFragment()?.setEditable(true)

                tvEditBtnText.text = "완료"
                tvEditBtn.setBackgroundResource(R.drawable.bg_black_button_soft)
            } else {
                showCompletionDialog(tvEditBtn, tvEditBtnText)
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

        ivHeaderEmoji.background = null
        ivHeaderEmoji.clearColorFilter()

        val myEmoticons = intArrayOf(
            R.drawable.emoticon1, R.drawable.emoticon2, R.drawable.emoticon3,
            R.drawable.emoticon4, R.drawable.emoticon5
        )

        val mixKey = "${diaryDate}_$intentFolderId"
        var distributionId = 7
        for (ch in mixKey) {
            distributionId = distributionId * 31 + ch.code
        }
        val stableIndex = kotlin.math.abs(distributionId) % 5
        val finalEmojiId = myEmoticons[stableIndex]

        ivHeaderEmoji.setImageResource(finalEmojiId)
        ivHeaderEmoji.visibility = View.VISIBLE

        try {
            val journals = DiaryPrefsManager.getJournals(this, intentFolderId)
            val matchedJournal = journals.find { it.id.toString() == diaryIdStr }

            if (matchedJournal != null) {
                matchedJournalData = matchedJournal

                val displayStartTime = if (matchedJournal.startTime.isNullOrEmpty() || matchedJournal.startTime == "null") "00:00" else matchedJournal.startTime
                val displayEndTime = if (matchedJournal.endTime.isNullOrEmpty() || matchedJournal.endTime == "null") "00:00" else matchedJournal.endTime
                val displayTotalTime = if (matchedJournal.totalTime.isNullOrEmpty()) "00:00:00" else matchedJournal.totalTime

                tvMainTimer.text = displayTotalTime
                tvStartTime.text = "시작\n$displayStartTime"
                tvEndTime.text = "종료\n$displayEndTime"

                savedExerciseList = matchedJournal.exerciseList ?: emptyList()
            } else {
                tvMainTimer.text = "01:12:32"
                tvStartTime.text = "시작\n16:16"
                tvEndTime.text = "종료\n17:30"
            }
        } catch (e: Exception) {
            tvMainTimer.text = "01:12:32"
            tvStartTime.text = "시작\n16:16"
            tvEndTime.text = "종료\n17:30"
        }

        globalPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = globalPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = layoutInflater.inflate(R.layout.custom_tab, tabLayout, false) as TextView

            if (position == 0) {
                customView.text = "운동 기록"
                customView.setTextColor(ContextCompat.getColor(this, R.color.black))
                customView.setTypeface(null, Typeface.BOLD)
            } else {
                customView.text = conditionname
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
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.apply {
                    setTextColor(ContextCompat.getColor(this@WorkoutActivity, R.color.chip_selected))
                    setTypeface(null, Typeface.NORMAL)
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewPager.post { viewPager.setCurrentItem(startTab, false) }
        arrowBtn.setOnClickListener { finish() }
    }

    // 🎯 [수술 부위] ViewPager2 구조 규격에 맞게 인스턴스를 정확하게 역추적하는 태그 인덱서 연산 가드 개조
    private fun getVisibleConditionFragment(): com.example.healthcareapp.fragment.ConditionCheckFragment? {
        return supportFragmentManager.findFragmentByTag("f1") as? com.example.healthcareapp.fragment.ConditionCheckFragment
            ?: supportFragmentManager.fragments.find { it is com.example.healthcareapp.fragment.ConditionCheckFragment } as? com.example.healthcareapp.fragment.ConditionCheckFragment
    }

    private fun getVisibleExerciseFragment(): com.example.healthcareapp.fragment.WorkoutRecordFragment? {
        return supportFragmentManager.findFragmentByTag("f0") as? com.example.healthcareapp.fragment.WorkoutRecordFragment
            ?: supportFragmentManager.fragments.find { it is com.example.healthcareapp.fragment.WorkoutRecordFragment } as? com.example.healthcareapp.fragment.WorkoutRecordFragment
    }

    private fun showCompletionDialog(btnFrame: FrameLayout, btnTextView: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_complete, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        dialogView.findViewById<View>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btn_yes).setOnClickListener {
            isEditMode = false

            getVisibleExerciseFragment()?.saveUpdatedExerciseData()
            getVisibleConditionFragment()?.saveUpdatedConditionData()
            getVisibleExerciseFragment()?.setEditable(false)
            getVisibleConditionFragment()?.setEditable(false)

            btnTextView.text = "수정"
            btnFrame.setBackgroundResource(R.drawable.bg_blue_button_soft)

            dialog.dismiss()
        }
    }
}