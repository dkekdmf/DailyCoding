package com.example.healthcareapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.FolderAdapter
import com.example.healthcareapp.data.*
import com.example.healthcareapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FolderMainFragment : Fragment() {

    // 데이터 및 어댑터
    private val folderList = mutableListOf<FolderItem>()
    private lateinit var folderAdapter: FolderAdapter

    // UI 변수
    private lateinit var emptyView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var FilterShared: ImageView
    private lateinit var SortModified: TextView
    private lateinit var SortCreated: TextView
    private lateinit var btnAddFolder: ImageView
    private lateinit var btnMakeFolderEmpty: Button
    private lateinit var layoutSortFilter: View

    // 상태 변수
    private var currentSortType = "최근수정일순"
    private var isFilterSharedChecked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 기존 레이아웃 인플레이트
        val view = inflater.inflate(R.layout.folder_list_test2, container, false)

        initViews(view)
        setupRecyclerView()
        setupListeners()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadFolders() // 프래그먼트가 다시 보일 때 데이터 새로고침
    }

    private fun initViews(view: View) {
        emptyView = view.findViewById(R.id.layout_empty)
        recyclerView = view.findViewById(R.id.folder_list)
        FilterShared = view.findViewById(R.id.filter_shared)
        SortModified = view.findViewById(R.id.sort_modified)
        SortCreated = view.findViewById(R.id.sort_created)
        btnAddFolder = view.findViewById(R.id.btn_add_folder)
        btnMakeFolderEmpty = view.findViewById(R.id.btn_make_empty)
        layoutSortFilter = view.findViewById(R.id.layout_sort_filter)
    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(mutableListOf()) { clickedFolder ->
            // 옵션 바텀시트 연결
            val bottomSheet = FolderOptionSheet(
                folder = clickedFolder,
                onLinkClick = { shareFolder(clickedFolder) },
                onEditClick = {
                    val editSheet = FolderEditSheet(clickedFolder.name) { newName ->
                        updateFolderName(clickedFolder, newName)
                    }
                    editSheet.show(childFragmentManager, "EditSheet")
                },
                onExitClick = {
                    val exitSheet = FolderExitSheet(clickedFolder.name) {
                        leaveFolder(clickedFolder)
                    }
                    exitSheet.show(childFragmentManager, "ExitSheet")
                }
            )
            bottomSheet.show(childFragmentManager, "Options")
        }

        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        FilterShared.setOnClickListener {
            isFilterSharedChecked = !isFilterSharedChecked
            FilterShared.isSelected = isFilterSharedChecked
            applyFilterAndSort()
        }

        SortModified.setOnClickListener {
            currentSortType = "최근수정일순"
            applyFilterAndSort()
        }

        SortCreated.setOnClickListener {
            currentSortType = "생성일순"
            applyFilterAndSort()
        }

        btnAddFolder.setOnClickListener {
            val lastNumber = folderList
                .filter { it.name.startsWith("새 폴더 ") }
                .mapNotNull { it.name.replace("새 폴더 ", "").toIntOrNull() }
                .maxOrNull() ?: 0

            createFolder("새 폴더 ${lastNumber + 1}")
        }

        btnMakeFolderEmpty.setOnClickListener {
            createFolder("새 폴더 ${folderList.size + 1}")
        }
    }

    private fun loadFolders() {
        RetrofitClient.folderService.getFolders()
            .enqueue(object : Callback<ApiResponse<FolderListResponse>> {
                override fun onResponse(call: Call<ApiResponse<FolderListResponse>>, response: Response<ApiResponse<FolderListResponse>>) {
                    if (isAdded && response.isSuccessful) { // isAdded 체크로 안전성 확보
                        val folders = response.body()?.data?.folders ?: return
                        folderList.clear()
                        folders.forEach { dto ->
                            folderList.add(FolderItem(
                                folderId = dto.folderId,
                                name = dto.name,
                                isShared = dto.isShared,
                                lastmodified = formatDate(dto.updatedAt ?: dto.createdAt)
                            ))
                        }
                        applyFilterAndSort()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<FolderListResponse>>, t: Throwable) {
                    if (isAdded) Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun applyFilterAndSort() {
        updateSortUI()

        var displayList = if (isFilterSharedChecked) {
            folderList.filter { it.isShared }
        } else {
            folderList.toList()
        }

        displayList = if (currentSortType == "최근수정일순") {
            displayList.sortedByDescending { it.lastmodified }
        } else {
            displayList.sortedBy { it.lastmodified }
        }

        folderAdapter.updateData(displayList)

        if (folderList.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            layoutSortFilter.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            layoutSortFilter.visibility = View.VISIBLE
        }
    }

    private fun createFolder(name: String) {
        RetrofitClient.folderService.createFolder(CreateFolderRequest(name))
            .enqueue(object : Callback<ApiResponse<CreateFolderResponse>> {
                override fun onResponse(call: Call<ApiResponse<CreateFolderResponse>>, response: Response<ApiResponse<CreateFolderResponse>>) {
                    if (isAdded && response.isSuccessful) loadFolders()
                }
                override fun onFailure(call: Call<ApiResponse<CreateFolderResponse>>, t: Throwable) {}
            })
    }

    private fun shareFolder(folder: FolderItem) {
        RetrofitClient.folderService.createInviteLink(folder.folderId)
            .enqueue(object : Callback<ApiResponse<InviteLinkResponse>> {
                override fun onResponse(call: Call<ApiResponse<InviteLinkResponse>>, response: Response<ApiResponse<InviteLinkResponse>>) {
                    if (isAdded && response.isSuccessful) {
                        val link = response.body()?.data?.inviteLink ?: return
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, link)
                        }
                        startActivity(Intent.createChooser(shareIntent, "초대 링크 공유"))
                    }
                }
                override fun onFailure(call: Call<ApiResponse<InviteLinkResponse>>, t: Throwable) {}
            })
    }

    private fun updateFolderName(folder: FolderItem, newName: String) {
        RetrofitClient.folderService.updateFolderName(folder.folderId, UpdateFolderRequest(newName))
            .enqueue(object : Callback<ApiResponse<UpdateFolderResponse>> {
                override fun onResponse(call: Call<ApiResponse<UpdateFolderResponse>>, response: Response<ApiResponse<UpdateFolderResponse>>) {
                    if (isAdded && response.isSuccessful) {
                        val body = response.body()?.data ?: return
                        val index = folderList.indexOf(folder)
                        if (index != -1) {
                            folder.name = body.name
                            folder.lastmodified = formatDate(body.updatedAt ?: body.createdAt)
                            folderAdapter.notifyItemChanged(index)
                        }
                    }
                }
                override fun onFailure(call: Call<ApiResponse<UpdateFolderResponse>>, t: Throwable) {}
            })
    }

    private fun leaveFolder(folder: FolderItem) {
        RetrofitClient.folderService.leaveFolder(folder.folderId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (isAdded && response.isSuccessful) {
                        loadFolders()
                        Toast.makeText(requireContext(), "폴더에서 나갔습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }

    private fun updateSortUI() {
        if (!isAdded) return
        val boldType = Typeface.defaultFromStyle(Typeface.BOLD)
        val normalType = Typeface.defaultFromStyle(Typeface.NORMAL)

        if (currentSortType == "최근수정일순") {
            SortModified.setTextColor(Color.BLACK)
            SortModified.typeface = boldType
            SortCreated.setTextColor(Color.parseColor("#AAAAAA"))
            SortCreated.typeface = normalType
        } else {
            SortCreated.setTextColor(Color.BLACK)
            SortCreated.typeface = boldType
            SortModified.setTextColor(Color.parseColor("#AAAAAA"))
            SortModified.typeface = normalType
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null) return ""
        return dateStr.replace("T", " ").take(16)
    }
}