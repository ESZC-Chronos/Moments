package com.example.data

import android.content.Context
import com.example.network.NetworkModule
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import java.io.InputStreamReader

class MomentsRepository(private val context: Context, private val momentsDao: MomentsDao) {

    val allMoments: Flow<List<Moment>> = momentsDao.getAllMoments()

    fun getMomentById(id: Int): Flow<Moment?> = momentsDao.getMomentById(id)

    suspend fun insertMoment(moment: Moment) = momentsDao.insertMoment(moment)
    
    suspend fun deleteAllMoments() = momentsDao.deleteAllMoments()
    
    suspend fun isQuestCompleted(questId: String): Boolean {
        return momentsDao.countMomentsForQuest(questId) > 0
    }

    suspend fun getQuestPack(packName: String, baseUrl: String?): QuestPack {
        if (packName.startsWith("custom_")) {
            return getCustomPack(packName) ?: generateFallbackPack(packName)
        }
        if (baseUrl == null) {
            return getQuestPackFromAssets(packName) ?: generateFallbackPack(packName)
        }
        return try {
            val url = if (baseUrl.endsWith("/")) "${baseUrl}${packName}.json" else "${baseUrl}/${packName}.json"
            NetworkModule.api.getQuestPack(url)
        } catch (e: Exception) {
            getQuestPackFromAssets(packName) ?: generateFallbackPack(packName)
        }
    }

    suspend fun getAvailablePacks(baseUrl: String?): List<String> {
        val serverPacks = if (baseUrl == null) {
            getAvailablePacksFromAssets()
        } else {
            try {
                val url = if (baseUrl.endsWith("/")) "${baseUrl}packs.json" else "${baseUrl}/packs.json"
                NetworkModule.api.getAvailablePacks(url).packs
            } catch (e: Exception) {
                getAvailablePacksFromAssets()
            }
        }
        
        val customPacks = context.filesDir.listFiles { file -> 
            file.name.startsWith("custom_") && file.name.endsWith(".json")
        }?.map { it.name.removeSuffix(".json") } ?: emptyList()
        
        return (serverPacks + customPacks).distinct()
    }
    
    fun saveCustomPack(packName: String, pack: QuestPack) {
        try {
            val json = moshi.adapter(QuestPack::class.java).toJson(pack)
            val file = java.io.File(context.filesDir, "${packName}.json")
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun getCustomPack(packName: String): QuestPack? {
        return try {
            val file = java.io.File(context.filesDir, "${packName}.json")
            if (file.exists()) {
                val json = file.readText()
                moshi.adapter(QuestPack::class.java).fromJson(json)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private fun getQuestPackFromAssets(packName: String): QuestPack? {
        return try {
            val inputStream = context.assets.open("packs/${packName}.json")
            val reader = InputStreamReader(inputStream)
            val json = reader.readText()
            reader.close()
            moshi.adapter(QuestPack::class.java).fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    private fun getAvailablePacksFromAssets(): List<String> {
        return try {
            val inputStream = context.assets.open("packs/packs.json")
            val reader = InputStreamReader(inputStream)
            val json = reader.readText()
            reader.close()
            val packsResponse = moshi.adapter(com.example.network.PacksResponse::class.java).fromJson(json)
            packsResponse?.packs ?: listOf("daily", "mindfulness", "walking", "couple", "urban_explore", "mega_pack")
        } catch (e: Exception) {
            listOf("daily", "mindfulness", "walking", "couple", "urban_explore", "mega_pack")
        }
    }
    
    private fun generateFallbackPack(packName: String): QuestPack {
        val quests = when(packName) {
            "daily" -> listOf(
                Quest("d1", "Finde etwas Friedliches", "Suche nach einem ruhigen Ort an deinem hektischen Tag.", "easy")
            )
            else -> listOf(
                Quest("u1", "Unbekanntes Abenteuer", "Fange etwas Unerwartetes ein.", "easy")
            )
        }
        return QuestPack(
            packName = packName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            version = 1,
            quests = quests
        )
    }
}
