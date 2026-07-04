package com.example.network

import com.example.data.QuestPack
import retrofit2.http.GET
import retrofit2.http.Url

data class PacksResponse(val packs: List<String>)

interface MomentsApi {
    @GET
    suspend fun getQuestPack(@Url url: String): QuestPack
    
    @GET
    suspend fun getAvailablePacks(@Url url: String): PacksResponse
}
