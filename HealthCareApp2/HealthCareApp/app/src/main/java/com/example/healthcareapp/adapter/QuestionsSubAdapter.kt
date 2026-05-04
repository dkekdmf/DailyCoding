//package com.example.healthcareapp.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.healthcareapp.data.StatusQuestion
//import com.example.healthcareapp.data.StatusQuestion1
//import com.example.healthcareapp.databinding.ItemConditionQuestionBinding
//
//class QuestionsSubAdapter(private val qList: List<StatusQuestion1>) :
//    RecyclerView.Adapter<QuestionsSubAdapter.QViewHolder>() {
//
//    inner class QViewHolder(val binding: ItemConditionQuestionBinding) : RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QViewHolder {
//        val binding = ItemConditionQuestionBinding.inflate(
//            LayoutInflater.from(parent.context), parent, false
//        )
//        return QViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: QViewHolder, position: Int) {
//        val q = qList[position]
//        holder.binding.apply {
//            tvStepCount.text = q.step
//            tvQuestionTitle.text = q.title
//            tvMinLabel.text = q.minLabel
//            tvMaxLabel.text = q.maxLabel
//
//            slider.value = q.score
//            tvSliderGuide.text = "${q.score.toInt()} - 단계"
//
//            slider.addOnChangeListener { _, value, _ ->
//                q.score = value
//                tvSliderGuide.text = "${value.toInt()} - 단계"
//            }
//        }
//    }
//
//    override fun getItemCount(): Int = qList.size
//}