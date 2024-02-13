package com.example.todolist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "ScheduleTable")
data class ScheduleEntity(@PrimaryKey(autoGenerate = true)
                          var identifier : Long =0L,
                          @ColumnInfo
                          var work : String,
                          @ColumnInfo
                          var checkState : Boolean ,
                          @ColumnInfo
                          var date : String) {

}