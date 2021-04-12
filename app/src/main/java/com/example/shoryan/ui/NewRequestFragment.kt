package com.example.shoryan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.data.CreateNewRequestResponse
import com.example.shoryan.data.ServerError
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.databinding.AppbarBinding
import com.example.shoryan.databinding.FragmentNewRequestBinding
import com.example.shoryan.di.MyApplication
import com.example.shoryan.viewmodels.NewRequestViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class NewRequestFragment : Fragment() {
    private val newRequestViewModel: NewRequestViewModel by navGraphViewModels(R.id.main_nav_graph)

    @Inject
    lateinit var tokensViewModel: TokensViewModel

    private lateinit var navController: NavController
    private var _binding: FragmentNewRequestBinding? = null
    private val binding get() = _binding!!
    private var toolbarBinding: AppbarBinding? = null
    private var createdRequest : CreateNewRequestResponse? = null
    private var snackbar: Snackbar? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MyApplication).appComponent.newRequestComponent().create().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewRequestBinding.inflate(inflater, container, false)
        binding.viewmodel = newRequestViewModel
        binding.lifecycleOwner = this
        toolbarBinding = binding.newRequestAppbar
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toolbarBinding = null
        _binding = null
        snackbar?.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        initializeNavController(view)
        setToolbarText(resources.getString(R.string.new_request))
        enableInput()
        checkIfUserCanRequest()
        observeEvents()
    }

    private fun initializeNavController(view: View){
        navController = Navigation.findNavController(view)
    }

    private fun checkIfUserCanRequest() {
        val canUserRequest = newRequestViewModel.getCachedCanUserRequestFlag()
        if( canUserRequest != null){
            if(canUserRequest)
                enableCreationOfRequest()
            else{
                disableInput()
                showMessage(resources.getString(R.string.cant_request_today))
            }
        }
          else {
            lifecycleScope.launch {
                newRequestViewModel.canUserRequest().observe(viewLifecycleOwner, { canUserRequest ->
                    canUserRequest.let {
                        if ((it != null) and (it == true)) enableCreationOfRequest()
                        else disableInput()
                    }
                })
            }
        }
    }

    private fun enableCreationOfRequest() {
        newRequestViewModel.updateCachedDailyLimitFlag(true)
        enableSubmitButton()
        enableGovSpinner()
    }

    private fun enableGovSpinner() {
        binding.governmentsSpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
        binding.governmentsSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
        setGovSpinnerAdapter(binding.spinnerGov, newRequestViewModel.getGovernoratesList())
    }

    private fun disableInput() {
        disableRadioButtons()
        disableGovSpinner()
        disableCitySpinner()
        disableBloodBankSpinner()
        disableIncDecButtons()
        disableSubmitButton()
    }

    private fun disableSubmitButton() {
        binding.confirmRequestButton.isEnabled = false
        binding.confirmRequestButton.setBackgroundResource(R.drawable.button_disabled_selector)
        binding.confirmRequestButton.setOnClickListener{}
    }

    private fun enableSubmitButton() {
        binding.confirmRequestButton.isEnabled = true
        setConfirmButtonClickListener()
        binding.confirmRequestButton.setBackgroundResource(R.drawable.button_curved_red)
    }

    private fun disableIncDecButtons() {
        binding.incrementBloodBags.isEnabled = false
        binding.decrementBloodBags.isEnabled = false
        binding.bagsNumberEditText.setText("")
        binding.bagsNumberEditText.isEnabled = false
        binding.incrementBloodBags.setBackgroundResource(R.drawable.button_blood_type_disabled)
        binding.decrementBloodBags.setBackgroundResource(R.drawable.button_blood_type_disabled)
    }

    private fun disableRadioButtons() {
        binding.plusTypesRadioGroup.children.forEach {
            it.isEnabled = false
            it.setBackgroundResource(R.drawable.button_blood_type_disabled)
            binding.plusTypesRadioGroup.clearCheck()
        }
        binding.minusTypesRadioGroup.children.forEach {
            it.isEnabled = false
            it.setBackgroundResource(R.drawable.button_blood_type_disabled)
            binding.minusTypesRadioGroup.clearCheck()
        }
    }

    private fun enableInput() {
        setRadioGroupsMutuallyExclusive()
        setIncDecButtonsClickListeners()
        setConfirmButtonClickListener()
    }

    private fun setToolbarText(text: String) {
        toolbarBinding!!.toolbarText.text = text
    }

    private fun setRadioGroupsMutuallyExclusive() {
        binding.plusTypesRadioGroup.children.forEach {
            it.setOnClickListener {
                binding.minusTypesRadioGroup.clearCheck()
            }
        }
        binding.minusTypesRadioGroup.children.forEach {
            it.setOnClickListener {
                binding.plusTypesRadioGroup.clearCheck()
            }
        }
    }

    private fun setGovSpinnerAdapter(spinner: Spinner, govList : List<String>) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, govList)
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = governmentSpinnerItemSelected()
                }
        }
    }

    private fun setCitySpinnerAdapter(spinner: Spinner, regionsList: List<String>) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, regionsList)
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = citySpinnerItemSelected()
                }
        }
    }

    private fun setBloodBankSpinnerAdapter(spinner: Spinner, bloodBankList: List<String>) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.let {
            ArrayAdapter(it, android.R.layout.simple_spinner_item, bloodBankList)
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = bloodBankSpinnerItemSelected()
                }
        }
    }

    private fun governmentSpinnerItemSelected() = object : OnItemSelectedListener {
        override fun onItemSelected(
            parentView: AdapterView<*>?,
            selectedItemView: View,
            position: Int,
            id: Long
        ) {
            if (position == 0) {
                binding.governmentsSpinnerTextView.visibility = View.VISIBLE
                disableCitySpinner()
            } else {
                binding.governmentsSpinnerTextView.visibility = View.GONE
                val selectedGovernments = binding.spinnerGov.selectedItem.toString()
                enableCitySpinner(selectedGovernments)
            }
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {}
    }

    private fun citySpinnerItemSelected() = object : OnItemSelectedListener {
        override fun onItemSelected(
            parentView: AdapterView<*>?,
            selectedItemView: View,
            position: Int,
            id: Long
        ) {
            if (position == 0) {
                binding.citySpinnerTextView.visibility = View.VISIBLE
                disableBloodBankSpinner()
                binding.citySpinnerTextView.visibility = View.VISIBLE
                binding.bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
                binding.bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
            } else {
                binding.citySpinnerTextView.visibility = View.GONE
                val selectedCity = binding.spinnerCity.selectedItem.toString()
                enableBloodBankSpinner(selectedCity)
            }
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {}
    }

    private fun bloodBankSpinnerItemSelected() = object : OnItemSelectedListener {
        override fun onItemSelected(
            parentView: AdapterView<*>?,
            selectedItemView: View,
            position: Int,
            id: Long
        ) {
            if (position == 0) {
                binding.bloodBankSpinnerTextView.visibility = View.VISIBLE
            } else {
                binding.bloodBankSpinnerTextView.visibility = View.GONE
                binding.citySpinnerTextView.visibility = View.GONE
                binding.bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
                binding.bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
            }
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {}
    }

    private fun disableGovSpinner() {
        binding.governmentsSpinnerTextView.visibility = View.VISIBLE
        binding.spinnerGov.adapter = null
        binding.governmentsSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        binding.governmentsSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
        binding.citySpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        binding.citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
        binding.bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        binding.bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
    }

    private fun disableCitySpinner() {
        with(binding){
            citySpinnerTextView.visibility = View.VISIBLE
            spinnerCity.adapter = null
            citySpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
            citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
            bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
            bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
        }
    }

    private fun enableCitySpinner(selectedGovernment: String) {
        binding.citySpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
        binding.citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
        setCitySpinnerAdapter(binding.spinnerCity, newRequestViewModel.getRegionsList(selectedGovernment))
    }

    private fun disableBloodBankSpinner() {
        binding.bloodBankSpinnerTextView.visibility = View.VISIBLE
        binding.spinnerBloodBank.adapter = null
        binding.bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        binding.bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
    }

    private fun enableBloodBankSpinner(city : String) {
        binding.bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
        binding.bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
        setBloodBankSpinnerAdapter(binding.spinnerBloodBank, newRequestViewModel.getBloodBanksList(city))
    }


    private fun setIncDecButtonsClickListeners() {
        binding.incrementBloodBags.setOnClickListener { incNumOfBloodBags() }
        binding.decrementBloodBags.setOnClickListener { decNumOfBloodBags() }
    }

    private fun incNumOfBloodBags() {
        val numOfCurrentBloodBags = getCurrentBagsCount()
        if (numOfCurrentBloodBags < 99) {
            val newBagsNumber = numOfCurrentBloodBags + 1
            binding.bagsNumberEditText.setText(newBagsNumber.toString())
        }
    }

    private fun decNumOfBloodBags() {
        val numOfCurrentBloodBags = getCurrentBagsCount()
        if (numOfCurrentBloodBags > 1) {
            val newBagsNumber = numOfCurrentBloodBags - 1
            binding.bagsNumberEditText.setText(newBagsNumber.toString())
        }
    }

    fun getCurrentBagsCount(): Int {
        return binding.bagsNumberEditText.text.toString().trim().toIntOrNull() ?: 0
    }

    private fun setConfirmButtonClickListener() {
        binding.confirmRequestButton.setOnClickListener {
            if (isBloodTypeSelected() and isBagsCountSet() and isLocationSelected()) {
                binding.progressBar.visibility = View.VISIBLE
                createNewRequest()
            } else {
                showMessage(resources.getString(R.string.fill_all_data))
            }
        }
    }

    private fun createNewRequest() {
        lifecycleScope.launch{
            newRequestViewModel.createNewRequest(getSelectedBloodType().text.toString(), getCurrentBagsCount(),
                getSelectedItemFromSpinner(binding.spinnerBloodBank)).observe(viewLifecycleOwner,
                { response -> showCreationOfRequestResult(response)})
        }
    }

    private fun showCreationOfRequestResult(response: CreateNewRequestResponse?) {
        binding.progressBar.visibility = View.GONE
        if(response?.successfulResponse != null)
            showMessage(resources.getString(R.string.request_created), true)
        else if(response?.error != null)
            handleError(response.error.message)
        else
            handleError(ServerError.CONNECTION_ERROR)


    }

    private fun isBloodTypeSelected(): Boolean {
        return ((binding.plusTypesRadioGroup.checkedRadioButtonId != -1)
                or (binding.minusTypesRadioGroup.checkedRadioButtonId != -1))
    }

    private fun isBagsCountSet(): Boolean {
        return getCurrentBagsCount() > 0
    }

    private fun isLocationSelected(): Boolean {
        return isSpinnerValueSelected(binding.spinnerGov) and isSpinnerValueSelected(binding.spinnerCity) and
                isSpinnerValueSelected(binding.spinnerBloodBank)
    }

    private fun isSpinnerValueSelected(spinner: Spinner): Boolean {
        return spinner.selectedItem?.toString() != ""
    }

    private fun showMessage(message: String, successFlag: Boolean = false) {
        if (successFlag)
            Snackbar.make(binding.scrollView, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(resources.getString(R.string.show_my_requests)) {
                    // Starting the MyRequestDetailsActivity
                    openMyRequestsFragment()
                }
                .show()
        else
            AndroidUtility.displaySnackbarMessage(binding.scrollView, message, Snackbar.LENGTH_LONG)
    }
    private fun openMyRequestsFragment() {
        navController.navigate(R.id.action_newRequest_to_myRequestsFragment)
    }

    private fun getSelectedBloodType(): RadioButton =
            if (binding.plusTypesRadioGroup.checkedRadioButtonId != -1)
                binding.plusTypesRadioGroup.findViewById(binding.plusTypesRadioGroup.checkedRadioButtonId)
            else
                binding.minusTypesRadioGroup.findViewById(binding.minusTypesRadioGroup.checkedRadioButtonId)


    private fun getSelectedItemFromSpinner(spinner : Spinner): String? {
        return spinner.selectedItem?.toString()
    }

    private fun observeEvents(){
        newRequestViewModel.eventsFlow.onEach {
            when(it){
                is ViewEvent.ShowSnackBar -> {showMessage(resources.getString(it.stringResource))}
                is ViewEvent.ShowTryAgainSnackBar -> {showTryAgainMessage(it.text)}
                is ViewEvent.ErrorHandler -> {
                    handleError(it.error)
                    disableInput()
                }
            }

        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showTryAgainMessage(message: String){
        snackbar = AndroidUtility.makeTryAgainSnackbar(binding.scrollView, message, ::checkIfUserCanRequest)
        snackbar!!.show()
    }

    private fun handleError(error: ServerError){
        if(error == ServerError.JWT_EXPIRED){
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                val response = tokensViewModel.getNewAccessToken(requireContext())
                // If an error happened when refreshing tokens, log user out
                response.error?.let{
                    forceLogOut()
                }
            }
        }
        else{
            error.doErrorAction(binding.root)
            if(error == ServerError.REQUESTS_DAILY_LIMIT){
                newRequestViewModel.updateCachedDailyLimitFlag(false)
                disableInput()
            }
        }
    }

    private fun forceLogOut(){
        Toast.makeText(requireContext(), resources.getString(R.string.re_login), Toast.LENGTH_LONG).show()
        val intent = Intent(context, LandingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}