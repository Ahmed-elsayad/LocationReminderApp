package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import com.udacity.project4.R
import com.udacity.project4.locationreminder.MainCoroutineRule
import com.udacity.project4.locationreminder.getOrAwaitValue

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //COMPLETED: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var  remindersListViewModel : RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource
    val targetContext = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupRemindersListViewModel() {
        stopKoin()
        // We initialise the repository with no reminders
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }

    @Test
    fun loadReminders_loading(){
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.value, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.value, `is`(false))
    }

    @Test
    fun loadReminders() = runBlocking{
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.value, `is` (not(nullValue())))
    }


    @Test
    fun getReminder() = runBlocking {
        remindersListViewModel.loadReminders()
        val reminder = fakeDataSource.getFirstReminder()
        assertThat(remindersListViewModel.getReminder(reminder.id), `is`(not(nullValue())))     // verify that getReminder() works as expected

    }

    @Test
    fun deleteReminder() = runBlocking{
        remindersListViewModel.loadReminders()
        val reminder = fakeDataSource.getFirstReminder()
        val returnedReminder = remindersListViewModel.getReminder(reminder.id)
        assertThat(returnedReminder, instanceOf(Result::class.java))       // verify the type of the result
        //assertThat(returnedReminder.)// verify that the reminder is deleted
    }

    @Test
    fun loadRmindersWhenUnavailable_callErrorToDisplay() = runBlocking{
        // Make the repository return errors.
        mainCoroutineRule.pauseDispatcher()
        fakeDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()
        mainCoroutineRule.resumeDispatcher()

        // Then empty and error are true (which triggers an error message to be shown).
        assertThat(remindersListViewModel.error.getOrAwaitValue(), `is`(true))
        assertThat(remindersListViewModel.empty.getOrAwaitValue(), `is`(true))
        assertThat(remindersListViewModel.remindersList.value , instanceOf(Result::class.java))
        assertThat(remindersListViewModel.remindersList.value , instanceOf(Result.Error::class.java))
        assertThat((remindersListViewModel.remindersList.value as Result.Error).message,`is`(targetContext.getString(R.string.getreminders_test_exception)))
    }

    @Test
    fun loadRminderWhenUnavailable_callErrorToDisplay() = runBlocking{
        // Make the repository return errors.
        mainCoroutineRule.pauseDispatcher()
        fakeDataSource.setReturnError(true)
        val result = remindersListViewModel.getReminder("unavailable")
        mainCoroutineRule.resumeDispatcher()

        // Then empty and error are true (which triggers an error message to be shown).
        assertThat(result, instanceOf(Result::class.java))
        assertThat(result, instanceOf(Result.Error::class.java))
        assertThat((result as Result.Error).message,`is`(targetContext.getString(R.string.getreminder_test_exception)))
    }

    @Test
    fun check_loading() = runBlocking {
        remindersListViewModel.loadReminders(true)
        assertThat(remindersListViewModel.showLoading.value, `is`(true))
    }
}