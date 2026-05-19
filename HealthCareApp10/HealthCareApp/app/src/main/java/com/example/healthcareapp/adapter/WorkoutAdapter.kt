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

/**
 * [상위 어댑터] 운동 종목별 카드(벤치프레스, 스쿼트 등)를 관리
 */
class WorkoutAdapter(private val items: MutableList<ExerciseRecord>) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    // 🎯 [마스터 원격 스위치] WorkoutActivity -> ExerciseRecordFragment를 거쳐 들어오는 연동 플래그
    var isEditMode: Boolean = false

    private val viewPool = RecyclerView.RecycledViewPool()

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_exercise_title)
        val rvSets: RecyclerView = view.findViewById(R.id.rv_sets)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record_workoutcard, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val exercise = items[position]

        android.util.Log.d("DEBUG_WORKOUT", "Bind 시작: ${exercise.name}")

        holder.tvTitle.text = String.format("%02d %s", position + 1, exercise.name)

        // 🎯 [핵심 토스] 하위 세트 어댑터를 만들 때 마스터 플래그(isEditMode)를 함께 배달합니다!
        val setAdapter = SetAdapter(exercise.sets, isEditMode)
        holder.rvSets.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = setAdapter
            isNestedScrollingEnabled = false
        }
    }

    override fun getItemCount() = items.size
}

/**
 * [하위 어댑터] 각 운동 카드 내부에 들어가는 세트(무게, 횟수) 줄을 관리
 */
class SetAdapter(
    private val sets: MutableList<ExerciseSet>,
    private val isEditMode: Boolean // 🎯 [락 스위치 장착] 생성자로 플래그를 수신합니다.
) : RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    class SetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSetNum: TextView = view.findViewById(R.id.tv_set_number)
        val etWeight: EditText = view.findViewById(R.id.etWeight)
        val etReps: EditText = view.findViewById(R.id.etReps)

        var weightWatcher: TextWatcher? = null
        var repsWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set_row, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]

        // 1. 기존에 등록된 리스너 제거 (재사용 방지)
        holder.etWeight.removeTextChangedListener(holder.weightWatcher)
        holder.etReps.removeTextChangedListener(holder.repsWatcher)

        // 2. UI 데이터 설정
        holder.tvSetNum.text = "${set.setNumber} "
        holder.etWeight.setText(if (set.weight == 0) "" else set.weight.toString())
        holder.etReps.setText(if (set.reps == 0) "" else set.reps.toString())

        // ==================================================================
        // 🎯 [실시간 무게/횟수 칸 터치 락 제어]
        // ==================================================================
        // 상단 [수정] 버튼 활성화 여부(isEditMode)에 따라 키보드 입력 및 포커스를 제어합니다.
        holder.etWeight.isEnabled = isEditMode
        holder.etWeight.isFocusableInTouchMode = isEditMode

        holder.etReps.isEnabled = isEditMode
        holder.etReps.isFocusableInTouchMode = isEditMode

        // 3. 새로운 리스너(TextWatcher) 정의
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

        // 4. 새로운 리스너 연결
        holder.etWeight.addTextChangedListener(holder.weightWatcher)
        holder.etReps.addTextChangedListener(holder.repsWatcher)
    }

    override fun getItemCount() = sets.size
}