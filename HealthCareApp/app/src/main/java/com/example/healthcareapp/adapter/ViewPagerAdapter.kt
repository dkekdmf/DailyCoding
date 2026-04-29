package com.example.healthcareapp.adapter

import WorkoutRecordFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.healthcareapp.ConditionCheckFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2 // 탭 개수

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WorkoutRecordFragment() // 첫 번째 탭: 운동 기록
            else -> ConditionCheckFragment() // 두 번째 탭: 컨디션 체크
        }
    }
}