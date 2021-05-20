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
import com.example.shoryan.databinding.FragmentAccountInfoBinding
import com.example.shoryan.getStringWithoutAdditionalSpaces
import com.example.shoryan.interfaces.LoadingFragmentHolder
import com.example.shoryan.repos.TokensRefresher
import com.example.shoryan.viewmodels.LocationPickerViewModel
import com.example.shoryan.viewmodels.RegistrationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


// the fragment initialization parameters, e.g. zs
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountInfoFragment : Fragment(), LoadingFragmentHolder {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var navController: NavController
    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationPickerViewModel: LocationPickerViewModel
    private val registrationViewModel: RegistrationViewModel by navGraphViewModels(R.id.main_nav_graph)
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        initializeLocationPickerViewModel()
        initializeUserLocation()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        _binding!!.viewmodel = registrationViewModel
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

        // Setting the textview of the address with the current location of the user if it exists
        setAddress()

        // Observing the viewmodel events
        observeViewModelEvents()
    }

    /**
     * Links the viewModel to the activity instead of the fragment
     */
    private fun initializeLocationPickerViewModel(){
        locationPickerViewModel = ViewModelProvider(requireActivity())
            .get(LocationPickerViewModel::class.java)
    }

    private fun initializeUserLocation(){
        if(isNoLocationAlreadyStored()){
            askUserForLocation()
        }
    }

    private fun isNoLocationAlreadyStored() = locationPickerViewModel.getCurrentSavedAddress().isEmpty()

    private fun askUserForLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            requireActivity()
        )
        if (!isLocationPermissionGranted()){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        else {
            getUserLastKnownLocation()
        }
    }

    private fun isLocationPermissionGranted() =
        (ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED)

    // What happens when users accept or deny accessing their location
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isLocationPermissionGranted()){
                getUserLastKnownLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLastKnownLocation(){
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(requireActivity()) { location: Location? ->
            if(location != null){
                getUserAddressFromLocation(location)
            }
            else{
                Toast.makeText(requireContext(), R.string.location_failed, Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * Takes the location of the user and uses reverse geocoding to find the address.
     * This method stores both the location and the address in the viewmodel
     */
    private fun getUserAddressFromLocation(location: Location){
        val locationLatLng = LatLng(location.latitude, location.longitude)
        locationPickerViewModel.locationLatLng = locationLatLng
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val address = locationPickerViewModel.getAddressFromLatLng(
                    requireActivity(),
                    locationLatLng
                )
                address?.let {
                    locationPickerViewModel.setLocation(it)
                }
            }
        }
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
                registrationViewModel.setBirthDate(BirthDate(year, monthOfYear + 1, dayOfMonth))
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
            registrationViewModel.setBloodType(item.title.toString())
        }
        else if(item.groupId == 1){
            registrationViewModel.setGender(item.title.toString())
        }
        return true
    }

    private fun openLocationPicker(){
        navController.navigate(R.id.action_accountInfoFragment_to_mapFragment2)
    }

    private fun setAddress(){
        locationPickerViewModel.locationStringLiveData.observe(viewLifecycleOwner){
            registrationViewModel.setAddress(locationPickerViewModel.getLocation())
            binding.addressEditText.setText(it)
        }
    }

    private fun observeViewModelEvents(){
        registrationViewModel.eventsFlow.onEach {
            when(it){
                is RegistrationViewModel.RegistrationViewEvent.ShowSnackBarFromString -> showSnackbar(it.text)
                is RegistrationViewModel.RegistrationViewEvent.ShowSnackBarFromResource -> showSnackbar(resources.getString(it.textResourceId))
                RegistrationViewModel.RegistrationViewEvent.OpenSMSFragment -> goToSMSFragment()
                RegistrationViewModel.RegistrationViewEvent.ToggleLoadingIndicator -> toggleLoadingIndicator()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showSnackbar(message: String){
        AndroidUtility.displaySnackbarMessage(binding.rootLayout, message, Snackbar.LENGTH_LONG)
    }

    private fun onSuccessfulRegistration(accessToken: String, refreshToken: String){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            TokensRefresher.saveTokens(accessToken, refreshToken, requireContext())
            goToSMSFragment()
        }
    }

    private fun goToSMSFragment(){
        val phoneNumber = binding.registrationPhoneEditText.getStringWithoutAdditionalSpaces()
        val registrationBundle = bundleOf("phoneNumber" to phoneNumber,
            "registrationQuery" to registrationViewModel.createUserRegistrationQuery())
        navController.navigate(R.id.action_registrationFragment_to_SMSFragment, registrationBundle)
    }

    private fun toggleLoadingIndicator(){
        val loadingFragment: DialogFragment? = childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        if(loadingFragment == null)
            LoadingFragment(this).show(childFragmentManager, "loading")
        else
            loadingFragment.dismiss()
    }

    override fun onLoadingFragmentDismissed() {
        registrationViewModel.cancelRegistrationProcess()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountInfoFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}