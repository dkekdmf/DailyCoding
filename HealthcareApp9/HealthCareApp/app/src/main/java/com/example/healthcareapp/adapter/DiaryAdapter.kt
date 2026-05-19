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
        val diaryemoticon: ImageView = view.findViewById(R.id.diaryemoticon)
        val btndot: ImageView = view.findViewById(R.id.exercise_dot)

        // 🎯 [신규 추가] XML에 새로 추가한 사진 벡터 아이콘 뷰홀더 등록!
        val ivDiaryPhotoIcon: ImageView = view.findViewById(R.id.iv_diary_photo_icon)

        fun bind(item: DiaryItem, position: Int) {
            tvMainText.text = item.date
            tvSubText.text = item.title

            // 전달받은 정식 이모티콘 리소스 ID 바인딩 고정
            diaryemoticon.setImageResource(item.emojiResId)

            // 🎯 [신규 추가] 실시간 사진 벡터 아이콘 가시성 제어 파이프라인
            // 일기 아이템에 이미지 데이터(Base64)가 실존한다면 사진 마크를 보여주고, 없으면 숨깁니다.
            if (!item.imageString.isNullOrEmpty()) {
                ivDiaryPhotoIcon.visibility = View.VISIBLE
            } else {
                ivDiaryPhotoIcon.visibility = View.GONE
            }

            // 왼쪽 띠 색상은 순차적으로 적용
            val targetColor = stripeColors[position % stripeColors.size]
            viewStripe.setBackgroundColor(targetColor)

            // 전체 카드 아이템 클릭 시 상세화면 이동 리스너
            itemView.setOnClickListener { onItemClick(item) }

            // 우측 더보기 점 버튼(btndot)을 눌렀을 때 팝업/바텀시트 액션 트리거
            btndot.setOnClickListener {
                val currentPos = bindingAdapterPosition // 안정적인 최신 순정 API 권장 교정
                if (currentPos != RecyclerView.NO_POSITION) {
                    onDotClick(currentPos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 기존 재훈님의 레이아웃 파일명 규칙(diary_item)을 그대로 완벽하게 보존합니다.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}