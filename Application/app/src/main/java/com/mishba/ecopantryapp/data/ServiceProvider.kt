package com.mishba.ecopantryapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Central point for obtaining Firebase SDK singletons (the app's external / cloud database).
 * Firebase Authentication handles secure registration & login; Firestore stores the
 * `donations` collection so listings can be browsed by every user in real time.
 */
object ServiceProvider {
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}
