package com.pads.taskmaster.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pads.taskmaster.R
import com.pads.taskmaster.model.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit,
    private val onTaskMarkDone: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val onTaskPin: (Task) -> Unit,
    private val showCategoryLabel: Boolean = true // Used to hide category in category-specific views
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        private val taskCategory: TextView = itemView.findViewById(R.id.taskCategory)
        private val taskStatus: TextView = itemView.findViewById(R.id.taskStatus)
        private val taskDate: TextView? = itemView.findViewById(R.id.taskDate)
        private val pinButton: ImageButton = itemView.findViewById(R.id.pinButton)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val doneButton: ImageButton = itemView.findViewById(R.id.doneButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(task: Task) {
            taskTitle.text = task.title
            taskDescription.text = task.description ?: "No description"
            
            // Only show category label if showCategoryLabel is true
            if (showCategoryLabel) {
                taskCategory.visibility = View.VISIBLE
                taskCategory.text = task.category
                
                // Set background color based on category
                val categoryBackground = when (task.category) {
                    "Important" -> android.R.color.holo_red_light
                    "Urgent" -> android.R.color.holo_orange_light
                    else -> android.R.color.darker_gray
                }
                taskCategory.setBackgroundResource(categoryBackground)
            } else {
                taskCategory.visibility = View.GONE
            }
            
            taskStatus.text = task.status
            
            // Format and set date if the TextView exists
            taskDate?.let {
                task.lastUpdated?.let { date ->
                    it.text = dateFormat.format(date)
                } ?: run {
                    it.text = "No date"
                }
            }

            // Set background color based on status
            val statusBackground = when (task.status) {
                "Done" -> android.R.color.holo_green_dark
                "Deleted" -> android.R.color.holo_red_dark
                else -> android.R.color.holo_blue_light
            }
            taskStatus.setBackgroundResource(statusBackground)
            
            // Set pin button icon based on pinned status
            if (task.isPinned) {
                pinButton.setImageResource(R.drawable.ic_bookmark_fill)
            } else {
                pinButton.setImageResource(R.drawable.ic_bookmark_nofill)
            }
            
            // Hide done button for tasks already marked as done
            if (task.status == "Done") {
                doneButton.visibility = View.GONE
            } else {
                doneButton.visibility = View.VISIBLE
            }
            
            // Hide all action buttons for deleted tasks
            if (task.status == "Deleted") {
                pinButton.visibility = View.GONE
                editButton.visibility = View.GONE
                doneButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
            } else {
                pinButton.visibility = View.VISIBLE
                editButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
            }
            
            // Set click listeners
            itemView.setOnClickListener { onTaskClick(task) }
            pinButton.setOnClickListener { onTaskPin(task) }
            editButton.setOnClickListener { onTaskEdit(task) }
            doneButton.setOnClickListener { onTaskMarkDone(task) }
            deleteButton.setOnClickListener { onTaskDelete(task) }
        }
    }
} 