package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R

// 데이터 클래스 정의 (혹시 기존 파일과 분리되어 있다면 이 구역은 빼셔도 됩니다)
data class BodyPart(val name: String)

/**
 * 신체 세부 부위 리스트 어댑터 (바텀시트 미오픈 버그 완전 치료본)
 */
class BodyPartAdapter(
    private val items: MutableList<BodyPart>,
    private val onItemClick: (BodyPart) -> Unit // 🎯 액티비티에서 넘어오는 클릭 콜백 람다
) : RecyclerView.Adapter<BodyPartAdapter.BodyPartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BodyPartViewHolder {
        // 기존 재훈님의 순정 item_body_part_selection 레이아웃을 그대로 인플레이트합니다.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_body_part_selection, parent, false)
        return BodyPartViewHolder(view)
    }

    override fun onBindViewHolder(holder: BodyPartViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    // 데이터 갱신용 순정 헬퍼 함수
    fun updateItems(newItems: List<BodyPart>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class BodyPartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // item_body_part_selection.xml 내부에 배치된 부위 텍스트뷰 주소 매싱
        // (혹시 ID가 다르면 재훈님의 ID명으로 바꿔주세요. 보통 tv_body_part_name 등을 씁니다)
        private val tvPartName = itemView.findViewById<TextView>(R.id.tv_part_name)

        fun bind(item: BodyPart) {
            tvPartName?.text = item.name

            // 🎯 [핵심 패치 구역] 아이템 루트 뷰 전체에 클릭 리스너를 달아
            // 어떤 부위를 누르든 액티비티의 바텀시트 오픈 람다로 다이렉트 패스시킵니다!
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}