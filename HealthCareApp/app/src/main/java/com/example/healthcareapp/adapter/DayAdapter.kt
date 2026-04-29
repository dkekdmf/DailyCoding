package com.example.healthcareapp.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.data.DayItem

// 주간 달력의 개별 날짜를 관리
class DayAdapter(
    private var items: List<DayItem>,       // 날짜 데이터 리스트 (요일, 일자 등)
    private val onDayClick: (DayItem) -> Unit // 날짜 클릭 시 실행할 콜백
) : RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    // 현재 선택된 날짜의 위치를 저장함. (초기값: 데이터 중 isSelected가 true인 위치)
    private var selectedPosition = items.indexOfFirst { it.isSelected }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutDay: View = view.findViewById(R.id.layout_day_parent) // 날짜 칸 전체 레이아웃
        val tvDayOfWeek: TextView = view.findViewById(R.id.tv_day_of_week) // 요일 (월, 화...)
        val tvDayNumber: TextView = view.findViewById(R.id.tv_day_number) // 일자 (27, 28...)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calenar_day, parent, false)

        // 화면 너비를 7등분 하여 한 화면에 7일이 다 보이도록 설정
        val layoutParams = view.layoutParams
        layoutParams.width = parent.width / 7
        view.layoutParams = layoutParams

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 데이터 표시
        holder.tvDayOfWeek.text = item.dayOfWeek
        holder.tvDayNumber.text = item.dayNumber

        // 현재 아이템이 선택된 상태인지 확인
        val isCurrentSelected = (position == selectedPosition)

        // 선택 여부에 따른 UI 디자인 변경 (날짜 숫자 부분)
        holder.tvDayNumber.apply {
            isSelected = isCurrentSelected // XML의 selector 연동용
            if (isCurrentSelected) {
                // 선택되었을 때: 검정색 + 굵게
                setTextColor(Color.BLACK)
                setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            } else {
                // 선택되지 않았을 때: 회색 + 일반
                setTextColor(Color.parseColor("#888888"))
                setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
            }
        }

        // 날짜 클릭 이벤트 처리
        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition // 현재 클릭한 위치 확인

            // 유효한 위치이고, 이미 선택된 날짜가 아닐 때만 실행
            if (currentPos != RecyclerView.NO_POSITION && selectedPosition != currentPos) {
                val previousPosition = selectedPosition // 이전 선택 위치 보관
                selectedPosition = currentPos           // 새 선택 위치 저장

                // 선택된 위치가 바뀌었으므로 이전 항목과 새 항목만 다시 그림
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // 클릭 콜백 호출 Activity에 날짜가 바뀌었음을 알림
                onDayClick(items[currentPos])
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("CALENDAR_CHECK", "아이템 개수: ${items.size}")
        return items.size
    }

    // 새로운 주차 데이터로 갱신할 때 사용하는 함수
    fun updateData(newItems: List<DayItem>) {
        this.items = newItems
        // 데이터가 바뀌면 선택된 위치도 새로 계산
        this.selectedPosition = items.indexOfFirst { it.isSelected }
        notifyDataSetChanged() // 전체 리스트 다시 그리기
    }
}