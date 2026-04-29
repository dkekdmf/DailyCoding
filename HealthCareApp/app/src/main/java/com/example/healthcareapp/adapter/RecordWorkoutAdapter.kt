package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.healthcareapp.databinding.ItemWorkoutCardBinding
import com.example.healthcareapp.data.WorkoutExercise
import com.example.healthcareapp.data.WorkoutSet

class RecordWorkoutAdapter(private val items: MutableList<WorkoutExercise>) :
    RecyclerView.Adapter<RecordWorkoutAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWorkoutCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            // 인덱스와 이름을 합쳐서 표시 (01 벤치프레스 머신)
            tvExerciseTitle.text = String.format("%02d %s", position + 1, item.name)

            // 내부 세트 리사이클러뷰 설정
            val setAdapter = WorkoutSetAdapter(item.sets)
            rvSets.apply {
                layoutManager = LinearLayoutManager(root.context)
                adapter = setAdapter
            }

            // 더보기 버튼 클릭 (필요 시)
            btnMore.setOnClickListener {
                // 삭제나 순서 변경 메뉴 팝업 로직
            }

            // 세트 추가/삭제 버튼이 카드 레이아웃 하단에 추가된다면 여기에 리스너를 구현합니다.
        }
        }


    override fun getItemCount(): Int = items.size
}