package com.udacity.project4.locationreminders.data.local

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import java.io.IOException

// testing RemindersLocal Repository
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var scope: TestScope
    
    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var remindersDao: RemindersDao

    private val reminderDTO = ReminderDTO(
        "Title",
        "Desc",
        "Loc",
        0.0,
        0.0
    )

    @Before
    fun setUp() = runTest {
        dispatcher = StandardTestDispatcher()
        scope = TestScope(dispatcher)

        remindersDao = LocalDB.createTestRemindersDao(ApplicationProvider.getApplicationContext())

        remindersLocalRepository = RemindersLocalRepository(
            remindersDao,
            dispatcher
        )

        remindersDao.saveReminder(reminderDTO)
    }

    // the reminders that we get matches with the examined object
    @Test
    fun getReminders_Success() = scope.runTest {
        // when getting reminders
        val result = remindersLocalRepository.getReminders()
        // then reminders and the success must match
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
    }

    // clear reminders and getting it
    @Test
    fun getReminders_NoData() = scope.runTest {
        //clear db
        remindersDao.deleteAllReminders()
       //  getting reminders
        val result = remindersLocalRepository.getReminders()
    // the result must mach the examined object
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        val data = (result as Result.Success<List<ReminderDTO>>).data
 // and the result must be empty
        assertThat(data, empty())
    }

// gettting reminder by id and must match the examined object
    @Test
    fun getReminder_Success() = scope.runTest {
    //gettting reminder by its id
        val result = remindersLocalRepository.getReminder(reminderDTO.id)
   // assert that the result matches the examined object success
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
    }

   //when the id is null the result must be error
    @Test
    fun getReminder_NotFound() = scope.runTest {
        val result = remindersLocalRepository.getReminder("")
       // assert that the result matches the examined object error
        assertThat(result, `is`(instanceOf(Result.Error::class.java)))

        val message = (result as Result.Error).message
// the error messege must equal to (reminder not found)
        assertThat(message, equalTo("Reminder not found!"))
    }

   // insert data and assert that  this data matches the success object
    @Test
    fun saveReminder() = scope.runTest {
       //insert reminder
        remindersLocalRepository.saveReminder(reminderDTO)
   // getting reminder by id
        val result = remindersLocalRepository.getReminder(reminderDTO.id)
 // the result must match the success object
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))

        val data = (result as Result.Success<ReminderDTO>).data
 //the data must match the inserted item
        assertThat(data, equalTo(reminderDTO))
    }
// delete reminders and assert that db is empty
    @Test
    fun deleteReminders() = scope.runTest {
    // clear db
        remindersLocalRepository.deleteAllReminders()
    // get empty list
        val result = remindersLocalRepository.getReminders()
// the result must match with success object
        assertThat(result, `is`(instanceOf(Result.Success::class.java)))

        val data = (result as Result.Success<List<ReminderDTO>>).data

    //the data must be empty
        assertThat(data, empty())
    }
}