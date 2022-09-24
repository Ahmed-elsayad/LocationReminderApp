package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminder.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //COMPLETED: provide testing to the SaveReminderView and its live data objects
    private lateinit var  saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var testReminder : ReminderDataItem

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    //Executes each task synchronously using Architecture components
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupSaveReminderViewModel(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
        testReminder = ReminderDataItem("test title","test description","test location",11.1,22.2)
    }

    @Test
    fun testGetReminderTitle() = runBlocking {
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.saveReminder(testReminder)
        assertThat(saveReminderViewModel.reminderTitle.value, `is`(testReminder.title))
    }

    @Test
    fun testGetReminderDescription() = runBlocking {
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.saveReminder(testReminder)
        assertThat(saveReminderViewModel.reminderDescription.value, `is`(testReminder.description))
    }

    @Test
    fun testGetReminderSelectedLocationStr() = runBlocking {
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.saveReminder(testReminder)
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`(testReminder.location))
    }

    @Test
    fun testGetSelectedPOI(): Unit = runBlocking {
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.saveReminder(testReminder)
        val poi = PointOfInterest(LatLng(1.1,2.2) ,"1", "Test pin")

        saveReminderViewModel.selectedPOI.postValue(poi)
        assertThat(saveReminderViewModel.selectedPOI.value, `is`(notNullValue()))
        saveReminderViewModel.selectedPOI.value?.let {
            assertThat(it.latLng, `is`(poi.latLng))
            assertThat(it.placeId, `is`(poi.placeId))
            assertThat(it.name, `is`(poi.name))
        }
    }

    @Test
    fun testGetLatitude() = runBlocking {
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.saveReminder(testReminder)
        assertThat(saveReminderViewModel.latitude.value, `is`(testReminder.latitude))
    }

    @Test
    fun testGetLongitude() = runBlocking {
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.saveReminder(testReminder)
        assertThat(saveReminderViewModel.longitude.value, `is`(testReminder.longitude))

    }

    @Test
    fun testOnClear() = runBlocking {
        saveReminderViewModel.saveReminder(testReminder)
        saveReminderViewModel.onClear()
        assertThat(saveReminderViewModel.reminderTitle.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.value, `is`(nullValue()))
    }

    fun testValidateAndSaveReminder() = runBlocking {
        val emptyTitleReminder = ReminderDataItem("","test description","test location",11.1,22.2)
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.validateAndSaveReminder(emptyTitleReminder)
        var result = fakeDataSource.getReminders()
        assertThat(result, `is`(Result.Error( InstrumentationRegistry.getInstrumentation().context.getString(
            R.string.no_reminders))))

        val emptyDescriptionReminder = ReminderDataItem("test title","","test location",11.1,22.2)
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.validateAndSaveReminder(emptyDescriptionReminder)
        result = fakeDataSource.getReminders()
        assertThat(result, `is`(Result.Error( InstrumentationRegistry.getInstrumentation().context.getString(
            R.string.no_reminders))))
    }

    fun testSaveReminder() = runBlocking {
        fakeDataSource.deleteAllReminders()
        saveReminderViewModel.saveReminder(testReminder)
        val reminder = fakeDataSource.getFirstReminder()
        assertThat(reminder.title, `is`(testReminder.title))
        assertThat(reminder.description,`is`(testReminder.description))
        assertThat(reminder.location,`is`(testReminder.location))
        assertThat(reminder.latitude,`is`(testReminder.latitude))
        assertThat(reminder.location,`is`(testReminder.location))
    }

    fun testValidateEnteredData() = runBlocking{
        val reminder = ReminderDataItem("","test description","test location",11.1,22.2)
        assertThat(saveReminderViewModel.validateEnteredData(reminder),`is`(false))     // title is empty

        reminder.title = "test title"
        reminder.description = ""
        assertThat(saveReminderViewModel.validateEnteredData(reminder),`is`(false))     // description is empty

        reminder.description = "test description"
        reminder.location = ""
        assertThat(saveReminderViewModel.validateEnteredData(reminder),`is`(false))     // location is empty

        reminder.location = "test location"
        assertThat(saveReminderViewModel.validateEnteredData(reminder),`is`(true))      // no empty fields
    }

    @Test
    fun testShowLoading(){
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(testReminder)
        assertThat(saveReminderViewModel.showLoading.value, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.value, `is`(false))
    }



}