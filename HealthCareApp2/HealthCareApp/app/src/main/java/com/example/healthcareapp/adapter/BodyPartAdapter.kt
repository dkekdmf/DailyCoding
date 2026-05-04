package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.databinding.ItemBodyPartSelectionBinding

// BodyPart.kt (데이터 클래스)
data class BodyPart(val name: String)

// BodyPartAdapter.kt
class BodyPartAdapter(private var parts: List<BodyPart>, private val onItemClick: (BodyPart) -> Unit) :
    RecyclerView.Adapter<BodyPartAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBodyPartSelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBodyPartSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val part = parts[position]

        // 1. binding을 통해 tvPartName에 접근 (언더바가 사라짐에 주의!)
        holder.binding.tvPartName.text = part.name

        // 2. 만약 화살표 아이콘을 코드로 제어하고 싶다면
        holder.binding.ivArrow.setImageResource(R.drawable.arrow1_calendar)
    }
    fun updateItems(newItems: List<BodyPart>) {
        this.parts = newItems
        notifyDataSetChanged() // 리스트 전체 갱신
    }

    override fun getItemCount(): Int = parts.size

}