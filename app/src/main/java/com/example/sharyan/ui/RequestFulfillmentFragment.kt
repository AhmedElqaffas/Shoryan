package com.example.sharyan.ui

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.viewModels
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.R
import com.example.sharyan.data.DonationAbility
import com.example.sharyan.data.DonationDetails
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
        showIndefiniteMessage(resources.getString(R.string.checking_donating_ability))
        getDonationDetails()
        setupMap()
        donateButton.setOnClickListener { startDonation() }
        cancelDonationButton.setOnClickListener { cancelDonation() }
        confirmDonationButton.setOnClickListener { confirmDonation() }
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
        if(requestViewModel.isAlreadyDonatingToThisRequest(request.id)){
            showCancelAndConfirmButtons()
        }
        fetchRequestDetails()
    }

    private fun fetchRequestDetails(){
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

    private fun donationDetailsReceived(donationDetails: DonationDetails){
        request = donationDetails.request
        displayRequestDetails()
        updateMapLocation()
        disableShimmer()
        enableOrDisableDonation(donationDetails.donationAbility)
        dismissSnackBar()
    }

    private fun displayRequestDetails(){
        requestBloodType.text = request.bloodType
        requesterName.text = request.requester?.name?.getFullName()
        requestLocation.text = resources.getString(R.string.address_full,
            request.bloodBank?.name,
            EnglishToArabicConverter().convertDigits(request.bloodBank?.location?.buildingNumber.toString()),
            request.bloodBank?.location?.streetName,
            request.bloodBank?.location?.region,
            request.bloodBank?.location?.governorate)
        val remainingBags = request.numberOfBagsRequired!! - request.numberOfBagsFulfilled!!
        requestBagsRequired.text = resources.getString(R.string.blood_bags,
            EnglishToArabicConverter().convertDigits(remainingBags.toString()))
        personsDonatingToRequest.text = resources.getString(R.string.persons_going,
            EnglishToArabicConverter().convertDigits(request.numberOfComingDonors.toString()))
        requestDetailsLayout.visibility = View.VISIBLE
    }

    private fun updateMapLocation(){
        mapInstance.apply {
            addMarker(MarkerOptions().position(LatLng(request.bloodBank!!.location.latitude,
                request.bloodBank!!.location.longitude)))
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(request.bloodBank!!.location.latitude,
                request.bloodBank!!.location.longitude)))
        }
    }

    private fun disableShimmer(){
        requestDetailsShimmer.visibility = View.INVISIBLE
    }

    private fun enableOrDisableDonation(donationAbility: DonationAbility) {
        if(donationAbility.canUserDonate && !requestViewModel.isAlreadyDonatingToThisRequest(request.id)){
            enableDonation()
        }
        else if(!donationAbility.canUserDonate){
            showIndefiniteMessage(donationAbility.reasonForDisability!!)
            disableDonation()
        }
    }

    private fun startDonation(){
        requestViewModel.addUserToDonorsList(request.id).observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()){
                showCancelAndConfirmButtons()
                requestViewModel.setUserPendingRequest(request.id)
            }
            else{
                showDefiniteMessage(it)
            }
        }
    }

    private fun cancelDonation(){
        suspendButtons(true)
        requestViewModel.cancelDonation(request.id).observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()){
                enableDonation()
                requestViewModel.removeUserPendingRequest()
                showDefiniteMessage("تم الغاء التبرّع")
            }
            else{
                showDefiniteMessage(it)
                suspendButtons(false)
            }
        }
    }

    private fun confirmDonation(){
        suspendButtons(true)
        requestViewModel.confirmDonation(request.id).observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()){
                requestViewModel.removeUserPendingRequest()
                showDefiniteMessage("شكراً لتبرّعك")
                disableDonation()
            }
            else{
                showDefiniteMessage(it)
                suspendButtons(false)
            }
        }
    }

    private fun disableDonation(){
        donateButton.visibility = View.VISIBLE
        donateButton.isEnabled = false
        donateButton.alpha = 0.5f
        waitingConfirmationSentence.visibility = View.GONE
        cancelDonationButton.visibility = View.GONE
        confirmDonationButton.visibility = View.GONE
    }

    private fun enableDonation(){
        donateButton.visibility = View.VISIBLE
        donateButton.alpha = 1.0f
        donateButton.isEnabled = true
        waitingConfirmationSentence.visibility = View.GONE
        cancelDonationButton.visibility = View.GONE
        confirmDonationButton.visibility = View.GONE
    }

    private fun showCancelAndConfirmButtons(){
        donateButton.visibility = View.GONE
        waitingConfirmationSentence.visibility = View.VISIBLE
        cancelDonationButton.visibility = View.VISIBLE
        confirmDonationButton.visibility = View.VISIBLE
    }

    private fun suspendButtons(shouldSuspendButtons: Boolean){
        donateButton.isEnabled = !shouldSuspendButtons
        cancelDonationButton.isEnabled = !shouldSuspendButtons
        confirmDonationButton.isEnabled = !shouldSuspendButtons
    }

    private fun showIndefiniteMessage(message: String){
        snackbar = Snackbar.make(design_bottom_sheet,
            message,
            Snackbar.LENGTH_INDEFINITE)
        snackbar?.show()
    }

    private fun showDefiniteMessage(message: String){
        snackbar = Snackbar.make(design_bottom_sheet,
            message,
            Snackbar.LENGTH_LONG)
            .setAction(R.string.ok){}
            .setActionTextColor(resources.getColor(R.color.colorAccent))
        snackbar?.show()
    }

    private fun dismissSnackBar(){
        snackbar?.dismiss()
    }
}