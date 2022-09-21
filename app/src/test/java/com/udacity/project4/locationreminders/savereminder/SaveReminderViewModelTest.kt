package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

//testing SaveReminderViewModel with fakeDataSource

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


/*
* Rules defined by fields will always be applied after
* Rules defined by methods, i.e. the Statements returned by
* the former will be executed around those returned by the latter.
* */

    // Executes each task synchronously using Architecture Components.

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCommand = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    // Use a fake data source to be injected into the SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    private fun makeReminderInvalid() = saveReminderViewModel.run {
        reminderTitle.value = ""
        reminderDescription.value = "Desc"
        reminderSelectedLocationStr.value = null
        latitude.value = 0.0
        longitude.value = 0.0
    }

    private fun makeReminderValid() = saveReminderViewModel.run {
      reminderTitle.value = "Title"
        reminderDescription.value = "Desc"
        reminderSelectedLocationStr.value = "Loc"
        latitude.value = 0.0
        longitude.value = 0.0
    }

    @Before
    fun setUp() {
        stopKoin()
        val context = ApplicationProvider.getApplicationContext<Application>()
        dataSource = FakeDataSource()

        saveReminderViewModel = SaveReminderViewModel(
            context,
            dataSource
        )
    }

    //In this function we clear all reminders Live Data and test if they all null
    @Test
    fun onClear() {
        //WHEN - call on clear

        saveReminderViewModel.onClear()
        //THEN - expect that all values egualTo null
        with(saveReminderViewModel) {
            assertThat(reminderTitle.getOrAwaitValue(), equalTo(null))
            assertThat(reminderDescription.getOrAwaitValue(), equalTo(null))
            assertThat(reminderSelectedLocationStr.getOrAwaitValue(), equalTo(null))
            assertThat(latitude.getOrAwaitValue(), equalTo(null))
            assertThat(longitude.getOrAwaitValue(), equalTo(null))
        }
    }
// testing add Reminder to Data Source via our ViewModel.saveReminder function
    @Test
    fun saveReminder_Valid() = runTest {
        makeReminderValid()
        val validReminder = saveReminderViewModel.saveReminder{
            it.description = "description"
            it.latitude = 1.0
            it.longitude= 1.0
            it.location = "location"
            it.title = "title"

        }

        // run all pending coroutines
        runCurrent()

        assertThat(dataSource.getReminder(validReminder.id), `is`(instanceOf(Result.Success::class.java)))
       // assert that navigates back to the previous fragment
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue(), equalTo(NavigationCommand.Back))
    }

    // testing validation by passing null title and null location
    // we expect showing snackBar to indicate to err_enter_title, and validate return false
    @Test
    fun shouldReturnError() {
        makeReminderInvalid()
        saveReminderViewModel.validateEnteredData()

        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), equalTo(R.string.err_enter_title))
    }

    @Test
    fun checkLoading() = runTest {
     //   Moves the virtual clock of this dispatcher forward by the specified amount, running the scheduled tasks in the meantime.
        //   Moves the virtual clock of this dispatcher forward by the specified amount, running the scheduled tasks in the meantime.
        this.advanceTimeBy(0)


        //GIVEN - reminder to be saved

        saveReminderViewModel.saveReminder{
            it.description = "description"
            it.latitude = 1.0
            it.longitude= 1.0
            it.location = "location"
            it.title = "title"
        }

        // THEN -  loading indicator is shown

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), equalTo(true))
   // running the scheduled tasks in the meantime.
        this.runCurrent()
    // THEN -  loading indicator is hidden
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), equalTo(false))

        // after loading the reminders the loading indicator should be false
    }
}