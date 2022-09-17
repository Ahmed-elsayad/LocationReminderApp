package com.udacity.project4.utils

import android.content.Context
import android.view.View
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.IdlingResource

import android.widget.Toast




/**
 * A singleton class that contains the idle resources
 * it tracks whether long running tasks are still working
 *
 * make a singleton so you can use it anywhere in the code
 */
object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)
    // counter > 0 the app is considered working
    // counter <= 0 the app is considered idle

    fun increment() { // provide access to outside world
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        function() // do the work, runs whatever code it's wrapped around
    } finally {
        EspressoIdlingResource.decrement() // Set app as idle as the work is done
    }
}

// approach taken from  https://stackoverflow.com/a/32023568/
object ToastManager {
    val idlingResource = CountingIdlingResource("toast")
    private val listener: View.OnAttachStateChangeListener =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                idlingResource.increment()
            }

            override fun onViewDetachedFromWindow(v: View?) {
                idlingResource.decrement()
            }
        }

    fun makeText(context: Context?, text: CharSequence?, duration: Int): Toast {
        val t = Toast.makeText(context, text, duration)
        t.view?.addOnAttachStateChangeListener(listener)
        return t
    }

    // For testing
    fun getIdlingResource(): IdlingResource {
        return idlingResource
    }
}
