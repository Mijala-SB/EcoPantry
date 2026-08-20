package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.DonationRepository
import com.mishba.ecopantryapp.data.NotificationTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.Donation
import com.mishba.ecopantryapp.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DonationDetailUiState(
    val donation: Donation? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val isClaiming: Boolean = false,
    val claimSuccess: Boolean = false,
    val errorMessage: String = ""
)

class DonationDetailScreenViewModel(context: Context, private val donationId: String) : ViewModel() {

    private val donationRepository = DonationRepository()
    private val repository = Repository(AppDatabase.getInstance(context)) // NEW
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(DonationDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = appDataStore.loggedInUserIdFlow().first { it != null } ?: ""
            val result = donationRepository.getDonationById(donationId)
            _uiState.value = _uiState.value.copy(
                donation = result.getOrNull(),
                currentUserId = userId,
                isLoading = false,
                errorMessage = if (result.isFailure) "Could not load this donation." else ""
            )
        }
    }

    fun claim() {
        val s = _uiState.value
        val donation = s.donation ?: return
        viewModelScope.launch {
            _uiState.value = s.copy(isClaiming = true, errorMessage = "")
            val user = repository.getUserById(s.currentUserId)
            val result = donationRepository.claimDonation(donation.donationId, s.currentUserId, user?.fullName ?: "EcoPantry user")
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isClaiming = false,
                    claimSuccess = true,
                    donation = donation.copy(status = "CLAIMED", claimantId = s.currentUserId)
                )
                // --- NEW: Insert notification for the donor ---
                val donorId = donation.donorId
                val donorNotification = NotificationTable(
                    userId = donorId,
                    type = NotificationType.DONATION_CLAIMED,
                    title = "Your donation was claimed",
                    message = "${user?.fullName ?: "Someone"} has claimed your ${donation.itemName}.",
                    relatedItemId = donation.donationId
                )
                repository.insertNotification(donorNotification)
                // --- END NEW ---
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isClaiming = false, errorMessage = e.message ?: "Could not claim this item.")
            }
        }
    }
}