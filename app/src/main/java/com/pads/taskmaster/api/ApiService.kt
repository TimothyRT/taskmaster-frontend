package com.pads.taskmaster.api

import com.pads.taskmaster.model.Task
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit interface for API endpoints
 */
interface ApiService {
    // Get all tasks
    @GET("tasks")
    fun getTasks(): Call<List<Task>>
    
    // Get tasks by category
    @GET("tasks/category/{category}")
    fun getTasksByCategory(@Path("category") category: String): Call<List<Task>>
    
    // Get a single task by id
    @GET("tasks/id/{id}")
    fun getTaskById(@Path("id") id: String): Call<Task>
    
    // Create a new task
    @POST("tasks")
    fun createTask(@Body task: Task): Call<Task>
    
    // Update a task
    @PUT("tasks/{taskId}")
    fun updateTask(@Path("taskId") taskId: String, @Body task: Task): Call<Task>
    
    // Delete a task
    @DELETE("tasks/{taskId}")
    fun deleteTask(@Path("taskId") taskId: String): Call<Task>
    
    // Mark task as done
    @PUT("tasks/{taskId}/done")
    fun markTaskAsDone(@Path("taskId") taskId: String): Call<Task>
    
    // Toggle task pin status
    @PUT("tasks/pin/{taskId}")
    fun toggleTaskPin(@Path("taskId") taskId: String): Call<Task>
} 