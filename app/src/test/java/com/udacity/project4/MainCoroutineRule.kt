package com.udacity.project4

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description


/*
* Marks declarations that are still experimental in coroutines API,
*  which means that the design of the corresponding declarations has open
* issues which may (or may not) lead to their changes in the future.
* */
@ExperimentalCoroutinesApi
class MainCoroutineRule(private val dispatcher: TestDispatcher = StandardTestDispatcher()):
TestWatcher(){

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        dispatcher.cancel()
        Dispatchers.resetMain()
    }
}