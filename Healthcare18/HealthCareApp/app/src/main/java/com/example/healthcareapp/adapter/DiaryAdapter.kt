package com.example.healthcareapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.DiaryItem
import com.example.healthcareapp.utils.ColorUtils

class DiaryAdapter(
    private val items: MutableList<DiaryItem>,
    private val folderId: Long,
    private val onItemClick: (DiaryItem) -> Unit,
    private val onDotClick: (Int) -> Unit,
    private val onPhotoClick: (DiaryItem) -> Unit
) : RecyclerView.Adapter<DiaryAdapter.ViewHolder>() {

    private val stripeColors = intArrayOf(
        Color.parseColor("#FF6969"), Color.parseColor("#FF9245"),
        Color.parseColor("#FFD153"), Color.parseColor("#94A769"),
        Color.parseColor("#5DCE46"), Color.parseColor("#83F2FA"),
        Color.parseColor("#53A1FF"), Color.parseColor("#8A38F5"),
        Color.parseColor("#FF5DEF"), Color.parseColor("#A39288")
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMainText: TextView = view.findViewById(R.id.tv_date)
        val tvSubText: TextView = view.findViewById(R.id.tv_sub)
        val viewStripe: View = view.findViewById(R.id.view_stripe)
        val diaryemoticon: ImageView = view.findViewById(R.id.diaryemoticon)
        val btndot: ImageView = view.findViewById(R.id.exercise_dot)
        val ivDiaryPhotoIcon: ImageView = view.findViewById(R.id.iv_diary_photo_icon)
        val viewDashedLine: View = view.findViewById(R.id.view_dashed_line)
        val viewDashedLine2 : View = view.findViewById(R.id.view_dashed_line2)
        fun bind(item: DiaryItem, position: Int) {
            tvMainText.text = item.date
            tvSubText.text = item.title

            // 🎯 [핵심] 배경색 로직 제거 후 아이콘만 설정
            val finalEmojiResId = if (item.emojiResId > 0) {
                item.emojiResId
            } else {
                // 만약 없다면 id를 넣어 계산
                ColorUtils.getStableEmojiResId(item.date, item.id, folderId)
            }
            diaryemoticon.setImageResource(finalEmojiResId)

            diaryemoticon.setImageResource(finalEmojiResId)
            diaryemoticon.background = null // 배경 제거 (투명 처리)
            diaryemoticon.clearColorFilter() // 필터 제거

            // 사진 아이콘 처리
            if (!item.imageString.isNullOrEmpty()) {
                ivDiaryPhotoIcon.visibility = View.VISIBLE
                ivDiaryPhotoIcon.setOnClickListener { onPhotoClick(item) }
            } else {
                ivDiaryPhotoIcon.visibility = View.GONE
            }

            // 스트라이프 색상
            viewStripe.setBackgroundColor(stripeColors[position % stripeColors.size])

            itemView.setOnClickListener { onItemClick(item) }
            btndot.setOnClickListener {
                val currentPos = bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) onDotClick(currentPos)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
        // 만약 view_dashed_line(아래쪽 점선)을 마지막에만 숨기고 싶다면:
        if (position == items.size - 1) {
            holder.viewDashedLine.visibility = View.GONE
        } else {
            holder.viewDashedLine.visibility = View.VISIBLE
        }

        // 만약 위쪽 점선(view_dashed_line2)도 있다면 이것도 함께 제어하세요:
        val viewDashedLine2 = holder.itemView.findViewById<View>(R.id.view_dashed_line2)
        // 예: 첫 번째 아이템 위에만 점선을 표시하고 싶다면:
        viewDashedLine2.visibility = if (position == 0) View.VISIBLE else View.GONE

    }

    override fun getItemCount() = items.size
}