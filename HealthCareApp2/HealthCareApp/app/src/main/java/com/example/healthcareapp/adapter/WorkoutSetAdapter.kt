package com.example.healthcareapp.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.ExerciseSet
import com.example.healthcareapp.databinding.ItemWorkoutSetInputBinding

class WorkoutSetAdapter(private val sets: MutableList<ExerciseSet>) :
    RecyclerView.Adapter<WorkoutSetAdapter.SetViewHolder>() {

    inner class SetViewHolder(val binding: ItemWorkoutSetInputBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemWorkoutSetInputBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]
        holder.binding.apply {

            tvSetNumber.text = "${set.setNumber}"
            etWeight.setText(if (set.weight == 0) "" else "${set.weight}")
            etReps.setText(if (set.reps == 0) "" else "${set.reps}")


            etWeight.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    set.weight = s.toString().toIntOrNull() ?: 0
                }
                override fun afterTextChanged(s: Editable?) {}
            })


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