package com.example.healthcareapp.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
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
import com.example.healthcareapp.R
import com.example.healthcareapp.adapter.FolderAdapter
import com.example.healthcareapp.data.*
import com.example.healthcareapp.network.RetrofitClient
import com.example.healthcareapp.sheet.FolderEditSheet
import com.example.healthcareapp.sheet.FolderExitSheet
import com.example.healthcareapp.sheet.FolderOptionSheet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 하단 탭의 '폴더' 메뉴를 담당하는 프래그먼트
 * 서버의 폴더 목록을 관리하고 정렬, 필터링, 편집 기능을 제공
 */
class FolderMainFragment : Fragment() {

    private val folderList = mutableListOf<FolderItem>()
    private lateinit var folderAdapter: FolderAdapter

    private lateinit var emptyView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var FilterShared: ImageView
    private lateinit var SortModified: TextView
    private lateinit var SortCreated: TextView
    private lateinit var btnAddFolder: ImageView
    private lateinit var btnMakeFolderEmpty: Button
    private lateinit var layoutSortFilter: View

    private var currentSortType = "최근수정일순"
    private var isFilterSharedChecked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.folder_list_test2, container, false)
        initViews(view)
        setupRecyclerView()
        setupListeners()
        return view
    }

    override fun onResume() {
        super.onResume()
        // 화면이 다시 보일 때마다 서버로부터 진짜 데이터를 안전하게 땡겨옵니다.
        loadFolders()
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

    /**
     * [Retrofit] 서버에서 전체 폴더 목록 로드
     */
    private fun loadFolders() {
        RetrofitClient.folderService.getFolders()
            .enqueue(object : Callback<ApiResponse<FolderListResponse>> {
                override fun onResponse(call: Call<ApiResponse<FolderListResponse>>, response: Response<ApiResponse<FolderListResponse>>) {
                    if (isAdded && response.isSuccessful) {
                        val folders = response.body()?.data?.folders ?: return
                        folderList.clear()
                        folders.forEach { dto ->
                            folderList.add(
                                FolderItem(
                                    folderId = dto.folderId,
                                    name = dto.name,
                                    isShared = dto.isShared,
                                    lastmodified = formatDate(dto.updatedAt ?: dto.createdAt)
                                )
                            )
                        }
                        applyFilterAndSort()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<FolderListResponse>>, t: Throwable) {
                    if (isAdded) Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * 필터링과 정렬 기준에 맞춰 리스트 가공 후 어댑터 갱신
     */
    private fun applyFilterAndSort() {
        if (!isAdded) return
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

        // 리스트 상태에 따른 빈 화면 노출 여부 결정
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

    /**
     * [Retrofit] 초대 링크 생성 및 공유 시스템 다이얼로그 호출
     * 🎯 [안전빵 교정완료] requireContext() 대신 비동기 안전 가드를 채워 튕김과 데이터 유실을 막습니다.
     */
    private fun shareFolder(folder: FolderItem) {
        RetrofitClient.folderService.createInviteLink(folder.folderId)
            .enqueue(object : Callback<ApiResponse<InviteLinkResponse>> {
                override fun onResponse(call: Call<ApiResponse<InviteLinkResponse>>, response: Response<ApiResponse<InviteLinkResponse>>) {
                    if (isAdded && response.isSuccessful) {

                        // 🔒 [안전 가드] 프래그먼트가 유효하게 붙어있을 때만 주머니 데이터를 안전하게 굽습니다.
                        activity?.let { act ->
                            val sharedPrefs = act.getSharedPreferences("MockSharedPrefs", Context.MODE_PRIVATE)
                            sharedPrefs.edit().putBoolean("IS_FOLDER_SHARING_${folder.folderId}", true).apply()
                            Log.d("JaehoonLog", "🎯 공유링크 실시간 매핑 성공 -> 폴더명: ${folder.name}, ID: ${folder.folderId}")
                        }

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