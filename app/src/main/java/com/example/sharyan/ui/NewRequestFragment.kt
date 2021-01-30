package com.example.sharyan.ui

import android.content.Intent
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.appbar.toolbarText
import kotlinx.android.synthetic.main.fragment_new_request.*
import kotlinx.coroutines.*


class NewRequestFragment : Fragment() {
    private val newRequestViewModel: NewRequestViewModel by navGraphViewModels(R.id.main_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        disableIncDecButtons()
        showMessage("نأسف لا يمكنك طلب تبرع بالدم اكثر من ثلاثة مرات في اليوم")
    }

    private fun enableSubmitButton() {
        confirmRequestButton.isEnabled = true
        setConfirmButtonClickListener()
        confirmRequestButton.setBackgroundResource(R.drawable.button_curved_red)
        progressBar.visibility = View.GONE
        checkingPermissionSentence.visibility = View.GONE
    }

    private fun disableIncDecButtons() {
        incrementBloodBags.isEnabled = false
        decrementBloodBags.isEnabled = false
        bagsNumberEditText.setText("")
        bagsNumberEditText.isEnabled = false
    }

    private fun disableRadioButtons() {
        plusTypesRadioGroup.children.forEach {
            it.isEnabled = false
            plusTypesRadioGroup.clearCheck()
        }
        minusTypesRadioGroup.children.forEach {
            it.isEnabled = false
            minusTypesRadioGroup.clearCheck()
        }
    }

    private fun enableInput() {
        setRadioGroupsMutuallyExclusive()
        setGovSpinnerAdapter(spinnerGov, R.array.governments)
        setIncDecButtonsClickListeners()
        setConfirmButtonClickListener()
    }

    private fun setToolbarText(text: String) {
        toolbarText.text = text
    }

    private fun setRadioGroupsMutuallyExclusive() {
        plusTypesRadioGroup.children.forEach {
            it.setOnClickListener {
                minusTypesRadioGroup.clearCheck()
            }
        }
        minusTypesRadioGroup.children.forEach {
            it.setOnClickListener {
                plusTypesRadioGroup.clearCheck()
            }
        }
    }

