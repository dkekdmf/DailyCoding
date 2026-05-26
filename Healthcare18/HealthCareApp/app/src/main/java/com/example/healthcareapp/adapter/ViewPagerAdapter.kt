package com.example.healthcareapp.adapter

import com.example.healthcareapp.fragment.WorkoutRecordFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.healthcareapp.fragment.ConditionCheckFragment

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val workoutFragment: WorkoutRecordFragment,   // 고정된 인스턴스 주입
    private val conditionFragment: ConditionCheckFragment // 고정된 인스턴스 주입
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> workoutFragment
            else -> conditionFragment
        }
    }
}