package com.example.todolist

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class MainActivityViewModel(val appDatabase: AppDatabase) : ViewModel() {
    private val _date = mutableStateOf("")
    private var _scheduleList = mutableStateListOf<ScheduleEntity>()
    val scheduleList: SnapshotStateList<ScheduleEntity> get() = _scheduleList
    val date: State<String> get() = _date

    val mainActivityModel = MainActivityModel(appDatabase)

    val TAG = "MainActivityViewModel"

    private val _editItem = mutableStateOf(ScheduleEntity(0L, "", false, ""))

    val editItem: State<ScheduleEntity> get() = _editItem

    private var _showDialog = mutableStateOf(false)

    val showDialog: State<Boolean> get() = _showDialog

    private var _showEditDialog = mutableStateOf(false)
    val showEditDialog: State<Boolean> get() = _showEditDialog


    fun selectDate(year: String, month: String, day: String) {
        _date.value = "${year} ${month.toInt() + 1} ${day}"
        Log.d(TAG, "selectDate: ${_date.value} / ${date.value}")
    }

    init {
        val now = System.currentTimeMillis();
        val date = Date(now)
        val dateFormat = SimpleDateFormat("yyyy M d")
        _date.value = dateFormat.format(date)
    }

    fun test() {
        _date.value = "gdgdgd"
    }

    suspend fun removeSchedule(item: ScheduleEntity) {
        _scheduleList.remove(item)
        mainActivityModel.deleteSchedule(item.identifier)
    }

    suspend fun selectSchedule(date: String) {
        _scheduleList.clear()
        for (i in mainActivityModel.selectSchedule(date)) {
            _scheduleList.add(i)
        }
        val comparator = Comparator<ScheduleEntity> { o1, o2 ->
            // true는 뒤로 보내고 false는 앞으로 보내도록 비교합니다.
            if (o1.checkState && !o2.checkState) {
                1 // o1이 뒤로 가게 됩니다.
            } else if (!o1.checkState && o2.checkState) {
                -1 // o1이 앞으로 가게 됩니다.
            } else {
                0 // 순서를 변경하지 않습니다.
            }
        }
        _scheduleList.sortWith(comparator)

        Log.d(TAG, "selectSchedule: ${_scheduleList.toList()}")
    }

    suspend fun addSchedule(scheduleEntity: ScheduleEntity) {
        withContext(Dispatchers.IO) {
            mainActivityModel.insertSchedule(scheduleEntity)
            selectSchedule(scheduleEntity.date)
        }
    }

    suspend fun updateSchedule(work: String) {
        mainActivityModel.updateSchedule(_editItem.value.identifier, work)
        selectSchedule(_editItem.value.date)
    }

    fun setShowDialog(isVisibility: Boolean) {

        _showDialog.value = isVisibility
    }

    fun setShowEditDialog(isVisibility: Boolean) {
        _showEditDialog.value = isVisibility

    }

    suspend fun workStateChange(idx: Int, state: Boolean) {
        _scheduleList[idx].checkState = state
        withContext(Dispatchers.IO) {
            mainActivityModel.updateCheckState(_scheduleList[idx].identifier, state)
        }
        selectSchedule(_scheduleList[idx].date)
    }

    fun setEditItem(item: ScheduleEntity) {
        _editItem.value = item
    }
}