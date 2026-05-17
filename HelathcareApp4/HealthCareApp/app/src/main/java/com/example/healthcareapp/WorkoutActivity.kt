package com.example.healthcareapp

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

    // 드롭다운 상태(열림/닫힘)를 추적하기 위한 플래그 변수
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 운동 시작/기록 화면 레이아웃 설정
        setContentView(R.layout.exercise_start)

        // 1. UI 컴포넌트 찾아오기
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val arrowBtn = findViewById<View>(R.id.arrow_btn)
        val tvMainTimer = findViewById<TextView>(R.id.tv_main_timer)
        val tvStartTime = findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = findViewById<TextView>(R.id.tv_end_time)

        // 상단 헤더의 날짜와 이모티콘 뷰
        val tvHeaderDate = findViewById<TextView>(R.id.tv_date_header)
        val ivHeaderEmoji = findViewById<ImageView>(R.id.condition_emoticon)

        val conditionname = "컨디션 체크"

        // 2. ⭐ 데이터 수신 및 로그 확인 (수정 포인트)
        val diaryDate = intent.getStringExtra("DIARY_DATE") ?: "날짜 없음"

        // 기본값을 -1로 설정해야 "진짜로 데이터가 넘어왔는지" 로그로 확인이 가능합니다.
        val emojiResId = intent.getIntExtra("EMOJI_RES_ID", -1)
        val startTab = intent.getIntExtra("SELECT_TAB", 0)

        // Logcat에서 'JaehoonTest' 태그로 검색하세요.
        Log.d("JaehoonTest", "--- WorkoutActivity 데이터 수신 확인 ---")
        Log.d("JaehoonTest", "수신된 날짜: $diaryDate")
        Log.d("JaehoonTest", "수신된 이모티콘 ID: $emojiResId")

        // 3. ⭐ 받아온 데이터를 상단 헤더에 적용
        tvHeaderDate.text = diaryDate

        if (emojiResId != -1) {
            // 정상적으로 전달받은 경우
            ivHeaderEmoji.setImageResource(emojiResId)
            ivHeaderEmoji.visibility = View.VISIBLE
            Log.d("JaehoonTest", "이모티콘 적용 성공")
        } else {
            // 데이터를 못 받은 경우 (기본 이미지 세팅 및 경고 로그)
            Log.e("JaehoonTest", "⚠️ EMOJI_RES_ID가 전달되지 않았습니다. 기본 이미지를 사용합니다.")
            ivHeaderEmoji.setImageResource(R.drawable.emoticon1)
            ivHeaderEmoji.visibility = View.VISIBLE
        }

        // 4. 타이머 데이터 더미 세팅 (실제 데이터 연동 필요 시 수정)
        tvMainTimer.text = "01:12:32"
        tvStartTime.text = "16:16"
        tvEndTime.text = "17:30"

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

        // 7. 탭 선택 시 스타일 변경 리스너
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

        // 8. 전달받은 포지션으로 탭 즉시 이동
        viewPager.post {
            viewPager.setCurrentItem(startTab, false)
        }

        // 9. 뒤로가기 버튼
        arrowBtn.setOnClickListener {
            finish()
        }
    }
}