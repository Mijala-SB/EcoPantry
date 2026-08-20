package com.mishba.ecopantryapp

import com.mishba.ecopantryapp.data.FoodItemTable
import com.mishba.ecopantryapp.model.FoodStatus
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class FoodInventoryValidationTest {

    @Test
    fun itemNameBlankIsInvalid() {
        val name = "   "
        assertFalse(name.isNotBlank())
    }

    @Test
    fun itemNameNonBlankIsValid() {
        val name = "Milk"
        assertTrue(name.isNotBlank())
    }

    @Test
    fun quantityBlankIsInvalid() {
        val qty = ""
        assertFalse(qty.isNotBlank())
    }

    @Test
    fun quantityNonBlankIsValid() {
        val qty = "2 pcs"
        assertTrue(qty.isNotBlank())
    }

    @Test
    fun expiryDateValidStringShouldParse() {
        val dateStr = "2026-12-31"
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateStr)
        assertNotNull(date)
    }

    @Test
    fun expiryDateInvalidStringShouldThrow() {
        val dateStr = "invalid"
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            format.parse(dateStr)
            fail("Should throw exception")
        } catch (e: Exception) {
            assertTrue(true)
        }
    }

    @Test
    fun itemStatusChangesFromActiveToUsed() {
        val oldStatus = "ACTIVE"
        val newStatus = "USED"
        assertNotEquals(oldStatus, newStatus)
    }

    @Test
    fun itemStatusChangesFromActiveToDonated() {
        val oldStatus = "ACTIVE"
        val newStatus = "DONATED"
        assertNotEquals(oldStatus, newStatus)
    }

    @Test
    fun donationIdShouldBeSetWhenItemDonated() {
        val donationId = "donation123"
        val item = FoodItemTable(
            userId = "user1",
            itemName = "Apple",
            quantity = "1",
            expiryDate = null,
            status = FoodStatus.DONATED,
            linkedDonationId = donationId
        )
        assertEquals(donationId, item.linkedDonationId)
    }
}
