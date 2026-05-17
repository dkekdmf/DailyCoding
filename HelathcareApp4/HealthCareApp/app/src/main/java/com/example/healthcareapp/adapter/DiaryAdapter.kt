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

        fun bind(item: DiaryItem, position: Int) {
            tvMainText.text = item.date
            tvSubText.text = item.title

            // 왼쪽 띠 색상은 순차적으로 적용
            val targetColor = stripeColors[position % stripeColors.size]
            viewStripe.setBackgroundColor(targetColor)

            // ⭐ [핵심 수정] 짝수/홀수 랜덤 로직을 지우고, 데이터에 담긴 emojiResId를 직접 사용합니다.
            // 이제 WorkoutFinishActivity에서 결정되어 배달된 그 색상이 그대로 나옵니다.
            btnDot.setImageResource(item.emojiResId)

            itemView.setOnClickListener { onItemClick(item) }
            btnDot.setOnClickListener { onDotClick(adapterPosition) }
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