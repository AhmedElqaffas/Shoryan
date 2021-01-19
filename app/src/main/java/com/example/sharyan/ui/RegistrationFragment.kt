package com.example.sharyan.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.sharyan.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class RegistrationFragment : Fragment(){

    private lateinit var navController: NavController
    private lateinit var locationPickerViewModel: LocationPickerViewModel
    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askUserForLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instantiateNavController(view)
        initializeLocationPickerViewModel()
        // Setting a click listener for the birthDatePicker button
        birthDatePicker.setOnClickListener{
            pickBirthDate()
        }

        // Registering the bloodTypePicker TextView for Context Menu
        registerForContextMenu(bloodTypePicker)
        bloodTypePicker.setOnClickListener {
            it.showContextMenu()
        }

        openMapButton.setOnClickListener {
            openLocationPicker()
        }

        setAddressText()

        // Adding a click listener for confirmRegistrationButton
        confirmRegistrationButton.setOnClickListener {
            startActivity(
                Intent(activity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        // Destroying activity when the back button is clicked
        registrationBack.setOnClickListener{
            navController.popBackStack()
        }
    }

    private fun instantiateNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun initializeLocationPickerViewModel(){
        locationPickerViewModel = ViewModelProvider(requireActivity())
            .get(LocationPickerViewModel::class.java)
    }

    private fun askUserForLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (!isLocationPermissionGranted()){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        else {
            getUserLastKnownLocation()
        }
    }

    private fun isLocationPermissionGranted() =
        (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)

    // What happens when users accept or deny accessing their location
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isLocationPermissionGranted()){
                getUserLastKnownLocation()
            }
        }
        else {
            // The user denied the permission, decide with the team what to do
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLastKnownLocation(){
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(requireActivity()) { location ->
            getUserAddressFromLocation(location)
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
                val address = locationPickerViewModel.getAddressFromLatLng(requireActivity(), locationLatLng)
                address?.let {
                    locationPickerViewModel.locationStringLiveData.postValue(address.getAddressLine(0))
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
            { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in the Button's text
                birthDatePicker.text = "$dayOfMonth/$monthOfYear/$year"
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
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val bloodTypesList = resources.getStringArray(R.array.blood_types)
        menu.setHeaderTitle("اختار فصيلة الدم")
        for(i in bloodTypesList.indices)
            menu.add(0, v.id, i, bloodTypesList[i].toString())  //groupId, itemId, order, title

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        bloodTypePicker.text = item.title
        return true
    }

    private fun openLocationPicker(){
        navController.navigate(R.id.action_registrationFragment_to_mapFragment)
    }

    private fun setAddressText(){
        locationPickerViewModel.locationStringLiveData.observe(viewLifecycleOwner){
            addressEditText.setText(it)
        }
    }
}