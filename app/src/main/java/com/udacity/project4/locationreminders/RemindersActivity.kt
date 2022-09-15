package com.udacity.project4.locationreminders

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthState
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.databinding.ActivityRemindersBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val authViewModel: AuthenticationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel.authStateLive.observe(this) {
            if (it is AuthState.UnAuthenticated) {
                finishAndLaunchAuthActivity()
            }
        }

        setSupportActionBar(binding.toolbar)

        binding.root.doOnPreDraw {
            findNavController(R.id.nav_host_fragment).let { navController ->
                binding.toolbar.setupWithNavController(
                    navController,
                    AppBarConfiguration(navController.graph)
                )
            }
        }
    }

    private fun finishAndLaunchAuthActivity() {
        startActivity(
            Intent(this, AuthenticationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
        finish()
    }
}
