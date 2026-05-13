package com.example.healthcareapp.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.FolderItem
import com.example.healthcareapp.HomeActivity
import com.example.healthcareapp.R

class FolderAdapter(
    var items: MutableList<FolderItem>,    // 폴더 데이터 리스트
    val onMoreClick: (FolderItem) -> Unit // 더보기(...) 버튼 클릭 시 호출할 함수
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
        val layoutstatus: ConstraintLayout = view.findViewById(R.id.layout_status_badge_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item3, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = items[position]

        holder.tvTitle.text = folder.name
        holder.lastmodified.text = "${folder.lastmodified}에 최종 수정"

        val color = stripeColors[position % stripeColors.size]
        holder.leftStripe.setBackgroundColor(color)

        updateStatusUI(holder, folder.isShared)

        holder.tvStatus.setOnClickListener {
            folder.isShared = !folder.isShared
            updateStatusUI(holder, folder.isShared)
        }

        holder.btnMore.setOnClickListener {
            onMoreClick(folder)
        }

        holder.itemView.setOnClickListener {
            val homeActivity = it.context as? HomeActivity
            if (homeActivity != null) {
                // ⭐ 로그 추가: 클릭한 폴더의 실제 상태를 로그캣(Logcat)에서 확인하세요.
                android.util.Log.d("JaehoonLog", "폴더명: ${folder.name}, 공유상태: ${folder.isShared}")

                homeActivity.moveToJournalTab(
                    folderId = folder.folderId,
                    folderName = folder.name,
                    isSharedMode = folder.isShared // 이 값이 false여야 개인 일지가 뜹니다.
                )
            }
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
        this.items.clear()
        this.items.addAll(newItems)
        notifyDataSetChanged()
    }
}