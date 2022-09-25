package com.udacity.project4.locationreminders.reminderslist

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.locationreminder.MainCoroutineRule
import com.udacity.project4.locationreminder.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var remindersRepository: FakeDataSource

    //Subject under test
    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_showLoading() {
        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()

        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()

    }

    @Test
    fun loadReminders_remainderListNotEmpty() = mainCoroutineRule.runBlockingTest  {
        val reminder = ReminderDTO("o2re", "uff", "Abuja", 6.454202, 7.599545)

        remindersRepository.saveReminder(reminder)
        viewModel.loadReminders()

        assertThat(viewModel.remindersList.getOrAwaitValue(),notNullValue())
    }

    @Test
    fun loadReminders_updateSnackBarValue() {
        mainCoroutineRule.pauseDispatcher()

        remindersRepository.setReturnError(true)

        viewModel.loadReminders()

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error getting reminders")
    }
}