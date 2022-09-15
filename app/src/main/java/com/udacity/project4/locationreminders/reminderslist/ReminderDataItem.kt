package com.udacity.project4.locationreminders.reminderslist

import com.udacity.project4.base.BaseDataClass
import java.io.Serializable
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
data class ReminderDataItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    override val id: String = UUID.randomUUID().toString()
) : Serializable, BaseDataClass()