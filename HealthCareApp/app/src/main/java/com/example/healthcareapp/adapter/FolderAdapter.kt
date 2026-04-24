package com.example.healthcareapp.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.FolderItem
import com.example.healthcareapp.R
import java.util.Random


class FolderAdapter(
    val items: MutableList<FolderItem>,
    val onMoreClick: (FolderItem) -> Unit
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {
    private val stripeColors = intArrayOf(
        Color.parseColor("#FF6969"),
        Color.parseColor("#FF9245"),
        Color.parseColor("#FFD153"),
        Color.parseColor("#94A769"),
        Color.parseColor("#5DCE46"),
        Color.parseColor("#83F2FA"),
        Color.parseColor("#53A1FF"),
        Color.parseColor("#8A38F5"),
        Color.parseColor("#FF5DEF"),
        Color.parseColor("#A39288")



    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_folder_title)
        val btnMore: View = view.findViewById(R.id.btn_more)
        val lastmodified: TextView = view.findViewById(R.id.tv_last_modified)
        val leftStripe: View = view.findViewById(R.id.view_left_stripe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_item3, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folderData = items[position] // 현재 줄의 데이터
        holder.tvTitle.text = folderData.name
        holder.lastmodified.text = "최종 수정일: ${folderData.lastmodified}"

        // 🌟 여기에 색상 적용 로직을 직접 넣으세요!
        val leftStripe: View = holder.itemView.findViewById(R.id.view_left_stripe)
        val randomColor = stripeColors[Random().nextInt(stripeColors.size)]

        // 순서대로 예쁘게 나오게 하려면 position을 활용하세요 (추천)
//        val color = stripeColors[position % stripeColors.size]
        leftStripe.setBackgroundColor(randomColor)

        holder.btnMore.setOnClickListener {
            onMoreClick(folderData)
        }
    }

    override fun getItemCount() = items.size

}
