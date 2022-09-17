package com.udacity.project4.locationreminders.data.local

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var remindersDao: RemindersDao

    private val reminder = ReminderDTO(
        "Title", "Desc", "Loc", 0.0, 0.0
    )

    @Before
    fun setUp() = runTest {
        remindersDao = LocalDB.createTestRemindersDao(ApplicationProvider.getApplicationContext())

        remindersDao.saveReminder(reminder)
    }

    @After
    fun tearDown() = runTest {
        remindersDao.deleteAllReminders()
    }

    @Test
    fun getReminders_Success() = runTest {
        val returnedReminders = remindersDao.getReminders()

        assertThat(returnedReminders, hasItem(reminder))
    }

    @Test
    fun getReminders_Error() = runTest {
        remindersDao.deleteAllReminders()

        val returnedReminders = remindersDao.getReminders()

        assertThat(returnedReminders.size, equalTo(0))
    }

    @Test
    fun getReminderByID_Success() = runTest {
        val returnedReminder = remindersDao.getReminderById(reminder.id)

        assertThat(returnedReminder, equalTo(reminder))
    }

    @Test
    fun getReminderById_Error() = runTest {
        remindersDao.deleteAllReminders()

        val returnedReminder = remindersDao.getReminderById(reminder.id)

        assertThat(returnedReminder, equalTo(null))
    }

    @Test
    fun saveReminder() = runTest {
        remindersDao.saveReminder(reminder)

        val returnReminder = remindersDao.getReminderById(reminder.id)

        assertThat(returnReminder, equalTo(reminder))
    }
}