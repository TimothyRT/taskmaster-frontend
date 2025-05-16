package com.pads.taskmaster

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pads.taskmaster.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Example API call using Retrofit
        fetchTasksFromServer()
    }
    
    private fun fetchTasksFromServer() {
        RetrofitClient.apiService.getTasks().enqueue(object : Callback<List<Any>> {
            override fun onResponse(call: Call<List<Any>>, response: Response<List<Any>>) {
                if (response.isSuccessful) {
                    val tasks = response.body()
                    Log.d(TAG, "Tasks fetched successfully: $tasks")
                    Toast.makeText(this@MainActivity, "Tasks fetched successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Error fetching tasks: ${response.code()}")
                    Toast.makeText(this@MainActivity, "Error fetching tasks: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<List<Any>>, t: Throwable) {
                Log.e(TAG, "API call failed", t)
                Toast.makeText(this@MainActivity, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}