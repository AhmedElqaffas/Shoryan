package com.example.sharyan.ui

import android.graphics.Point
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_request_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class RequestFulfillmentFragment : BottomSheetDialogFragment() {

    companion object {

        const val ARGUMENT_KEY = "request"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param donationRequest - The clicked request object.
         * @return A new instance of fragment RequestDetailsFragment.
         */

        @JvmStatic
        fun newInstance(donationRequest: DonationRequest) =
            RequestFulfillmentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARGUMENT_KEY, donationRequest)
                }
            }
    }

    private lateinit var request: DonationRequest
    private var apiCallJob: Job? = null
    private val requestViewModel: RequestFulfillmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        request = getClickedRequest()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRequestDetails()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiCallJob?.cancel()
    }

    override fun onStart() {
        super.onStart()
        setWindowSize()
    }

    private fun getClickedRequest(): DonationRequest{
        return requireArguments().getSerializable(ARGUMENT_KEY) as DonationRequest
    }

    private fun setWindowSize(){
        dialog?.also {
            val window: Window? = dialog!!.window
            val size = Point()
            val display: Display = window!!.windowManager.defaultDisplay
            display.getSize(size)
            val height: Int = size.y
            design_bottom_sheet.layoutParams.height = (height*0.85).toInt()
            val behavior = BottomSheetBehavior.from<View>(design_bottom_sheet)
            behavior.peekHeight = height
            view?.requestLayout()
        }
    }

    private fun getRequestDetails(){
        apiCallJob = CoroutineScope(Dispatchers.Main).launch {
            requestViewModel.getRequestDetails(request.id).observe(viewLifecycleOwner){
                println(it)
            }
        }
    }


}