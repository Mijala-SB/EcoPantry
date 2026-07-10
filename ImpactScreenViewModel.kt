package com.ecopantry.app.model

/**
 * Represents a donation listing stored in the Firebase Firestore `donations`
 * collection so it can be browsed and claimed across users (FR07-FR09).
 * Plain data class (not a Room @Entity) because donations only live in the cloud.
 */
data class Donation(
    val donationId: String = "",
    val donorId: String = "",
    val donorName: String = "",
    val itemName: String = "",
    val quantity: String = "",
    val category: String = FoodCategory.OTHER.name,
    val expiryDate: Long? = null,
    val storageArea: String = StorageArea.OTHER.name,
    val pickupAddress: String = "",
    val pickupCity: String = "",
    val availability: String = "",
    val remarks: String = "",
    val status: String = DonationStatus.AVAILABLE.name,
    val claimantId: String? = null,
    val claimantName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Empty, no-arg friendly map used for Firestore writes. */
    fun toMap(): Map<String, Any?> = mapOf(
        "donationId" to donationId,
        "donorId" to donorId,
        "donorName" to donorName,
        "itemName" to itemName,
        "quantity" to quantity,
        "category" to category,
        "expiryDate" to expiryDate,
        "storageArea" to storageArea,
        "pickupAddress" to pickupAddress,
        "pickupCity" to pickupCity,
        "availability" to availability,
        "remarks" to remarks,
        "status" to status,
        "claimantId" to claimantId,
        "claimantName" to claimantName,
        "createdAt" to createdAt
    )
}
