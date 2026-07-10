package com.ecopantry.app.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecopantry.app.data.AppDataStore
import com.ecopantry.app.data.AppDatabase
import com.ecopantry.app.data.DonationRepository
import com.ecopantry.app.data.FoodLogTable
import com.ecopantry.app.data.Repository
import com.ecopantry.app.model.Donation
import com.ecopantry.app.model.FoodStatus
import com.ecopantry.app.model.LogActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AddDonationUiState(
    val itemName: String = "",
    val quantity: String = "",
    val pickupAddress: String = "",
    val pickupCity: String = "",
    val availability: String = "",
    val remarks: String = "",
    val addressError: String = "",
    val cityError: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val errorMessage: String = ""
)

/** Converts an inventory item into a public donation listing in Firestore (FR07, US 5.1). */
class AddDonationScreenViewModel(context: Context, private val sourceItemId: String) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val donationRepository = DonationRepository()
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(AddDonationUiState())
    val uiState = _uiState.asStateFlow()

    private var category = ""
    private var expiryDate: Long? = null
    private var storageArea = ""

    init {
        viewModelScope.launch {
            val item = repository.getFoodItemById(sourceItemId)
            if (item != null) {
                category = item.category.name
                expiryDate = item.expiryDate
                storageArea = item.storageLocation.name
                _uiState.value = _uiState.value.copy(
                    itemName = item.itemName,
                    quantity = item.quantity,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Item not found")
            }
        }
    }

    fun onAddressChange(v: String) { _uiState.value = _uiState.value.copy(pickupAddress = v, addressError = "") }
    fun onCityChange(v: String) { _uiState.value = _uiState.value.copy(pickupCity = v, cityError = "") }
    fun onAvailabilityChange(v: String) { _uiState.value = _uiState.value.copy(availability = v) }
    fun onRemarksChange(v: String) { _uiState.value = _uiState.value.copy(remarks = v) }

    fun submit() {
        val s = _uiState.value
        var valid = true
        var addrErr = ""; var cityErr = ""
        if (s.pickupAddress.isBlank()) { addrErr = "Pickup address is required"; valid = false }
        if (s.pickupCity.isBlank()) { cityErr = "City is required"; valid = false }
        if (!valid) {
            _uiState.value = s.copy(addressError = addrErr, cityError = cityErr)
            return
        }

        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true, errorMessage = "")
            val userId = appDataStore.loggedInUserIdFlow().first { it != null } ?: return@launch
            val user = repository.getUserById(userId)

            val donation = Donation(
                donorId = userId,
                donorName = user?.fullName ?: "EcoPantry user",
                itemName = s.itemName,
                quantity = s.quantity,
                category = category,
                expiryDate = expiryDate,
                storageArea = storageArea,
                pickupAddress = s.pickupAddress.trim(),
                pickupCity = s.pickupCity.trim(),
                availability = s.availability.trim(),
                remarks = s.remarks.trim()
            )

            val result = donationRepository.createDonation(donation)
            result.onSuccess { donationId ->
                val item = repository.getFoodItemById(sourceItemId)
                if (item != null) {
                    repository.updateFoodItem(
                        item.copy(status = FoodStatus.DONATED, linkedDonationId = donationId, isPublic = true)
                    )
                    repository.insertFoodLog(
                        FoodLogTable(
                            userId = userId, itemId = item.itemId, itemName = item.itemName,
                            actionType = LogActionType.DONATED, category = item.category.label
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saveComplete = true)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message ?: "Could not publish donation.")
            }
        }
    }
}
