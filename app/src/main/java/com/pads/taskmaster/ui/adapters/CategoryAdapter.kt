package com.pads.taskmaster.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pads.taskmaster.R
import com.pads.taskmaster.model.TaskCategory

class CategoryAdapter(
    private val categories: List<TaskCategory>,
    private val taskCounts: Map<String, Int>,
    private val onCategoryClick: (TaskCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, taskCounts[category.id] ?: 0)
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount() = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        private val taskCount: TextView = itemView.findViewById(R.id.taskCount)

        fun bind(category: TaskCategory, count: Int) {
            categoryName.text = category.displayName
            categoryIcon.setImageResource(category.iconResId)
            taskCount.text = count.toString()
            taskCount.visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }
} 