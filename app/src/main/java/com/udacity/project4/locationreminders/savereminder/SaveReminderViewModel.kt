package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    // below values are set from user input
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()

    // below values are set from location choose
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    val reminderDataItem
        get() = run {
            val title = reminderTitle.value
            val description = reminderDescription.value
            val location = reminderSelectedLocationStr.value
            val latitude = latitude.value
            val longitude = longitude.value

            ReminderDataItem(title, description, location, latitude, longitude)
        }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(onSaveComplete: (ReminderDataItem)->Unit): ReminderDataItem {
        showLoading.value = true
        val reminderData = reminderDataItem
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                ).also { Log.d("ReminderID", "the id = ${it.id}") }
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
            onSaveComplete(reminderData)
        }

        return reminderData
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(): Boolean {
        val reminderData = reminderDataItem

        if (reminderData.title.isNullOrBlank()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrBlank()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}