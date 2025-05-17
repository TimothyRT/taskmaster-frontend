package com.pads.taskmaster.model

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.util.Date
import java.util.UUID

data class Task(
    @SerializedName("id")
    var id: String = UUID.randomUUID().toString(),
    
    @SerializedName("title")
    var title: String,
    
    @SerializedName("description")
    var description: String? = null,
    
    @SerializedName("category")
    var category: String = "Regular", // Important, Urgent, Regular
    
    @SerializedName("last_updated")
    var lastUpdated: Date? = Date(),
    
    @SerializedName("status")
    var status: String = "Active", // Active, Done, Deleted
    
    @SerializedName(value = "is_pinned", alternate = ["isPinned", "pinned"])
    var isPinned: Boolean = false
) {
    fun logTaskDetails(tag: String) {
        Log.d(tag, "Task Details: id=$id, title=$title, category=$category, " +
                "status=$status, isPinned=$isPinned, lastUpdated=$lastUpdated")
    }
} 