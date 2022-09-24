package com.udacity.project4.locationreminders
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    lateinit var binding : ActivityRemindersBinding
    private val _viewModel by viewModel<SaveReminderViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                _viewModel.navigationCommand.postValue(NavigationCommand.Back)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
