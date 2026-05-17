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
    // 🎯 [수정 포인트 3 관련] 좀 더 유연한 클릭 처리를 위해 람다 인자에 Item 객체도 함께 전달할 수 있도록 유지하거나 보정합니다.
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

        // 🎯 [수정 포인트 1] XML에서 수정한 id인 iv_diary_emoji로 정확하게 매칭 (NullPointerException 방지)
        val diaryemoticon: ImageView = view.findViewById(R.id.diaryemoticon)
        val btndot: ImageView = view.findViewById(R.id.exercise_dot)

        fun bind(item: DiaryItem, position: Int) {
            tvMainText.text = item.date
            tvSubText.text = item.title

            // 🎯 [수정 포인트 2] 중복 마인딩 제거 및 전달받은 정식 이모티콘 리소스 ID 바인딩 고정
            diaryemoticon.setImageResource(item.emojiResId)

            // 왼쪽 띠 색상은 순차적으로 적용
            val targetColor = stripeColors[position % stripeColors.size]
            viewStripe.setBackgroundColor(targetColor)

            // 전체 카드 아이템 클릭 시 상세화면 이동 리스너
            itemView.setOnClickListener { onItemClick(item) }

            // 🎯 [수정 포인트 3] 이모티콘이 아닌 우측 더보기 점 버튼(btndot)을 눌렀을 때 팝업/바텀시트 액션이 트리거되도록 수정!!
            btndot.setOnClickListener {
                val currentPos = adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onDotClick(currentPos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 🎯 [수정 포인트 4] 실제 파일명이 item_diary 라면 R.layout.item_diary 로 명칭 크로스 체크 필요
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}