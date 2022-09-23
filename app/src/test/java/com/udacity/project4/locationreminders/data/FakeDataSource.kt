package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result



class FakeDataSource(var tasks: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    var makeErrorWhileGetReminders = false

    fun setReturnError(value: Boolean) {
        makeErrorWhileGetReminders = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (makeErrorWhileGetReminders) {
            return Result.Error("Error getting reminders")        }
        return Result.Success(ArrayList(tasks))

        //tasks?.let { return Result.Success(it) }
        //return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        tasks?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = tasks?.find { reminderDTO ->
            reminderDTO.id == id
        }

        return when {
            makeErrorWhileGetReminders -> {
                Result.Error("Reminder not found!")
            }

            reminder != null -> {
                Result.Success(reminder)
            }
            else -> {
                Result.Error("Reminder not found!")
            }
        }
    }

    override suspend fun deleteAllReminders() {
        tasks?.clear()
    }
}