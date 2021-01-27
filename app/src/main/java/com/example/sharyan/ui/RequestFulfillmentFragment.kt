package com.example.sharyan.ui

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.R
import com.example.sharyan.data.DonationAbility
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.databinding.FragmentRequestFulfillmentBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_request_fulfillment.design_bottom_sheet
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
    private var snackbar: Snackbar? = null
    private lateinit var binding: FragmentRequestFulfillmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRequestFulfillmentBinding.inflate(inflater, container, false)
        binding.viewmodel = requestViewModel
        binding.englishArabicConverter = EnglishToArabicConverter()
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiCallJob?.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        request = getClickedRequest()
        setWindowSize()
        showIndefiniteMessage(resources.getString(R.string.checking_donating_ability))
        setupMap()
        getDonationDetails()
        binding.donateButton.setOnClickListener { startDonation() }
        binding.cancelDonationButton.setOnClickListener { cancelDonation() }
        binding.confirmDonationButton.setOnClickListener { confirmDonation() }
    }

    private fun getClickedRequest() = requireArguments().getSerializable(ARGUMENT_KEY) as DonationRequest

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

    private fun showIndefiniteMessage(message: String){
        snackbar = Snackbar.make(design_bottom_sheet, message, Snackbar.LENGTH_INDEFINITE)
        ViewCompat.setLayoutDirection(snackbar!!.view, ViewCompat.LAYOUT_DIRECTION_RTL)
        snackbar?.show()
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
        binding.mapFragmentContainer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.mapFragmentContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                mapInstance.setPadding(0, 0, 0, (binding.mapFragmentContainer.height/4))
            }
        })
    }

    private fun getDonationDetails(){
        apiCallJob = CoroutineScope(Dispatchers.Main).launch {
            requestViewModel.getDonationDetails(request.id).observe(viewLifecycleOwner){
                if(it != null){
                    donationDetailsReceived(it)
                }
                else{
                    showDefiniteMessage(resources.getString(R.string.fetching_data_error))
                }
            }
        }
    }

    private fun showDefiniteMessage(message: String){
        snackbar = Snackbar.make(design_bottom_sheet,
            message,
            Snackbar.LENGTH_LONG)
            .setAction(R.string.ok){}
            .setActionTextColor(resources.getColor(R.color.colorAccent))
        ViewCompat.setLayoutDirection(snackbar!!.view, ViewCompat.LAYOUT_DIRECTION_RTL)
        snackbar?.show()
    }


    private fun donationDetailsReceived(donationDetails: DonationDetails){
        request = donationDetails.request
        showDonationDisabilityReasonIfExists(donationDetails.donationAbility)
        updateMapLocation()
        binding.requestDetailsShimmer.stopShimmer()
        snackbar?.dismiss()
    }

    private fun updateMapLocation(){
        mapInstance.apply {
            addMarker(MarkerOptions().position(LatLng(request.bloodBank!!.location.latitude,
                request.bloodBank!!.location.longitude)))
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(request.bloodBank!!.location.latitude,
                request.bloodBank!!.location.longitude)))
        }
    }

    private fun showDonationDisabilityReasonIfExists(donationAbility: DonationAbility) {
        if(!donationAbility.canUserDonate){
            showIndefiniteMessage(donationAbility.reasonForDisability!!)
        }
    }

    private fun startDonation(){
        requestViewModel.addUserToDonorsList(request.id).observe(viewLifecycleOwner){ errorMessage ->
            if(!errorMessage.isNullOrEmpty()){
                showDefiniteMessage(errorMessage)
            }
        }
    }

    private fun cancelDonation(){
        requestViewModel.cancelDonation(request.id).observe(viewLifecycleOwner){ errorMessage ->
            if(errorMessage.isNullOrEmpty()){
                showDefiniteMessage("تم الغاء التبرّع")
            }
            else{
                showDefiniteMessage(errorMessage)
            }
        }
    }

    private fun confirmDonation(){
        requestViewModel.confirmDonation(request.id).observe(viewLifecycleOwner){ errorMessage ->
            if(errorMessage.isNullOrEmpty()){
                showDefiniteMessage("شكراً لتبرّعك")
            }
            else{
                showDefiniteMessage(errorMessage)
            }
        }
    }
}