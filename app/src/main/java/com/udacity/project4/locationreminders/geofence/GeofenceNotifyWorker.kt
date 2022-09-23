package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result.Empty
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.utils.sendNotification
import org.koin.core.component.KoinComponent

class GeofenceNotifyWorker(
    private val context: Context,
    params: WorkerParameters,
    private val dataSource: ReminderDataSource
) : CoroutineWorker(
    context, params
), KoinComponent {

    companion object {
        private const val TAG = "GeofenceNotifyWorker"
        const val KEY_REQUEST_IDS = "request_ids"
    }

    override suspend fun doWork(): Result {
        val requestIdList = inputData.getStringArray(KEY_REQUEST_IDS)

        requestIdList?.forEach { requestId ->
            when (val reminderDTO = dataSource.getReminder(requestId)) {
                is Empty ->  {
                    Log.e(
                        TAG, reminderDTO.message ?: "No Message"
                    )
                    return Result.failure()
                }
                is Success -> sendNotification(
                    context,
                    reminderDTO.data.toDataItem()
                )
            }
        }?: return Result.failure()

        return Result.success()
    }
}