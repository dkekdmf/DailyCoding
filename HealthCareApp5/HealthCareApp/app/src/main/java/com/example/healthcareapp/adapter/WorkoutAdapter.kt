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

    // 하위 리사이클러뷰들의 뷰 홀더를 공유하여 성능을 높이기 위한 풀
    private val viewPool = RecyclerView.RecycledViewPool()

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_exercise_title) // 운동 이름 (01 벤치프레스 형식)
        val rvSets: RecyclerView = view.findViewById(R.id.rv_sets)          // 내부에 들어갈 세트 리스트
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        // 운동 카드 레이아웃 인플레이트
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record_workoutcard, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val exercise = items[position]

        // 로그를 통해 어떤 운동 카드가 바인딩되는지 확인 가능
        android.util.Log.d("DEBUG_WORKOUT", "Bind 시작: ${exercise.name}")

        // 상단 타이틀 설정 (예: 01 벤치프레스)
        holder.tvTitle.text = String.format("%02d %s", position + 1, exercise.name)

        // [중첩 리사이클러뷰 설정]
        // 하위 어댑터(SetAdapter)에 해당 운동의 세트 데이터를 넘겨줌
        val setAdapter = SetAdapter(exercise.sets)
        holder.rvSets.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context)
            adapter = setAdapter

            // 상위 스크롤과 충돌을 방지하기 위해 내부 스크롤 비활성화
            isNestedScrollingEnabled = false

            // 성능 최적화: 뷰 풀 공유 (필요 시 활성화 가능)
            // setRecycledViewPool(viewPool)
        }

        // TODO: 세트 추가/삭제 버튼 활성화 시 여기서 리스너 구현
    }

    override fun getItemCount() = items.size
}

/**
 * [하위 어댑터] 각 운동 카드 내부에 들어가는 세트(무게, 횟수) 줄을 관리
 */
class SetAdapter(private val sets: MutableList<ExerciseSet>) :
    RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    class SetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSetNum: TextView = view.findViewById(R.id.tv_set_number)
        val etWeight: EditText = view.findViewById(R.id.etWeight)
        val etReps: EditText = view.findViewById(R.id.etReps)

        // ⭐ 재사용 시 리스너 중복 등록을 막기 위해 뷰홀더에 와처 저장
        var weightWatcher: TextWatcher? = null
        var repsWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        // 세트 한 줄(set_row) 레이아웃 인플레이트
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set_row, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]

        // 1. [매우 중요] 기존에 등록된 리스너를 먼저 제거하여 재사용 시 데이터 꼬임 방지
        holder.etWeight.removeTextChangedListener(holder.weightWatcher)
        holder.etReps.removeTextChangedListener(holder.repsWatcher)

        // 2. UI 데이터 설정
        holder.tvSetNum.text = "${set.setNumber} "
        // 값이 0이면 빈칸으로 보여주어 사용자 입력을 편하게 유도
        holder.etWeight.setText(if (set.weight == 0) "" else set.weight.toString())
        holder.etReps.setText(if (set.reps == 0) "" else set.reps.toString())

        // 3. 새로운 리스너(TextWatcher) 정의
        holder.weightWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 입력값을 실시간으로 데이터 모델(ExerciseSet)에 저장
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