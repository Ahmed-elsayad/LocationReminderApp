package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData : LiveData<FirebaseUser?>() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        // Use the FirebaseAuth instance instantiated at the beginning of the class to get an entry
        // point into the Firebase Authentication SDK the app is using.
        // With an instance of the FirebaseAuth class, you can now query for the current user.
        value = firebaseAuth.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

}
