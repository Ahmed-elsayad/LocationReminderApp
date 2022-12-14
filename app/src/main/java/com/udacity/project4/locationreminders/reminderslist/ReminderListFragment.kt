package com.udacity.project4.locationreminders.reminderslist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.geofence.GeofencingConstants
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

      //  setHasOptionsMenu(true)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                when (menuItem.itemId) {
                    R.id.logout -> {
                        // COMPLETED: add the logout implementation
                        AuthUI.getInstance().signOut(requireContext())
                        findNavController().navigate(ReminderListFragmentDirections.actionReminderListFragmentToAuthenticationActivity())
                    }
                }
                return true
            }
        },this.viewLifecycleOwner)

        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadReminders()
            binding.refreshLayout.isRefreshing = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            //NavController(requireContext()).navigate(ReminderListFragmentDirections.toSaveReminder())
            binding.remindersRecyclerView.adapter?.let {
                if (it.itemCount <GeofencingConstants.MAX_GEOFENCES) {
                    navigateToAddReminder()
                } else {
                    Toast.makeText(requireContext(),getString(R.string.max_geofences_reached,GeofencingConstants.MAX_GEOFENCES),Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.setValue(
            NavigationCommand.To(ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter( { reminder ->
            startActivity(
                ReminderDescriptionActivity.newIntent(requireContext(),reminder)
            )
        }, {reminder ->
            runBlocking {
                LocationServices.getGeofencingClient(requireActivity()).
                removeGeofences(listOf(reminder.id)).run {
                    addOnSuccessListener {
                        _viewModel.showSnackBarInt.postValue(R.string.geofence_removed)
                    }
                    addOnFailureListener{
                        _viewModel.showErrorMessage.postValue(getString(R.string.geofence_remove_error) + it.localizedMessage)
                    }
                }
            }


            _viewModel.loadReminders()

        })
//        setup the recycler view using the extension function
            binding.remindersRecyclerView.setup(adapter)

    }


}
