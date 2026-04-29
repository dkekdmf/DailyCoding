package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.WorkoutSet
import com.example.healthcareapp.databinding.ItemWorkoutSetInputBinding

class WorkoutSetAdapter(private val sets: MutableList<WorkoutSet>) :
    RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    inner class SetViewHolder(val binding: ItemWorkoutSetInputBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemWorkoutSetInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.apply {
            tvSetNum.text = "${set.setNum}"
            etWeight.setText("${set.weight}")
            etReps.setText("${set.reps}")

            // 데이터 변경 시 실시간 반영 (TextWatcher 등 추가 가능)
        }
    }

    override fun getItemCount(): Int = sets.size
}