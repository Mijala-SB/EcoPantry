package com.ecopantry.app.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecopantry.app.data.AppDataStore
import com.ecopantry.app.data.AppDatabase
import com.ecopantry.app.data.DonationRepository
import com.ecopantry.app.data.Repository
import com.ecopantry.app.model.LogActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ImpactUiState(
    val totalUsed: Int = 0,
    val totalDonated: Int = 0,
    val totalWasted: Int = 0,
    val categoryBreakdown: Map<String, Int> = emptyMap(),
    val estimatedKgSaved: Double = 0.0,
    val donationsPublished: Int = 0
) {
    /** Simple "waste diverted" ratio used for the progress ring on the Impact screen (FR10). */
    val diversionRate: Float
        get() {
            val total = totalUsed + totalDonated + totalWasted
            return if (total == 0) 0f else (totalUsed + totalDonated).toFloat() / total
        }
}

/** Backs the "Track My Impact" analytics dashboard (FR10, US 6.1). */
class ImpactScreenViewModel(context: Context) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val donationRepository = DonationRepository()
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(ImpactUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { userId ->
                if (userId == null) return@collectLatest
                repository.getAllFoodLogs(userId).collectLatest { logs ->
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
                        estimatedKgSaved = estimatedKg
                    )
                }
                val donationsResult = donationRepository.getDonationsByDonor(userId)
                _uiState.value = _uiState.value.copy(donationsPublished = donationsResult.getOrDefault(emptyList()).size)
            }
        }
    }
}
