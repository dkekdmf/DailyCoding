package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material3.Slider
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.data.StatusQuestion
import com.example.healthcareapp.databinding.ItemConditionQuestionBinding
import com.example.healthcareapp.databinding.StatusQuestionBinding
import com.google.android.material.slider.Slider

class StatusQuestionAdapter(private val questions: List<StatusQuestion>) :
    RecyclerView.Adapter<StatusQuestionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemConditionQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConditionQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questions[position]
        holder.binding.apply {
            tvStepCount.text = "${item.id}/5"
            tvQuestionTitle.text = item.title
            tvMinLabel.text = item.minLabel
            tvMaxLabel.text = item.maxLabel

            // 슬라이더 초기 설정
            slider.value = item.score.toFloat()
            tvSliderGuide.text = item.guides[item.score] ?: "${item.score} - 선택됨"

            // 슬라이더 변경 리스너
            slider.addOnChangeListener { _, value, _ ->
                val selectedScore = value.toInt()
                item.score = selectedScore // 데이터 모델 업데이트
                tvSliderGuide.text = item.guides[selectedScore] ?: "$selectedScore - 선택됨"
            }
        }
    }

    override fun getItemCount(): Int = questions.size
}