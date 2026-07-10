package com.ecopantry.app.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecopantry.app.data.AppDataStore
import com.ecopantry.app.data.AppDatabase
import com.ecopantry.app.data.FoodLogTable
import com.ecopantry.app.data.Repository
import com.ecopantry.app.model.LogActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DayTally(val label: String, val added: Int, val used: Int, val donated: Int)

data class WeeklyTrackerUiState(
    val logs: List<FoodLogTable> = emptyList(),
    val dailyTallies: List<DayTally> = emptyList(),
    val totalAdded: Int = 0,
    val totalUsed: Int = 0,
    val totalDonated: Int = 0,
    val totalWasted: Int = 0
)

/** Backs the Weekly Food Tracker screen (US 3.2, 6.1, 6.2). */
class WeeklyTrackerScreenViewModel(context: Context) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(WeeklyTrackerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { userId ->
                if (userId == null) return@collectLatest
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -6)
                calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis
                val end = System.currentTimeMillis()

                repository.getFoodLogsByDateRange(userId, start, end).collectLatest { logs ->
                    _uiState.value = WeeklyTrackerUiState(
                        logs = logs,
                        dailyTallies = buildDailyTallies(logs),
                        totalAdded = logs.count { it.actionType == LogActionType.ADDED },
                        totalUsed = logs.count { it.actionType == LogActionType.USED },
                        totalDonated = logs.count { it.actionType == LogActionType.DONATED },
                        totalWasted = logs.count { it.actionType == LogActionType.DELETED }
                    )
                }
            }
        }
    }

    private fun buildDailyTallies(logs: List<FoodLogTable>): List<DayTally> {
        val labelFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val days = (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            cal
        }
        return days.map { cal ->
            val dayStart = cal.clone() as Calendar
            dayStart.set(Calendar.HOUR_OF_DAY, 0); dayStart.set(Calendar.MINUTE, 0); dayStart.set(Calendar.SECOND, 0)
            val dayEnd = dayStart.timeInMillis + TimeUnit.DAYS.toMillis(1)

            val dayLogs = logs.filter { it.timestamp in dayStart.timeInMillis until dayEnd }
            DayTally(
                label = labelFormat.format(dayStart.time),
                added = dayLogs.count { it.actionType == LogActionType.ADDED },
                used = dayLogs.count { it.actionType == LogActionType.USED },
                donated = dayLogs.count { it.actionType == LogActionType.DONATED }
            )
        }
    }
}
