package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.DonationRepository
import com.mishba.ecopantryapp.data.FoodItemTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.Donation
import com.mishba.ecopantryapp.model.FoodStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class InventoryUiState(
    val userId: String = "",
    val foodItems: List<FoodItemTable> = emptyList(),
    val myDonations: List<Donation> = emptyList(),
    val searchQuery: String = "",
    val isLoadingDonations: Boolean = false
)

/** Backs the Inventory screen's two tabs: household Food List (FR05-FR06) and the user's own Donation List (FR07). */
class InventoryScreenViewModel(context: Context) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val donationRepository = DonationRepository()
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { userId ->
                if (userId == null) return@collectLatest
                _uiState.value = _uiState.value.copy(userId = userId)
                launch {
                    repository.getFoodItemsByStatus(userId, FoodStatus.ACTIVE).collectLatest { items ->
                        _uiState.value = _uiState.value.copy(foodItems = items)
                    }
                }
                refreshMyDonations(userId)
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun refreshMyDonations(userId: String = _uiState.value.userId) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDonations = true)
            val result = donationRepository.getDonationsByDonor(userId)
            _uiState.value = _uiState.value.copy(
                myDonations = result.getOrDefault(emptyList()),
                isLoadingDonations = false
            )
        }
    }

    fun filteredItems(): List<FoodItemTable> {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) return _uiState.value.foodItems
        return _uiState.value.foodItems.filter {
            it.itemName.contains(query, ignoreCase = true) || it.category.label.contains(query, ignoreCase = true)
        }
    }
}
