package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 *
 * And as IntentService and JobIntentService Deprecated
 * So do that we can use standard service to launch a work manager as the work manager can only access
 * the location from the background
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val dataSource: ReminderDataSource by inject(ReminderDataSource::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: run {
                Log.e(TAG, "Event is null")
                return
            }

            if (geofencingEvent.hasError()) {
                Log.e(TAG, "Error Code: ${geofencingEvent.errorCode}")
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                // i don't understand why the instructions said to notify about the only one ?!!
                geofencingEvent.triggeringGeofences?.forEach {
                    scope.launch {
                        when (val reminderDTO = dataSource.getReminder(it.requestId)) {
                            is Result.Error -> Log.e(TAG, reminderDTO.message ?: "No Message")
                            is Result.Success -> sendNotification(
                                context,
                                reminderDTO.data.toDataItem()
                            )
                        }
                    }
                } ?: run {
                    Log.e(TAG, "No Geofence Trigger Found")
                }
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiv"
        const val ACTION_GEOFENCE_EVENT = "com.udacity.project4.geofence_event"
    }
}