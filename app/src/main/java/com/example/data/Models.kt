package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String
)

@JsonClass(generateAdapter = true)
data class QuestPack(
    val packName: String,
    val version: Int,
    val quests: List<Quest>
)
