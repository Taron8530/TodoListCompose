package com.example.todolist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [ScheduleEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getScheduleDAO() : ScheduleDAO
    companion object{
        @Volatile
        private var INSTANCE : AppDatabase? = null

        private fun buildDatabase(context : Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,"favorite-books").build()
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
    }
}