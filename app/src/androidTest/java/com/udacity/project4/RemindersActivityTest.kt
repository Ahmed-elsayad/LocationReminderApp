package com.udacity.project4

import android.Manifest
import android.app.Activity
import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.permission.PermissionRequester
import com.google.android.material.internal.ContextUtils
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var mRuntimePermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun init() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        repository = get()

        runBlocking {
            repository.deleteAllReminders()
        }


        PermissionRequester().apply { addPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) }
    }

    @After
    fun finalize(){
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm revoke ${appContext.packageName} android.permission.ACCESS_BACKGROUND_LOCATION")
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm revoke ${appContext.packageName} android.permission.ACCESS_FINE_LOCATION")
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("pm revoke ${appContext.packageName} android.permission.ACCESS_COARSE_LOCATION")
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }




    @Test
    fun ui_Test() = runBlocking {
        // repository.saveReminder(ReminderDTO("Test 1","Description 1", "Here", 12.4,24.2))
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        val decorView = ContextUtils.getActivity(appContext)?.window?.decorView

        dataBindingIdlingResource.monitorActivity(activityScenario)

        val title = "test title"
        val description = "test description"

        // click the FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // check if a snackbar is displayed if the user tries to save the reminder before typing the title
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        // Fill in the title
        onView(withId(R.id.reminderTitle)).perform(typeText(title))
        Espresso.closeSoftKeyboard()

        // check if a snackbar is displayed if the user tries to save the reminder before typing the descriptiom
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_enter_description)))

        // Fill in the description
        onView(withId(R.id.reminderDescription)).perform(typeText(description))
        Espresso.closeSoftKeyboard()

        // check if a snackbar is displayed if the user tries to save the reminder before setting the location
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.err_select_location)))


        // open the map
        onView(withId(R.id.selectLocation)).perform(click())

        // Click on the map and return to the previous view
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.save_location)).perform(click())

        // verify that the viewmodel is bound correctly. Also verify that a location is selected
        onView(withId(R.id.reminderTitle)).check(matches(withText(title)))
        onView(withId(R.id.reminderDescription)).check(matches(withText(description)))
        onView(withId(R.id.selectLocation)).check(matches(not(withText(""))))
        onView(withId(R.id.saveReminder)).perform(click())

        // check if the toast and the snackbar are displayed
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.geofence_added)))
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(decorView)))).check(matches(isDisplayed()))

        // verify the displayed data and open the reminder description view`
        onView(withText(title)).check(matches(withId(R.id.title)))
        onView(withText(description)).check(matches(withId(R.id.description)))
        onView(withId(R.id.remindersRecyclerView)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        // verify the view text
        onView(withId(R.id.reminder_title)).check(matches(withText(title)))
        onView(withId(R.id.reminder_description)).check(matches(withText(description)))
        onView(withId(R.id.reminder_latitude)).check(matches(not(withText(""))))
        onView(withId(R.id.reminder_longitude)).check(matches(not(withText(""))))
        onView(withId(R.id.reminder_location)).check(matches(not(withText(""))))

        // return to the main screen
        Espresso.pressBackUnconditionally()
        activityScenario.close()
    }




//    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
//        lateinit var activity: Activity
//        activityScenario.onActivity {
//            activity = it
//        }
//        return activity
//    }
}