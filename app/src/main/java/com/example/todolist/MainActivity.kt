package com.example.todolist

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.todolist.ui.theme.TodoListTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val TAG = "MainActivity"
    lateinit var mainActivityViewModel: MainActivityViewModel
    lateinit var appDatabase: AppDatabase


    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appDatabase = AppDatabase.getInstance(this)
        setContent {
            mainActivityViewModel = MainActivityViewModel(appDatabase)
            TodoListTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                }
                calenderView(mainActivityViewModel)
            }
        }
        GlobalScope.launch(Dispatchers.Main) {
            mainActivityViewModel.selectSchedule(mainActivityViewModel.date.value)
        }
    }

    private fun addSchedule(work: String) {
        GlobalScope.launch {
            mainActivityViewModel.addSchedule(
                ScheduleEntity(
                    work = work,
                    checkState = false,
                    date = mainActivityViewModel.date.value
                )
            )
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun calenderView(viewModel: MainActivityViewModel) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {

            AndroidView(factory = { CalendarView(it) }, update = { it ->
                it.setOnDateChangeListener { calenderView, year, month, day ->
                    mainActivityViewModel.selectDate(
                        year.toString(), month.toString(),
                        day.toString()
                    )
                    GlobalScope.launch {
                        mainActivityViewModel.selectSchedule(mainActivityViewModel.date.value)
                    }
                }
            }, modifier = Modifier.fillMaxWidth())
            if (viewModel.scheduleList.size <= 0) {
                Text(text = "일정이 없습니다.")
            }
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(10f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(viewModel.scheduleList) { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                                .clickable {
                                    Log.d(TAG, "calenderView: 아이템 클릭 됨 ${item}")
                                    viewModel.setEditItem(item)
                                    viewModel.setShowEditDialog(true)
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (item.checkState) {
                                Text(
                                    text = "${item.work}",
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .weight(1f),
                                    textDecoration = TextDecoration.LineThrough
                                )
                            } else {
                                Text(
                                    text = "${item.work}",
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically)
                                )
                            }
                            Checkbox(checked = item.checkState, onCheckedChange = {
                                Log.d(TAG, "calenderView: ${it}")
                                GlobalScope.launch {
                                    mainActivityViewModel.workStateChange(idx, it)
                                }
                            }, modifier = Modifier.weight(1f))
                            Button(
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(16.dp),
                                onClick = {
                                    GlobalScope.launch {

                                        viewModel.removeSchedule(item)
                                    }
                                }
                            ) {
                                Text(text = "삭제")
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.setShowDialog(true)

                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Text(text = "추가")
                }
            }
            DialogWithInput(
                showDialog = viewModel.showDialog.value,
                onDismiss = { viewModel.setShowDialog(false) },
                onConfirm = { message ->
                    addSchedule(message)
                    // 완료 버튼이 클릭됐을 때 실행될 로직 작성
                    viewModel.setShowDialog(false)
                }
            )
            DialogWithEdit(
                showDialog = viewModel.showEditDialog.value,
                onDismiss = { viewModel.setShowEditDialog(false) },
                onConfirm = { message ->
//                    addSchedule(message)
                    // 완료 버튼이 클릭됐을 때 실행될 로직 작성
                    viewModel.setShowEditDialog(false)
                    GlobalScope.launch {
                        viewModel.updateSchedule(message)
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DialogWithInput(
        showDialog: Boolean,
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        if (showDialog) {
            var message by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = "일정 입력") },
                text = {
                    Column {
                        TextField(
                            value = message,
                            onValueChange = { message = it },
                            placeholder = { Text(text = "일정을 입력해주세요!") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirm(message) }
                    ) {
                        Text(text = "완료")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = onDismiss
                    ) {
                        Text(text = "취소")
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DialogWithEdit(
        showDialog: Boolean,
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit,
    ) {
        if (showDialog) {
            var message by remember { mutableStateOf("") }
            message = mainActivityViewModel.editItem.value.work

            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = "일정 수정") },
                text = {
                    Column {
                        TextField(
                            value = message,
                            onValueChange = { message = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirm(message) }
                    ) {
                        Text(text = "완료")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = onDismiss
                    ) {
                        Text(text = "취소")
                    }
                }
            )
        }
    }

}
