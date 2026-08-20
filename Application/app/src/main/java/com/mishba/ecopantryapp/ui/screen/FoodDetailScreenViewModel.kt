package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.FoodItemTable
import com.mishba.ecopantryapp.data.FoodLogTable
import com.mishba.ecopantryapp.data.NotificationTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.FoodStatus
import com.mishba.ecopantryapp.model.LogActionType
import com.mishba.ecopantryapp.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FoodDetailUiState(
    val item: FoodItemTable? = null,
    val isLoading: Boolean = true,
    val deleted: Boolean = false
)

class FoodDetailScreenViewModel(context: Context, private val itemId: String) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val item = repository.getFoodItemById(itemId)
            _uiState.value = FoodDetailUiState(item = item, isLoading = false)
        }
    }

    fun markAsUsed() {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            val updated = item.copy(status = FoodStatus.USED, updatedAt = System.currentTimeMillis())
            repository.updateFoodItem(updated)
            repository.insertFoodLog(
                FoodLogTable(
                    userId = item.userId, itemId = item.itemId, itemName = item.itemName,
                    actionType = LogActionType.USED, category = item.category.label
                )
            )
            _uiState.value = _uiState.value.copy(item = updated)
        }
    }

    fun deleteItem() {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            repository.deleteFoodItem(item)
            repository.insertFoodLog(
                FoodLogTable(
                    userId = item.userId, itemId = item.itemId, itemName = item.itemName,
                    actionType = LogActionType.DELETED, category = item.category.label
                )
            )
            // --- NEW: Insert notification on deletion ---
            val notification = NotificationTable(
                userId = item.userId,
                type = NotificationType.INVENTORY_ALERT,
                title = "Item Deleted",
                message = "${item.itemName} was removed from your inventory.",
                relatedItemId = item.itemId
            )
            repository.insertNotification(notification)
            // --- END NEW ---
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }
}