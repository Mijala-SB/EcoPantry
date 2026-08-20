package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.DonationRepository
import com.mishba.ecopantryapp.data.FoodLogTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.ImpactRangeFilter
import com.mishba.ecopantryapp.model.LogActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

data class ImpactUiState(
    val rangeFilter: ImpactRangeFilter = ImpactRangeFilter.ALL_TIME,
    val selectedCategory: String? = null,
    val allLogs: List<FoodLogTable> = emptyList(),
    val totalUsed: Int = 0,
    val totalDonated: Int = 0,
    val totalWasted: Int = 0,
    val categoryBreakdown: Map<String, Int> = emptyMap(),
    val estimatedKgSaved: Double = 0.0,
    val donationsPublished: Int = 0,
    val isLoading: Boolean = true
) {
    /** Simple "waste diverted" ratio used for the progress ring on the Impact screen (FR10). */
    val diversionRate: Float
        get() {
            val total = totalUsed + totalDonated + totalWasted
            return if (total == 0) 0f else (totalUsed + totalDonated).toFloat() / total
        }

    /** Alternative Course 3a: no food-saving activity has been logged for the current filter yet. */
    val hasNoData: Boolean
        get() = !isLoading && totalUsed == 0 && totalDonated == 0 && totalWasted == 0

    /** Activity log entries filtered by the selected category, for the drill-down list (step 5-6). */
    val filteredActivity: List<FoodLogTable>
        get() = allLogs.filter {
            (it.actionType == LogActionType.USED || it.actionType == LogActionType.DONATED) &&
                (selectedCategory == null || it.category.ifBlank { "Other" } == selectedCategory)
        }
}

/** Backs the "Track My Impact" analytics dashboard (Use Case 4, FR10, US 6.1). */
class ImpactScreenViewModel(context: Context) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val donationRepository = DonationRepository()
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(ImpactUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { uid ->
                if (uid == null) return@collectLatest
                repository.getAllFoodLogs(uid).collectLatest { logs ->
                    _uiState.value = _uiState.value.copy(allLogs = logs)
                    recompute()
                }
            }
        }
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { uid ->
                if (uid == null) return@collectLatest
                val donationsResult = donationRepository.getDonationsByDonor(uid)
                _uiState.value = _uiState.value.copy(donationsPublished = donationsResult.getOrDefault(emptyList()).size)
            }
        }
    }

    /** User clicks a date-range filter chip (step 5-6). */
    fun selectRangeFilter(filter: ImpactRangeFilter) {
        _uiState.value = _uiState.value.copy(rangeFilter = filter)
        recompute()
    }

    /** User clicks a category bar/data point to drill into that category, or taps it again to clear (step 5-6). */
    fun selectCategory(category: String?) {
        val current = _uiState.value.selectedCategory
        _uiState.value = _uiState.value.copy(selectedCategory = if (current == category) null else category)
    }

    private fun recompute() {
        val state = _uiState.value
        val (start, end) = rangeBounds(state.rangeFilter)
        val logs = state.allLogs.filter { it.timestamp in start..end }

        val used = logs.count { it.actionType == LogActionType.USED }
        val donated = logs.count { it.actionType == LogActionType.DONATED }
        val wasted = logs.count { it.actionType == LogActionType.DELETED }
        val breakdown = logs
            .filter { it.actionType == LogActionType.USED || it.actionType == LogActionType.DONATED }
            .groupingBy { it.category.ifBlank { "Other" } }
            .eachCount()

        // Rough estimate: ~0.4kg of food saved from waste per "used/donated" log entry.
        val estimatedKg = (used + donated) * 0.4

        _uiState.value = _uiState.value.copy(
            totalUsed = used,
            totalDonated = donated,
            totalWasted = wasted,
            categoryBreakdown = breakdown,
            estimatedKgSaved = estimatedKg,
            isLoading = false
        )
    }

    private fun rangeBounds(filter: ImpactRangeFilter): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        if (filter == ImpactRangeFilter.ALL_TIME) return 0L to now

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)

        when (filter) {
            ImpactRangeFilter.WEEKLY -> calendar.add(Calendar.DAY_OF_YEAR, -6)
            ImpactRangeFilter.MONTHLY -> calendar.add(Calendar.DAY_OF_YEAR, -29)
            ImpactRangeFilter.ALL_TIME -> Unit
        }
        return calendar.timeInMillis to now
    }
}
