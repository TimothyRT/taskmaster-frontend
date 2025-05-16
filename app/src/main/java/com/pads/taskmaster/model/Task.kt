package com.pads.taskmaster.model

import java.util.Date
import java.util.UUID

data class Task(
    var id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String? = null,
    var category: String = "Regular", // Important, Urgent, Regular
    var lastUpdated: Date? = Date(),
    var status: String = "Active" // Active, Done, Deleted
) 