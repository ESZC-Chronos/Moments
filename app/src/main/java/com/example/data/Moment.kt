package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moments")
data class Moment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questId: String,
    val questTitle: String,
    val questDescription: String,
    val packName: String,
    val photoUri: String,
    val filterName: String = "none",
    val timestamp: Long = System.currentTimeMillis()
)
