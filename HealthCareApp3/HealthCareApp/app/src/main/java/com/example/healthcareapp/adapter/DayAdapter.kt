package com.example.healthcareapp.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.DayItem

/**
 * 주간 캘린더의 개별 날짜(요일, 숫자, 이모티콘)를 관리하는 어댑터
 */
class DayAdapter(
    private var items: List<DayItem>,
    private val onDayClick: (DayItem) -> Unit
) : RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    // 현재 선택된 날짜의 인덱스를 저장 (초기 선택 상태 반영)
    private var selectedPosition = items.indexOfFirst { it.isSelected }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutDay: View = view.findViewById(R.id.layout_day_parent) // 날짜 전체 영역
        val tvDayOfWeek: TextView = view.findViewById(R.id.tv_day_of_week) // 요일 (월, 화...)
        val tvDayNumber: TextView = view.findViewById(R.id.tv_day_number) // 날짜 숫자 (10, 11...)
        val ivEmoji: ImageView = view.findViewById(R.id.iv_emoji)        // 운동 완료 이모티콘
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calenar_day, parent, false)

        // ⭐ [핵심 로직] 리사이클러뷰 너비를 7로 나누어 요일 칸을 균등하게 배분
        parent.post {
            val layoutParams = view.layoutParams
            if (parent.width > 0) {
                layoutParams.width = parent.width / 7
                view.layoutParams = layoutParams
            }
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val isCurrentSelected = (position == selectedPosition)

        // 1. [데이터 바인딩] 요일과 날짜 텍스트 세팅
        holder.tvDayOfWeek.text = item.dayOfWeek
        holder.tvDayNumber.text = item.dayNumber

        // 2. [선택 UI 처리] 배경 및 텍스트 스타일/색상 변경
        holder.layoutDay.isSelected = isCurrentSelected

        if (isCurrentSelected) {
            // 선택된 경우: 볼드체 적용 및 진한 색상
            holder.tvDayNumber.setTypeface(null, Typeface.BOLD)
            holder.tvDayOfWeek.setTextColor(Color.parseColor("#94A3B8")) // 요일은 연하게
            holder.tvDayNumber.setTextColor(Color.parseColor("#1E293B")) // 숫자는 진하게
        } else {
            // 선택되지 않은 경우: 일반 서체 및 연한 색상
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL)
            holder.tvDayOfWeek.setTextColor(Color.parseColor("#CBD5E1"))
            holder.tvDayNumber.setTextColor(Color.parseColor("#94A3B8"))
        }

        // 3. [이모티콘 표시] 해당 날짜에 운동 기록이 있는 경우에만 이모티콘 노출
        if (item.hasExercise && item.emojiResId != 0) {
            holder.ivEmoji.setImageResource(item.emojiResId)
            holder.ivEmoji.visibility = View.VISIBLE
            holder.ivEmoji.clearColorFilter() // 이미지 원본 색상 유지
        } else {
            holder.ivEmoji.visibility = View.GONE
        }

        // 4. [클릭 이벤트] 날짜 클릭 시 선택 상태 변경 및 콜백 호출
        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION && selectedPosition != currentPos) {
                val previousPosition = selectedPosition
                selectedPosition = currentPos

                // 전체 리스트의 isSelected 상태 업데이트
                items.forEachIndexed { index, day ->
                    day.isSelected = (index == selectedPosition)
                }

                // 전체 갱신 대신 바뀐 두 항목만 갱신하여 성능 최적화 및 깜빡임 방지
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // 외부(Activity/Fragment)로 클릭된 아이템 정보 전달
                onDayClick(items[currentPos])
            }
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * 주간 이동 등으로 데이터가 완전히 바뀔 때 호출하는 함수
     */
    fun updateData(newList: List<DayItem>) {
        this.items = newList
        notifyDataSetChanged() // ⭐ 이게 있어야 화면이 다시 그려집니다!
    }
}