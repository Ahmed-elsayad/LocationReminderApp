package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.not
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
// testing ReminderListFragment by the help of Koin as service locator
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        // use Koin Library as a service locator
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
    //testing the ReminderList to check if it shows the reminders correctly
    @Test
    fun reminderList_DisplayedInUI() = runTest {
        val reminderDTO = ReminderDTO("T", "D", "L", 0.0, 0.0)

        repository.saveReminder(reminderDTO)

        val fragmentScenario = FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            themeResId = R.style.AppTheme
        )
        idlingRResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))

        onView(withText(reminderDTO.title)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.description)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.location)).check(matches(isDisplayed()))

        fragmentScenario.close()
    }

  //testing the clickFab and we expect to navigate to the Reminder Fragment
    @Test
    fun onFabClick_navigate() {
        val fragmentScenario = FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            themeResId = R.style.AppTheme
        )
        idlingRResource.monitorFragment(fragmentScenario)

        val navController = mock(NavController::class.java)

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }
         // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

      // THEN - Verify that we navigate to the save reminder fragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )

        fragmentScenario.close()
    }
}