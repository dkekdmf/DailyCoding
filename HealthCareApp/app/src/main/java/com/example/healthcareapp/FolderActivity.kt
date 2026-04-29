//package com.example.healthcareapp
//
//import android.graphics.Color
//import android.graphics.Typeface
//import android.icu.text.SimpleDateFormat
//import android.os.Bundle
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.healthcareapp.adapter.FolderAdapter
//import java.util.Locale
//
//
//class FolderActivity : AppCompatActivity() {
//
//    // 데이터 및 어댑터 변수
//    private val allFolders = mutableListOf<FolderItem2>() // 앱이 켜져 있는 동안 유지되는 폴더 리스트
//    private lateinit var folderAdapter: FolderAdapter     // 리스트 뷰와 데이터를 연결
//
//    // UI 변수
//    private lateinit var emptyView: View            // 폴더가 없을 때 보여줄 안내 화면
//    private lateinit var recyclerView: RecyclerView  // item들이 표시됨
//    private lateinit var FilterShared: ImageView   // 공유된 폴더 필터 버튼
//    private lateinit var SortModified: TextView    // 최근수정일순 정렬 버튼
//    private lateinit var SortCreated: TextView     // 생성일순 정렬 버튼
//    private lateinit var btnAddFolder: ImageView     // 폴더 추가 버튼 (상단)
//    private lateinit var btnMakeFolderEmpty: Button  // 폴더 추가 버튼 (빈 화면 중앙)
//    private lateinit var layoutSortFilter: View      // 정렬 및 필터 버튼들을 담고 있는 상단 바 영역
//
//    private var currentSortType = "최근수정일순" // 현재 어떤 정렬이 선택되었는지 저장하는 상태 변수
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.folder_list_test2) // 레이아웃 연결
//
//        initViews()          // 뷰 객체들을 ID와 연결
//        setupRecyclerView()  // 어댑터 설정 및 바텀시트 클릭 이벤트 정의
//        setupListeners()     // 버튼들의 클릭 리스너(동작) 설정
//        applyFilterAndSort() // 초기 화면 데이터 구성
//    }
//
//
//    private fun initViews() {
//        emptyView = findViewById(R.id.layout_empty)
//        recyclerView = findViewById(R.id.folder_list)
//        FilterShared = findViewById(R.id.filter_shared)
//        SortModified = findViewById(R.id.sort_modified)
//        SortCreated = findViewById(R.id.sort_created)
//        btnAddFolder = findViewById(R.id.btn_add_folder)
//        btnMakeFolderEmpty = findViewById(R.id.btn_make_empty)
//        layoutSortFilter = findViewById(R.id.layout_sort_filter)
//    }
//
//    // 리사이클러뷰 세팅
//    private fun setupRecyclerView() {
//        // 어댑터 생성: 두 번째 인자로 폴더의 더보기 버튼을 눌렀을 때 실행될 코드를 전달함
//        folderAdapter = FolderAdapter(mutableListOf()) { clickedFolder ->
//
//            // 옵션 바텀시트(공유/수정/삭제 선택창) 띄우기
//            val bottomSheet = FolderOptionSheet(
//                folder = clickedFolder,
//                onLinkClick = {
//                    // 공유 클릭 시: 공유 확인 시트 호출
//                    val shareSheet = FolderShareSheet(clickedFolder.name) {
//                        clickedFolder.isShared = true // 공유 상태로 변경
//                        clickedFolder.status = "공유중"
//                        applyFilterAndSort() // 변경사항 반영을 위해 리스트 갱신
//                    }
//                    shareSheet.show(supportFragmentManager, "Share")
//                },
//                onEditClick = {
//                    // 이름 수정 클릭 시: 수정 입력 시트 호출
//                    val editSheet = FolderEditSheet(clickedFolder.name) { newName ->
//                        clickedFolder.name = newName
//                        clickedFolder.lastmodified = getCurrentTime() // 수정 시간 업데이트
//                        applyFilterAndSort()
//                    }
//                    editSheet.show(supportFragmentManager, "Edit")
//                },
//                onExitClick = {
//                    // 나가기 클릭 시: 삭제 확인 시트 호출
//                    val exitSheet = FolderExitSheet(clickedFolder.name) {
//                        allFolders.remove(clickedFolder) // 리스트에서 데이터 삭제
//                        applyFilterAndSort()
//                    }
//                    exitSheet.show(supportFragmentManager, "Exit")
//                }
//            )
//            bottomSheet.show(supportFragmentManager, "Options")
//        }
//
//        recyclerView.adapter = folderAdapter
//        recyclerView.layoutManager = LinearLayoutManager(this)
//    }
//
//    private var isFilterSharedChecked = false // 현재 공유 필터가 켜져 있는지 여부
//
//    private fun setupListeners() {
//        // 필터 아이콘 클릭 시 토글 동작
//        FilterShared.setOnClickListener {
//            isFilterSharedChecked = !isFilterSharedChecked
//            FilterShared.isSelected = isFilterSharedChecked // XML Selector에 따라 이미지 색상 변경
//            applyFilterAndSort()
//        }
//
//        // 정렬 기준 변경 버튼들
//        SortModified.setOnClickListener {
//            currentSortType = "최근수정일순"
//            applyFilterAndSort()
//        }
//        SortCreated.setOnClickListener {
//            currentSortType = "생성일순"
//            applyFilterAndSort()
//        }
//
//        // 새 폴더 만들기 버튼 (두 종류 모두 같은 함수 호출)
//        btnAddFolder.setOnClickListener { addNewFolder() }
//        btnMakeFolderEmpty.setOnClickListener { addNewFolder() }
//    }
//
//    // 데이터 필터링 및 정렬을 수행한 뒤 화면을 새로고침하는 핵심 함수
//    private fun applyFilterAndSort() {
//        updateSortUI() // 정렬 글자 Bold 처리 업데이트
//
//        // 필터링: 공유 필터가 켜져 있으면 공유 중인 폴더만 추출
//        val displayList = if (isFilterSharedChecked) {
//            allFolders.filter { it.isShared }
//        } else {
//            allFolders
//        }
//
//        // 어댑터에 최종 리스트 전달
//        folderAdapter.updateData(displayList)
//
//        // 데이터 유무에 따른 화면 전환 ,전체 데이터가 0개면 빈 화면
//        if (allFolders.isEmpty()) {
//            emptyView.visibility = View.VISIBLE
//            recyclerView.visibility = View.GONE
//            layoutSortFilter.visibility = View.GONE // 상단바 영역도 숨김
//        } else {
//            emptyView.visibility = View.GONE
//            layoutSortFilter.visibility = View.VISIBLE
//            recyclerView.visibility = View.VISIBLE
//        }
//    }
//
//
//    private fun addNewFolder() {
//        // 진짜 추가할 것인지 묻는 확인 시트 호출
//        val confirmSheet = FolderAddConfirmSheet {
//            val newFolder = FolderItem2(
//                name = "untitled${allFolders.size + 1}",
//                lastmodified = getCurrentTime(),
//                status = "공유대기",
//                isShared = false,
//                createdAt = System.currentTimeMillis()
//            )
//            allFolders.add(newFolder) // 원본 데이터에 추가
//            applyFilterAndSort()      // UI 갱신
//
//            // 완료 알림 팝업 호출
//            FolderAddCompleteSheet().show(supportFragmentManager, "AddComplete")
//        }
//        confirmSheet.show(supportFragmentManager, "AddConfirm")
//    }
//
//    // 현재 선택된 정렬 방식에 따라 글씨를 굵게/희미하게 표시
//    private fun updateSortUI() {
//        if (currentSortType == "최근수정일순") {
//            SortModified.setTextColor(Color.BLACK)
//            SortModified.setTypeface(null, Typeface.BOLD)
//            SortCreated.setTextColor(Color.parseColor("#AAAAAA"))
//            SortCreated.setTypeface(null, Typeface.NORMAL)
//        } else {
//            SortCreated.setTextColor(Color.BLACK)
//            SortCreated.setTypeface(null, Typeface.BOLD)
//            SortModified.setTextColor(Color.parseColor("#AAAAAA"))
//            SortModified.setTypeface(null, Typeface.NORMAL)
//        }
//    }
//
//    // 현재 날짜 구하기
//    private fun getCurrentTime(): String {
//        val sdf = SimpleDateFormat("yy.MM.dd", Locale.KOREA)
//        return sdf.format(java.util.Date())
//    }
//}