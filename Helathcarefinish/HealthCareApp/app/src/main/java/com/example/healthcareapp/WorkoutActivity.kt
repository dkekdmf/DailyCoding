package com.example.healthcareapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapp.adapter.ViewPagerAdapter
import com.example.healthcareapp.utils.ColorUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class WorkoutActivity : AppCompatActivity() {

    private var isExpanded = false
    var savedExerciseList: List<com.example.healthcareapp.data.ExerciseRecord> = emptyList()
    var matchedJournalData: com.example.healthcareapp.data.JournalSummaryDto? = null
    private var isEditMode = false
    private lateinit var globalPagerAdapter: ViewPagerAdapter
    private val workoutFragment = com.example.healthcareapp.fragment.WorkoutRecordFragment()
    private val conditionFragment = com.example.healthcareapp.fragment.ConditionCheckFragment()
    private lateinit var viewPager: ViewPager2
    private var intentFolderId: Long = -1L
    private var diaryDate: String = "날짜 없음"
    private var diaryIdStr: String = "-1"

    val conditionCheckLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
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

            getVisibleConditionFragment()?.onResume()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_start)

        val tvEditBtn = findViewById<FrameLayout>(R.id.btn_edit)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val arrowBtn = findViewById<View>(R.id.arrow_btn)
        globalPagerAdapter = ViewPagerAdapter(this, workoutFragment, conditionFragment)
        viewPager.adapter = globalPagerAdapter
        val tvMainTimer = findViewById<TextView>(R.id.tv_main_timer)
        val tvStartTime = findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = findViewById<TextView>(R.id.tv_end_time)

        val tvHeaderDate = findViewById<TextView>(R.id.tv_date_header)
        val ivHeaderEmoji = findViewById<ImageView>(R.id.condition_emoticon)

        diaryDate = intent.getStringExtra("DIARY_DATE") ?: "날짜 없음"
        val startTab = intent.getIntExtra("SELECT_TAB", 0)
        diaryIdStr = intent.getStringExtra("DIARY_ID") ?: "-1"
        intentFolderId = intent.getLongExtra("FOLDER_ID", -1L)

        tvEditBtn.translationZ = 10f
        val tvEditBtnText = tvEditBtn.findViewById<TextView>(R.id.tv_edit_text)

        tvEditBtn.setOnClickListener {
            if (!isEditMode) {
                // 편집 시작 모드
                isEditMode = true

                // 🎯 [핵심] 어댑터들에게 즉시 편집 모드임을 알림
                getVisibleExerciseFragment()?.setEditable(true)
                getVisibleConditionFragment()?.setEditable(true)

                tvEditBtnText.text = "완료"
                tvEditBtn.setBackgroundResource(R.drawable.bg_black_button_soft)
            } else {
                // 완료 모드 (다이얼로그 띄움)
                showCompletionDialog(tvEditBtn, tvEditBtnText)
            }
        }

        val formattedDate = if (diaryDate.contains("-")) diaryDate.replace("-", ".") else diaryDate
        tvHeaderDate.text = formattedDate

        // 🎯 수정 (3개 인자 전달: date, id, folderId)
        val finalEmojiId = ColorUtils.getStableEmojiResId(
            diaryDate,
            diaryIdStr,      // 👈 일지 고유 ID를 추가합니다!
            intentFolderId
        )

        ivHeaderEmoji.setImageResource(finalEmojiId)
        ivHeaderEmoji.setBackgroundResource(0) // 배경색 없앰 (투명)
        ivHeaderEmoji.visibility = View.VISIBLE

        // 타이머 데이터 세팅
        try {
            val journals = DiaryPrefsManager.getJournals(this, intentFolderId)
            val matchedJournal = journals.find { it.id.toString() == diaryIdStr }
            if (matchedJournal != null) {
                matchedJournalData = matchedJournal
                tvMainTimer.text = matchedJournal.totalTime ?: "00:00:00"
                tvStartTime.text = "시작\n${matchedJournal.startTime ?: "00:00"}"
                tvEndTime.text = "종료\n${matchedJournal.endTime ?: "00:00"}"
                savedExerciseList = matchedJournal.exerciseList ?: emptyList()
            }
        } catch (e: Exception) {
            tvMainTimer.text = "01:12:32"
        }

        globalPagerAdapter = ViewPagerAdapter(
            this,
            workoutFragment,
            conditionFragment
        )
        viewPager.adapter = globalPagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = layoutInflater.inflate(R.layout.custom_tab, tabLayout, false) as TextView
            customView.text = if (position == 0) "운동 기록" else "컨디션 체크"

            // 초기 폰트 설정
            val isSelected = (position == 0)
            customView.setTextColor(Color.parseColor(if (isSelected) "#2E3D4F" else "#8896A8"))
            customView.typeface = ResourcesCompat.getFont(this, if (isSelected) R.font.pretendard_medium else R.font.pretendard_medium)

            tab.customView = customView
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            // 폰트 리소스 미리 로드
            val mediumFont = ResourcesCompat.getFont(this@WorkoutActivity, R.font.pretendard_regular)
            val boldFont = ResourcesCompat.getFont(this@WorkoutActivity, R.font.pretendard_medium) // 💡 regular를 bold로 변경 추천

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val customView = tab?.customView as? TextView
                customView?.let {
                    it.setTextColor(Color.parseColor("#2E3D4F"))
                    // 🎯 선택 시: Bold 폰트 적용
                    it.typeface = boldFont
                }

                val isWorkout = tab?.position == 0
                tvMainTimer.visibility = if (isWorkout) View.VISIBLE else View.GONE
                tvStartTime.visibility = if (isWorkout) View.VISIBLE else View.GONE
                tvEndTime.visibility = if (isWorkout) View.VISIBLE else View.GONE
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val customView = tab?.customView as? TextView
                customView?.let {
                    it.setTextColor(Color.parseColor("#8896A8"))
                    // 🎯 해제 시: Medium 폰트 적용
                    it.typeface = mediumFont
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewPager.post { viewPager.setCurrentItem(startTab, false) }
        arrowBtn.setOnClickListener { finish() }
    }
