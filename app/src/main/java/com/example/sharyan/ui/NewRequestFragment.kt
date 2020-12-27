package com.example.sharyan.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.sharyan.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.fragment_new_request.*


class NewRequestFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarText(resources.getString(R.string.new_request))
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

    private fun governmentSpinnerItemSelected() = object : OnItemSelectedListener {
        override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
            if (position == 0) {
                governmentsSpinnerTextView.visibility = View.VISIBLE
                disableCitySpinner()
            } else {
                governmentsSpinnerTextView.visibility = View.GONE
                val selectedGovernments = spinnerGov.selectedItem.toString()
                enableCitySpinner(selectedGovernments)
            }
        }

        override fun onNothingSelected(parentView: AdapterView<*>?){}
    }

    private fun citySpinnerItemSelected() = object : OnItemSelectedListener {
        override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
            if (position == 0) {
                citySpinnerTextView.visibility = View.VISIBLE
                bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
                bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
            } else {
                citySpinnerTextView.visibility = View.GONE
                bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
                bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
            }
        }

        override fun onNothingSelected(parentView: AdapterView<*>?) {}
    }

    private fun disableCitySpinner(){
        citySpinnerTextView.visibility = View.VISIBLE
        spinnerCity.adapter = null
        citySpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
        bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
    }

    private fun enableCitySpinner(selectedGovernment : String){
        citySpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
        citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
        when(selectedGovernment){
            "القاهرة" -> setCitySpinnerAdapter(spinnerCity, R.array.cairo_cities)
            "الإسكندرية" -> setCitySpinnerAdapter(spinnerCity, R.array.alex_cities)
            else -> setCitySpinnerAdapter(spinnerCity, R.array.example_cities)
        }
    }

    private fun setIncDecButtonsClickListeners() {
        incrementBloodBags.setOnClickListener { incNumOfBloodBags() }
        decrementBloodBags.setOnClickListener { decNumOfBloodBags() }
    }

    private fun incNumOfBloodBags() {
        val numOfCurrentBloodBags = getCurrentBagsCount()
        if(numOfCurrentBloodBags < 99){
            val newBagsNumber = numOfCurrentBloodBags + 1
            bagsNumberEditText.setText(newBagsNumber.toString())
        }
    }

    private fun decNumOfBloodBags() {
        val numOfCurrentBloodBags = getCurrentBagsCount()
        if(numOfCurrentBloodBags > 1){
            val newBagsNumber = numOfCurrentBloodBags - 1
            bagsNumberEditText.setText(newBagsNumber.toString())
        }
    }

    private fun getCurrentBagsCount(): Int {
        return bagsNumberEditText.text.toString().trim().toIntOrNull()?: 0
    }

    private fun setConfirmButtonClickListener(){
        confirmRequestButton.setOnClickListener{
            if(isBloodTypeSelected() and isBagsCountSet() and isLocationSelected()){
                showMessage("تم الطلب بنجاح")
            }
            else{
                showMessage("ارجوك اكمل ادخال البيانات")
            }
        }
    }

    private fun isBloodTypeSelected(): Boolean{
        return ((plusTypesRadioGroup.checkedRadioButtonId != -1)
                or (minusTypesRadioGroup.checkedRadioButtonId != -1))
    }

    private fun isBagsCountSet(): Boolean{
        return getCurrentBagsCount() > 0
    }

    private fun isLocationSelected(): Boolean{
        return isSpinnerValueSelected(spinnerGov) and isSpinnerValueSelected(spinnerCity)
    }

    private fun isSpinnerValueSelected(spinner: Spinner): Boolean{
        return spinner.selectedItem?.toString() != ""
    }

    private fun showMessage(message: String){
        Snackbar.make(scrollView, message, Snackbar.LENGTH_LONG)
            .setAction("حسناً") {
                // By default, the snackbar will be dismissed
            }
            .show()
    }
}

