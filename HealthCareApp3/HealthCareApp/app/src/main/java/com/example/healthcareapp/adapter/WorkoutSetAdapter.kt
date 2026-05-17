package com.example.healthcareapp.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.ExerciseSet
import com.example.healthcareapp.databinding.ItemWorkoutSetInputBinding

/**
 * 특정 운동의 '세트' 정보(세트 번호, 무게, 횟수)를 입력받는 리사이클러뷰 어댑터
 */
class WorkoutSetAdapter(private val sets: MutableList<ExerciseSet>) :
    RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    // ViewBinding을 사용하여 아이템 레이아웃 홀딩
    inner class SetViewHolder(val binding: ItemWorkoutSetInputBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        // item_workout_set_input.xml 레이아웃 인플레이트
        val binding = ItemWorkoutSetInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.apply {

            // 1. [데이터 반영] 현재 세트 정보 표시
            tvSetNumber.text = "${set.setNumber}"
            // 값이 0일 경우 사용자가 입력하기 편하도록 빈 칸으로 표시
            etWeight.setText(if (set.weight == 0) "" else "${set.weight}")
            etReps.setText(if (set.reps == 0) "" else "${set.reps}")

            // 2. [실시간 데이터 동기화] 무게(Weight) 입력 감지
            etWeight.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 입력값이 숫자가 아닐 경우 0으로 처리하여 에러 방지
                    set.weight = s.toString().toIntOrNull() ?: 0
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // 3. [실시간 데이터 동기화] 횟수(Reps) 입력 감지
            etReps.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    set.reps = s.toString().toIntOrNull() ?: 0
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    override fun getItemCount(): Int = sets.size
}