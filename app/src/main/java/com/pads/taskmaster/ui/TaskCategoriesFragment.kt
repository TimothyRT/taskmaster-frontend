package com.pads.taskmaster.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pads.taskmaster.R
import com.pads.taskmaster.api.RetrofitClient
import com.pads.taskmaster.model.Task
import com.pads.taskmaster.model.TaskCategory
import com.pads.taskmaster.ui.adapters.CategoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskCategoriesFragment : Fragment() {

    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var addTaskFab: FloatingActionButton
    private val TAG = "TaskCategoriesFragment"

    private val categories = listOf(
        TaskCategory("all", "All Tasks", R.drawable.ic_cat_all),
        TaskCategory("important", "Important", R.drawable.ic_cat_important),
        TaskCategory("urgent", "Urgent", R.drawable.ic_cat_urgent),
        TaskCategory("regular", "Regular", R.drawable.ic_cat_regular)
    )

    private val taskCounts = mutableMapOf<String, Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView)
        addTaskFab = view.findViewById(R.id.addTaskFab)
        
        // Hide back button for home screen
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        
        fetchTaskCounts()
        setupFab()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(categories, taskCounts) { category ->
            navigateToTaskList(category)
        }
        categoriesRecyclerView.adapter = categoryAdapter
    }
    
    private fun setupFab() {
        addTaskFab.setOnClickListener {
            navigateToTaskForm()
        }
    }

    private fun navigateToTaskList(category: TaskCategory) {
        val action = TaskCategoriesFragmentDirections
            .actionTaskCategoriesFragmentToTaskListFragment(category.id)
        findNavController().navigate(action)
    }
    
    private fun navigateToTaskForm() {
        val action = TaskCategoriesFragmentDirections
            .actionTaskCategoriesFragmentToTaskFormFragment(null)
        findNavController().navigate(action)
    }

    private fun fetchTaskCounts() {
        // First, fetch all tasks to get the total count
        RetrofitClient.apiService.getTasks().enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    response.body()?.let { allTasks ->
                        taskCounts["all"] = allTasks.count { it.status != "Deleted" }
                        
                        // Count tasks by category
                        taskCounts["important"] = allTasks.count { it.category == "Important" && it.status != "Deleted" }
                        taskCounts["urgent"] = allTasks.count { it.category == "Urgent" && it.status != "Deleted" }
                        taskCounts["regular"] = allTasks.count { it.category == "Regular" && it.status != "Deleted" }
                        
                        // Now that we have the counts, set up the RecyclerView
                        setupRecyclerView()
                    }
                } else {
                    Log.e(TAG, "Error fetching tasks: ${response.code()}")
                    setupRecyclerView() // Set up with empty counts
                }
            }
                
            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Log.e(TAG, "Failed to fetch tasks", t)
                // Toast.makeText(context, "Error fetching task counts: ${t.message}", Toast.LENGTH_SHORT).show()
                setupRecyclerView() // Set up with empty counts
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Refresh task counts when returning to this fragment
        fetchTaskCounts()
    }
} 