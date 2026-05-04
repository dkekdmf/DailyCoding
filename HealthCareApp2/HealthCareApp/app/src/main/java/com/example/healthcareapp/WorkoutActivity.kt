package com.example.healthcareapp

import android.graphics.Typeface
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.healthcareapp.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class WorkoutActivity : AppCompatActivity() {

    // 드롭다운 상태를 추적하기 위한 변수
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exercise_start)

        // 1. UI 컴포넌트 연결 (상단바 및 타이머)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val arrowBtn = findViewById<View>(R.id.arrow_btn)
        val tvMainTimer = findViewById<TextView>(R.id.tv_main_timer)
        val tvStartTime = findViewById<TextView>(R.id.tv_start_time)
        val tvEndTime = findViewById<TextView>(R.id.tv_end_time)
        val conditionname = "컨디션 체크"



        // 3. 데이터 세팅 (타이머)
        tvMainTimer.text = "01:12:32"
        tvStartTime.text = "16:16"
        tvEndTime.text = "17:30"

        // 4. 상태 질문 리사이클러뷰 설정 (2~5번 질문용 어댑터 연결)

        // 6. 뷰페이저 어댑터 설정
        viewPager.adapter = ViewPagerAdapter(this)

        // 7. 탭 레이아웃과 뷰페이저2 연결 (커스텀 탭 설정 유지)
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

        // 8. 탭 선택 리스너 (텍스트 스타일 및 단위 설정 유지)
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

        // 9. 뒤로가기 버튼 이벤트
        arrowBtn.setOnClickListener {
            finish()
        }
    }
}