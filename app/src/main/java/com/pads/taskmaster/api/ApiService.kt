package com.pads.taskmaster.api

import retrofit2.Call
import retrofit2.http.GET

/**
 * Retrofit interface for API endpoints
 */
interface ApiService {
    // Example endpoint
    @GET("tasks")
    fun getTasks(): Call<List<Any>>
    
    // Add more API endpoints as needed
} 