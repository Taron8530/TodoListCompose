package com.example.todolist

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface ScheduleDAO {
    @Query("SELECT * FROM ScheduleTable WHERE date = :date")
    fun getSchedule(date : String): List<ScheduleEntity>    // 등록된 회원인지 확인

    @Insert
    fun insertSchedule(schedule: ScheduleEntity)    // 회원 등록

    @Query("DELETE FROM ScheduleTable WHERE identifier = :identifier")
    fun deleteSchedule(identifier : Long)    // 회원 삭제


}