package com.example.healthcareapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.databinding.ItemWorkoutCardBinding
import com.example.healthcareapp.data.ExerciseRecord // ExerciseRecord로 통일
import com.example.healthcareapp.data.ExerciseSet    // ExerciseSet으로 통일

class RecordWorkoutAdapter(private val items: MutableList<ExerciseRecord>) :
    RecyclerView.Adapter<RecordWorkoutAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWorkoutCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]


        Log.d("DEBUG_WORKOUT", "Bind 시작: ${item.name}")

        holder.binding.apply {
            tvExerciseTitle.text = String.format("%02d %s", position + 1, item.name)

            // 내부 세트 리사이클러뷰 설정
            val setAdapter = WorkoutSetAdapter(item.sets)
            rvSets.apply {
                layoutManager = LinearLayoutManager(root.context)
                adapter = setAdapter
                isNestedScrollingEnabled = false // 중첩 스크롤 간섭 방지
            }

            // 세트 추가 버튼 클릭 리스너
            btnAddSet.setOnClickListener {
                Log.d("DEBUG_WORKOUT", "세트 추가 클릭됨: ${item.name}")
                val nextSetNum = item.sets.size + 1
                item.sets.add(ExerciseSet(nextSetNum, 0, 0))

                // 자식 어댑터에게 데이터 변경 알림
                setAdapter.notifyDataSetChanged()
            }

            // 세트 삭제 버튼 클릭 리스너
            btnDeleteSet.setOnClickListener {
                if (item.sets.isNotEmpty()) {
                    Log.d("DEBUG_WORKOUT", "세트 삭제 클릭됨: ${item.name}")
                    item.sets.removeAt(item.sets.size - 1)

                    // 삭제 후 세트 번호 재정렬
                    item.sets.forEachIndexed { index, set ->
                        set.setNumber = index + 1
                    }

                    setAdapter.notifyDataSetChanged()
                }
            }

            btnMore.setOnClickListener {
                // 메뉴 팝업 로직 (필요 시)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}