//
//    private fun getVisibleConditionFragment() = supportFragmentManager.fragments.find { it is com.example.healthcareapp.fragment.ConditionCheckFragment } as? com.example.healthcareapp.fragment.ConditionCheckFragment
//    private fun getVisibleExerciseFragment() = supportFragmentManager.fragments.find { it is com.example.healthcareapp.fragment.WorkoutRecordFragment } as? com.example.healthcareapp.fragment.WorkoutRecordFragment
    fun getVisibleConditionFragment() = conditionFragment
    fun getVisibleExerciseFragment() = workoutFragment
    private fun showCompletionDialog(btnFrame: FrameLayout, btnTextView: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_complete, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        dialogView.findViewById<View>(R.id.btn_no).setOnClickListener { dialog.dismiss() }

        dialogView.findViewById<View>(R.id.btn_yes).setOnClickListener {
            isEditMode = false

            // 1. 안전하게 프래그먼트 가져오기 및 메서드 호출
            val exerciseFragment = getVisibleExerciseFragment()
            val conditionFragment = getVisibleConditionFragment()

            // 2. Fragment가 null이 아니고(존재하고), isAdded(액티비티에 붙어있는) 상태인지 확인 후 호출
            if (exerciseFragment?.isAdded == true) {
                exerciseFragment.setEditable(false)
                exerciseFragment.saveUpdatedExerciseData()
                exerciseFragment.onResume() // 일반적으로 onResume은 직접 호출하지 않는 것이 좋습니다 (아래 팁 확인)
            }

            if (conditionFragment?.isAdded == true) {
                conditionFragment.setEditable(false)
                conditionFragment.saveUpdatedConditionData()
                conditionFragment.refreshData() // 여기서 크래시가 났었으므로 isAdded 체크가 필수입니다
            }

            // 3. UI 상태 복구
            btnTextView.text = "수정"
            btnFrame.setBackgroundResource(R.drawable.bg_blue_button_soft)
            dialog.dismiss()
        }
    }
}