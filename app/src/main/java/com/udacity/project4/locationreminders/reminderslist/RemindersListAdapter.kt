package com.udacity.project4.locationreminders.reminderslist

import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter


//Use data binding to show the reminder on the item
class RemindersListAdapter(clickCallBack: (selectedReminder: ReminderDataItem) -> Unit,deleteCallBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(clickCallBack,deleteCallBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}