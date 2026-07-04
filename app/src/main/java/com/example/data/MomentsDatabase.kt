package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Moment::class], version = 3, exportSchema = false)
abstract class MomentsDatabase : RoomDatabase() {
    abstract fun momentsDao(): MomentsDao

    companion object {
        @Volatile
        private var Instance: MomentsDatabase? = null

        fun getDatabase(context: Context): MomentsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MomentsDatabase::class.java,
                    "moments_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { Instance = it }
            }
        }
    }
}
