package com.pads.taskmaster.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // Enable back button in action bar
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
    }

    private fun setupViews(taskAdapter: TaskAdapter) {
        val title = when (args.categoryType) {
            "important" -> "Important Tasks"
            "urgent" -> "Urgent Tasks"
            "regular" -> "Regular Tasks"
            else -> "All Tasks"
        }
        Log.d("fuck", taskAdapter.itemCount.toString())
        categoryTitle.text = title + " (${taskAdapter.itemCount})"
    }

    private fun setupRecyclerView() {
        // Outside of "All tasks", hide the category label as it's basically redundant
        val showCategoryLabel = args.categoryType == "all"
        
        taskAdapter = TaskAdapter(
            emptyList(),
            onTaskClick = { task ->
                // Show task details
                // Toast.makeText(context, "Task: ${task.title}", Toast.LENGTH_SHORT).show()
            },
            onTaskEdit = { task ->
                // Navigate to edit task form
                val action = TaskListFragmentDirections
                    .actionTaskListFragmentToTaskFormFragment(task.id)
                findNavController().navigate(action)
            },
            onTaskMarkDone = { task ->
                // Show confirmation dialog before marking task as done
                showMarkAsDoneConfirmation(task)
            },
            onTaskDelete = { task ->
                // Delete task
                deleteTask(task)
            },
            onTaskPin = { task ->
                // Toggle pin status
                toggleTaskPin(task)
            },
            showCategoryLabel = showCategoryLabel
        )
        tasksRecyclerView.adapter = taskAdapter

        setupFab()
        fetchTasks()
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
        
        // Handle "all" as a special case since the API has only got category-specific endpoints
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
                            setupViews(taskAdapter)
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
                    setupViews(taskAdapter)
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
        // Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showMarkAsDoneConfirmation(task: Task) {
        // Show confirmation dialog
        val context = context ?: return
        
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Mark Task as Done")
            .setMessage("Are you sure you want to mark this task as done?")
            .setPositiveButton("Mark as Done") { _, _ ->
                performMarkTaskAsDone(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun performMarkTaskAsDone(task: Task) {
        RetrofitClient.apiService.markTaskAsDone(task.id).enqueue(object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                if (response.isSuccessful) {
                    // Toast.makeText(context, "Task marked as done", Toast.LENGTH_SHORT).show()
                    fetchTasks() // Refresh the task list
                } else {
                    Log.e(TAG, "Error marking task as done: ${response.code()}")
                    // Toast.makeText(context, "Error marking task as done", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<Task>, t: Throwable) {
                Log.e(TAG, "Failed to mark task as done", t)
                // Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun markTaskAsDone(task: Task) {
        // This method is now replaced by showMarkAsDoneConfirmation for consistency
        showMarkAsDoneConfirmation(task)
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
                    // Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                    fetchTasks() // Refresh the task list
                } else {
                    Log.e(TAG, "Error deleting task: ${response.code()}")
                    // Toast.makeText(context, "Error deleting task", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<Task>, t: Throwable) {
                Log.e(TAG, "Failed to delete task", t)
                // Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleTaskPin(task: Task) {
        // Log detailed task information before API call
        task.logTaskDetails(TAG)
        Log.d(TAG, "Toggling pin for task: ${task.id}, current isPinned: ${task.isPinned}")
        
        // Set up a flag to track expected state after the toggle
        val expectedPinnedState = !task.isPinned
        
        RetrofitClient.apiService.toggleTaskPin(task.id).enqueue(object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                if (response.isSuccessful) {
                    val updatedTask = response.body()
                    
                    updatedTask?.let {
                        // Log detailed task information after API call
                        it.logTaskDetails(TAG)
                        Log.d(TAG, "Pin toggled successfully. New isPinned: ${it.isPinned}")
                        
                        // Check if the pin state was actually toggled as expected
                        if (it.isPinned == expectedPinnedState) {
                            val message = if (it.isPinned) "Task pinned" else "Task unpinned"
                            // Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        } else {
                            Log.w(TAG, "Server returned unexpected pin state. Expected: $expectedPinnedState, Actual: ${it.isPinned}")
                            // Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Log.e(TAG, "Successful response but body is null")
                    }
                    
                    fetchTasks()  // Refresh the task list
                } else {
                    Log.e(TAG, "Error toggling pin: ${response.code()}")
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error body: $errorBody")
                    // Toast.makeText(context, "Error updating task", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<Task>, t: Throwable) {
                Log.e(TAG, "Failed to toggle pin", t)
                // Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Refresh tasks when returning to this fragment
        fetchTasks()
    }
} 