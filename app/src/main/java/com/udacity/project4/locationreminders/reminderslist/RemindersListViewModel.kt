package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.*
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.Result.Success
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<Result<List<ReminderDataItem>>>()

    val error: LiveData<Boolean> = remindersList.map { it is Result.Error }

    val empty: LiveData<Boolean> = remindersList.map { ((it as? Result.Success)?.data.isNullOrEmpty())}

    override val showNoData = MediatorLiveData<Boolean>().apply {
        addSource(error){errorValue ->
            empty.value?.let {emptyValue ->
                value =  errorValue || emptyValue
            }?:run {
                value = true
            }
        }

        addSource(empty){emptyValue ->
            empty.value?.let {  errorValue ->
                value = errorValue || emptyValue
            }?:run {
                value = true
            }
        }
    }

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    @JvmOverloads // required in order to support optional parameter
    fun loadReminders(showLoadingUI:Boolean = false) {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            when (result) {
                is Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    @Suppress("UNCHECKED_CAST")
                    dataList.addAll((result.data as MutableList<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    remindersList.value = Success(dataList)
                }
                is Result.Error -> {
                    showSnackBar.value = result.message!!
                    remindersList.postValue(result)
                }
            }

            showLoading.value = showLoadingUI
            // check if no data has to be shown
            // invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    //  private fun invalidateShowNoData() {
    //      showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    // }



    suspend fun getReminder(id: String) : Result<ReminderDataItem>{
         when (val result = dataSource.getReminder(id)) {
            is Success<ReminderDTO> -> {
                val reminder = result.data
                return Success(
                    ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                )
            }
            is Result.Error ->
            {
                showSnackBar.value = result.message!!
                return result
            }
        }
    }

    suspend fun deleteAllReminders(){
        if (remindersList.value !is Result.Error){
            ((remindersList.value as Success).data as ArrayList<ReminderDataItem>).clear()
        }
        dataSource.deleteAllReminders()
        // invalidateShowNoData()
    }

}