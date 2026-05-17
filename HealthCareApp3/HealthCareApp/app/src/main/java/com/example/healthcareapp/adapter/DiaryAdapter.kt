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

class DiaryAdapter(
    private val items: MutableList<DiaryItem>,
    private val onItemClick: (DiaryItem) -> Unit,
    private val onDotClick: (Int) -> Unit
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
        val btnDot: ImageView = view.findViewById(R.id.exercise_dot)

        // 🚨 [새로 추가] XML에 새로 추가한 이모티콘 뷰를 연결합니다.
        val ivEmoji: ImageView = view.findViewById(R.id.iv_emoji)

        fun bind(item: DiaryItem, position: Int) {
            tvMainText.text = item.date
            tvSubText.text = item.title

            // 왼쪽 띠 색상은 순차적으로 적용
            val targetColor = stripeColors[position % stripeColors.size]
            viewStripe.setBackgroundColor(targetColor)

            // 🚨 [수정 및 분리]
            // 1. 상단의 원래 점(dot)은 원래 디자인인 기본 점 이미지로 채워줍니다.
            btnDot.setImageResource(R.drawable.dot)

            // 2. 점 밑에 새로 만든 이모티콘 뷰에다가 서버에서 넘어온 컨디션 이모티콘 이미지를 박아줍니다!
            ivEmoji.setImageResource(item.emojiResId)

            itemView.setOnClickListener { onItemClick(item) }

            // 혹시 점이나 이모티콘을 눌렀을 때의 이벤트가 필요하다면 활용
            btnDot.setOnClickListener { onDotClick(adapterPosition) }
            ivEmoji.setOnClickListener { onDotClick(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}