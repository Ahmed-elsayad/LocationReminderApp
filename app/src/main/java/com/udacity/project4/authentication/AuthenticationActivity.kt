package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    companion object{
        const val TAG = "AuthenticationActivity"
    }
    private val viewModel by viewModels<LoginViewModel>()
    val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private lateinit var  binding  : ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)

        setContentView(binding.root)
//         COMPLETED: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

        binding.button.setOnClickListener{
            createSignInIntent()
        }



    }

    override fun onStart() {
        super.onStart()
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            // val user = FirebaseAuth.getInstance().currentUser
            //          COMPLETED : If the user was authenticated, send him to RemindersActivity
            Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)

        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.i(TAG, "Sign in unsuccessful ${response?.error?.message}")
            Snackbar.make(
                binding.root, getString(R.string.login_unsuccessful_msg),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        //  COMPLETED: a bonus is to customize the sign in flow to look nice using :
        // https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        val customlayout = AuthMethodPickerLayout.Builder(R.layout.authentication_picker_layout)
            .setGoogleButtonId(R.id.google_login_btn)
            .setEmailButtonId(R.id.mail_login_btn)
            .build()

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setAuthMethodPickerLayout(customlayout)
            .build()

        signInLauncher.launch(signInIntent)

    }
}
