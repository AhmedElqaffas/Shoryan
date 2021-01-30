package com.example.sharyan.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.example.sharyan.R
import com.example.sharyan.Utility
import com.example.sharyan.data.*
import com.example.sharyan.databinding.FragmentRegistrationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class RegistrationFragment : Fragment(), LoadingFragmentHolder{

    private lateinit var navController: NavController
    private lateinit var locationPickerViewModel: LocationPickerViewModel
    private val registrationViewModel: RegistrationViewModel by navGraphViewModels(R.id.landing_nav_graph)
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var registrationProcess: LiveData<RegistrationResponse>
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        _binding!!.viewmodel = registrationViewModel
        _binding!!.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLocationPickerViewModel()
        initializeUserLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instantiateNavController(view)
        observeEditTexts()
        // Setting a click listener for the birthDatePicker button
        binding.birthDatePicker.setOnClickListener{
            pickBirthDate()
        }

        // Registering the bloodTypePicker TextView for Context Menu
        registerForContextMenu(binding.bloodTypePicker)
        binding.bloodTypePicker.setOnClickListener {
            it.showContextMenu()
        }

        registerForContextMenu(binding.genderPicker)
        binding.genderPicker.setOnClickListener {
            it.showContextMenu()
        }

        binding.openMapButton.setOnClickListener {
            openLocationPicker()
        }

        setAddressText()
        setConfirmRegistrationButtonListener()

        // Destroying fragment when the back button is clicked
        binding.registrationBack.setOnClickListener{
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

    private fun observeEditTexts(){
        binding.registrationFirstNameEditText.addTextChangedListener {
            observeNameText(binding.registrationFirstNameTextLayout,
                binding.registrationFirstNameEditText.text.toString()
            )
        }
        binding.registrationLastNameEditText.addTextChangedListener {
            observeNameText(binding.registrationLastNameTextLayout,
                binding.registrationLastNameEditText.text.toString()
            )
        }
        binding.registrationPhoneEditText.addTextChangedListener { observePhoneText(it) }
        binding.registrationPasswordEditText.addTextChangedListener{observePasswordText(it)}
        binding.confirmPasswordEditText.addTextChangedListener{observeConfirmPasswordText()}
    }

    private fun observeNameText(editTextLayout: TextInputLayout, text: String) {
        if(registrationViewModel.isValidNameEntered(text)){
            editTextLayout.error = ""
        }
        else{
            editTextLayout.error = resources.getString(R.string.name_format_message)
        }
    }

    private fun observePhoneText(editable: Editable?) {
        if(registrationViewModel.isValidMobilePhoneEntered(editable.toString())){
            binding.registrationPhoneTextLayout.error = ""
            binding.registrationRootLayout.requestFocus()
            Utility.hideSoftKeyboard(requireActivity(), binding.registrationPhoneEditText)
        }
        else{
            binding.registrationPhoneTextLayout.error = resources.getString(R.string.phone_format_message)
        }
    }

    private fun observePasswordText(editable: Editable?){
        if(registrationViewModel.isValidPasswordEntered(editable.toString())){
            binding.passwordTextLayout.error = ""
        }
        else{
            binding.passwordTextLayout.error = resources.getString(R.string.password_format_message)
        }
    }

    private fun observeConfirmPasswordText(){
        if(doPasswordsMatch()){
            binding.confirmPasswordTextLayout.error = ""
        }
        else{
            binding.confirmPasswordTextLayout.error = resources.getString(R.string.password_mismatch)
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
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
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
        navController.navigate(R.id.action_registrationFragment_to_mapFragment)
    }

    private fun setAddressText(){
        locationPickerViewModel.locationStringLiveData.observe(viewLifecycleOwner){
            binding.addressEditText.setText(it)
        }
    }

    private fun setConfirmRegistrationButtonListener(){
        binding.confirmRegistrationButton.setOnClickListener {
            if(areInputsValidAndComplete()){
                registerUser(createUser())
            }
        }
    }

    private fun areInputsValidAndComplete(): Boolean{
        var areInputsValidAndComplete = true
        // Validating first name field
        if(!registrationViewModel.isValidNameEntered(binding.registrationFirstNameEditText.text.toString())){
            areInputsValidAndComplete = false
            binding.registrationFirstNameTextLayout.error = resources.getString(R.string.name_format_message)
        }
        // Validating last name field
        if(!registrationViewModel.isValidNameEntered(binding.registrationLastNameEditText.text.toString())){
            areInputsValidAndComplete = false
            binding.registrationLastNameTextLayout.error = resources.getString(R.string.name_format_message)
        }
        // Validating phone number field
        if(!registrationViewModel.isValidMobilePhoneEntered(binding.registrationPhoneEditText.text.toString())){
            areInputsValidAndComplete = false
            binding.registrationPhoneTextLayout.error = resources.getString(R.string.phone_format_message)
        }
        // Validating birth date
        if(registrationViewModel.getBirthDate() == null){
            areInputsValidAndComplete = false
            Utility.displaySnackbarMessage(binding.registrationRootLayout,
                "ادخل تاريخ ميلادك",
                Snackbar.LENGTH_SHORT)
        }
        // Validating location
        if(locationPickerViewModel.locationStringLiveData.value.isNullOrEmpty()){
            areInputsValidAndComplete = false
            binding.addressTextLayout.error = "اختر عنوان تواجدك"
        }
        // Validating password
        if(!registrationViewModel.isValidPasswordEntered(binding.registrationPasswordEditText.text.toString())){
            areInputsValidAndComplete = false
            binding.passwordTextLayout.error = resources.getString(R.string.password_format_message)
        }
        // Validating confirm password
        if(!doPasswordsMatch()){
            areInputsValidAndComplete = false
            binding.confirmPasswordTextLayout.error = resources.getString(R.string.password_mismatch)
        }

        return areInputsValidAndComplete
    }


    private fun doPasswordsMatch() = (binding.confirmPasswordEditText.text.toString()
            == binding.registrationPasswordEditText.text.toString())


    private fun registerUser(user: User){
        toggleLoggingInIndicator()
        registrationProcess = registrationViewModel.registerUser(user)
        registrationProcess.observe(viewLifecycleOwner){
            toggleLoggingInIndicator()
            it.error?.apply {
                Utility.displaySnackbarMessage(binding.registrationRootLayout, this, Snackbar.LENGTH_LONG)
            }
            it.id?.apply {
                goToSMSFragment(user)
            }
        }
    }

    private fun toggleLoggingInIndicator(){
        val loadingFragment: DialogFragment? = childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        if(loadingFragment == null)
            LoadingFragment(this).show(childFragmentManager, "loading")
        else
            loadingFragment.dismiss()
    }

    private fun createUser() = User(
        name = Name(binding.registrationFirstNameEditText.text.toString().trim(),
            binding.registrationLastNameEditText.text.toString().trim()),
        phoneNumber = binding.registrationPhoneEditText.text.toString().trim().substring(1),
        password = binding.registrationPasswordEditText.text.toString(),
        location = locationPickerViewModel.getLocation(),
        bloodType = registrationViewModel.bloodType.value,
        gender = registrationViewModel.gender.value,
        birthDate = registrationViewModel.getBirthDate()
    )

    private fun goToSMSFragment(user: User){
        val phoneNumber = getEditTextValue(binding.registrationPhoneEditText)
        val phoneNumberBundle = bundleOf("phoneNumber" to phoneNumber)
        navController.navigate(R.id.action_registrationFragment_to_SMSFragment, phoneNumberBundle)
    }

    private fun getEditTextValue(editText: EditText):String =  editText.text.toString().trim()

    override fun onLoadingFragmentDismissed(){
        registrationProcess.removeObservers(viewLifecycleOwner)
    }
}