package com.udacity.project4.utils

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.udacity.project4.base.BaseRecyclerViewAdapter
import com.udacity.project4.locationreminders.data.dto.Result

object BindingAdapters {

    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, items: MutableLiveData<Result<List<T>>>?) {
        items?.value?.let {
            if (it !is Error) {
                (it as Result.Success<List<T>>).data.let { itemList ->
                    (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                        clear()
                        addData(itemList)
                    }
                }
            }
        }

    }

    /**
     * Use this binding adapter to show and hide the views using boolean variables
     */
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE)
                    view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.fadeOut()
            }
        }
    }
}