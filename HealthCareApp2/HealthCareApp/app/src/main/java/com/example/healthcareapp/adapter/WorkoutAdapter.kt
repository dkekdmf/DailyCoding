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

class WorkoutAdapter(private val items: MutableList<ExerciseRecord>) : // 1. MutableList로 변경
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_exercise_title)
        val rvSets: RecyclerView = view.findViewById(R.id.rv_sets)
        val btnDeleteSet: View = view.findViewById(R.id.btn_delete_set)
        val btnAddSet: View = view.findViewById(R.id.btn_add_set)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout_card, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val exercise = items[position]


        android.util.Log.d("DEBUG_WORKOUT", "Bind 시작: ${exercise.name}")

        holder.tvTitle.text = String.format("%02d %s", position + 1, exercise.name)


        val setAdapter = SetAdapter(exercise.sets)
        holder.rvSets.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = setAdapter

            isNestedScrollingEnabled = false
        }


        holder.btnAddSet.setOnClickListener {
            android.util.Log.d("DEBUG_WORKOUT", "추가 버튼 눌림!")
            val nextSetNum = exercise.sets.size + 1
            exercise.sets.add(ExerciseSet(nextSetNum, 0, 0))
            setAdapter.notifyDataSetChanged()
        }

        holder.btnDeleteSet.setOnClickListener {
            android.util.Log.d("DEBUG_WORKOUT", "삭제 버튼 눌림!")
            if (exercise.sets.isNotEmpty()) {
                exercise.sets.removeAt(exercise.sets.size - 1)
                setAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = items.size
}

class SetAdapter(private val sets: MutableList<ExerciseSet>) :
    RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    class SetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSetNum: TextView = view.findViewById(R.id.tv_set_number)
        val etWeight: EditText = view.findViewById(R.id.et_weight)
        val etReps: EditText = view.findViewById(R.id.et_reps)


        var weightWatcher: TextWatcher? = null
        var repsWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set_row, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]


        holder.etWeight.removeTextChangedListener(holder.weightWatcher)
        holder.etReps.removeTextChangedListener(holder.repsWatcher)


        holder.tvSetNum.text = "${set.setNumber} 세트"
        // 0일 때는 빈칸으로 보여주어 사용자가 바로 입력하기 편하게 함
        holder.etWeight.setText(if (set.weight == 0) "" else set.weight.toString())
        holder.etReps.setText(if (set.reps == 0) "" else set.reps.toString())

        holder.weightWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.weight = s.toString().toIntOrNull() ?: 0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        holder.repsWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.reps = s.toString().toIntOrNull() ?: 0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        holder.etWeight.addTextChangedListener(holder.weightWatcher)
        holder.etReps.addTextChangedListener(holder.repsWatcher)
    }

    override fun getItemCount() = sets.size
}