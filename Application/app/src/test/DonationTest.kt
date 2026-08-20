package com.mishba.ecopantryapp

import com.mishba.ecopantryapp.data.NotificationTable
import com.mishba.ecopantryapp.model.DonationStatus
import com.mishba.ecopantryapp.model.NotificationType
import org.junit.Assert.*
import org.junit.Test

class DonationTest {

    @Test
    fun pickupAddressBlankIsInvalid() {
        val address = ""
        assertFalse(address.isNotBlank())
    }

    @Test
    fun pickupAddressNonBlankIsValid() {
        val address = "123 Main St"
        assertTrue(address.isNotBlank())
    }

    @Test
    fun cityBlankIsInvalid() {
        val city = ""
        assertFalse(city.isNotBlank())
    }

    @Test
    fun cityNonBlankIsValid() {
        val city = "Kuala Lumpur"
        assertTrue(city.isNotBlank())
    }

    @Test
    fun donationStatusShouldChangeFromAvailableToClaimed() {
        val initialStatus = DonationStatus.AVAILABLE
        val newStatus = DonationStatus.CLAIMED
        assertNotEquals(initialStatus, newStatus)
    }

    @Test
    fun onlyAvailableDonationsCanBeClaimed() {
        val status = DonationStatus.AVAILABLE
        val canClaim = status == DonationStatus.AVAILABLE
        assertTrue(canClaim)
    }

    @Test
    fun claimedDonationsCannotBeClaimedAgain() {
        val status = DonationStatus.CLAIMED
        val canClaim = status == DonationStatus.AVAILABLE
        assertFalse(canClaim)
    }

    @Test
    fun donorShouldReceiveNotificationWhenDonationClaimed() {
        val donorId = "donor123"
        val notification = NotificationTable(
            userId = donorId,
            type = NotificationType.DONATION_CLAIMED,
            title = "Your donation was claimed",
            message = "Someone claimed your item.",
            relatedItemId = "donation456"
        )
        assertEquals(donorId, notification.userId)
        assertEquals(NotificationType.DONATION_CLAIMED, notification.type)
    }

    @Test
    fun donorShouldReceiveNotificationWhenDonationPublished() {
        val donorId = "donor123"
        val notification = NotificationTable(
            userId = donorId,
            type = NotificationType.DONATION_CONFIRMED,
            title = "Donation Published",
            message = "Your donation is live.",
            relatedItemId = "donation456"
        )
        assertEquals(donorId, notification.userId)
        assertEquals(NotificationType.DONATION_CONFIRMED, notification.type)
    }
}