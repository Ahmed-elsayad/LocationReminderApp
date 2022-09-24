package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SaveReminderFragment : BaseFragment(), ActivityCompat.OnRequestPermissionsResultCallback {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    var foregroundLocationApproved = false
    var backgroundPermissionApproved = false
    lateinit var reminderDataItem : ReminderDataItem
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(
            requireActivity(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location

            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            _viewModel.saveClicked.value = true
            saveLocationAndCreateGeofence()
        }
    }

    @SuppressLint("MissingPermission")
    private val requestBackgroundLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        )
        { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                createGeofence()
            } else {
                // Explain to the user that the featurxe is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                _viewModel.showErrorMessage.postValue(getString(R.string.geofence_background_permission_denied))
                // requireActivity().finish()
            }
        }

    private fun saveLocationAndCreateGeofence(){


        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude
        val longitude = _viewModel.longitude.value
        reminderDataItem = ReminderDataItem(title, description.value, location, latitude.value, longitude)
        if (_viewModel.validateEnteredData(reminderDataItem)){
            _viewModel.permissionDenied.value = false
           requestAccessFineLocationPermission()
        }
    }

    private fun  createGeofence(){
        //             COMPLETED: use the user entered reminder details to:
        //             1) add a geofencing request
        //             2) save the reminder to the local db

        if (_viewModel.permissionDenied.value!!) return
        if (_viewModel.saveClicked.value!!) {
            _viewModel.saveClicked.value = false
            val geofence = Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(reminderDataItem.id)
                // Set the circular region of this geofence.
                .setCircularRegion(
                    reminderDataItem.latitude!!,
                    reminderDataItem.longitude!!,
                    GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                // Create the geofence.
                .build()

            val geofencingRequest = GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                addGeofence(geofence)
            }.build()

            // some times the test failed because the geofence was created asynchronously. I used idling resource and now the test works as expected.
            EspressoIdlingResource.increment()
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                addOnSuccessListener {
                    _viewModel.saveReminder(reminderDataItem)
                    EspressoIdlingResource.decrement()
                    _viewModel.showSnackBarInt.value = R.string.geofence_added
                    _viewModel.navigationCommand.postValue(NavigationCommand.Back)
                }
                addOnFailureListener {
                    if (it is ApiException) {
                        when ((it).statusCode) {
                            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> {
                                _viewModel.showErrorMessage.value =
                                    getString(R.string.geofence_not_available)
                            }
                            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> {
                                _viewModel.showErrorMessage.value =
                                    getString(R.string.geofence_too_many_geofences)
                            }
                            GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION -> {
                                _viewModel.showErrorMessage.value =
                                    getString(R.string.geofence_insufficient_location_permissions)
                            }
                            GeofenceStatusCodes.GEOFENCE_REQUEST_TOO_FREQUENT -> {
                                _viewModel.showErrorMessage.value =
                                    getString(R.string.geofence_request_too_frequent)
                            }
                            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> {
                                _viewModel.showErrorMessage.value =
                                    getString(R.string.geofence_too_many_pending_intents)
                            }
                            else -> {
                                _viewModel.showErrorMessage.value =
                                    "${getString(R.string.geofences_not_added)}\n${it.localizedMessage}"
                            }
                        }
                    }
                    _viewModel.showErrorMessage.value =
                        "${getString(R.string.geofences_not_added)}\n${it.localizedMessage}"
                    EspressoIdlingResource.decrement()
                    _viewModel.navigationCommand.postValue(NavigationCommand.Back)
                }
            }
        }
    }

    val checkDeviceLocationSettingsAndStartGeofenceLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
         _ ->
            checkDeviceLocationSettingsAndStartGeofence(false)
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    checkDeviceLocationSettingsAndStartGeofenceLauncher.launch(IntentSenderRequest.Builder(exception.resolution).build())
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                createGeofence()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val requestFineLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        )
        { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.

            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                _viewModel.showErrorMessage.postValue(getString(R.string.permission_location_required_toast))
                _viewModel.permissionDenied.value = true
                // requireActivity().finish()
            }

            if (runningQOrLater) {
                requestBackgroundPermission()
            } else {
                checkDeviceLocationSettingsAndStartGeofence()
            }
        }

    private fun requestAccessFineLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), REQUIRED_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                if (runningQOrLater) {
                    requestBackgroundPermission()
                } else {
                    checkDeviceLocationSettingsAndStartGeofence()
                }
            }

            shouldShowRequestPermissionRationale(REQUIRED_FINE_LOCATION) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.geofence_request_access_fine_location))
                    .setPositiveButton(android.R.string.ok ) { _, _ ->
                        requestFineLocationPermissionLauncher.launch(REQUIRED_FINE_LOCATION                        )
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        _viewModel.showErrorMessage.postValue(requireContext().getString(R.string.permission_location_required_toast))
                        _viewModel.permissionDenied.value = true

                        if (runningQOrLater) {
                            requestBackgroundPermission()
                        } else {
                            checkDeviceLocationSettingsAndStartGeofence()
                        }
                    }
                    .create().show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestFineLocationPermissionLauncher.launch(REQUIRED_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val requestBackgroundPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        )
        { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                _viewModel.permissionDenied.value = true
                _viewModel.showErrorMessage.postValue(getString(R.string.geofence_background_permission_denied))
            }
            checkDeviceLocationSettingsAndStartGeofence()
        }

    private fun requestBackgroundPermission() {

        when {
            ContextCompat.checkSelfPermission(requireContext(), REQUIRED_BACKGROUND_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
                checkDeviceLocationSettingsAndStartGeofence()
            }

            shouldShowRequestPermissionRationale(REQUIRED_BACKGROUND_PERMISSION) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.permission_rationale_geofence))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        requestBackgroundPermissionLauncher.launch(
                            REQUIRED_BACKGROUND_PERMISSION
                        )
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ ->
                        _viewModel.permissionDenied.value = true
                        _viewModel.showErrorMessage.postValue(requireContext().getString(R.string.geofence_background_permission_denied))
                        checkDeviceLocationSettingsAndStartGeofence()
                    }
                    .create().show()
            }
            else -> {
                // You can directly ask for the permission.
                requestBackgroundPermissionLauncher.launch(REQUIRED_BACKGROUND_PERMISSION)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.savereminder.action.ACTION_GEOFENCE_EVENT"
        private const val REQUIRED_BACKGROUND_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        private const val REQUIRED_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val TAG = "SaveReminderFragment"
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 1

    }
}
