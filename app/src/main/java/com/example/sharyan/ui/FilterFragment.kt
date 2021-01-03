package com.example.sharyan.ui

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import com.example.sharyan.R
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.RequestsFilter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.design_bottom_sheet

class FilterFragment(private val filterHolder: FilterHolder, private val requestsFilter: RequestsFilter?)
    : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val bloodTypeSet = getSelectedBloodTypeFiltersSet()
        if(bloodTypeSet.isNotEmpty())
            filterHolder.submitFilters(RequestsFilter(bloodTypeSet))
        else
            filterHolder.submitFilters(null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWindowSize()
        restoreViewsState()
        clearBloodTypeFilter.setOnClickListener{ clearBloodTypeFilters() }
        matchingBloodFilterButton.setOnClickListener { chooseCompatibleTypes() }
    }

    private fun setWindowSize(){
        dialog?.also {
            val window: Window? = dialog!!.window
            val size = Point()
            val display: Display = window!!.windowManager.defaultDisplay
            display.getSize(size)
            val windowHeight = size.y
            design_bottom_sheet.layoutParams.height = (windowHeight)
            val behavior = BottomSheetBehavior.from<View>(design_bottom_sheet)
            behavior.peekHeight = windowHeight
            view?.requestLayout()
        }
    }

    private fun restoreViewsState(){
        requestsFilter?.bloodType?.let { filtersSet ->
            bloodTypeFilterLayout.children.forEach { linearLayout ->
                (linearLayout as LinearLayout).children.forEach {
                    if(it is ToggleButton && filtersSet.contains(it.text) ){
                        it.isChecked = true
                    }
                }
            }
        }
    }

    private fun clearBloodTypeFilters(){
        bloodTypeFilterLayout.children.forEach { linearLayout ->
            (linearLayout as LinearLayout).children.forEach {
                if(it is ToggleButton){
                    it.isChecked = false
                }
           }
        }
    }

    private fun getSelectedBloodTypeFiltersSet(): MutableSet<String>{
        val selectedTypes = mutableSetOf<String>()
        bloodTypeFilterLayout.children.forEach { linearLayout ->
            (linearLayout as LinearLayout).children.forEach {
                if(it is ToggleButton && it.isChecked){
                    selectedTypes.add(it.text.toString())
                }
            }
        }
        return selectedTypes
    }

    private fun chooseCompatibleTypes(){
        val compatibilityTable = mapOf(
                "O-" to setOf("A-","A+","B-","B+","AB-","AB+","O-","O+"),
                "O+" to setOf("A+","B+","AB+","O+"),
                "B-" to setOf("B-","B+","AB-","AB+"),
                "B+" to setOf("B+","AB+"),
                "A-" to setOf("A-","A+","AB-","AB+"),
                "A+" to setOf("A+","AB+"),
                "AB-" to setOf("AB-","AB+"),
                "AB+" to setOf("AB+")
        )
        val compatibleTypes = compatibilityTable[CurrentAppUser.bloodType!!]
        bloodTypeFilterLayout.children.forEach { linearLayout ->
            (linearLayout as LinearLayout).children.forEach {
                if(it is ToggleButton){
                    it.isChecked = compatibleTypes!!.contains(it.text)
                }
            }
        }
    }
}