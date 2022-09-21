package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.Constants
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ReminderDescriptionActi"

        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityReminderDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (val reminder = intent.getSerializableExtra(EXTRA_ReminderDataItem)) {
            is ReminderDataItem -> {
                binding.reminderDataItem = reminder
                (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
                    .run {
                    this.getMapAsync { map ->
                        Log.d(TAG, "map is ready")
                        // safe !! as we only save the not null reminder locations
                        val position = LatLng(reminder.latitude!!, reminder.longitude!!)

                        map.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.builder()
                                    .target(position)
                                    .zoom(16f)
                                    .build()
                            )
                        )
                        map.addMarker(
                            MarkerOptions().position(position)
                        )
                        map.addCircle(
                            CircleOptions()
                                .center(position)
                                .radius(Constants.REMINDER_LOCATION_CIRCLE_RADIUS.toDouble())
                        )
                    }
                }
            }
            else -> {
                Log.e(TAG, "Wrong Serializable Passed")
            }
        }
    }
}
