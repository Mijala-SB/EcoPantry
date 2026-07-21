package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.FoodItemTable
import com.mishba.ecopantryapp.data.FoodLogTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.FoodCategory
import com.mishba.ecopantryapp.model.LogActionType
import com.mishba.ecopantryapp.model.StorageArea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AddEditFoodUiState(
    val itemId: String? = null,
    val itemName: String = "",
    val quantity: String = "",
    val expiryDateMillis: Long? = null,
    val category: FoodCategory = FoodCategory.OTHER,
    val storageArea: StorageArea = StorageArea.OTHER,
    val remarks: String = "",
    val isPublic: Boolean = false,
    val nameError: String = "",
    val quantityError: String = "",
    val expiryError: String = "",
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false
)

/** Backs both "Add Food" and "Edit Food" (FR05, FR06, US 2.1-2.3). */
class AddEditFoodScreenViewModel(context: Context, private val editItemId: String?) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val appDataStore = AppDataStore(context)
    private var userId: String = ""

    private val _uiState = MutableStateFlow(AddEditFoodUiState(itemId = editItemId))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().first { it != null }?.let { userId = it }
        }
        if (editItemId != null) {
            viewModelScope.launch {
                val item = repository.getFoodItemById(editItemId)
                if (item != null) {
                    userId = item.userId
                    _uiState.value = _uiState.value.copy(
                        itemName = item.itemName,
                        quantity = item.quantity,
                        expiryDateMillis = item.expiryDate,
                        category = item.category,
                        storageArea = item.storageLocation,
                        remarks = item.remarks,
                        isPublic = item.isPublic
                    )
                }
            }
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(itemName = v, nameError = "") }
    fun onQuantityChange(v: String) { _uiState.value = _uiState.value.copy(quantity = v, quantityError = "") }
    fun onExpiryChange(millis: Long?) { _uiState.value = _uiState.value.copy(expiryDateMillis = millis, expiryError = "") }
    fun onCategoryChange(c: FoodCategory) { _uiState.value = _uiState.value.copy(category = c) }
    fun onStorageChange(s: StorageArea) { _uiState.value = _uiState.value.copy(storageArea = s) }
    fun onRemarksChange(v: String) { _uiState.value = _uiState.value.copy(remarks = v) }
    fun onPublicToggle(v: Boolean) { _uiState.value = _uiState.value.copy(isPublic = v) }

    fun save() {
        val s = _uiState.value
        var valid = true
        var nameErr = ""; var qtyErr = ""

        if (s.itemName.isBlank()) { nameErr = "Item name is required"; valid = false }
        if (s.quantity.isBlank()) { qtyErr = "Quantity is required"; valid = false }

        if (!valid) {
            _uiState.value = s.copy(nameError = nameErr, quantityError = qtyErr)
            return
        }

        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            val currentUserId = appDataStoreUserId()
            val isEdit = editItemId != null

            val item = if (isEdit) {
                repository.getFoodItemById(editItemId!!)!!.copy(
                    itemName = s.itemName.trim(),
                    quantity = s.quantity.trim(),
                    expiryDate = s.expiryDateMillis,
                    category = s.category,
                    storageLocation = s.storageArea,
                    remarks = s.remarks.trim(),
                    isPublic = s.isPublic,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                FoodItemTable(
                    userId = currentUserId,
                    itemName = s.itemName.trim(),
                    quantity = s.quantity.trim(),
                    expiryDate = s.expiryDateMillis,
                    category = s.category,
                    storageLocation = s.storageArea,
                    remarks = s.remarks.trim(),
                    isPublic = s.isPublic
                )
            }

            if (isEdit) repository.updateFoodItem(item) else repository.insertFoodItem(item)

            repository.insertFoodLog(
                FoodLogTable(
                    userId = currentUserId,
                    itemId = item.itemId,
                    itemName = item.itemName,
                    actionType = if (isEdit) LogActionType.EDITED else LogActionType.ADDED,
                    category = item.category.label
                )
            )

            _uiState.value = _uiState.value.copy(isSaving = false, saveComplete = true)
        }
    }

    private suspend fun appDataStoreUserId(): String {
        if (userId.isNotBlank()) return userId
        val id = appDataStore.loggedInUserIdFlow().first { it != null } ?: ""
        userId = id
        return id
    }
}
