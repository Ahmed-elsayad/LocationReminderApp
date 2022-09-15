package com.udacity.project4.locationreminders.geofence

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceTransitionsService : Service() {

    companion object {
        private const val TAG = "GeofenceTransitionsJobI"
        const val ACTION_GEOFENCE_EVENT = "com.udacity.project4.geofence_event_background"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (intent.action != ACTION_GEOFENCE_EVENT) {
                Log.d(TAG, "Not My Action")
                return@let
            }

            val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: run {
                Log.e(TAG, "Event is null")
                stopSelf()
                return@let
            }

            if (geofencingEvent.hasError()) {
                Log.e(TAG, "Error Code: ${geofencingEvent.errorCode}")
                return@let
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                val geofencesIDs =
                    geofencingEvent.triggeringGeofences?.map { it.requestId }?.toTypedArray()
                        ?: run {
                            Log.e(TAG, "Ids is null")
                            return@let
                        }

                val inputData = Data.Builder()
                    .putStringArray(GeofenceNotifyWorker.KEY_REQUEST_IDS, geofencesIDs)
                    .build()

                WorkManager.getInstance(applicationContext).enqueue(
                    OneTimeWorkRequestBuilder<GeofenceNotifyWorker>()
                        .setInputData(inputData)
                        .build()
                )
            }
        } ?: Log.e(TAG, "Intent is null")


        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
