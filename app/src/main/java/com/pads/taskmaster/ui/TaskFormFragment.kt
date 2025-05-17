package com.pads.taskmaster.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.pads.taskmaster.R
import com.pads.taskmaster.api.RetrofitClient
import com.pads.taskmaster.model.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskFormFragment : Fragment() {

    private val TAG = "TaskFormFragment"
    private val args: TaskFormFragmentArgs by navArgs()
    
    private lateinit var formTitle: TextView
    private lateinit var titleInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var categoryRadioGroup: RadioGroup
    private lateinit var saveButton: Button
    
    private var taskId: String? = null // null for new task, non-null for edit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get taskId from arguments
        taskId = args.taskId
        
        // Initialize views
        formTitle = view.findViewById(R.id.formTitle)
        titleInput = view.findViewById(R.id.titleInput)
        descriptionInput = view.findViewById(R.id.descriptionInput)
        categoryRadioGroup = view.findViewById(R.id.categoryRadioGroup)
        saveButton = view.findViewById(R.id.saveButton)
        
        // Enable back button in action bar
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Set up save button
        saveButton.setOnClickListener {
            saveTask()
        }
        
        // If editing task, load task data
        if (taskId != null) {
            formTitle.text = "Edit Task"
            loadTaskData()
        }
    }

    private fun loadTaskData() {
        taskId?.let { id ->
            RetrofitClient.apiService.getTaskById(id).enqueue(object : Callback<Task> {
                override fun onResponse(call: Call<Task>, response: Response<Task>) {
                    if (response.isSuccessful) {
                        val task = response.body()
                        task?.let {
                            titleInput.setText(it.title)
                            descriptionInput.setText(it.description)
                            
                            // Set the correct radio button
                            val radioButtonId = when (it.category) {
                                "Important" -> R.id.importantRadio
                                "Urgent" -> R.id.urgentRadio
                                else -> R.id.regularRadio
                            }
                            categoryRadioGroup.check(radioButtonId)
                        }
                    } else {
                        Log.e(TAG, "Error loading task: ${response.code()}")
                        // Toast.makeText(context, "Error loading task details", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Task>, t: Throwable) {
                    Log.e(TAG, "Failed to load task", t)
                    // Toast.makeText(context, "Failed to load task: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveTask() {
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        
        // Validate input
        if (title.isEmpty()) {
            titleInput.error = "Title is required"
            return
        }
        
        // Get selected category
        val selectedRadioButtonId = categoryRadioGroup.checkedRadioButtonId
        val categoryRadioButton = view?.findViewById<RadioButton>(selectedRadioButtonId)
        val category = categoryRadioButton?.text.toString()
        
        val task = Task(
            title = title,
            description = description,
            category = category
        )
        
        // Create or update task
        if (taskId == null) {
            createTask(task)
        } else {
            task.id = taskId!!
            updateTask(task)
        }
    }

    private fun createTask(task: Task) {
        RetrofitClient.apiService.createTask(task).enqueue(object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                if (response.isSuccessful) {
                    // Toast.makeText(context, "Task created successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Log.e(TAG, "Error creating task: ${response.code()}")
                    // Toast.makeText(context, "Error creating task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Task>, t: Throwable) {
                Log.e(TAG, "Failed to create task", t)
                // Toast.makeText(context, "Failed to create task: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTask(task: Task) {
        taskId?.let { id ->
            RetrofitClient.apiService.updateTask(id, task).enqueue(object : Callback<Task> {
                override fun onResponse(call: Call<Task>, response: Response<Task>) {
                    if (response.isSuccessful) {
                        // Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Log.e(TAG, "Error updating task: ${response.code()}")
                        // Toast.makeText(context, "Error updating task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Task>, t: Throwable) {
                    Log.e(TAG, "Failed to update task", t)
                    // Toast.makeText(context, "Failed to update task: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
} 