package com.mishba.ecopantryapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * Wraps Firebase Authentication for EcoPantry's registration / login / 2FA flows
 * (FR01-FR03, US 1.1-1.3).
 */
class AuthRepository(
    private val firestore: FirebaseFirestore = ServiceProvider.firestore
) {
    private val auth = ServiceProvider.firebaseAuth

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw IllegalStateException("Registration failed - no user returned.")
        Log.d("AuthRepository", "register() success uid=${user.uid}")
        Result.success(user)
    } catch (e: Exception) {
        Log.w("AuthRepository", "register() failed: ${e.message}")
        Result.failure(e)
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw IllegalStateException("Login failed - no user returned.")
        Log.d("AuthRepository", "login() success uid=${user.uid}")
        Result.success(user)
    } catch (e: Exception) {
        Log.w("AuthRepository", "login() failed: ${e.message}")
        Result.failure(e)
    }

    fun signOut() = auth.signOut()

    suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email.trim()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Generates a fresh 6-digit OTP and stores it. Returns the code for demo display. */
    suspend fun generateAndSendOtp(uid: String, email: String): Result<String> = try {
        val code = Random.nextInt(100000, 999999).toString()
        val expiry = System.currentTimeMillis() + (10 * 60 * 1000L) // 10-minute expiry
        firestore.collection("users").document(uid)
            .set(mapOf("otpCode" to code, "otpExpiry" to expiry), com.google.firebase.firestore.SetOptions.merge())
            .await()
        
        // Note: Firebase's built-in sendEmailVerification() sends a link, not a code.
        // For production, you would trigger a Cloud Function here to send a custom email.
        auth.currentUser?.sendEmailVerification()?.await()
        
        Log.d("AuthRepository", "generateAndSendOtp() code=$code")
        Result.success(code)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Validates the code entered on the OTP screen against Firestore (US 1.2). */
    suspend fun verifyOtp(uid: String, enteredCode: String): Result<Boolean> = try {
        val snapshot = firestore.collection("users").document(uid).get().await()
        val storedCode = snapshot.getString("otpCode")
        val expiry = snapshot.getLong("otpExpiry") ?: 0L
        val valid = storedCode != null && storedCode == enteredCode.trim() && System.currentTimeMillis() <= expiry
        if (valid) {
            firestore.collection("users").document(uid)
                .set(mapOf("isVerified" to true), com.google.firebase.firestore.SetOptions.merge()).await()
        }
        Result.success(valid)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveUserProfile(uid: String, fullName: String, email: String, householdSize: Int): Result<Unit> = try {
        firestore.collection("users").document(uid).set(
            mapOf(
                "fullName" to fullName,
                "email" to email,
                "householdSize" to householdSize,
                "isVerified" to false,
                "twoFactorEnabled" to false,
                "createdAt" to System.currentTimeMillis()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun setTwoFactorEnabled(uid: String, enabled: Boolean): Result<Unit> = try {
        firestore.collection("users").document(uid)
            .set(mapOf("twoFactorEnabled" to enabled), com.google.firebase.firestore.SetOptions.merge()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
