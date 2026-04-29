package com.example.healthcareapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.adapter.FolderAdapter
import com.example.healthcareapp.data.ApiResponse
import com.example.healthcareapp.data.CreateFolderRequest
import com.example.healthcareapp.data.CreateFolderResponse
import com.example.healthcareapp.data.FolderListResponse
import com.example.healthcareapp.data.InviteLinkResponse
import com.example.healthcareapp.data.UpdateFolderRequest
import com.example.healthcareapp.data.UpdateFolderResponse
import com.example.healthcareapp.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FolderActivity2 : AppCompatActivity() {

    // 원본 데이터 (서버에서 가져온 전체 리스트)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.folder_list_test2)

        initViews()          // 1. 뷰 초기화
        setupRecyclerView()  // 2. 어댑터 및 바텀시트 연결
        setupListeners()     // 3. 필터/정렬/추가 버튼 리스너
    }

    override fun onResume() {
        super.onResume()
        loadFolders() // 화면 돌아올 때마다 데이터 새로고침
    }

    private fun initViews() {
        emptyView = findViewById(R.id.layout_empty)
        recyclerView = findViewById(R.id.folder_list)
        FilterShared = findViewById(R.id.filter_shared)
        SortModified = findViewById(R.id.sort_modified)
        SortCreated = findViewById(R.id.sort_created)
        btnAddFolder = findViewById(R.id.btn_add_folder)
        btnMakeFolderEmpty = findViewById(R.id.btn_make_empty)
        layoutSortFilter = findViewById(R.id.layout_sort_filter)
    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(mutableListOf()) { clickedFolder ->
            // 1. 메인 옵션 시트 (더보기 버튼 클릭 시)
            val bottomSheet = FolderOptionSheet(
                folder = clickedFolder,
                onLinkClick = {
                    // 초대 링크 생성 및 공유 (기존 shareFolder 호출)
                    shareFolder(clickedFolder)
                },
                onEditClick = {
                    // 2. 이름 수정 시트 호출
                    val editSheet = FolderEditSheet(clickedFolder.name) { newName ->
                        // 시트에서 확인을 누르면 서버 API 호출
                        updateFolderName(clickedFolder, newName)
                    }
                    editSheet.show(supportFragmentManager, "EditSheet")
                },
                onExitClick = {
                    // 3. 나가기 확인 시트 호출
                    val exitSheet = FolderExitSheet(clickedFolder.name) {
                        // 시트에서 나가기를 누르면 서버 API 호출
                        leaveFolder(clickedFolder)
                    }
                    exitSheet.show(supportFragmentManager, "ExitSheet")
                }
            )
            bottomSheet.show(supportFragmentManager, "Options")
        }

        recyclerView.adapter = folderAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {
        // 공유 필터 토글
        FilterShared.setOnClickListener {
            isFilterSharedChecked = !isFilterSharedChecked
            FilterShared.isSelected = isFilterSharedChecked
            applyFilterAndSort()
        }

        // 정렬: 최근수정일순
        SortModified.setOnClickListener {
            currentSortType = "최근수정일순"
            applyFilterAndSort()
        }

        // 정렬: 생성일순
        SortCreated.setOnClickListener {
            currentSortType = "생성일순"
            applyFilterAndSort()
        }
//        btnAddFolder.setOnLongClickListener { // 예시: 추가 버튼을 길게 누르면 전체 삭제 실행
//            AlertDialog.Builder(this)
//                .setTitle("서버 데이터 초기화")
//                .setMessage("꼬여있는 모든 폴더를 하나씩 삭제하시겠습니까?")
//                .setPositiveButton("삭제 실행") { _, _ ->
//                    // 리스트의 복사본을 만들어 반복문을 돌립니다 (안전성 확보)
//                    val tempTargetList = folderList.toList()
//                    tempTargetList.forEach { folder ->
//                        leaveFolder(folder)
//                    }
//                }
//                .setNegativeButton("취소", null)
//                .show()
//            true
//        }


        // setupListeners 내부의 수정된 코드
        btnAddFolder.setOnClickListener {
            // 1. 이름이 "새 폴더 "로 시작하는 것들 중에서 숫자만 추출
            val lastNumber = folderList
                .filter { it.name.startsWith("새 폴더 ") }
                .mapNotNull { it.name.replace("새 폴더 ", "").toIntOrNull() }
                .maxOrNull() ?: 0 // 폴더가 없으면 0부터 시작

            val nextNumber = lastNumber + 1
            createFolder("새 폴더 $nextNumber")
        }

        btnMakeFolderEmpty.setOnClickListener {
            val nextNumber = folderList.size + 1
            createFolder("새 폴더 $nextNumber")
        }
    }

    // 서버에서 폴더 목록 로드
    private fun loadFolders() {
        RetrofitClient.folderService.getFolders()
            .enqueue(object : Callback<ApiResponse<FolderListResponse>> {
                override fun onResponse(call: Call<ApiResponse<FolderListResponse>>, response: Response<ApiResponse<FolderListResponse>>) {
                    if (response.isSuccessful) {
                        val folders = response.body()?.data?.folders ?: return

                        folderList.clear() // 이 부분이 확실히 있어야 합니다!

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
                    Toast.makeText(this@FolderActivity2, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // 핵심: 필터링과 정렬을 동시에 처리
    private fun applyFilterAndSort() {
        updateSortUI()

        // 1. 필터링
        var displayList = if (isFilterSharedChecked) {
            folderList.filter { it.isShared }
        } else {
            folderList.toList()
        }

        // 2. 정렬 (서버 데이터의 lastmodified 문자열 기준 정렬)
        displayList = if (currentSortType == "최근수정일순") {
            displayList.sortedByDescending { it.lastmodified }
        } else {
            displayList.sortedBy { it.lastmodified } // 서버 데이터 구조에 따라 createdAt 기준 정렬이 더 정확함
        }

        // 3. 어댑터 갱신
        folderAdapter.updateData(displayList)

        // 4. 빈 화면 처리
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
                    if (response.isSuccessful) {
                        loadFolders() // 생성 후 전체 다시 로드하여 리스트 동기화
                    }
                }
                override fun onFailure(call: Call<ApiResponse<CreateFolderResponse>>, t: Throwable) { /* 생략 */ }
            })
    }

    private fun shareFolder(folder: FolderItem) {
        RetrofitClient.folderService.createInviteLink(folder.folderId)
            .enqueue(object : Callback<ApiResponse<InviteLinkResponse>> {
                override fun onResponse(call: Call<ApiResponse<InviteLinkResponse>>, response: Response<ApiResponse<InviteLinkResponse>>) {
                    if (response.isSuccessful) {
                        val link = response.body()?.data?.inviteLink ?: return
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, link)
                        }
                        startActivity(Intent.createChooser(shareIntent, "초대 링크 공유"))
                    } else {
                        val msg = when (response.code()) {
                            403 -> "폴더 접근 권한이 없습니다."
                            409 -> "이미 2명이 참여 중입니다."
                            400 -> "닫힌 폴더입니다."
                            else -> "초대 링크 생성 실패 (${response.code()})"
                        }
                        Toast.makeText(this@FolderActivity2, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<InviteLinkResponse>>, t: Throwable) {
                    Toast.makeText(this@FolderActivity2, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun showEditDialog(folder: FolderItem) {
        val editText = EditText(this).apply {
            setText(folder.name)



            setSelectAllOnFocus(true)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("폴더 이름 변경")
            .setView(editText)
            .setPositiveButton("변경") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "폴더 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    updateFolderName(folder, newName)
                }
            }
            .setNegativeButton("취소", null)
            .create()

        // 3. 다이얼로그가 뜰 때 키보드를 강제로 한글 모드로 유도
        dialog.setOnShowListener {
            editText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }

        dialog.show()
    }

    private fun updateFolderName(folder: FolderItem, newName: String) {
        RetrofitClient.folderService.updateFolderName(folder.folderId, UpdateFolderRequest(newName))
            .enqueue(object : Callback<ApiResponse<UpdateFolderResponse>> {
                override fun onResponse(call: Call<ApiResponse<UpdateFolderResponse>>, response: Response<ApiResponse<UpdateFolderResponse>>) {
                    if (response.isSuccessful) {
                        val body = response.body()?.data ?: return
                        val index = folderList.indexOf(folder)
                        if (index != -1) {
                            folder.name = body.name
                            folder.lastmodified = formatDate(body.updatedAt ?: body.createdAt)
                            folderAdapter.notifyItemChanged(index)
                        }
                    } else {
                        val msg = when (response.code()) {
                            400 -> "폴더 이름이 올바르지 않습니다."
                            403 -> "권한이 없습니다."
                            else -> "이름 변경 실패 (${response.code()})"
                        }
                        Toast.makeText(this@FolderActivity2, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<UpdateFolderResponse>>, t: Throwable) {
                    Toast.makeText(this@FolderActivity2, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmAndExit(folder: FolderItem) {
        AlertDialog.Builder(this)
            .setTitle("폴더 나가기")
            .setPositiveButton("나가기") { _, _ -> leaveFolder(folder) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun leaveFolder(folder: FolderItem) {
        RetrofitClient.folderService.leaveFolder(folder.folderId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        // 서버에서 지워졌으므로, 전체 리스트를 다시 불러와서 화면을 갱신합니다.
                        // 이렇게 하면 중복된 '새 폴더 21'들도 서버 상태에 맞춰 정확히 사라집니다.
                        loadFolders()
                        Toast.makeText(this@FolderActivity2, "폴더에서 나갔습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@FolderActivity2, "나가기 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@FolderActivity2, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateSortUI() {
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
    private fun updateEmptyView() {
        if (folderList.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null) return ""
        return dateStr.replace("T", " ").take(16)
    }
}