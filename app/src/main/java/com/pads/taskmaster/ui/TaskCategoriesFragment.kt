package com.pads.taskmaster.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pads.taskmaster.R
import com.pads.taskmaster.model.TaskCategory
import com.pads.taskmaster.ui.adapters.CategoryAdapter

class TaskCategoriesFragment : Fragment() {

    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var addTaskFab: FloatingActionButton

    private val categories = listOf(
        TaskCategory("all", "All Tasks", android.R.drawable.ic_menu_more),
        TaskCategory("important", "Important", android.R.drawable.ic_dialog_alert),
        TaskCategory("urgent", "Urgent", android.R.drawable.ic_menu_recent_history),
        TaskCategory("regular", "Regular", android.R.drawable.ic_menu_agenda)
    )

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
        
        setupRecyclerView()
        setupFab()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(categories) { category ->
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
} 