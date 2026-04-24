//package com.example.healthcareapp
//
//import android.app.AlertDialog
//import android.content.Context
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.LinearLayout
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.healthcareapp.adapter.FolderAdapter
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class PrivateFolderFragment : Fragment() {
//
//    private val folderList = mutableListOf<FolderItem>()
//    private val PREFS_NAME = "private_folder_prefs"
//    private val KEY_FOLDERS = "private_folder_list"
//    private lateinit var folderAdapter: FolderAdapter
//    private lateinit var centerGroup: LinearLayout
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var btnadd: ImageView
//    private val viewModel: FolderViewModel by activityViewModels()
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//
//        return inflater.inflate(R.layout.folder_test, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewModel.addFolderEvent.observe(viewLifecycleOwner) {
//            createAndSaveFolder()
//        }
//
//
//
//        centerGroup = view.findViewById<LinearLayout>(R.id.center_group)
//        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView_folders)
//
//        //btnadd = view.findViewById<ImageView>(R.id.btn_add_folder)
//
//
//        // 2. 어댑터 초기화 로직 이사
//        folderAdapter = FolderAdapter(folderList) { clickedFolder ->
//            val bottomSheet = FolderOptionSheet(
//                folder = clickedFolder,
//                //onShareClick = { toggleShareStatus(clickedFolder) },
//                onEditClick = { showEditDialog(clickedFolder) },
//                onExitClick = { exitFolder(clickedFolder) }
//            )
//            // supportFragmentManager 대신 parentFragmentManager 사용
//            bottomSheet.show(parentFragmentManager, "FolderOptions")
//        }
//
//
//        recyclerView.adapter = folderAdapter
//        recyclerView.layoutManager = LinearLayoutManager(context)
//
//        loadData()
//        updateUI()
//
//        view.findViewById<Button>(R.id.btn_create_first_folder).setOnClickListener {
//            createAndSaveFolder()
//        }
//        //btnadd.setOnClickListener { createAndSaveFolder() }
//        // 3. 추가 버튼 클릭 로직 이사
////        btnCreate.setOnClickListener {
////            val newFolder = FolderItem(
////                name = "untitled (${folderList.size + 1})",
////                isShared = false,
////                lastmodified = getCurrentTime()
////            )
////            folderList.add(newFolder)
////            centerGroup.visibility = View.GONE
////            recyclerView.visibility = View.VISIBLE
////            btnadd.visibility = View.VISIBLE;
////            folderAdapter.notifyItemInserted(folderList.size - 1)
////        }
////        btnadd.setOnClickListener{
////            val newFolder = FolderItem(
////                name = "untitled (${folderList.size + 1})",
////                isShared = false,
////                lastmodified = getCurrentTime()
////            )
////            folderList.add(newFolder)
////            folderAdapter.notifyItemInserted(folderList.size - 1)
////        }
//
//    }
//    fun createAndSaveFolder() {
//        val newFolder = FolderItem(
//            name = "untitled (${folderList.size + 1})",
//           // isShared = false,
//            lastmodified = getCurrentTime()
//        )
//        folderList.add(newFolder)
//        saveData()   // 리스트가 바뀌었으니 저장!
//        updateUI()   // 바뀐 상태에 맞춰 화면 갱신
//        centerGroup.visibility = View.GONE
//        recyclerView.visibility = View.VISIBLE
//        //btnadd.visibility = View.VISIBLE;
//        folderAdapter.notifyItemInserted(folderList.size - 1)
//    }
//
//
//    private fun showEditDialog(folder: FolderItem) {
//        val editText = EditText(requireContext()) // this 대신 requireContext()
//        editText.setText(folder.name)
//
//        AlertDialog.Builder(requireContext()) // this 대신 requireContext()
//            .setTitle("폴더 이름 변경")
//            .setView(editText)
//            .setPositiveButton("변경") { _, _ ->
//                val newName = editText.text.toString()
//                if (newName.isNotEmpty()) {
//                    folder.name = newName
//                    folder.lastmodified = getCurrentTime()
//                    folderAdapter.notifyDataSetChanged()
//                }
//            }
//            .setNegativeButton("취소", null)
//            .show()
//    }
////
////    private fun toggleShareStatus(folder: FolderItem) {
////        folder.isShared = !folder.isShared
////        folderAdapter.notifyDataSetChanged()
////    }
//
//    private fun exitFolder(folder: FolderItem) {
//        val index = folderList.indexOf(folder)
//        if (index != -1) {
//            folderList.removeAt(index)
//            folderAdapter.notifyItemRemoved(index)
//            if (folderList.isEmpty()) {
//                view?.findViewById<View>(R.id.layout_empty)?.visibility = View.VISIBLE
//            }
//        }
//    }
//    private fun saveData() {
//        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        val gson = Gson()
//        val json = gson.toJson(folderList) // 리스트를 문자열(JSON)로 변환
//        editor.putString(KEY_FOLDERS, json)
//        editor.apply()
//    }
//
//    // 저장된 데이터를 불러오는 함수
//    private fun loadData() {
//        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val gson = Gson()
//        val json = sharedPreferences.getString(KEY_FOLDERS, null)
//
//        if (json != null) {
//            val type = object : TypeToken<MutableList<FolderItem>>() {}.type
//            val savedList: MutableList<FolderItem> = gson.fromJson(json, type)
//            folderList.clear()
//            folderList.addAll(savedList)
//            folderAdapter.notifyDataSetChanged()
//        }
//    }
//
//    private fun updateUI() {
//
//        if (folderList.isNotEmpty()) {
//            centerGroup.visibility = View.VISIBLE
//            recyclerView.visibility = View.GONE
//            //btnadd.visibility = View.GONE
//
//        } else {
//            centerGroup.visibility = View.GONE
//            recyclerView.visibility = View.VISIBLE
//
//            // btnadd.visibility = View.VISIBLE
//        }
//    }
//
//    private fun getCurrentTime(): String {
//        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
//        return sdf.format(Date())
//    }
//}
