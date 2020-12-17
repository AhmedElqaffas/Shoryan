package com.example.sharyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.children
import com.example.sharyan.R
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
        setSpinnerAdapter(governmentsSpinner, R.array.governments)
    }

    private fun setToolbarText(text: String){
        toolbarText.text = text
    }

    private fun setRadioGroupsMutuallyExclusive(){
        plusTypesRadioGroup.children.forEach {
            it.setOnClickListener {
                minusTypesRadioGroup.clearCheck()
            }
        }
        minusTypesRadioGroup.children.forEach{
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
            }
        }
    }
}