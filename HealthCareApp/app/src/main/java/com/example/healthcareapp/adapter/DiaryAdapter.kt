package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.DiaryItem


class DiaryAdapter(
    private val items: List<DiaryItem>,           // 표시할 일지 데이터 리스트
    private val onItemClick: (DiaryItem) -> Unit  //콜백 함수
) : RecyclerView.Adapter<DiaryAdapter.ViewHolder>() {


    private val stripeColors = intArrayOf(
        android.graphics.Color.parseColor("#FF6969"), android.graphics.Color.parseColor("#FF9245"),
        android.graphics.Color.parseColor("#FFD153"), android.graphics.Color.parseColor("#94A769"),
        android.graphics.Color.parseColor("#5DCE46"), android.graphics.Color.parseColor("#83F2FA"),
        android.graphics.Color.parseColor("#53A1FF"), android.graphics.Color.parseColor("#8A38F5"),
        android.graphics.Color.parseColor("#FF5DEF"), android.graphics.Color.parseColor("#A39288")
    )

    // 홀더 클래스(각 아이템을 보관)
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        val tvMainText: TextView = view.findViewById(R.id.tv_date)  // 상단 날짜 텍스트
        val tvSubText: TextView = view.findViewById(R.id.tv_sub)    // 하단 제목 텍스트
        val viewStripe: View = view.findViewById(R.id.view_stripe)  // 왼쪽 컬러 띠 뷰

        //실제 데이터를 뷰에 입히는 바인딩 함수
        fun bind(item: DiaryItem, position: Int) {
            // 데이터 설정
            tvMainText.text = item.date   // 날짜 표시
            tvSubText.text = item.title   // 운동 제목 표시

            //  색상 띠 설정: 리스트 순서에 맞게 무지개 색상을 순환하며 적용
            val color = stripeColors[position % stripeColors.size]
            viewStripe.setBackgroundColor(color)

            //  아이템 전체 클릭 리스너 설정
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // 아이템 레이아웃을 생성하여 뷰홀더에 담아 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // diary_item.xml 파일을 인플레이트
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diary_item, parent, false)
        return ViewHolder(view)
    }

    // 생성된 뷰홀더에 특정 위치(position)의 데이터를 연결
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    // 전체 아이템 개수 반환
    override fun getItemCount() = items.size
}