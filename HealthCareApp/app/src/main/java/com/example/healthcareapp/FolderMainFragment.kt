package com.example.healthcareapp

import FolderOptionSheet
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
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


import androidx.fragment.app.Fragment

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FolderMainFragment : Fragment() {

    private val allFolders = mutableListOf<FolderItem>()
    private lateinit var folderAdapter: FolderAdapter

    private lateinit var emptyView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var cbFilterShared: ImageView
    private lateinit var tvSortModified: TextView
    private lateinit var tvSortCreated: TextView
    private lateinit var btnAddFolder: ImageView
    private lateinit var btnMakeFolderEmpty: Button
    private lateinit var layoutSortFilter: View

    private var currentSortType = "최근수정일순"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.folder_list_test2, container, false)

        initViews(view)
        setupAdapter()
        setupListeners()
        applyFilterAndSort()

        return view
    }

    private fun initViews(view: View) {
        emptyView = view.findViewById(R.id.layout_empty)
        recyclerView = view.findViewById(R.id.rv_folder_list)
        cbFilterShared = view.findViewById(R.id.cb_filter_shared)
        tvSortModified = view.findViewById(R.id.tv_sort_modified)
        tvSortCreated = view.findViewById(R.id.tv_sort_created)
        btnAddFolder = view.findViewById(R.id.btn_add_folder)
        btnMakeFolderEmpty = view.findViewById(R.id.btn_make_folder_empty)
        layoutSortFilter = view.findViewById(R.id.layout_sort_filter)
    }

    private fun setupAdapter() {
        folderAdapter = FolderAdapter(mutableListOf()) { clickedFolder ->
            // 1단계: 기본 옵션 바텀시트 띄우기
            val bottomSheet = FolderOptionSheet(
                folder = clickedFolder,
                onLinkClick = {

                    showShareSheet(clickedFolder)
                },
                onEditClick = { showEditSheet(clickedFolder) },
                onExitClick = {

                    showExitSheet(clickedFolder)
                }
            )
            bottomSheet.show(parentFragmentManager, "FolderOptions")
        }
        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }


    private fun showShareSheet(folder: FolderItem) {
        val shareSheet = FolderShareSheet(folder.name) {

            folder.isShared = true
            folder.status = "공유중"
            applyFilterAndSort()

        }
        shareSheet.show(parentFragmentManager, "FolderShare")
    }

    // --- 나가기 확인 바텀시트 호출 ---
    private fun showExitSheet(folder: FolderItem) {
        val exitSheet = FolderExitSheet(folder.name) {

            allFolders.remove(folder)
            applyFilterAndSort()
        }
        exitSheet.show(parentFragmentManager, "FolderExit")
    }
    private var isFilterSharedChecked = false
    private fun setupListeners() {
        cbFilterShared.setOnClickListener {
            isFilterSharedChecked = !isFilterSharedChecked // 상태 반전

            // 이미지 교체 로직 (Selector를 쓰신다면 isSelected만 바꿔주면 됨)
            cbFilterShared.isSelected = isFilterSharedChecked

            applyFilterAndSort()
        }

        tvSortModified.setOnClickListener {
            currentSortType = "최근수정일순"
            applyFilterAndSort()
        }

        tvSortCreated.setOnClickListener {
            currentSortType = "생성일순"
            applyFilterAndSort()
        }

        btnAddFolder.setOnClickListener { addNewFolder() }
        btnMakeFolderEmpty.setOnClickListener { addNewFolder() }
    }

    private fun applyFilterAndSort() {
        updateSortUI()

        // 1. 필터링 로직
        var displayList = if (isFilterSharedChecked) {
            allFolders.filter { it.isShared }
        } else {
            allFolders
        }

        // 2. 정렬 로직
        displayList = when (currentSortType) {
            "최근수정일순" -> displayList.sortedByDescending { it.lastmodified }
            "생성일순" -> displayList.sortedByDescending { it.createdAt }
            else -> displayList
        }

        folderAdapter.updateData(displayList)

        // 3. 🌟 가시성 제어 (이 부분이 핵심입니다)
        if (allFolders.isEmpty()) {
            // [상황 1] 폴더가 아예 하나도 없는 '완전 빈 상태'
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            layoutSortFilter.visibility = View.GONE // 상단바도 숨김
        } else {
            // [상황 2] 폴더가 1개라도 있는 상태
            // 💡 필터링 결과가 있든 없든, 안내 문구(emptyView)는 절대 띄우지 않습니다.
            emptyView.visibility = View.GONE

            // 상단바와 리사이클러뷰는 항상 유지합니다.
            // 리스트가 비어있으면(공유 폴더가 없으면) 그냥 하얀 빈 화면으로 보입니다.
            layoutSortFilter.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateSortUI() {
        val activeColor = Color.BLACK
        val inactiveColor = Color.parseColor("#AAAAAA")

        if (currentSortType == "최근수정일순") {
            tvSortModified.setTextColor(activeColor)
            tvSortModified.setTypeface(null, Typeface.BOLD)
            tvSortCreated.setTextColor(inactiveColor)
            tvSortCreated.setTypeface(null, Typeface.NORMAL)
        } else {
            tvSortCreated.setTextColor(activeColor)
            tvSortCreated.setTypeface(null, Typeface.BOLD)
            tvSortModified.setTextColor(inactiveColor)
            tvSortModified.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun addNewFolder() {
        // 1단계: 추가 확인 바텀시트 띄우기
        val confirmSheet = FolderAddConfirmSheet {
            // 2단계: 실제 데이터 추가 로직 (사용자가 '추가하기'를 눌렀을 때만 실행)
            val newFolder = FolderItem(
                name = "untitled${allFolders.size + 1}",
                lastmodified = getCurrentTime(),
                status = "공유대기",
                isShared = false,
                createdAt = System.currentTimeMillis()
            )
            allFolders.add(newFolder)
            applyFilterAndSort()

            // 3단계: 추가 완료 바텀시트 띄우기
            val completeSheet = FolderAddCompleteSheet()
            completeSheet.show(parentFragmentManager, "AddComplete")
        }

        confirmSheet.show(parentFragmentManager, "AddConfirm")
    }

    private fun showEditDialog(folder: FolderItem) {
        val editText = EditText(requireContext())
        editText.setText(folder.name)
        AlertDialog.Builder(requireContext())
            .setTitle("폴더 이름 변경")
            .setView(editText)
            .setPositiveButton("변경") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotEmpty()) {
                    folder.name = newName
                    folder.lastmodified = getCurrentTime()
                    applyFilterAndSort()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
    private fun showEditSheet(folder: FolderItem) {
        val editSheet = FolderEditSheet(folder.name) { newName ->
            // 여기서 실제 이름 변경 로직
            folder.name = newName
            folder.lastmodified = getCurrentTime()
            applyFilterAndSort()
        }
        editSheet.show(parentFragmentManager, "FolderEdit")
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("yy.MM.dd", Locale.KOREA).format(Date())
    }
}
