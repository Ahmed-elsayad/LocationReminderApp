package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRemindersBinding.inflate(
                inflater, container, false
            )

        with(binding) {
            viewModel = _viewModel
            lifecycleOwner = viewLifecycleOwner

            refreshLayout.setOnRefreshListener {
                _viewModel.loadReminders {
                    refreshLayout.isRefreshing = false
                }
            }

            addReminderFAB.setOnClickListener {
                navigateToAddReminder()
            }

            setupRecyclerView()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun FragmentRemindersBinding.setupRecyclerView() {
        val adapter = RemindersListAdapter { reminderDataItem ->
            ReminderDescriptionActivity.newIntent(
                requireContext(), reminderDataItem
            ).also { startActivity(it) }
        }

        // setup the recycler view using the extension function
        remindersRecyclerView.setup(adapter)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // display logout as menu item
        menuInflater.inflate(R.menu.main_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.logout -> {
                AuthUI.getInstance().signOut(requireContext())
            }
        }
        return false
    }
}
