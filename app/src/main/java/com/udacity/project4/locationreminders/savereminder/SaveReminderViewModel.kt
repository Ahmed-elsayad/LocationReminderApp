package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()
    var saveClicked = MutableLiveData<Boolean>().apply { postValue(false) }
    var permissionDenied =  MutableLiveData<Boolean>().apply { postValue(false) }
    var initialLoad = false
    var requestingLocationUpdates = false
    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) : Boolean {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
            return true
        }
        return false
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.postValue(true)
        reminderTitle.postValue(reminderData.title)
        reminderDescription.postValue(reminderData.description)
        reminderSelectedLocationStr.postValue(reminderData.location)
        latitude.postValue(reminderData.latitude)
        longitude.postValue(reminderData.longitude)
        EspressoIdlingResource.increment()
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            EspressoIdlingResource.decrement()
            showLoading.postValue( false)
            showToast.value = app.getString(R.string.reminder_saved)
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
     fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.postValue(R.string.err_enter_title)
            return false
        }

        if (reminderData.description.isNullOrEmpty()) {
            showSnackBarInt.postValue(R.string.err_enter_description)
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {

            showSnackBarInt.postValue(R.string.err_select_location)
            return false
        }
        return true
    }


}