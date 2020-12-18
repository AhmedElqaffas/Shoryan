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
        setSpinnerAdapter(spinnerGov, R.array.governments)

        //Adding click listeners for increment and decrement buttons
        setIncDecButtons()
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

    private fun setSpinnerAdapter(spinner: Spinner, arrayResource: Int) {
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
                        } else {
                            governmentsSpinnerTextView.visibility = View.GONE
                        }
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {

                    }
                }
            }
        }

    }
}

class HintAdapter<String>(context: Context, resource: Int, objects: Int) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getCount(): Int {
        val count = super.getCount()
        // The last item will be the hint.
        return if (count > 0) count - 1 else count
    }
}
