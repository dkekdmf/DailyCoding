import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.healthcareapp.FolderExitSheet
import com.example.healthcareapp.FolderItem
import com.example.healthcareapp.FolderShareSheet
import com.example.healthcareapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class FolderOptionSheet(
    val folder: FolderItem,
    val onLinkClick: () -> Unit,
    val onEditClick: () -> Unit,
    val onExitClick: () -> Unit
) : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet1, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_sheet_title)
        val btnClose = view.findViewById<View>(R.id.btn_close_sheet)
        val btnCreateLink = view.findViewById<View>(R.id.btn_create_link)
        val btnEditName = view.findViewById<View>(R.id.btn_edit_folder_name)
        val btnExit = view.findViewById<View>(R.id.btn_exit_folder)

        tvTitle.text = folder.name

        // 공유 상태에 따른 가시성 제어
        btnCreateLink.visibility = if (folder.isShared) View.GONE else View.VISIBLE

        btnClose.setOnClickListener { dismiss() }

        // [공유 링크 만들기] 클릭 시: 알림만 주고 자신은 퇴장
        btnCreateLink.setOnClickListener {
            onLinkClick()
            dismiss()
        }

        // [이름 변경] 클릭 시
        btnEditName.setOnClickListener {
            onEditClick()
            dismiss()
        }

        // [폴더 나가기] 클릭 시: 알림만 주고 자신은 퇴장
        btnExit.setOnClickListener {
            onExitClick()
            dismiss()
        }

        return view
    }
}