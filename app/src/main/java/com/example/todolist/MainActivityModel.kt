package com.example.todolist

import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivityModel(var appDatabase: AppDatabase) { // ROOM 사용
    suspend fun selectSchedule(date : String) : List<ScheduleEntity> {
        val scheduleList = withContext(Dispatchers.IO) {
            appDatabase.getScheduleDAO().getSchedule(date)
        }
        return scheduleList
    }
    suspend fun insertSchedule(scheduleEntity: ScheduleEntity){
        withContext(Dispatchers.IO){
            appDatabase.getScheduleDAO().insertSchedule(scheduleEntity)
        }
    }
    suspend fun deleteSchedule(identifier : Long){
        appDatabase.getScheduleDAO().deleteSchedule(identifier)
    }
    suspend fun updateSchedule(identifier: Long,work:String){
        withContext(Dispatchers.IO){
            appDatabase.getScheduleDAO().updateSchedule(identifier,work)
        }
    }

    suspend fun updateCheckState(identifier : Long,state:Boolean){
        withContext(Dispatchers.IO){
            appDatabase.getScheduleDAO().updateCheckState(identifier = identifier,state)
        }
    }
}