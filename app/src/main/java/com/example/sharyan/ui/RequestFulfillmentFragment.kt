package com.example.sharyan.ui

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.R
import com.example.sharyan.data.DonationDetails
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
         * @param requestId - The clicked request id.
         * @return A new instance of fragment RequestDetailsFragment.
         */

        @JvmStatic
        fun newInstance(requestId: String) =
            RequestFulfillmentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGUMENT_KEY, requestId)
                }
            }
    }

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
        setWindowSize()
        showIndefiniteMessage(resources.getString(R.string.loading_details))
        setupMap()
        getDonationDetails()
        observeMessages()
    }

    private fun getClickedRequest() = requireArguments().getString(ARGUMENT_KEY)!!

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
        showMessage(message, Snackbar.LENGTH_INDEFINITE)
    }

    private fun showDefiniteMessage(message: String){
        showMessage(message, Snackbar.LENGTH_LONG)
    }

    private fun showMessage(message: String, duration: Int){
        snackbar = Snackbar.make(design_bottom_sheet, message, duration)
        snackbar!!.setAction(R.string.ok){}
        snackbar!!.setActionTextColor(resources.getColor(R.color.colorAccent))
        ViewCompat.setLayoutDirection(snackbar!!.view, ViewCompat.LAYOUT_DIRECTION_RTL)
        snackbar!!.show()
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
        apiCallJob?.cancel()
        apiCallJob = CoroutineScope(Dispatchers.Main).launch {
            requestViewModel.getDonationDetails(getClickedRequest()).observe(viewLifecycleOwner){
                if(it != null){
                    donationDetailsReceived(it)
                }
                else{
                    showTryAgainSnackbar(::getDonationDetails)
                }
            }
        }
    }

    private fun showTryAgainSnackbar(whatToTry: () -> Unit){
        showMessage(resources.getString(R.string.fetching_data_error), Snackbar.LENGTH_INDEFINITE)
        snackbar!!.setAction(R.string.try_again) {
             whatToTry()
        }
    }

    private fun donationDetailsReceived(donationDetails: DonationDetails){
        donationDetails.request?.apply {
            updateMapLocation(LatLng(this.bloodBank!!.location.latitude, this.bloodBank.location.longitude))
            binding.requestDetailsShimmer.stopShimmer()
            snackbar?.dismiss()
        }
    }

    private fun updateMapLocation(location: LatLng){
        mapInstance.apply {
            addMarker(MarkerOptions().position(location))
            moveCamera(CameraUpdateFactory.newLatLng(location))
        }
    }

    /**
     * Observes messages published by the viewmodel and shows them to the user in the form of
     * a snackbar
     */
    private fun observeMessages(){
        requestViewModel.message.observe(viewLifecycleOwner){
            it?.let { message ->
                showDefiniteMessage(message)
            }
        }
    }
}