package com.example.sharyan.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.R
import com.example.sharyan.data.CreateNewRequestResponse
import com.example.sharyan.data.DonationAbility
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.data.DonationRequest
import com.example.sharyan.databinding.FragmentMyRequestDetailsBinding
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


class MyRequestDetailsFragment : BottomSheetDialogFragment(){

    companion object {

        const val ARGUMENT_KEY = "request"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param requestID - The clicked request object's ID.
         * @return A new instance of fragment RequestDetailsFragment.
         */

        @JvmStatic
        fun newInstance(requestID : String) =
            MyRequestDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGUMENT_KEY, requestID)
                }
            }
    }
    private lateinit var requestID: String
    private lateinit var donationRequest: DonationRequest
    private var apiCallJob: Job? = null
    private val requestViewModel: MyRequestDetailsViewModel by viewModels()
    private lateinit var mapInstance: GoogleMap
    private var snackbar: Snackbar? = null
    private lateinit var binding: FragmentMyRequestDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyRequestDetailsBinding.inflate(inflater, container, false)
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
        requestID = getClickedRequest()
        setWindowSize()
        setupMap()
        getDonationDetails()
        binding.cancelRequestButton.setOnClickListener {checkIfUserIsSure()}
    }

    private fun getClickedRequest(): String = requireArguments().getString(ARGUMENT_KEY) as String

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
            requestViewModel.getDonationDetails(requestID).observe(viewLifecycleOwner){
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
        donationDetails.request?.apply {
            donationRequest = this
            updateMapLocation()
            binding.requestDetailsShimmer.stopShimmer()
            snackbar?.dismiss()
        }
    }

    private fun updateMapLocation(){
        mapInstance.apply {
            addMarker(MarkerOptions().position(LatLng(donationRequest.bloodBank!!.location.latitude,
                donationRequest.bloodBank!!.location.longitude)))
            moveCamera(CameraUpdateFactory.newLatLng(LatLng(donationRequest.bloodBank!!.location.latitude,
                donationRequest.bloodBank!!.location.longitude)))
        }
    }


    private fun cancelRequest(){
        /*requestViewModel.cancelRequest().observe(viewLifecycleOwner){ errorMessage ->
            if(errorMessage.isNullOrEmpty()){
                showDefiniteMessage("تم الغاء الطلب")
                closeBottomSheetDialog()
            }
            else{
                showDefiniteMessage(errorMessage)
            }
        }*/
    }

    private fun closeBottomSheetDialog() {
        val activity = requireActivity()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    private fun checkIfUserIsSure(){
        val builder = AlertDialog.Builder(requireActivity())
        val positiveButtonClick = { dialog: DialogInterface, which: Int -> cancelRequest() }
        with(builder)
        {
            setMessage("هل انت متأكد من انك تريد الغاء الطلب؟")
            setPositiveButton("نعم", DialogInterface.OnClickListener(function = positiveButtonClick))
            setNegativeButton("لا",null)
            show()
        }

    }

}