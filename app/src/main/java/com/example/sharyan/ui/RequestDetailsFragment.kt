package com.example.sharyan.ui

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.sharyan.BR
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.R
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.databinding.FragmentMyRequestDetailsBinding
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class RequestDetailsFragment : BottomSheetDialogFragment(){

    companion object {

         const val ARGUMENT_REQUEST_KEY = "request"
         const val ARGUMENT_BINDING_KEY = "binding"
        const val MY_REQUEST_BINDING = 1
        const val REQUEST_FULFILLMENT_BINDING = 2

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param requestId - The clicked request id.
         * @param binding - A integer to select which xml layout to inflate, 1: fragment_my_request_details,
         * 2: fragment_request_fulfillment
         * @return A new instance of fragment RequestDetailsFragment.
         */

        @JvmStatic
        fun newInstance(requestId: String, binding: Int) =
            RequestDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGUMENT_REQUEST_KEY, requestId)
                    putInt(ARGUMENT_BINDING_KEY, binding)
                }
            }
    }

    private var apiCallJob: Job? = null
    private val requestFulfillmentViewModel: RequestFulfillmentViewModel by viewModels()
    private val myRequestDetailsViewModel: MyRequestDetailsViewModel by viewModels()
    private lateinit var mapInstance: GoogleMap
    private var snackbar: Snackbar? = null
    private lateinit var binding: ViewDataBinding
    private var fragmentType: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentType = requireArguments().getInt(ARGUMENT_BINDING_KEY)
        if(fragmentType == MY_REQUEST_BINDING){
            binding = DataBindingUtil.inflate(inflater,R.layout.fragment_my_request_details , container, false) as FragmentMyRequestDetailsBinding
            binding.setVariable(BR.viewmodel, myRequestDetailsViewModel)
            observeMessages(myRequestDetailsViewModel)
            listenForDismissRequest(myRequestDetailsViewModel)
        }
        else{
            binding = DataBindingUtil.inflate(inflater,R.layout.fragment_request_fulfillment , container, false) as FragmentRequestFulfillmentBinding
            binding.setVariable(BR.viewmodel, requestFulfillmentViewModel)
            observeMessages(requestFulfillmentViewModel)
            listenForDismissRequest(requestFulfillmentViewModel)
        }
        binding.setVariable(BR.englishArabicConverter, EnglishToArabicConverter())
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
        setupMap()
        getDonationDetails()
    }

    private fun getClickedRequest() = requireArguments().getString(ARGUMENT_REQUEST_KEY)!!

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
        when(binding){
            is FragmentRequestFulfillmentBinding -> (binding as FragmentRequestFulfillmentBinding).mapFragmentContainer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    (binding as FragmentRequestFulfillmentBinding).mapFragmentContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    mapInstance.setPadding(0, 0, 0, (binding as FragmentRequestFulfillmentBinding).mapFragmentContainer.height/4)
                }
            })

            is FragmentMyRequestDetailsBinding -> (binding as FragmentMyRequestDetailsBinding).mapFragmentContainer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    (binding as FragmentMyRequestDetailsBinding).mapFragmentContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    mapInstance.setPadding(0, 0, 0, (binding as FragmentMyRequestDetailsBinding).mapFragmentContainer.height/4)
                }
            })
        }
    }

    private fun getDonationDetails(){
        apiCallJob?.cancel()
        apiCallJob = lifecycleScope.launch {
            when(binding){
                is FragmentRequestFulfillmentBinding -> observeDonationDetails(requestFulfillmentViewModel)
                is FragmentMyRequestDetailsBinding -> observeDonationDetails(myRequestDetailsViewModel)
            }
        }
    }

    private suspend fun observeDonationDetails(viewModel: RequestDetailsViewModel){
        viewModel.getDonationDetails(getClickedRequest()).observe(viewLifecycleOwner) {
            if (it != null) {
                donationDetailsReceived(it)
            } else {
                showTryAgainSnackbar(::getDonationDetails)
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
            when(binding){
                is FragmentRequestFulfillmentBinding -> (binding as FragmentRequestFulfillmentBinding).requestDetailsShimmer.stopShimmer()
                is FragmentMyRequestDetailsBinding -> (binding as FragmentMyRequestDetailsBinding).requestDetailsShimmer.stopShimmer()
            }
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
    private fun observeMessages(viewModel: RequestDetailsViewModel){
        viewModel.message.observe(viewLifecycleOwner){
            it?.let { message ->
                showDefiniteMessage(message)
            }
        }
    }

    private fun listenForDismissRequest(viewModel: RequestDetailsViewModel){
        viewModel.shouldDismissFragment.observe(viewLifecycleOwner){
            if(it){
                closeBottomSheetDialog()
            }
        }
    }

    private fun closeBottomSheetDialog() {
        requireParentFragment().childFragmentManager.findFragmentByTag("requestDetails")?.let {
            requireParentFragment().childFragmentManager.beginTransaction().remove(it).commit()
        }
    }
}