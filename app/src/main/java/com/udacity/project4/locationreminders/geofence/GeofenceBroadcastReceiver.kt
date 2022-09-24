package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.udacity.project4.locationreminders.geofence.GeofenceTransitionsJobIntentService.Companion.enqueueWork
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

//COMPLETED: implement the onReceive method to receive the geofencing events at the background
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            enqueueWork(context, intent)
        }

    }
}


internal object GeofencingConstants {
    const val GEOFENCE_RADIUS_IN_METERS = 500f
    const val MAX_GEOFENCES = 100 //  limit of 100  https://developer.android.com/training/location/geofencing
}