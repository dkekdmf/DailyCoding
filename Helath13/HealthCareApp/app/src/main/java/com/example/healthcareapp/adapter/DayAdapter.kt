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

class DayAdapter(
    private var items: List<DayItem>,
    private val onDayClick: (DayItem) -> Unit
) : RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    private var selectedPosition = items.indexOfFirst { it.isSelected }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutDay: View = view.findViewById(R.id.layout_day_parent)
        val tvDayOfWeek: TextView = view.findViewById(R.id.tv_day_of_week)
        val tvDayNumber: TextView = view.findViewById(R.id.tv_day_number)
        val ivEmoji: ImageView = view.findViewById(R.id.iv_emoji)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calenar_day, parent, false)
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

        holder.tvDayOfWeek.text = item.dayOfWeek
        holder.tvDayNumber.text = item.dayNumber
        holder.layoutDay.isSelected = isCurrentSelected

        if (isCurrentSelected) {
            holder.tvDayNumber.setTypeface(null, Typeface.BOLD)
            holder.tvDayOfWeek.setTextColor(Color.parseColor("#94A3B8"))
            holder.tvDayNumber.setTextColor(Color.parseColor("#1E293B"))
        } else {
            holder.tvDayNumber.setTypeface(null, Typeface.NORMAL)
            holder.tvDayOfWeek.setTextColor(Color.parseColor("#CBD5E1"))
            holder.tvDayNumber.setTextColor(Color.parseColor("#94A3B8"))
        }

        holder.ivEmoji.background = null
        holder.ivEmoji.clearColorFilter()

        val myEmoticons = intArrayOf(
            R.drawable.emoticon1,
            R.drawable.emoticon2,
            R.drawable.emoticon3,
            R.drawable.emoticon4,
            R.drawable.emoticon5
        )

        if (item.hasExercise) {
            val rawId = item.emojiResId
            if (rawId in myEmoticons) {
                holder.ivEmoji.setImageResource(rawId)
                holder.ivEmoji.visibility = View.VISIBLE
            } else if (rawId in 0..4) {
                holder.ivEmoji.setImageResource(myEmoticons[rawId])
                holder.ivEmoji.visibility = View.VISIBLE
            } else {
                val stableIndex = kotlin.math.abs(item.fullDate.hashCode()) % 5
                holder.ivEmoji.setImageResource(myEmoticons[stableIndex])
                holder.ivEmoji.visibility = View.VISIBLE
            }
        } else {
            holder.ivEmoji.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION && selectedPosition != currentPos) {
                val previousPosition = selectedPosition
                selectedPosition = currentPos

                items.forEachIndexed { index, day ->
                    day.isSelected = (index == selectedPosition)
                }

                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onDayClick(items[currentPos])
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newList: List<DayItem>) {
        this.items = newList
        notifyDataSetChanged()
    }
}