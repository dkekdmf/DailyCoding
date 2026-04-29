package com.example.healthcareapp.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet

// 1. 부모 어댑터 (운동 종목 카드)
class WorkoutAdapter(private val items: List<ExerciseRecord>) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_exercise_title)
        val rvSets: RecyclerView = view.findViewById(R.id.rv_sets) // XML에 추가한 RecyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout_card, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val exercise = items[position]
        holder.tvTitle.text = String.format("%02d %s", position + 1, exercise.name)

        // 내부 리사이클러뷰(세트) 설정
        holder.rvSets.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = SetAdapter(exercise.sets)
            setHasFixedSize(true) // 성능 최적화
        }
    }

    override fun getItemCount() = items.size
}

// 2. 자식 어댑터 (세트별 입력 줄)
class SetAdapter(private val sets: List<ExerciseSet>) :
    RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    class SetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSetNum: TextView = view.findViewById(R.id.tv_set_number)
        val etWeight: EditText = view.findViewById(R.id.et_weight)
        val etReps: EditText = view.findViewById(R.id.et_reps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set_row, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]
        holder.tvSetNum.text = "${set.setNumber} 세트"
        holder.etWeight.setText(set.weight.toString())
        holder.etReps.setText(set.reps.toString())

        // 사용자가 수정한 값을 실시간으로 데이터 모델에 반영
        holder.etWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { set.weight = s.toString().toIntOrNull() ?: 0 }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.etReps.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { set.reps = s.toString().toIntOrNull() ?: 0 }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount() = sets.size
}