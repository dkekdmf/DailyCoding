package com.example.healthcareapp

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.DayAdapter
import com.example.healthcareapp.adapter.DiaryAdapter
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.sheet.FolderExitSheet
import com.example.healthcareapp.sheet.FolderExitSheet2
import com.example.healthcareapp.utils.DateUtils
import java.util.Calendar

class DiaryListActivity : AppCompatActivity() {

    // 뷰 객체 변수 선언
    private lateinit var rvCalendar: RecyclerView    // 상단 가로 날짜 리스트
    private lateinit var rvDiaryList: RecyclerView   // 중앙 세로 일지 목록 리스트
    private lateinit var tvFolderName: TextView      // 화면 상단 폴더 이름 (예: 운동 기록)
    private lateinit var tvWeekTitle: TextView       // 현재 표시되는 달 (예: 4월)
    private lateinit var btnPrevWeek: ImageView      // 저번 주 버튼
    private lateinit var btnNextWeek: ImageView      // 다음 주 버튼
    private lateinit var btnPlus: ImageView          // 새 일지 추가 버튼

    private lateinit var dayAdapter: DayAdapter       // 날짜 전용 어댑터
    private lateinit var diaryAdapter: DiaryAdapter   // 일지 리스트 전용 어댑터
    private var currentCalendar = Calendar.getInstance() // 현재 날짜를 관리하는 객체
    private val dummyDiaryList = ArrayList<DiaryItem>() // 화면에 보여줄 가짜 데이터 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.folder_detial) // '폴더 상세(일지 목록)' 레이아웃 연결

        initViews()         // 1. 뷰 초기화
        setupCalendar()     // 2. 상단 날짜바 설정
        createDummyData()   // 3. 테스트용 데이터 생성
        setupDiaryList()    // 4. 하단 일지 목록 설정
        val folderId = intent.getLongExtra("FOLDER_ID", -1L) // DB 조회를 위해 저장해두세요
        val folderName = intent.getStringExtra("FOLDER_NAME") ?: "Folder"
        tvFolderName.text = folderName // 상단 바에 폴더 이름 표시
        // Intent(화면 전환 시 넘어온 데이터)에서 폴더 이름을 가져오고 없으면 "Folder"로 표시

        tvFolderName.text = folderName

        initClickListeners() // 5. 클릭 이벤트
    }

    private fun initViews() {
        rvCalendar = findViewById(R.id.rv_calendar)
        rvDiaryList = findViewById(R.id.rv_diary_list)
        tvFolderName = findViewById(R.id.tv_folder_name)
        tvWeekTitle = findViewById(R.id.tv_week_title)
        btnPrevWeek = findViewById(R.id.btn_prev_week)
        btnNextWeek = findViewById(R.id.btn_next_week)
        btnPlus = findViewById(R.id.plusbutton3) // 새 일지 추가 버튼
    }

    // 더미데이터
    private fun createDummyData() {
        // 우리가 아까 고친 레이아웃대로: 첫 번째는 날짜, 두 번째는 제목(운동명)
        dummyDiaryList.add(DiaryItem("1", "26.04.27", "PT (가슴/삼두)", "재훈", null))
        dummyDiaryList.add(DiaryItem("2", "26.04.25", "개인운동 (하체)", "재훈", null))
    }

    // 하단 일지 리스트(RecyclerView) 설정
    private fun setupDiaryList() {
        rvDiaryList.layoutManager = LinearLayoutManager(this)

        diaryAdapter = DiaryAdapter(
            items = dummyDiaryList, // 여기서 dummyDiaryList는 ArrayList여야 합니다.
            onItemClick = { item ->
                Toast.makeText(this, "${item.title} 확인", Toast.LENGTH_SHORT).show()
            },
            onDotClick = { position -> // 👈 몇 번째 아이템인지 순서(position)를 받습니다.
                // 1. 리스트에서 해당 데이터 삭제
                val exitSheet = FolderExitSheet2(
                    folderName = tvFolderName.text.toString(),
                    onExitConfirm = {
                        // 2. 나가기 확정 시 실행될 로직
                        dummyDiaryList.removeAt(position)
                        diaryAdapter.notifyItemRemoved(position)
                        diaryAdapter.notifyItemRangeChanged(position, dummyDiaryList.size)

                        Toast.makeText(this, "폴더에서 나갔습니다.", Toast.LENGTH_SHORT).show()
                    }
                )

                // 3. 바텀시트 띄우기
                exitSheet.show(supportFragmentManager, "FolderExitSheet")
            }
        )
        rvDiaryList.adapter = diaryAdapter
    }

    // 상단 주간 날짜바(RecyclerView) 설정 함수
    private fun setupCalendar() {
        // 현재 캘린더 기준으로 이번 주의 달(title)과 날짜들(days)을 가져옴
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)
        tvWeekTitle.text = title

        dayAdapter = DayAdapter(days) { clickedDay ->
            // 날짜 클릭 시 동작할 로직 (현재는 비어 있음)
        }

        rvCalendar.apply {
            layoutManager = LinearLayoutManager(this@DiaryListActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dayAdapter
            post { dayAdapter.notifyDataSetChanged() } // 화면이 그려진 후 데이터 새로고침
        }
    }
