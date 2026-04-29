
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.adapter.WorkoutAdapter
import com.example.healthcareapp.data.ExerciseRecord
import com.example.healthcareapp.data.ExerciseSet

class WorkoutRecordFragment : Fragment(R.layout.fragment_workout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기존 WorkoutActivity에 있던 리사이클러뷰 설정 코드를 일로 가져옵니다
        val rvWorkout = view.findViewById<RecyclerView>(R.id.rv_workout_list)

        val workoutList = mutableListOf(
            ExerciseRecord(1, "벤치프레스 머신", mutableListOf(
                ExerciseSet(1, 60, 10),
                ExerciseSet(2, 60, 10)
            ))
        )

        rvWorkout.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = WorkoutAdapter(workoutList)
        }
    }
}