package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MomentsDao {
    @Query("SELECT * FROM moments ORDER BY timestamp DESC")
    fun getAllMoments(): Flow<List<Moment>>

    @Query("SELECT * FROM moments WHERE id = :id LIMIT 1")
    fun getMomentById(id: Int): Flow<Moment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: Moment)
    
    @Query("SELECT COUNT(*) FROM moments WHERE questId = :questId")
    suspend fun countMomentsForQuest(questId: String): Int

    @Query("DELETE FROM moments")
    suspend fun deleteAllMoments()
}
