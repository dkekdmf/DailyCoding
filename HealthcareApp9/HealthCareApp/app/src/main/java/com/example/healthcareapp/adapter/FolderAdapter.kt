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

class FolderAdapter(
    private var items: MutableList<FolderItem>,
    private val onMoreClick: (FolderItem) -> Unit
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    private val stripeColors = listOf(
        Color.parseColor("#FF8A8A"),
        Color.parseColor("#FFB38A"),
        Color.parseColor("#FFE58A"),
        Color.parseColor("#96FF8A"),
        Color.parseColor("#8AFFFF"),
        Color.parseColor("#8AAEFF"),
        Color.parseColor("#C88AFF")
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_folder_title)
        val lastmodified: TextView = view.findViewById(R.id.tv_last_modified)
        val leftStripe: View = view.findViewById(R.id.view_left_stripe)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        // 🎯 [신규 추가] 배지 배경을 바꾸고 클릭 범위를 넓히기 위해 부모 컨테이너를 포착합니다!
        val layoutBadgeContainer: View = view.findViewById(R.id.layout_status_badge_container)
        val btnMore: ImageView = view.findViewById(R.id.btn_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item3, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = items[position]
        val context = holder.itemView.context

        holder.tvTitle.text = folder.name
        holder.lastmodified.text = "${folder.lastmodified}에 최종 수정"

        val randomColorIndex = Math.abs(folder.folderId.hashCode()) % stripeColors.size
        val color = stripeColors[randomColorIndex]
        holder.leftStripe.setBackgroundColor(color)

        // ==================================================================
        // 🎯 [목업 제어 시스템]
        // ==================================================================
        val sharedPrefs = context.getSharedPreferences("MockSharedPrefs", Context.MODE_PRIVATE)
        val isThisFolderMockSharing = sharedPrefs.getBoolean("IS_FOLDER_SHARING_${folder.folderId}", false)

        val finalSharedState = if (isThisFolderMockSharing) true else folder.isShared

        // 정상화된 데이터 상태로 배지 디자인 마킹 (글자 + 부모 배경 통틀어 제어)
        updateStatusUI(holder, finalSharedState)

        // 🎯 [터치 씹힘 해결] 글자뿐만 아니라 사각형 배지 컨테이너 전체에 클릭 리스너를 결합합니다!
        val badgeClickListener = View.OnClickListener {
            folder.isShared = !folder.isShared
            updateStatusUI(holder, folder.isShared)
        }
        holder.tvStatus.setOnClickListener(badgeClickListener)
        holder.layoutBadgeContainer.setOnClickListener(badgeClickListener)

        holder.btnMore.setOnClickListener {
            onMoreClick(folder)
        }

        holder.itemView.setOnClickListener {
            val homeActivity = it.context as? HomeActivity
            if (homeActivity != null) {
                android.util.Log.d("JaehoonLog", "🚀 폴더진입 -> 폴더명: ${folder.name}, 최종공유상태(UI): $finalSharedState")

                // ==================================================================
                // 🔓 [진입 분기 정상화]
                // UI가 [공유중](finalSharedState == true)이면 일지 화면도
                // 확실하게 공유 모드(true)로 진입하도록 파이프라인을 일치시킵니다!
                // ==================================================================
                homeActivity.moveToJournalTab(
                    folderId = folder.folderId,
                    folderName = folder.name,
                    isSharedMode = finalSharedState // 👈 기존의 강제 false 가드를 풀고, 진짜 상태값을 그대로 넘겨줍니다!
                )
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<FolderItem>) {
        this.items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    // ==================================================================
    // 🎯 [시안 매칭] 공유 여부에 따른 배지 스타일 정밀 변환 구역
    // ==================================================================
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