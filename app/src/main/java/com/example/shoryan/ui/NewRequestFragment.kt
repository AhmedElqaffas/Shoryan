package com.example.shoryan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.example.shoryan.AndroidUtility
import com.example.shoryan.R
import com.example.shoryan.data.CreateNewRequestResponse
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.databinding.AppbarBinding
import com.example.shoryan.databinding.FragmentNewRequestBinding
import com.example.shoryan.viewmodels.NewRequestViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class NewRequestFragment : Fragment() {
    private val newRequestViewModel: NewRequestViewModel by navGraphViewModels(R.id.main_nav_graph)

    private var _binding: FragmentNewRequestBinding? = null
    private val binding get() = _binding!!
    private var toolbarBinding: AppbarBinding? = null
    private var createdRequest : CreateNewRequestResponse? = null
    private var snackbar: Snackbar? = null

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
        setToolbarText(resources.getString(R.string.new_request))
        enableInput()
        checkIfUserCanRequest()
        observeEvents()
    }

    private fun checkIfUserCanRequest() {
        lifecycleScope.launch {
            newRequestViewModel.canUserRequest().observe(viewLifecycleOwner, { canUserRequest ->
                canUserRequest?.let {
                  if(it) enableSubmitButton() else disableInput()
                }
            })
        }
    }

    private fun disableInput() {
        disableRadioButtons()
        disableGovSpinner()
        disableCitySpinner()
        disableBloodBankSpinner()
        disableIncDecButtons()
        disableSubmitButton()
        showMessage("نأسف لا يمكنك طلب تبرع بالدم اكثر من ثلاثة مرات في اليوم")
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
        setGovSpinnerAdapter(binding.spinnerGov, newRequestViewModel.getGovernoratesList())
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
                showMessage("ارجوك اكمل ادخال البيانات")
            }
        }
    }

    private fun createNewRequest() {
        lifecycleScope.launch{
            newRequestViewModel.createNewRequest(getSelectedBloodType().text.toString(), getCurrentBagsCount(),
                getSelectedItemFromSpinner(binding.spinnerBloodBank)).observe(viewLifecycleOwner,
                { response -> showSuccessMessage(response)})
        }
    }

    private fun showSuccessMessage(response: CreateNewRequestResponse?) {
        binding.progressBar.visibility = View.GONE
        createdRequest = response
        if(createdRequest?.id != null)
            showMessage("لقد تم الطلب بنجاح", true)
        else{
            showMessage("نأسف لا يمكنك طلب تبرع بالدم اكثر من ثلاثة مرات في اليوم")
            newRequestViewModel.updateCachedDailyLimitFlag(false)
            disableInput()
        }
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
            Snackbar.make(binding.scrollView, message, Snackbar.LENGTH_LONG)
                .setAction("اظهر بيانات الطلب") {
                    // Starting the MyRequestDetailsActivity
                    openRequestDetails()
                }
                .show()
        else
            AndroidUtility.displaySnackbarMessage(binding.scrollView, message, Snackbar.LENGTH_LONG)
    }
    private fun openRequestDetails() {
        val fragment = RequestDetailsFragment.newInstance(
            createdRequest!!.id,
            RequestDetailsFragment.MY_REQUEST_BINDING
        )
        fragment.show(childFragmentManager, "requestDetails")
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
                is ViewEvent.ShowSnackBar -> {showMessage(it.text)}
                is ViewEvent.ShowTryAgainSnackBar -> {showTryAgainMessage(it.text)}
            }

        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showTryAgainMessage(message: String){
        snackbar = AndroidUtility.makeTryAgainSnackbar(binding.scrollView, message, ::checkIfUserCanRequest)
        snackbar!!.show()
    }

}