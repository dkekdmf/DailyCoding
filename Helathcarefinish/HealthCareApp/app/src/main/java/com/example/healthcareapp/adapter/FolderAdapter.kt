package com.example.healthcareapp.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.HomeActivity
import com.example.healthcareapp.R
import com.example.healthcareapp.data.FolderItem
import java.text.SimpleDateFormat
import java.util.Locale

class FolderAdapter(
    private var items: MutableList<FolderItem>,
    private val onMoreClick: (FolderItem) -> Unit
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    private val stripeColors = listOf(
        Color.parseColor("#FF69B4"), // 핑크 순정 색상 포함 유연한 매핑
        Color.parseColor("#FF8A8A"),
        Color.parseColor("#FFB38A"),
        Color.parseColor("#FFE58A"),
        Color.parseColor("#96FF8A"),
        Color.parseColor("#8AFFFF"),
        Color.parseColor("#8AAEFF"),
        Color.parseColor("#C88AFF")
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 🎯 [정밀 바인딩] 올려주신 XML의 ID 명칭과 100% 매싱을 완료하여 널포인터를 사전에 차단합니다.
        val tvTitle: TextView = view.findViewById(R.id.tv_folder_title)
        val lastmodified: TextView = view.findViewById(R.id.tv_last_modified)
        val leftStripe: View = view.findViewById(R.id.view_left_stripe)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val layoutBadgeContainer: View = view.findViewById(R.id.layout_status_badge_container)
        val btnMore: ImageView = view.findViewById(R.id.btn_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 원래 재훈님의 폴더 리스트 아이템 파일 규격을 그대로 안전하게 결합합니다.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item3, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = items[position]
        val context = holder.itemView.context

        holder.tvTitle.text = folder.name
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        // 2. 원하는 출력 형식 (26.04.10)
        val outputFormat = SimpleDateFormat("yy.MM.dd", Locale.getDefault())

        val date = inputFormat.parse(folder.lastmodified)
        val formattedDate = if (date != null) outputFormat.format(date) else folder.lastmodified

        // 3. 변환된 날짜 뒤에 문구 결합
        holder.lastmodified.text = "${formattedDate}에 최종 수정"

        // 왼쪽 스트라이프 색상 부여
        val randomColorIndex = Math.abs(folder.folderId.hashCode()) % stripeColors.size
        val color = stripeColors[randomColorIndex]
        holder.leftStripe.setBackgroundColor(color)

        // ==================================================================
        // 🎯 [목업 제어 시스템] 고유 폴더 ID 추적 모드
        // ==================================================================
        val sharedPrefs = context.getSharedPreferences("MockSharedPrefs", Context.MODE_PRIVATE)
        val isThisFolderMockSharing = sharedPrefs.getBoolean("IS_FOLDER_SHARING_${folder.folderId}", false)

        val finalSharedState = if (isThisFolderMockSharing) true else folder.isShared

        // 정밀 판독된 상태값으로 배지 UI 데코레이션 가동
        updateStatusUI(holder, finalSharedState)

        // 배지 영역 수동 토글 대신 안전하게 데이터 락 유지 (불필요한 클릭 간섭 차단)
        holder.tvStatus.setOnClickListener(null)
        holder.layoutBadgeContainer.setOnClickListener(null)

        holder.btnMore.setOnClickListener {
            onMoreClick(folder)
        }

        holder.itemView.setOnClickListener {
            val homeActivity = it.context as? HomeActivity
            if (homeActivity != null) {
                android.util.Log.d("JaehoonLog", "🚀 폴더진입 -> 폴더명: ${folder.name}, ID: ${folder.folderId}, 공유상태: $finalSharedState")

                // UI 배지 분기 상태와 내부 일지 모드 구조를 완벽하게 동기화하여 진입시킵니다!
                homeActivity.moveToJournalTab(
                    folderId = folder.folderId,
                    folderName = folder.name,
                    isSharedMode = finalSharedState
                )
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<FolderItem>) {
        this.items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * 🎯 [디자인 싱크] 공유 모드 활성화에 따른 실시간 배지 색상 전환 규격
     */
    private fun updateStatusUI(holder: ViewHolder, isShared: Boolean) {
        if (isShared) {
            holder.tvStatus.text = "공유중"
            holder.layoutBadgeContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E1EFFF"))
            holder.tvStatus.setTextColor(Color.parseColor("#3A8DFF"))
        } else {
            holder.tvStatus.text = "공유대기"
            holder.layoutBadgeContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F2F2F2"))
            holder.tvStatus.setTextColor(Color.parseColor("#888888"))
        }
    }
}