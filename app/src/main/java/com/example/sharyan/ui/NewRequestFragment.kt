package com.example.sharyan.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.example.sharyan.R
import com.example.sharyan.data.CreateNewRequestResponse
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.databinding.AppbarBinding
import com.example.sharyan.databinding.FragmentNewRequestBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*


class NewRequestFragment : Fragment() {
    private val newRequestViewModel: NewRequestViewModel by navGraphViewModels(R.id.main_nav_graph)

    private var _binding: FragmentNewRequestBinding? = null
    private val binding get() = _binding!!
    private var toolbarBinding: AppbarBinding? = null

    private var createdRequest : CreateNewRequestResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewRequestBinding.inflate(inflater, container, false)
        toolbarBinding = binding.newRequestAppbar
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toolbarBinding = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        setToolbarText(resources.getString(R.string.new_request))
        enableInput()
        checkIfUserCanRequest()
    }

    private fun checkIfUserCanRequest() {
        CoroutineScope(Dispatchers.Main).async {
            newRequestViewModel.canUserRequest().observe(viewLifecycleOwner, { canUserRequest ->
                if (!canUserRequest) disableInput() else enableSubmitButton()
            })
        }
    }

    private fun disableInput() {
        disableRadioButtons()
        disableGovSpinner()
        disableCitySpinner()
        disableBloodBankSpinner()
        disableIncDecButtons()
        disableProgressBar()
        disableSubmitButton()
        binding.checkingPermissionSentence.visibility = View.GONE
        showMessage("نأسف لا يمكنك طلب تبرع بالدم اكثر من ثلاثة مرات في اليوم")
    }

    private fun disableSubmitButton() {
        binding.confirmRequestButton.isEnabled = false
        binding.confirmRequestButton.setBackgroundResource(R.drawable.button_disabled_selector)
        binding.confirmRequestButton.setOnClickListener{}
    }

    private fun disableProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun enableSubmitButton() {
        binding.confirmRequestButton.isEnabled = true
        setConfirmButtonClickListener()
        binding.confirmRequestButton.setBackgroundResource(R.drawable.button_curved_red)
        binding.progressBar.visibility = View.GONE
        binding.checkingPermissionSentence.visibility = View.GONE
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
        setGovSpinnerAdapter(binding.spinnerGov, newRequestViewModel.getGovernotesList())
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
            val ArrayAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, govList)
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
            val ArrayAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, regionsList)
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
            val ArrayAdapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, bloodBankList)
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

    private fun enableGovSpinner() {
        binding.governmentsSpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
        binding.governmentsSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
        setGovSpinnerAdapter(binding.spinnerGov, newRequestViewModel.getGovernotesList())
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
        CoroutineScope(Dispatchers.Main).async {
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
            Snackbar.make(binding.scrollView, message, Snackbar.LENGTH_LONG)
                .setAction("حسناً") {
                    // By default, the snackbar will be dismissed
                }
                .show()
    }

    private fun openRequestDetails() {
        val fragment = MyRequestDetailsFragment.newInstance(createdRequest?.id as String)
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

}