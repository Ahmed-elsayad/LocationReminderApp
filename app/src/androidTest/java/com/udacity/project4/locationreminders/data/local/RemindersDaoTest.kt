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
       // insert reminder to db
        remindersDao.saveReminder(reminder)
    }

    @After
    fun tearDown() = runTest {
        remindersDao.deleteAllReminders()
    }
  //  we expect that we can get the item that inserted to the database
    @Test
    fun getReminders_Success() = runTest {
        // when getting reminder from database
        val returnedReminders = remindersDao.getReminders()
        // then - db must have the  reminder that we inserted
        assertThat(returnedReminders, hasItem(reminder))
    }

    // we expect that the returnedReminders is empty
    @Test
    fun getReminders_Error() = runTest {
        // clear database
        remindersDao.deleteAllReminders()
      //
        val returnedReminders = remindersDao.getReminders()

        assertThat(returnedReminders.size, equalTo(0))
    }
 // getting it with it id is success
    @Test
    fun getReminderByID_Success() = runTest {
        //when - getting an item with its id
        val returnedReminder = remindersDao.getReminderById(reminder.id)
       // then -  the loaded item hase the correct id
        assertThat(returnedReminder, equalTo(reminder))
    }
   //assert that we cant getting item by its id
    @Test
    fun getReminderById_Error() = runTest {
       //when clear data
        remindersDao.deleteAllReminders()
      // when  getting reminders by its id from empty db
        val returnedReminder = remindersDao.getReminderById(reminder.id)
    // then  return reminder must equal to null
        assertThat(returnedReminder, equalTo(null))
    }
    // testing reminder is saved
    @Test
    fun saveReminder() = runTest {
       //when insert reminder
        remindersDao.saveReminder(reminder)
   // when getting reminder py its id
        val returnReminder = remindersDao.getReminderById(reminder.id)
    // then the reminder we get  must equal to reminder we inserted
        assertThat(returnReminder, equalTo(reminder))
    }
}