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

    override fun onStart() {
        super.onStart()
        setWindowSize()
    }

    override fun onResume() {
        super.onResume()

        getDonationDetails()
        setupMap()
    }

    private fun setupMap(){
        val mapFragmentObject =  childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragmentObject.getMapAsync {
            mapInstance = it
            it.apply {
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
                    request = it
                    displayRequestDetails()
                    updateMapLocation()
                }
            }
        }
    }

    private fun displayRequestDetails(){
        requestBloodType.text = request.bloodType
        requesterName.text = request.requester.name?.getFullName()
        requestLocation.text = resources.getString(R.string.address_full,
            request.bloodBank.name,
            request.bloodBank.location.buildingNumber,
            request.bloodBank.location.streetName,
            request.bloodBank.location.region,
            request.bloodBank.location.governorate)
        requestBagsRequired.text = resources.getString(R.string.blood_bags,
            request.numberOfBagsRequired - request.numberOfBagsFulfilled)
        personsDonatingToRequest.text = resources.getString(R.string.persons_going, request.numberOfComingDonors)
    }

    private fun updateMapLocation(){
        mapInstance.apply {
            addMarker(MarkerOptions().position(LatLng(request.bloodBank.location.latitude,
                request.bloodBank.location.longitude)))
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(request.bloodBank.location.latitude,
                request.bloodBank.location.longitude)))
        }
    }
}