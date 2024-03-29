package com.example.shoryan.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.data.BirthDate
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.FragmentAccountInfoBinding
import com.example.shoryan.interfaces.LoadingFragmentHolder
import com.example.shoryan.viewmodels.AccountInfoViewModel
import com.example.shoryan.viewmodels.LocationPickerViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@AndroidEntryPoint
class AccountInfoFragment : Fragment(), LoadingFragmentHolder {

    private lateinit var navController: NavController
    private var _binding: FragmentAccountInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationPickerViewModel: LocationPickerViewModel
    private val accountInfoViewModel: AccountInfoViewModel by navGraphViewModels(R.id.main_nav_graph)
    val tokensViewModel: TokensViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLocationPickerViewModel()

        // Get user's current profile information
        getUserCurrentProfileInfo()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
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
        binding.birthDatePicker.setOnClickListener {
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

        // Observing the viewmodel events
        observeViewModelEvents()
    }

    /**
     * This functions fetches the current information of the profile of the user and uses it to
     * fill the initial values for the account info form
     */
    private fun getUserCurrentProfileInfo() {
        accountInfoViewModel.getUserProfileData()
        getUserAddressFromLocation(accountInfoViewModel.addressLiveData.value!!)
    }

    /**
     * Links the viewModel to the activity instead of the fragment
     */
    private fun initializeLocationPickerViewModel() {
        locationPickerViewModel = ViewModelProvider(requireActivity())
            .get(LocationPickerViewModel::class.java)
    }

    /**
     * This function opens a DatePicker dialog so that the user can modify his/her birth date
     */
    private fun pickBirthDate() {
        val c = Calendar.getInstance()
        val dpd = DatePickerDialog(
            requireActivity(),
            { _, year, monthOfYear, dayOfMonth ->
                accountInfoViewModel.setBirthDate(BirthDate(year, monthOfYear + 1, dayOfMonth))
            },
            accountInfoViewModel.getBirthYear(),
            accountInfoViewModel.getBirthMonth() - 1,
            accountInfoViewModel.getBirthDay()
        )

        // Setting the maximum date for the birthday
        c.set(2005, 11, 30)
        dpd.datePicker.maxDate = c.timeInMillis

        //Setting the minimum date for the birthday
        c.set(1920, 0, 1)
        dpd.datePicker.minDate = c.timeInMillis

        dpd.show()
    }

    /**
     * ContextMenu code for selecting the appropriate blood type
     */
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v == binding.bloodTypePicker) {
            showBloodTypesList(menu, v)
        } else if (v == binding.genderPicker) {
            showGenderList(menu, v)
        }

    }

    private fun showBloodTypesList(menu: ContextMenu, v: View) {
        val bloodTypesList = resources.getStringArray(R.array.blood_types)
        menu.setHeaderTitle(resources.getString(R.string.choose_bloodtype))
        for (i in bloodTypesList.indices)
            menu.add(0, v.id, i, bloodTypesList[i].toString())
    }

    private fun showGenderList(menu: ContextMenu, v: View) {
        val gendersList = resources.getStringArray(R.array.gender)
        menu.setHeaderTitle(resources.getString(R.string.choose_gender))
        for (i in gendersList.indices)
            menu.add(1, v.id, i, gendersList[i].toString())
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId == 0) {
            accountInfoViewModel.setBloodType(item.title.toString())
        } else if (item.groupId == 1) {
            accountInfoViewModel.setGender(item.title.toString())
        }
        return true
    }

    private fun openLocationPicker() {
        navController.navigate(R.id.action_accountInfoFragment_to_mapFragment2)
    }

    private fun setAddress() {

        locationPickerViewModel.locationStringLiveData.observe(viewLifecycleOwner) {
            accountInfoViewModel.setAddress(locationPickerViewModel.getLocation())
            binding.addressEditText.setText(it)
        }
    }

    /**
     * Takes the location of the user and uses reverse geocoding to find the address.
     * This method stores both the location and the address in the viewmodel
     */
    private fun getUserAddressFromLocation(location: com.example.shoryan.data.Location) {
        val locationLatLng = LatLng(location.latitude!!, location.longitude!!)
        locationPickerViewModel.locationLatLng = locationLatLng
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
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


    private fun observeViewModelEvents() {
        accountInfoViewModel.updateAccountInfoEventsFlow.onEach {
            when (it) {
                is AccountInfoViewModel.EditAccountInfoViewEvent.ShowSnackBarFromString ->
                    showMessage(it.text)
                is AccountInfoViewModel.EditAccountInfoViewEvent.HandleError ->
                    handleError(it.error)
                is AccountInfoViewModel.EditAccountInfoViewEvent.ToggleLoadingIndicator -> toggleLoadingIndicator()
                is AccountInfoViewModel.EditAccountInfoViewEvent.UpdatedAccountInfoSuccessfully -> onSuccessfulResponse()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    /**
     * This method is responsible for handling all possible occurring errors which could include
     * missing data, connection errors and expiration of tokens.
     */
    private fun handleError(errorMessage: ServerError) {
        if(errorMessage == ServerError.JWT_EXPIRED){
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                val response = tokensViewModel.getNewAccessToken(requireContext())
                // If an error happened when refreshing tokens, log user out
                response.error?.let{
                    forceLogOut()
                }
            }
        }
        else
            showMessage(resources.getString(errorMessage.errorStringResource))
    }

    /**
     * This method forces the logout of the user when the tokens have expired.
     */
    private fun forceLogOut(){
        Toast.makeText(requireContext(), resources.getString(R.string.re_login), Toast.LENGTH_LONG).show()
        val intent = Intent(context, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    /**
     * This callback method is called when the user's account information have been updated successfully.
     * It shows a success message to the user and navigates back to the Profile Settings screen.
     */
    private fun onSuccessfulResponse() {
        showMessage(resources.getString(R.string.account_info_update_success))
    }

    private fun showMessage(message: String) {
        AndroidUtility.displayAlertDialog(requireContext(), message)
    }

    private fun toggleLoadingIndicator() {
        val loadingFragment: DialogFragment? =
            childFragmentManager.findFragmentByTag("loading") as DialogFragment?
        if (loadingFragment == null)
            LoadingFragment(this).show(childFragmentManager, "loading")
        else
            loadingFragment.dismiss()
    }

    override fun onLoadingFragmentDismissed() {
        accountInfoViewModel.cancelUpdateInfoProcess()
    }
}