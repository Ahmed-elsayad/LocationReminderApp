package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val viewModel: AuthenticationViewModel by viewModels()

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            onSignInResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.authStateLive.observe(this) {
            if (it != null)
                when (it) {
                    is AuthState.Authenticated -> {
                        startActivity(Intent(this, RemindersActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                        finish()
                    }
                    AuthState.UnAuthenticated -> {
                        launchAuthenticationFlow()
                    }
                }
        }
    }

    private fun launchAuthenticationFlow() {
        signInLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    listOf(
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build()
                    )
                )
                .setTheme(R.style.AppTheme)
                .setIsSmartLockEnabled(false, true)
                .build()
        )
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult?) {
        Log.d(TAG, "sign in success: ${result?.resultCode == Activity.RESULT_OK}")
    }

    companion object {
        private const val TAG = "AuthenticationActivity"
    }
}
