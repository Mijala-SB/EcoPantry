package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.DonationRepository
import com.mishba.ecopantryapp.model.Donation
import com.mishba.ecopantryapp.model.FoodCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class BrowseDonationUiState(
    val donations: List<Donation> = emptyList(),
    val categoryFilter: String? = null,
    val cityFilter: String = "",
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String = ""
)

class BrowseDonationScreenViewModel(context: Context) : ViewModel() {

    private val donationRepository = DonationRepository()
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(BrowseDonationUiState())
    val uiState = _uiState.asStateFlow()

    val categories = FoodCategory.entries.map { it.name to it.label }

    init {
        viewModelScope.launch {
            val userId = appDataStore.loggedInUserIdFlow().first { it != null } ?: ""
            _uiState.value = _uiState.value.copy(currentUserId = userId)
        }
        refresh()
    }

    fun onCategoryFilterChange(category: String?) {
        _uiState.value = _uiState.value.copy(categoryFilter = category)
        refresh()
    }

    fun onCityFilterChange(city: String) {
        _uiState.value = _uiState.value.copy(cityFilter = city)
    }

    fun applyCityFilter() = refresh()

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            val s = _uiState.value
            val result = donationRepository.browseAvailableDonations(s.categoryFilter, s.cityFilter)
            result.onSuccess { list ->
                // REMOVED: filter that hid own donations – donors can now see their own listings
                _uiState.value = _uiState.value.copy(donations = list, isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Could not load donations.")
            }
        }
    }
}