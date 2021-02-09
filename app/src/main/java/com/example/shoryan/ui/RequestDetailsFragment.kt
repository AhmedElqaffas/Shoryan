package com.example.shoryan.ui

import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.BR
import com.example.shoryan.EnglishToArabicConverter
import com.example.shoryan.R
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.databinding.FragmentMyRequestDetailsBinding
import com.example.shoryan.databinding.FragmentRequestFulfillmentBinding
import com.example.shoryan.viewmodels.MyRequestDetailsViewModel
import com.example.shoryan.viewmodels.RequestDetailsViewModel
import com.example.shoryan.viewmodels.RequestFulfillmentViewModel
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
            observeViewModelEvents(myRequestDetailsViewModel)
        }
        else{
            binding = DataBindingUtil.inflate(inflater,R.layout.fragment_request_fulfillment , container, false) as FragmentRequestFulfillmentBinding
            binding.setVariable(BR.viewmodel, requestFulfillmentViewModel)
            observeViewModelEvents(requestFulfillmentViewModel)
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
        fetchDonationDetails()
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
        snackbar = Snackbar.make(binding.root, message, duration)
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

    private fun fetchDonationDetails(){
        apiCallJob?.cancel()
        apiCallJob = lifecycleScope.launch {
            when(binding){
                is FragmentRequestFulfillmentBinding -> requestFulfillmentViewModel.getDonationDetails(getClickedRequest())
                is FragmentMyRequestDetailsBinding -> myRequestDetailsViewModel.getDonationDetails(getClickedRequest())
            }
        }
    }

    private fun showTryAgainSnackbar(whatToTry: () -> Unit){
        showMessage(resources.getString(R.string.fetching_data_error), Snackbar.LENGTH_INDEFINITE)
        snackbar!!.setAction(R.string.try_again) {
            whatToTry()
        }
    }

    private fun updateMapLocation(location: LatLng){
        mapInstance.apply {
            addMarker(MarkerOptions().position(location))
            moveCamera(CameraUpdateFactory.newLatLng(location))
        }
    }

    private fun observeViewModelEvents(viewModel: RequestDetailsViewModel){
        viewModel.eventsFlow.onEach {
            when(it){
                is ViewEvent.ShowTryAgainSnackBar -> showTryAgainSnackbar { fetchDonationDetails() }
                is ViewEvent.ShowSnackBar -> showDefiniteMessage(it.text)
                is ViewEvent.UpdateMapLocation -> updateMapLocation(it.latlng)
                ViewEvent.DismissFragment -> closeBottomSheetDialog()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }


    private fun closeBottomSheetDialog() {
        requireParentFragment().childFragmentManager.findFragmentByTag("requestDetails")?.let {
            requireParentFragment().childFragmentManager.beginTransaction().remove(it).commit()
        }
    }
}