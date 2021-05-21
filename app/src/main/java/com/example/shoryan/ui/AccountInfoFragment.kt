package com.example.shoryan.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.data.BirthDate
import com.example.shoryan.data.ProfileResponse
import com.example.shoryan.databinding.FragmentAccountInfoBinding
import com.example.shoryan.getStringWithoutAdditionalSpaces
import com.example.shoryan.interfaces.LoadingFragmentHolder
import com.example.shoryan.repos.ProfileRepo
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.viewmodels.AccountInfoViewModel
import com.example.shoryan.viewmodels.LocationPickerViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*



class AccountInfoFragment : Fragment(), LoadingFragmentHolder {

    private lateinit var navController: NavController
    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationPickerViewModel: LocationPickerViewModel
    private val accountInfoViewModel: AccountInfoViewModel by navGraphViewModels(R.id.main_nav_graph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLocationPickerViewModel()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        _binding!!.viewmodel = accountInfoViewModel
        _binding!!.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Getting the navigation controller object
        navController = Navigation.findNavController(view)

        // Go to profile screen when the back button is clicked
        binding.backButton.setOnClickListener {
            navController.navigateUp()
        }

        // Setting a click listener for the birthDatePicker button
        binding.birthDatePicker.setOnClickListener{
            pickBirthDate()
        }

        // Registering the bloodTypePicker TextView for Context Menu
        registerForContextMenu(binding.bloodTypePicker)
        binding.bloodTypePicker.setOnClickListener {
            it.showContextMenu()
        }

        // Registering the genderPicker textview for context menu
        registerForContextMenu(binding.genderPicker)
        binding.genderPicker.setOnClickListener {
            it.showContextMenu()
        }

        // Open the map when the change location button is clicked
        binding.openMapButton.setOnClickListener {
            openLocationPicker()
        }

        // Setting the textview of the address with the location chosen by the user
        setAddress()

        // Get user's current profile information
        getUserCurrentProfileInfo()

        // Observing the viewmodel events
        observeViewModelEvents()
    }

    /**
     * This functions fetches the current information of the profile of the user and uses it to
     * fill the initial values for the account info form
     */
    private fun getUserCurrentProfileInfo() {
        GlobalScope.launch {
                accountInfoViewModel.getUserProfileData()
        }
    }

    /**
     * Links the viewModel to the activity instead of the fragment
     */
    private fun initializeLocationPickerViewModel(){
        locationPickerViewModel = ViewModelProvider(requireActivity())
            .get(LocationPickerViewModel::class.java)
    }

    /**
     * This function opens a DatePicker dialog so that the user can select his/her birth date for
     * registration
     */
    private fun pickBirthDate(){
        val c = Calendar.getInstance()
        val currentYear = c.get(Calendar.YEAR)
        val currentMonth = c.get(Calendar.MONTH)
        val currentDay = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            requireActivity(),
            { _, year, monthOfYear, dayOfMonth ->
                accountInfoViewModel.setBirthDate(BirthDate(year, monthOfYear + 1, dayOfMonth))
            },
            currentYear,
            currentMonth,
            currentDay
        )
        dpd.show()
    }

    /**
     * ContextMenu code for selecting the appropriate blood type
     */
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if(v == binding.bloodTypePicker) {
            showBloodTypesList(menu, v)
        }
        else if(v == binding.genderPicker){
            showGenderList(menu, v)
        }

    }

    private fun showBloodTypesList(menu: ContextMenu, v: View) {
        val bloodTypesList = resources.getStringArray(R.array.blood_types)
        menu.setHeaderTitle("اختر فصيلة الدم")
        for(i in bloodTypesList.indices)
            menu.add(0, v.id, i, bloodTypesList[i].toString())
    }

    private fun showGenderList(menu: ContextMenu, v: View){
        val gendersList = resources.getStringArray(R.array.gender)
        menu.setHeaderTitle("اختر النوع")
        for(i in gendersList.indices)
            menu.add(1, v.id, i, gendersList[i].toString())
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if(item.groupId == 0){
            accountInfoViewModel.setBloodType(item.title.toString())
        }
        else if(item.groupId == 1){
            accountInfoViewModel.setGender(item.title.toString())
        }
        return true
    }

    private fun openLocationPicker(){
        navController.navigate(R.id.action_accountInfoFragment_to_mapFragment2)
    }

    private fun setAddress(){
        locationPickerViewModel.locationStringLiveData.observe(viewLifecycleOwner){
            accountInfoViewModel.setAddress(locationPickerViewModel.getLocation())
            binding.addressEditText.setText(it)
        }
    }

    private fun observeViewModelEvents(){
        accountInfoViewModel.eventsFlow.onEach {
            when(it){
                is AccountInfoViewModel.RegistrationViewEvent.ShowSnackBarFromString -> showSnackbar(it.text)
                is AccountInfoViewModel.RegistrationViewEvent.ShowSnackBarFromResource -> showSnackbar(resources.getString(it.textResourceId))
                AccountInfoViewModel.RegistrationViewEvent.ToggleLoadingIndicator -> toggleLoadingIndicator()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showSnackbar(message: String){
        AndroidUtility.displaySnackbarMessage(binding.rootLayout, message, Snackbar.LENGTH_LONG)
    }

    private fun toggleLoadingIndicator(){
        val loadingFragment: DialogFragment? = childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        if(loadingFragment == null)
            LoadingFragment(this).show(childFragmentManager, "loading")
        else
            loadingFragment.dismiss()
    }

    override fun onLoadingFragmentDismissed() {
        accountInfoViewModel.cancelRegistrationProcess()
    }
}