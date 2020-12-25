package com.example.sharyan.ui

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.viewModels
import com.example.sharyan.R
import com.example.sharyan.data.DonationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_request_fulfillment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class RequestFulfillmentFragment : BottomSheetDialogFragment(){

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
    private lateinit var mapInstance: GoogleMap
    private var checkingAbilitySnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        request = getClickedRequest()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_fulfillment, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiCallJob?.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWindowSize()
        checkIfUserCanFulfilRequest()
        setupMap()
        getDonationDetails()
    }

    private fun setupMap(){
        val mapFragmentObject =  childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragmentObject.getMapAsync {
            mapInstance = it
            it.apply {
                addMarker(MarkerOptions().position(LatLng(30.048158, 31.371376)))
                moveCamera(CameraUpdateFactory.newLatLng(LatLng(30.048158, 31.371376)))
                setMapPaddingWhenLayoutIsReady()
            }
        }
    }

    private fun setMapPaddingWhenLayoutIsReady(){
        mapFragmentContainer!!.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mapFragmentContainer!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                mapInstance.setPadding(0, 0, 0, (mapFragmentContainer!!.height/4))
            }
        })
    }

    private fun checkIfUserCanFulfilRequest(){
        checkingAbilitySnackbar = Snackbar.make(design_bottom_sheet,
                resources.getString(R.string.checking_donating_ability),
                Snackbar.LENGTH_INDEFINITE)
        checkingAbilitySnackbar?.show()
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
            val windowHeight = size.y
            design_bottom_sheet.layoutParams.height = (windowHeight)
            val behavior = BottomSheetBehavior.from<View>(design_bottom_sheet)
            behavior.peekHeight = windowHeight
            view?.requestLayout()
        }
    }

    private fun getDonationDetails(){
        apiCallJob = CoroutineScope(Dispatchers.Main).launch {
            requestViewModel.getRequestDetails(request.id).observe(viewLifecycleOwner){
                it?.let {
                    displayRequestDetails(it)
                }

            }
        }
    }

    private fun displayRequestDetails(request: DonationRequest){
        requestBloodType.text = request.bloodType
        requesterName.text = request.requester.name?.getFullName()
        requestLocation.text = request.donationLocation.region
        requestBagsRequired.text = resources.getString(R.string.blood_bags, request.numberOfBagsRequired)

    }
}