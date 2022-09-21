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

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {


/*
* Rules defined by fields will always be applied after
* Rules defined by methods, i.e. the Statements returned by
* the former will be executed around those returned by the latter.
* */
    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
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

    @Test
    fun loadReminders_Empty() = runTest {
        dataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.value, equalTo(null))
    }


    @Test
    fun loadReminders_NotNull( ) = runTest {
        dataSource.saveReminder(ReminderDTO("Title", "Desc", "loc", null, null))
        remindersListViewModel.loadReminders()

        // run all pending coroutines
        runCurrent()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), not(equalTo(null)))
    }

    @Test
    fun shouldReturnError() = runTest {
        dataSource.makeErrorWhileGetReminders = true

        remindersListViewModel.loadReminders()

        // run all pending coroutines
        runCurrent()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), equalTo("Test exception"))
    }

    @Test
    fun dataNotFound() = runTest {
        dataSource.makeRemindersNull()
        remindersListViewModel.loadReminders()
        // run all pending coroutines
        runCurrent()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), equalTo("Reminders not found"))
    }

    @Test
    fun checkLoading() = runTest {
        advanceTimeBy(0)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), equalTo(true))

        runCurrent()
        // Then viewModel's livedata is back to false

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), equalTo(false))
    }
}