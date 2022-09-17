package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.hamcrest.Matchers.*
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
import org.mockito.Mockito.*


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderViewModelTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @get:Rule
    var activityScenarioRule: ActivityScenarioRule<RemindersActivity> =
        ActivityScenarioRule(
            RemindersActivity::class.java
        )

    private lateinit var decorView: View

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun getDecorView(){
        activityScenarioRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    @Before
    fun setUp() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
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
            single {
                val dataSource: ReminderDataSource = RemindersLocalRepository(get())
                dataSource // required cast
            }
            single { LocalDB.createTestRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val idlingRResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().run {
            register(idlingRResource)
            register(EspressoIdlingResource.countingIdlingResource)
        }
    }

    @After
    fun unRegisterIdlingResource() {
        IdlingRegistry.getInstance().run {
            unregister(idlingRResource)
            unregister(EspressoIdlingResource.countingIdlingResource)
        }
    }

    @Test
    fun noDataEntered_Fail_noNavigate() {
        val navController = mock(NavController::class.java)
        val fragmentScenario =
            FragmentScenario.launchInContainer(
                SaveReminderFragment::class.java,
                themeResId = R.style.AppTheme
            )

        idlingRResource.monitorFragment(fragmentScenario)

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        onView(withId(R.id.reminderTitle)).perform(clearText())
        onView(withId(R.id.reminderDescription)).perform(clearText())

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        verifyNoInteractions(navController)

        fragmentScenario.close()
    }

    @Test
    fun validData_Success_Navigate() = runTest {
        val navController = mock(NavController::class.java)
        val fragmentScenario =
            FragmentScenario.launchInContainer(
                SaveReminderFragment::class.java,
                themeResId = R.style.AppTheme
            )

        idlingRResource.monitorFragment(fragmentScenario)

        val reminder = ReminderDataItem("T", "D", "L", 0.0, 0.0)

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            with(it._viewModel) {
                // simulate choosing location
                reminderSelectedLocationStr.value = reminder.location
                latitude.value = reminder.latitude
                longitude.value = reminder.longitude
            }
        }

        onView(withId(R.id.reminderTitle)).perform(typeText(reminder.title))
        onView(withId(R.id.reminderDescription)).perform(typeText(reminder.description))

        Espresso.closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())

        val toastString = appContext.getString(R.string.reminder_saved)

        fragmentScenario.onFragment{
            assertThat(it._viewModel.showToast.getOrAwaitValue(), equalTo(toastString))
        }

        // source: https://stackoverflow.com/a/54127456/15262615
        onView(withText(toastString))
            .inRoot(withDecorView(not(decorView)))// Here we use decorView
            .check(matches(isDisplayed()))

        verify(navController).popBackStack()

        fragmentScenario.close()
    }
}