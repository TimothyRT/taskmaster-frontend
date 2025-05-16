package com.pads.taskmaster.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pads.taskmaster.R
import com.pads.taskmaster.api.RetrofitClient
import com.pads.taskmaster.model.Task
import com.pads.taskmaster.ui.adapters.TaskAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale

class TaskListFragment : Fragment() {

    private val args: TaskListFragmentArgs by navArgs()
    private lateinit var categoryTitle: TextView
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var addTaskFab: FloatingActionButton
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateView: View

    private val TAG = "TaskListFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryTitle = view.findViewById(R.id.categoryTitle)
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView)
        addTaskFab = view.findViewById(R.id.addTaskFab)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        emptyStateView = view.findViewById(R.id.emptyStateView)

        setupViews()
        setupRecyclerView()
        setupFab()
        fetchTasks()
    }

    private fun setupViews() {
        val title = when (args.categoryType) {
            "important" -> "Important Tasks"
            "urgent" -> "Urgent Tasks"
            "regular" -> "Regular Tasks"
            else -> "All Tasks"
        }
        categoryTitle.text = title
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            emptyList(),
            onTaskClick = { task ->
                // Show task details
                Toast.makeText(context, "Task: ${task.title}", Toast.LENGTH_SHORT).show()
            },
            onTaskEdit = { task ->
                // Navigate to edit task form
                val action = TaskListFragmentDirections
                    .actionTaskListFragmentToTaskFormFragment(task.id)
                findNavController().navigate(action)
            },
            onTaskMarkDone = { task ->
                // Mark task as done
                markTaskAsDone(task)
            },
            onTaskDelete = { task ->
                // Delete task
                deleteTask(task)
            }
        )
        tasksRecyclerView.adapter = taskAdapter
    }
    
    private fun setupFab() {
        addTaskFab.setOnClickListener {
            navigateToTaskForm()
        }
    }
    
    private fun navigateToTaskForm() {
        val action = TaskListFragmentDirections
            .actionTaskListFragmentToTaskFormFragment(null)
        findNavController().navigate(action)
    }
    
    // Extension function to replace the deprecated capitalize()
    private fun String.capitalize(): String {
        return if (this.isNotEmpty()) {
            this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1)
        } else {
            this
        }
    }

    private fun fetchTasks() {
        // Show loading state
        showLoading(true)
        
        // Handle "all" as a special case since the server only has category-specific endpoints
        if (args.categoryType == "all") {
            RetrofitClient.apiService.getTasks().enqueue(object : Callback<List<Task>> {
                override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        response.body()?.let { tasks ->
                            if (tasks.isEmpty()) {
                                showEmptyState(true)
                            } else {
                                showEmptyState(false)
                                taskAdapter.updateTasks(tasks)
                            }
                        } ?: showError("No tasks returned")
                    } else {
                        Log.e(TAG, "Error fetching tasks: ${response.code()}")
                        showError("Error fetching tasks: ${response.code()}")
                    }
                }
                
                override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                    showLoading(false)
                    Log.e(TAG, "Failed to fetch tasks", t)
                    showError("Network error: ${t.message}")
                }
            })
        } else {
            // Convert category ID to proper format ("important" -> "Important")
            val categoryValue = args.categoryType.capitalize()
            
            RetrofitClient.apiService.getTasksByCategory(categoryValue).enqueue(object : Callback<List<Task>> {
                override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        response.body()?.let { tasks ->
                            if (tasks.isEmpty()) {
                                showEmptyState(true)
                            } else {
                                showEmptyState(false)
                                taskAdapter.updateTasks(tasks)
                            }
                        } ?: showError("No tasks returned")
                    } else {
                        Log.e(TAG, "Error fetching tasks: ${response.code()}")
                        showError("Error fetching tasks: ${response.code()}")
                    }
                }
                
                override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                    showLoading(false)
                    Log.e(TAG, "Failed to fetch tasks", t)
                    showError("Network error: ${t.message}")
                }
            })
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingIndicator.visibility = View.VISIBLE
            tasksRecyclerView.visibility = View.GONE
            emptyStateView.visibility = View.GONE
        } else {
            loadingIndicator.visibility = View.GONE
            tasksRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyStateView.visibility = View.VISIBLE
            tasksRecyclerView.visibility = View.GONE
        } else {
            emptyStateView.visibility = View.GONE
            tasksRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun markTaskAsDone(task: Task) {
        RetrofitClient.apiService.markTaskAsDone(task.id).enqueue(object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Task marked as done", Toast.LENGTH_SHORT).show()
                    fetchTasks() // Refresh the task list
                } else {
                    Log.e(TAG, "Error marking task as done: ${response.code()}")
                    Toast.makeText(context, "Error marking task as done", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<Task>, t: Throwable) {
                Log.e(TAG, "Failed to mark task as done", t)
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun deleteTask(task: Task) {
        // Show confirmation dialog
        val context = context ?: return
        
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteTask(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performDeleteTask(task: Task) {
        RetrofitClient.apiService.deleteTask(task.id).enqueue(object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                    fetchTasks() // Refresh the task list
                } else {
                    Log.e(TAG, "Error deleting task: ${response.code()}")
                    Toast.makeText(context, "Error deleting task", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<Task>, t: Throwable) {
                Log.e(TAG, "Failed to delete task", t)
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Refresh tasks when returning to this fragment
        fetchTasks()
    }
} 