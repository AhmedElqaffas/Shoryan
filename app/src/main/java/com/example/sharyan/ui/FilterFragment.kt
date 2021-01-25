package com.example.sharyan.ui

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.core.view.children
import com.example.sharyan.data.BloodType
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.RequestsFiltersContainer
import com.example.sharyan.databinding.FragmentFilterBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_filter.design_bottom_sheet

class FilterFragment(private val filterHolder: FilterHolder, private val requestsFiltersContainer: RequestsFiltersContainer?)
    : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        submitFiltersToParent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWindowSize()
        restoreViewsState(requestsFiltersContainer)
        binding.clearBloodTypeFilter.setOnClickListener{ clearBloodTypeFilters() }
        binding.matchingBloodFilterButton.setOnClickListener { chooseCompatibleTypesWithUser() }
    }

    private fun setWindowSize(){
        dialog?.also {
            val window: Window? = dialog!!.window
            val size = Point()
            val display: Display = window!!.windowManager.defaultDisplay
            display.getSize(size)
            val windowHeight = size.y
            design_bottom_sheet.layoutParams.height = windowHeight
            val behavior = BottomSheetBehavior.from<View>(design_bottom_sheet)
            behavior.peekHeight = windowHeight
            view?.requestLayout()
        }
    }

    /**
     * The user may have opened this fragment and chosen some filters before, to restore the filters,
     * an object (requestsFiltersContainer) is used to preserve the filters information.
     * This method takes a requestsFiltersContainer as a parameter to change the layout of the
     * fragment according to the filters in the requestsFilter.
     * @param requestsFiltersContainer An object that encapsulates the filters chosen by the user.
     */
    private fun restoreViewsState(requestsFiltersContainer: RequestsFiltersContainer?){
        requestsFiltersContainer?.let{
            restoreBloodTypesFilter(it.bloodType)
        }
    }

    private fun restoreBloodTypesFilter(bloodTypesFilter: Set<BloodType>) {
        binding.bloodTypeFilterLayout.children.forEach { linearLayout ->
            (linearLayout as LinearLayout).children.forEach {
                if(it is ToggleButton && bloodTypesFilter.contains(BloodType.fromString(it.text.toString())) ){
                    it.isChecked = true
                }
            }
        }
    }

    private fun clearBloodTypeFilters(){
        binding.bloodTypeFilterLayout.children.forEach { linearLayout ->
            (linearLayout as LinearLayout).children.forEach {
                if(it is ToggleButton){
                    it.isChecked = false
                }
           }
        }
    }

    private fun getSelectedBloodTypeFiltersSet(): MutableSet<BloodType>{
        val selectedTypes = mutableSetOf<BloodType>()
        binding.bloodTypeFilterLayout.children.forEach { linearLayout ->
            (linearLayout as LinearLayout).children.forEach {
                if(it is ToggleButton && it.isChecked){
                    println(it.text.toString())
                    selectedTypes.add(BloodType.fromString(it.text.toString()))
                }
            }
        }
        return selectedTypes
    }

    private fun chooseCompatibleTypesWithUser(){
        val compatibleTypes = CurrentAppUser.bloodType!!.getCompatibleTypes()
        binding.bloodTypeFilterLayout.children.forEach { linearLayout ->
            (linearLayout as LinearLayout).children.forEach {
                if(it is ToggleButton){
                    it.isChecked = compatibleTypes.contains(BloodType.fromString(it.text.toString()))
                }
            }
        }
    }

    private fun submitFiltersToParent(){
        val bloodTypeSet = getSelectedBloodTypeFiltersSet()
        if(bloodTypeSet.isNotEmpty())
            filterHolder.submitFilters(RequestsFiltersContainer(bloodTypeSet))
        else
            filterHolder.submitFilters(null)
    }
}