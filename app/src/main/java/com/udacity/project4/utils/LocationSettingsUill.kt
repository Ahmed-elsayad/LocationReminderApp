package com.udacity.project4.utils

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.internal.RegistrationMethods
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

sealed class LocationSettingsState {
    object ON : LocationSettingsState()
    data class OFF(val exception: Exception) : LocationSettingsState()
}

fun Activity.locationStateFlow() = callbackFlow {

    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(this@locationStateFlow)
    val locationSettingsResponseTask =
        settingsClient.checkLocationSettings(builder.build())
    locationSettingsResponseTask.addOnCompleteListener { task ->
        trySend(
            if (task.isSuccessful) LocationSettingsState.ON
            else LocationSettingsState.OFF(task.exception!!)
        )
    }

    awaitClose()
}