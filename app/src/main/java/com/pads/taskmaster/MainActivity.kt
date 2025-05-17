package com.pads.taskmaster

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.pads.taskmaster.api.RetrofitClient
import com.pads.taskmaster.model.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    
    private val TAG = "MainActivity"
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Example API call using Retrofit
        fetchTasksFromServer()
        setupNavigation()
    }
    
    private fun fetchTasksFromServer() {
        RetrofitClient.apiService.getTasks().enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    val tasks = response.body()
                    Log.d(TAG, "Tasks fetched successfully: $tasks")
                    // Toast.makeText(this@MainActivity, "Tasks fetched successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Error fetching tasks: ${response.code()}")
                    // Toast.makeText(this@MainActivity, "Error fetching tasks: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Log.e(TAG, "API call failed", t)
                // Toast.makeText(this@MainActivity, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        setupActionBarWithNavController(navController)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}