    private fun setGovSpinnerAdapter(spinner: Spinner, arrayResource: Int) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.let {
            ArrayAdapter.createFromResource(it, arrayResource, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = governmentSpinnerItemSelected()
                }
        }

    }

    private fun setCitySpinnerAdapter(spinner: Spinner, arrayResource: Int) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.let {
            ArrayAdapter.createFromResource(it, arrayResource, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = citySpinnerItemSelected()
                }
        }

    }

    private fun setBloodBankSpinnerAdapter(spinner: Spinner, arrayResource: Int) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.let {
            ArrayAdapter.createFromResource(it, arrayResource, android.R.layout.simple_spinner_item)
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
                governmentsSpinnerTextView.visibility = View.VISIBLE
                disableCitySpinner()
            } else {
                governmentsSpinnerTextView.visibility = View.GONE
                val selectedGovernments = spinnerGov.selectedItem.toString()
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
                citySpinnerTextView.visibility = View.VISIBLE
                disableBloodBankSpinner()
            } else {
                citySpinnerTextView.visibility = View.GONE
                enableBloodBankSpinner()
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
                bloodBankSpinnerTextView.visibility = View.VISIBLE
            } else {
                bloodBankSpinnerTextView.visibility = View.GONE
            }
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {}
    }

    private fun disableGovSpinner() {
        governmentsSpinnerTextView.visibility = View.VISIBLE
        spinnerGov.adapter = null
        governmentsSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        governmentsSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
        citySpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
        bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
    }


    private fun disableCitySpinner() {
        citySpinnerTextView.visibility = View.VISIBLE
        spinnerCity.adapter = null
        citySpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
        bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
    }

    private fun enableCitySpinner(selectedGovernment: String) {
        citySpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
        citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
        when (selectedGovernment) {
            "القاهرة" -> setCitySpinnerAdapter(spinnerCity, R.array.cairo_cities)
            "الإسكندرية" -> setCitySpinnerAdapter(spinnerCity, R.array.alex_cities)
            else -> setCitySpinnerAdapter(spinnerCity, R.array.example_cities)
        }
    }

    private fun disableBloodBankSpinner() {
        bloodBankSpinnerTextView.visibility = View.VISIBLE
        spinnerBloodBank.adapter = null
        bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
    }

    private fun enableBloodBankSpinner() {
        bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
        bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
        setBloodBankSpinnerAdapter(spinnerBloodBank, R.array.example_blood_banks)
    }


    private fun setIncDecButtonsClickListeners() {
        incrementBloodBags.setOnClickListener { incNumOfBloodBags() }
        decrementBloodBags.setOnClickListener { decNumOfBloodBags() }
    }

    private fun incNumOfBloodBags() {
        val numOfCurrentBloodBags = getCurrentBagsCount()
        if (numOfCurrentBloodBags < 99) {
            val newBagsNumber = numOfCurrentBloodBags + 1
            bagsNumberEditText.setText(newBagsNumber.toString())
        }
    }

    private fun decNumOfBloodBags() {
        val numOfCurrentBloodBags = getCurrentBagsCount()
        if (numOfCurrentBloodBags > 1) {
            val newBagsNumber = numOfCurrentBloodBags - 1
            bagsNumberEditText.setText(newBagsNumber.toString())
        }
    }

    fun getCurrentBagsCount(): Int {
        return bagsNumberEditText.text.toString().trim().toIntOrNull() ?: 0
    }

    private fun setConfirmButtonClickListener() {
        confirmRequestButton.setOnClickListener {
            if (isBloodTypeSelected() and isBagsCountSet() and isLocationSelected()) {
                showMessage("تم الطلب بنجاح", successFlag = true)
            } else {
                showMessage("ارجوك اكمل ادخال البيانات")
            }
        }
    }

    private fun isBloodTypeSelected(): Boolean {
        return ((plusTypesRadioGroup.checkedRadioButtonId != -1)
                or (minusTypesRadioGroup.checkedRadioButtonId != -1))
    }

    private fun isBagsCountSet(): Boolean {
        return getCurrentBagsCount() > 0
    }

    private fun isLocationSelected(): Boolean {
        return isSpinnerValueSelected(spinnerGov) and isSpinnerValueSelected(spinnerCity) and
                isSpinnerValueSelected(spinnerBloodBank)
    }

    private fun isSpinnerValueSelected(spinner: Spinner): Boolean {
        return spinner.selectedItem?.toString() != ""
    }

    private fun showMessage(message: String, successFlag: Boolean = false) {
        if (successFlag)
            Snackbar.make(scrollView, message, Snackbar.LENGTH_LONG)
                .setAction("اظهر بيانات الطلب") {
                    // Starting the MyRequestDetailsActivity
                    openRequestDetails()
                }
                .show()
        else
            Snackbar.make(scrollView, message, Snackbar.LENGTH_LONG)
                .setAction("حسناً") {
                    // By default, the snackbar will be dismissed
                }
                .show()
    }

    private fun openRequestDetails() {
        val intent = Intent(context, MyRequestDetailsActivity::class.java)
        intent.putExtra("bloodType", getSelectedBloodType().text.toString())
        intent.putExtra("gov", getSelectedItemFromSpinner(spinnerGov))
        intent.putExtra("city", getSelectedItemFromSpinner(spinnerCity))
        intent.putExtra("bloodBagsCount", getCurrentBagsCount())
        startActivity(intent)
    }

    private fun getSelectedBloodType(): RadioButton =
        if (plusTypesRadioGroup.checkedRadioButtonId != -1)
            plusTypesRadioGroup.findViewById(plusTypesRadioGroup.checkedRadioButtonId)
        else
            minusTypesRadioGroup.findViewById(minusTypesRadioGroup.checkedRadioButtonId)


    private fun getSelectedItemFromSpinner(spinner : Spinner): String? {
        return spinner.selectedItem?.toString()
    }

}