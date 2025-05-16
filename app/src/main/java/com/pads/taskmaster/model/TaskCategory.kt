package com.pads.taskmaster.model

import androidx.annotation.DrawableRes

data class TaskCategory(
    val id: String,
    val displayName: String,
    @DrawableRes val iconResId: Int
) 