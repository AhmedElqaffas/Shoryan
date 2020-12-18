package com.example.sharyan.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.sharyan.R
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.fragment_new_request.*


class NewRequestFragment : Fragment() {

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
        setRadioGroupsMutuallyExclusive()
        setGovSpinnerAdapter(spinnerGov, R.array.governments)

        //Adding click listeners for increment and decrement buttons
        setIncDecButtons()

        //Adding click listener for confirm request button
        confirmRequestButton.setOnClickListener{
            if( ((plusTypesRadioGroup.checkedRadioButtonId != -1) or (minusTypesRadioGroup.checkedRadioButtonId != -1))
                and (getCurrentBagsCount() > 0) and (spinnerGov.selectedItem?.toString() != "") and (spinnerCity.selectedItem?.toString() != "")){
                Toast.makeText(requireContext(), "تم الطلب بنجاح", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(), "ارجوك اكمل ادخال البيانات", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setIncDecButtons() {
        incrementBloodBags.setOnClickListener { incNumOfBloodBags() }
        decrementBloodBags.setOnClickListener { decNumOfBloodBags() }
    }

    private fun getCurrentBagsCount(): Int {
        return bagsNumberEditText.text.toString().trim().toInt()
    }

    private fun incNumOfBloodBags() {
        var numOfCurrentBloodBags = getCurrentBagsCount()
        if (numOfCurrentBloodBags == 99)
            return
        val newBagsNumber = numOfCurrentBloodBags + 1
        bagsNumberEditText.setText(newBagsNumber.toString())
    }

    private fun decNumOfBloodBags() {
        var numOfCurrentBloodBags = getCurrentBagsCount()
        if (numOfCurrentBloodBags == 0)
            return
        val newBagsNumber = numOfCurrentBloodBags - 1
        bagsNumberEditText.setText(newBagsNumber.toString())
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
            ArrayAdapter.createFromResource(
                it,
                arrayResource,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
                spinner.onItemSelectedListener = object : OnItemSelectedListener {
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
                            val selectedGovernorate = spinner.selectedItem.toString()
                            enableCitySpinner(selectedGovernorate)
                        }
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {

                    }
                }
            }
        }

    }

    private fun setCitySpinnerAdapter(spinner: Spinner, arrayResource: Int) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        activity?.let {
            ArrayAdapter.createFromResource(
                it,
                arrayResource,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
                spinner.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        selectedItemView: View,
                        position: Int,
                        id: Long
                    ) {
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

                    override fun onNothingSelected(parentView: AdapterView<*>?) {

                    }
                }
            }
        }

    }

    private fun disableCitySpinner(){
        citySpinnerTextView.visibility = View.VISIBLE
        spinnerCity.adapter = null
        citySpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
        bloodBankSpinnerLayout.setBackgroundResource(R.drawable.spinner_grey_curve)
        bloodBankSpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_grey)
    }

    private fun enableCitySpinner(selectedGovernorate : String){
        citySpinnerLayout.setBackgroundResource(R.drawable.spinner_red_curve)
        citySpinnerImageView.setImageResource(R.drawable.iconfinder_nav_arrow_right_383100_big)
        when(selectedGovernorate){
            "القاهرة" -> setCitySpinnerAdapter(spinnerCity, R.array.cairo_cities)
            "الإسكندرية" -> setCitySpinnerAdapter(spinnerCity, R.array.alex_cities)
            else -> setCitySpinnerAdapter(spinnerCity, R.array.example_cities)
        }
    }
}

