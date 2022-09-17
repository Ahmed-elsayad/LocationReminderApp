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

    @Test
    fun getReminders_Success() = scope.runTest {
        val result = remindersLocalRepository.getReminders()

        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
    }

    @Test
    fun getReminders_NoData() = scope.runTest {
        remindersDao.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
        val data = (result as Result.Success<List<ReminderDTO>>).data

        assertThat(data, empty())
    }

    // ----------------------------------------//

    @Test
    fun getReminder_Success() = scope.runTest {
        val result = remindersLocalRepository.getReminder(reminderDTO.id)

        assertThat(result, `is`(instanceOf(Result.Success::class.java)))
    }

    @Test
    fun getReminder_NotFound() = scope.runTest {
        val result = remindersLocalRepository.getReminder("")

        assertThat(result, `is`(instanceOf(Result.Error::class.java)))

        val message = (result as Result.Error).message

        assertThat(message, equalTo("Reminder not found!"))
    }

    // ------------------------------------------//

    @Test
    fun saveReminder() = scope.runTest {
        remindersLocalRepository.saveReminder(reminderDTO)

        val result = remindersLocalRepository.getReminder(reminderDTO.id)

        assertThat(result, `is`(instanceOf(Result.Success::class.java)))

        val data = (result as Result.Success<ReminderDTO>).data

        assertThat(data, equalTo(reminderDTO))
    }

    @Test
    fun deleteReminders() = scope.runTest {
        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        assertThat(result, `is`(instanceOf(Result.Success::class.java)))

        val data = (result as Result.Success<List<ReminderDTO>>).data

        assertThat(data, empty())
    }
}