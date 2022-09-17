package com.udacity.project4.locationreminders.data

import androidx.annotation.VisibleForTesting
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private var reminders: MutableList<ReminderDTO>? = mutableListOf()

    var makeErrorWhileGetReminders = false

    fun makeRemindersNull() {
        reminders = null
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (makeErrorWhileGetReminders) {
            return Result.Error("Test exception")
        }
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (makeErrorWhileGetReminders) {
            return Result.Error("Test exception")
        } else {
            reminders?.firstOrNull { it.id == id }.also {
                return when(it) {
                    null -> Result.Error("Not Found")
                    else -> Result.Success(it)
                }
            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}