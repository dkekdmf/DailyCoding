package com.example.healthcareapp

import FolderOptionSheet
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.android.material.tabs.TabLayout
import java.sql.Date
import java.util.Locale
import java.util.Random
class FolderAdapter(
    var items: MutableList<FolderItem>,
    val onMoreClick: (FolderItem) -> Unit // 점점점 버튼 클릭 시 바텀시트 호출용
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    private val stripeColors = intArrayOf(
        Color.parseColor("#FF6969"), Color.parseColor("#FF9245"),
        Color.parseColor("#FFD153"), Color.parseColor("#94A769"),
        Color.parseColor("#5DCE46"), Color.parseColor("#83F2FA"),
        Color.parseColor("#53A1FF"), Color.parseColor("#8A38F5"),
        Color.parseColor("#FF5DEF"), Color.parseColor("#A39288")
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_folder_title)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnMore: View = view.findViewById(R.id.btn_more)
        val leftStripe: View = view.findViewById(R.id.view_left_stripe)
        val lastmodified: TextView = view.findViewById(R.id.tv_last_modified)
        val layoutstatus : ConstraintLayout = view.findViewById(R.id.layout_status_badge_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item3, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = items[position]

        holder.tvTitle.text = folder.name
        holder.lastmodified.text = "${folder.lastmodified}에 최종 수정"

        // 스트라이프 색상 고정
        val color = stripeColors[position % stripeColors.size]
        holder.leftStripe.setBackgroundColor(color)

        // 초기 UI 상태 설정 (아래 함수로 분리해서 재사용)
        updateStatusUI(holder, folder.isShared)

        // 🌟 1. 공유중/비공개 버튼 클릭 시 상태만 즉시 변경
        holder.tvStatus.setOnClickListener {
            folder.isShared = !folder.isShared
            folder.status = if (folder.isShared) "공유중" else "공유대기"

            // UI 즉시 업데이트
            updateStatusUI(holder, folder.isShared)
        }


        holder.btnMore.setOnClickListener {
            onMoreClick(folder)
        }
    }


    private fun updateStatusUI(holder: ViewHolder, isShared: Boolean) {
        if (isShared) {
            holder.tvStatus.text = "공유중"
            holder.layoutstatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E1EFFF"))
            holder.tvStatus.setTextColor(Color.parseColor("#3A8DFF"))
        } else {
            holder.tvStatus.text = "공유대기"
            holder.layoutstatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F2F2F2"))
            holder.tvStatus.setTextColor(Color.parseColor("#888888"))
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<FolderItem>) {
        this.items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
class FolderActivity : AppCompatActivity() {

    private val allFolders = mutableListOf<FolderItem>()
    private lateinit var folderAdapter: FolderAdapter

    // UI 컴포넌트
    private lateinit var emptyView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var cbFilterShared: ImageView
    private lateinit var tvSortModified: TextView
    private lateinit var tvSortCreated: TextView
    private lateinit var btnAddFolder: ImageView
    private lateinit var btnMakeFolderEmpty: Button
    private lateinit var layoutSortFilter: View
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView

    private var currentSortType = "최근수정일순"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.folder_list_test2)

        // 1. 뷰 연결
        initViews()

        // 2. 어댑터 및 리사이클러뷰 설정
        setupRecyclerView()

        // 3. 리스너 등록 (🌟 이 부분이 누락되어 클릭이 안 됐던 것입니다)
        setupListeners()

        // 4. 초기 화면 갱신 (🌟 이 부분이 누락되어 로그가 안 찍혔던 것입니다)
        applyFilterAndSort()

        Log.d("DEBUG_FOLDER", "Activity onCreate 완료 및 초기 로직 실행됨")
    }

    private fun initViews() {
        emptyView = findViewById(R.id.layout_empty)
        recyclerView = findViewById(R.id.rv_folder_list)
        cbFilterShared = findViewById(R.id.cb_filter_shared)
        tvSortModified = findViewById(R.id.tv_sort_modified)
        tvSortCreated = findViewById(R.id.tv_sort_created)
        btnAddFolder = findViewById(R.id.btn_add_folder)
        btnMakeFolderEmpty = findViewById(R.id.btn_make_folder_empty)
        layoutSortFilter = findViewById(R.id.layout_sort_filter)
        tvEmptyTitle = findViewById(R.id.tv_empty_title)
        tvEmptySubtitle = findViewById(R.id.tv_empty_subtitle)
    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(mutableListOf()) { clickedFolder ->
            val bottomSheet = FolderOptionSheet(
                folder = clickedFolder,
                onLinkClick = {

                    val shareSheet = FolderShareSheet(clickedFolder.name) {
                        clickedFolder.isShared = true
                        clickedFolder.status = "공유중"
                        applyFilterAndSort()
                    }
                    shareSheet.show(supportFragmentManager, "Share")
                },
                onEditClick = {
                    // 이름 변경 바텀시트 (우리가 만든 커스텀)
                    val editSheet = FolderEditSheet(clickedFolder.name) { newName ->
                        clickedFolder.name = newName
                        clickedFolder.lastmodified = getCurrentTime()
                        applyFilterAndSort()
                    }
                    editSheet.show(supportFragmentManager, "Edit")
                },
                onExitClick = {
                    // 나가기 확인 바텀시트
                    val exitSheet = FolderExitSheet(clickedFolder.name) {
                        allFolders.remove(clickedFolder)
                        applyFilterAndSort()
                    }
                    exitSheet.show(supportFragmentManager, "Exit")
                }
            )
            bottomSheet.show(supportFragmentManager, "Options")
        }
        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    private var isFilterSharedChecked = false
    private fun setupListeners() {
        // 필터 체크박스
        cbFilterShared.setOnClickListener {
            isFilterSharedChecked = !isFilterSharedChecked // 상태 반전

            // 이미지 교체 로직 (Selector를 쓰신다면 isSelected만 바꿔주면 됨)
            cbFilterShared.isSelected = isFilterSharedChecked

            applyFilterAndSort()
        }

        // 정렬 텍스트 클릭
        tvSortModified.setOnClickListener {
            currentSortType = "최근수정일순"
            applyFilterAndSort()
        }
        tvSortCreated.setOnClickListener {
            currentSortType = "생성일순"
            applyFilterAndSort()
        }

        // 폴더 추가 버튼들
        btnAddFolder.setOnClickListener { addNewFolder() }
        btnMakeFolderEmpty.setOnClickListener { addNewFolder() }
    }
    private fun applyFilterAndSort() {
        updateSortUI()

        // 1. 필터링 로직
        val displayList = if (isFilterSharedChecked) {
            allFolders.filter { it.isShared }
        } else {
            allFolders
        }

        // 2. 정렬 로직 (생략 - 기존과 동일)

        // 3. 어댑터 갱신 (데이터가 0개면 리스트 영역이 그냥 비어있게 됨)
        folderAdapter.updateData(displayList)

        // 4. 🌟 가시성 제어 (이 로직이 틀리면 안 됩니다)
        if (allFolders.isEmpty()) {
            // [케이스 A] 앱에 폴더가 단 하나도 없는 '완전 빈 상태'
            // 이때만 "폴더를 만드시겠습니까?" 로고와 버튼이 나와야 함
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            layoutSortFilter.visibility = View.GONE
        } else {
            // [케이스 B] 폴더가 1개라도 존재하는 상태
            // 필터 결과가 0개이든 10개이든 상관없이 무조건 안내 문구는 숨김!!
            emptyView.visibility = View.GONE

            // 상단바와 리사이클러뷰는 항상 유지 (리스트가 비어있으면 빈 대로 보여줌)
            layoutSortFilter.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun addNewFolder() {
        val confirmSheet = FolderAddConfirmSheet {
            val newFolder = FolderItem(
                name = "untitled${allFolders.size + 1}",
                lastmodified = getCurrentTime(),
                status = "공유대기",
                isShared = false,
                createdAt = System.currentTimeMillis()
            )
            allFolders.add(newFolder)
            applyFilterAndSort()

            // 추가 완료 바텀시트
            FolderAddCompleteSheet().show(supportFragmentManager, "AddComplete")
        }
        confirmSheet.show(supportFragmentManager, "AddConfirm")
    }

    private fun updateSortUI() {
        if (currentSortType == "최근수정일순") {
            tvSortModified.setTextColor(Color.BLACK)
            tvSortModified.setTypeface(null, Typeface.BOLD)
            tvSortCreated.setTextColor(Color.parseColor("#AAAAAA"))
            tvSortCreated.setTypeface(null, Typeface.NORMAL)
        } else {
            tvSortCreated.setTextColor(Color.BLACK)
            tvSortCreated.setTypeface(null, Typeface.BOLD)
            tvSortModified.setTextColor(Color.parseColor("#AAAAAA"))
            tvSortModified.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yy.MM.dd", Locale.KOREA)
        return sdf.format(java.util.Date())
    }
}