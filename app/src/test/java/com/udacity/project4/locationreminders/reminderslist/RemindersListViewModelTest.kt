package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
//testing SaveReminderViewModel with fakeDataSource
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Set the main coroutines dispatcher for unit testing.

/*
* Rules defined by fields will always be applied after
* Rules defined by methods, i.e. the Statements returned by
* the former will be executed around those returned by the latter.
* */
    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
// the class under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake data source to be injected into the viewModel
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setUp() {
        stopKoin()
        val context = ApplicationProvider.getApplicationContext<Application>()
        dataSource = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
            context,
            dataSource
        )
    }
   //  test deleting all reminders and then we try to load the reminders from our View Model
   //      We  testing remindersList
    @Test
    fun loadReminders_Empty() = runTest {
       //GIVEN - Empty DB
       dataSource.deleteAllReminders()
       //then - Try to load Reminders
        remindersListViewModel.loadReminders()

       //THEN - We expect that our reminder list is empty
        assertThat(remindersListViewModel.remindersList.value, equalTo(null))
    }

// testing that we can insert into database and the retreieve from data base and reminderList not equal to null
    @Test
    fun loadReminders_NotNull( ) = runTest {
        dataSource.saveReminder(ReminderDTO("Title", "Desc", "loc", null, null))
        remindersListViewModel.loadReminders()

        // run all pending coroutines
        runCurrent()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), not(equalTo(null)))
    }
   //Here in this test we testing showing an Error
    @Test
    fun shouldReturnError() = runTest {
        //GIVEN - Set should return error to "true"
        dataSource.makeErrorWhileGetReminders = true
      //WHEN - We load Reminders
        remindersListViewModel.loadReminders()

        // run all pending coroutines
        runCurrent()
       //THEN - We get showSnackBar in the view model giving us "test exception"
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), equalTo("Test exception"))
    }

    //Here in this test we testing showing reminders not found
    @Test
    fun dataNotFound() = runTest {
        //when reminders are null
        dataSource.makeRemindersNull()
        //loading  reminders
        remindersListViewModel.loadReminders()
        // run all pending coroutines
        runCurrent()
        //THEN - We get showSnackBar in the view model giving us "Reminders not found"
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), equalTo("Reminders not found"))
    }

    // this function we test check Loading
    @Test
    fun checkLoading() = runTest {
//        Moves the virtual clock of this dispatcher forward by the specified amount,
//        running the scheduled tasks in the meantime.
        advanceTimeBy(0)
        // when loading
        remindersListViewModel.loadReminders()

        // THEN -  loading indicator is shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), equalTo(true))
        //Run any tasks that are pending
        runCurrent()
        // Then viewModel's livedata is back to false
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), equalTo(false))
    }
}