package com.example.healthcareapp

import WorkoutRecordFragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.utils.DateUtils
import java.util.Calendar

class DiaryMainFragment : Fragment() {

    // 뷰 객체 및 데이터 변수
    private lateinit var rvCalendar: RecyclerView
    private lateinit var tvWeekTitle: TextView
    private lateinit var btnPrevWeek: ImageView
    private lateinit var btnNextWeek: ImageView
    private lateinit var exersizeStart: TextView
    private lateinit var tvFolderName: TextView // 폴더 이름을 표시할 곳 (있다면)

    private lateinit var dayAdapter: DayAdapter
    private var currentCalendar = Calendar.getInstance()

    // 전달받은 폴더 정보
    private var folderId: Long = -1L
    private var folderName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // [중요] HomeActivity에서 보낸 데이터를 여기서 꺼냅니다.
        arguments?.let {
            folderId = it.getLong("FOLDER_ID", -1L)
            folderName = it.getString("FOLDER_NAME")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 기존에 사용하시던 캘린더바 레이아웃(calendarbar.xml)을 그대로 사용합니다.
        val view = inflater.inflate(R.layout.calendarbar, container, false)

        initViews(view)
        setupCalendar()
        initClickListeners()

        // 상단에 폴더 이름을 표시하고 싶다면 세팅
        folderName?.let {
            // 레이아웃에 폴더명 표시용 텍스트뷰가 있다면 설정하세요.
            // tvFolderName.text = it
        }

        return view
    }

    private fun initViews(view: View) {
        tvWeekTitle = view.findViewById(R.id.tv_week_title)
        rvCalendar = view.findViewById(R.id.rv_calendar)
        btnPrevWeek = view.findViewById(R.id.btn_prev_week)
        btnNextWeek = view.findViewById(R.id.btn_next_week)
        exersizeStart = view.findViewById(R.id.exercise_start)
    }

    private fun setupCalendar() {
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        dayAdapter = DayAdapter(days) { clickedDay ->
            updateDiaryList(clickedDay.fullDate)
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter

            // [수정] 데이터가 들어온 후 리사이클러뷰를 강제로 갱신시킵니다.
            post {
                dayAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initClickListeners() {
        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        // '운동 기록 시작' 클릭 시 DiaryListActivity로 이동
        exersizeStart.setOnClickListener {
            // 프래그먼트에서는 context 대신 requireContext()를 사용합니다.
            val intent = Intent(requireContext(), WorkoutExerciseActivity::class.java).apply {
                putExtra("FOLDER_ID", folderId)
                putExtra("FOLDER_NAME", folderName)
            }
            startActivity(intent)
        }
    }

    private fun moveWeek(offset: Int) {
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title
        dayAdapter.updateData(days)
    }

    private fun updateDiaryList(date: String) {
        // 여기에 folderId와 date를 이용해 서버에서 일지를 불러오는 로직을 추가하세요!
    }
}