//    private fun showExitBottomSheet(position: Int) {
//        // 1. BottomSheetDialog 객체 생성
//        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(this)
//
//        // 2. 작성하신 XML 레이아웃 인플레이트
//        val view = layoutInflater.inflate(R.layout.folder_exit_sheet, null) // 파일명이 bottom_sheet_exit라고 가정
//        bottomSheet.setContentView(view)
//
//        // 3. 바텀시트 내 버튼 연결
//        val btnClose = view.findViewById<ImageView>(R.id.btn_close_exit)
//        val btnRealExit = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_real_exit)
//        val btnStay = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_stay)
//
//        // X 버튼이나 머물기 버튼 클릭 시 닫기
//        btnClose.setOnClickListener { bottomSheet.dismiss() }
//        btnStay.setOnClickListener { bottomSheet.dismiss() }
//
//        // 실제 나가기 버튼 클릭 시 리스트에서 제거
//        btnRealExit.setOnClickListener {
//            dummyDiaryList.removeAt(position)
//            diaryAdapter.notifyItemRemoved(position)
//            diaryAdapter.notifyItemRangeChanged(position, dummyDiaryList.size)
//
//            bottomSheet.dismiss() // 동작 완료 후 닫기
//            Toast.makeText(this, "폴더에서 나갔습니다.", Toast.LENGTH_SHORT).show()
//        }
//
//        bottomSheet.show()
//    }

    // 각종 클릭 이벤트 설정 함수
    private fun initClickListeners() {
        // 뒤로가기 화살표 버튼 클릭 시 현재 액티비티 종료 (이전 화면으로 이동)
        findViewById<ImageView>(R.id.arrow_btn).setOnClickListener { finish() }

        // 이전 주/다음 주 이동 버튼
        btnPrevWeek.setOnClickListener { moveWeek(-1) }
        btnNextWeek.setOnClickListener { moveWeek(1) }

        // 플러스(+) 버튼 클릭 시 새 일지 작성 안내
//        btnPlus.setOnClickListener {
//
//        }
    }

    // 주간 이동 로직 함수
    private fun moveWeek(offset: Int) {
        // 캘린더에서 7일씩 앞/뒤로 이동
        currentCalendar.add(Calendar.DAY_OF_MONTH, offset * 7)

        // 바뀐 날짜 기준으로 주 정보 다시 계산
        val (title, days) = DateUtils.getWeekInfo(currentCalendar.time)

        // UI 갱신
        tvWeekTitle.text = title
        dayAdapter.updateData(days) // 날짜 어댑터의 데이터만 갱신
    }
}