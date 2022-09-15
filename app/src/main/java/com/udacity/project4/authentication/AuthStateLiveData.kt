package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

sealed class AuthState {
    object UnAuthenticated : AuthState()
    data class Authenticated(val firebaseUser: FirebaseUser) : AuthState()
}

class AuthStateLiveData : LiveData<AuthState?>() {

    private val auth = Firebase.auth

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.UnAuthenticated
    }

    override fun onActive() {
        super.onActive()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onInactive() {
        super.onInactive()
        auth.removeAuthStateListener(authStateListener)
    }
}