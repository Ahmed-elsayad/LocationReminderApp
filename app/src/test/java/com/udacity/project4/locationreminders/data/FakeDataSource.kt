package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource

class FakeDataSource : ReminderDataSource {

  // this fake data source with our local db

    // this reminder mutableList Acting as our database table that we will insert and retrieve and delete data from it
    private var reminders: MutableList<ReminderDTO>? = mutableListOf()

    var makeErrorWhileGetReminders = false
   // making our fake  database empty
    fun makeRemindersNull() {
        reminders = null
    }

  // retrieve  all fake reminders inserted to our fake db
    //
    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if (makeErrorWhileGetReminders) {
            return Result.Empty("Test exception")
        }
      //when retrieve reminder from db by its id
        reminders?.let { return Result.Success(ArrayList(it)) }
      // when trying  get data from empty db
        return Result.Empty("Reminders not found")
    }
// insert fake reminder to database
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }
// getting fake reminder its id from the fake db
    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if (makeErrorWhileGetReminders) {
            return Result.Empty("Test exception")
        } else {
            reminders?.firstOrNull { it.id == id }.also {
                return when(it) {
                    //return null
                    null -> Result.Empty("Not Found")
                    // when the inserted id equal the the item returned
                    else -> Result.Success(it)
                }
            }
        }
    }
// clearing our fake db
    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}