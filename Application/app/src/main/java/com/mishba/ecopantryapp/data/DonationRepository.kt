package com.mishba.ecopantryapp.data

import android.util.Log
import com.mishba.ecopantryapp.model.Donation
import com.mishba.ecopantryapp.model.DonationStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles the `donations` collection in Firebase Firestore — the cloud database
 * that lets one user's donation be discovered and claimed by any other user
 * (FR07-FR09, US 5.1-5.3).
 */
class DonationRepository(
    private val firestore: FirebaseFirestore = ServiceProvider.firestore
) {
    private val collection = firestore.collection("donations")

    suspend fun createDonation(donation: Donation): Result<String> = try {
        val docRef = collection.document()
        val withId = donation.copy(donationId = docRef.id)
        docRef.set(withId.toMap()).await()
        Log.d("DonationRepository", "createDonation() success id=${docRef.id}")
        Result.success(docRef.id)
    } catch (e: Exception) {
        Log.w("DonationRepository", "createDonation() failed: ${e.message}")
        Result.failure(e)
    }

    /** All AVAILABLE donations, optionally filtered by category and/or city (US 5.1, 5.3). */
    suspend fun browseAvailableDonations(
        category: String? = null,
        city: String? = null
    ): Result<List<Donation>> = try {
        var query = collection.whereEqualTo("status", DonationStatus.AVAILABLE.name)
        if (!category.isNullOrBlank()) query = query.whereEqualTo("category", category)
        val snapshot = query.get().await()
        var results = snapshot.documents.mapNotNull { it.toObject(Donation::class.java) }
        if (!city.isNullOrBlank()) {
            results = results.filter { it.pickupCity.equals(city, ignoreCase = true) }
        }
        Result.success(results.sortedByDescending { it.createdAt })
    } catch (e: Exception) {
        Log.w("DonationRepository", "browseAvailableDonations() failed: ${e.message}")
        Result.failure(e)
    }

    suspend fun getDonationsByDonor(donorId: String): Result<List<Donation>> = try {
        val snapshot = collection.whereEqualTo("donorId", donorId).get().await()
        Result.success(snapshot.documents.mapNotNull { it.toObject(Donation::class.java) }
            .sortedByDescending { it.createdAt })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDonationById(id: String): Result<Donation?> = try {
        val snapshot = collection.document(id).get().await()
        Result.success(snapshot.toObject(Donation::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Atomically marks a donation as claimed so two users can't claim the same item (US 5.2). */
    suspend fun claimDonation(donationId: String, claimantId: String, claimantName: String): Result<Unit> = try {
        firestore.runTransaction { transaction ->
            val docRef = collection.document(donationId)
            val snapshot = transaction.get(docRef)
            val status = snapshot.getString("status")
            if (status != DonationStatus.AVAILABLE.name) {
                throw IllegalStateException("This item has already been claimed.")
            }
            transaction.update(
                docRef,
                mapOf(
                    "status" to DonationStatus.CLAIMED.name,
                    "claimantId" to claimantId,
                    "claimantName" to claimantName
                )
            )
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.w("DonationRepository", "claimDonation() failed: ${e.message}")
        Result.failure(e)
    }

    suspend fun deleteDonation(donationId: String): Result<Unit> = try {
        collection.document(donationId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
