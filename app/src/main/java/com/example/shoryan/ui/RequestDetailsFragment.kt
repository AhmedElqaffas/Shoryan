package com.example.shoryan.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.shoryan.AndroidUtility
import com.example.shoryan.BR
import com.example.shoryan.R
import com.example.shoryan.data.ServerError
import com.example.shoryan.databinding.FragmentMyRequestDetailsBinding
import com.example.shoryan.databinding.FragmentRequestFulfillmentBinding
import com.example.shoryan.interfaces.RequestsRecyclerInteraction
import com.example.shoryan.viewmodels.MyRequestDetailsViewModel
import com.example.shoryan.viewmodels.RequestDetailsViewModel
import com.example.shoryan.viewmodels.RequestFulfillmentViewModel
import com.example.shoryan.viewmodels.TokensViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
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

    val tokensViewModel: TokensViewModel by viewModels()
    @Inject lateinit var requestFulfillmentFactory: RequestFulfillmentViewModel.AssistedFactory
    val requestFulfillmentViewModel: RequestFulfillmentViewModel by viewModels{
        RequestFulfillmentViewModel.provideFactory(requestFulfillmentFactory, getClickedRequest())
    }

    @Inject lateinit var myRequestDetailsFactory: MyRequestDetailsViewModel.AssistedFactory
    val myRequestDetailsViewModel: MyRequestDetailsViewModel by viewModels{
        MyRequestDetailsViewModel.provideFactory(myRequestDetailsFactory, getClickedRequest())
    }

    private val viewModelsMap: Map<Int, RequestDetailsViewModel> by lazy{
        mapOf(
                (MY_REQUEST_BINDING to myRequestDetailsViewModel),
                (REQUEST_FULFILLMENT_BINDING to requestFulfillmentViewModel)
        )
    }

    private var apiCallJob: Job? = null
    private lateinit var mapInstance: GoogleMap
    private var snackbar: Snackbar? = null
    private lateinit var binding: ViewDataBinding
    private var fragmentType: Int = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentType = getFragmentType()
        binding = if(fragmentType == MY_REQUEST_BINDING){
           FragmentMyRequestDetailsBinding.inflate(inflater)
        }
        else{
            FragmentRequestFulfillmentBinding.inflate(inflater)
        }
        binding.setVariable(BR.viewmodel, viewModelsMap[getFragmentType()]!!)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apiCallJob?.cancel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (parentFragment as RequestsRecyclerInteraction).onRequestCardDismissed()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setWindowSize()
        setupMap()
        fetchDonationDetails()
        observeViewModelEvents(viewModelsMap[getFragmentType()]!!)
    }

    private fun getFragmentType() = requireArguments().getInt(ARGUMENT_BINDING_KEY)

    private fun getClickedRequest() = requireArguments().getString(ARGUMENT_REQUEST_KEY)!!

    private fun setWindowSize(){
        val bottomSheetView = binding.root.findViewById(R.id.design_bottom_sheet) as View
        bottomSheetView.layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
    }

    private fun showDefiniteMessage(message: String){
        showMessage(message, Snackbar.LENGTH_LONG)
    }

    private fun showMessage(message: String, duration: Int){
        snackbar = Snackbar.make(binding.root, message, duration)
        snackbar!!.setAction(R.string.ok){}
        snackbar!!.setActionTextColor(resources.getColor(R.color.colorAccent))
        snackbar!!.show()
    }

    private fun setupMap(){
        val mapFragmentObject =  childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragmentObject.getMapAsync {
            mapInstance = it
            it.apply {
                setMapPaddingWhenLayoutIsReady()
                observeMapLocation(viewModelsMap[getFragmentType()]!!)
            }
        }
    }

    private fun setMapPaddingWhenLayoutIsReady(){
        val mapContainer: View = binding.javaClass.getField("mapFragmentContainer")[binding] as View
        mapContainer.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mapContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                mapInstance.setPadding(0, 0, 0, mapContainer.height / 4)
            }
        })
    }

    private fun fetchDonationDetails(){
        apiCallJob?.cancel()
        apiCallJob = lifecycleScope.launch {
            viewModelsMap[getFragmentType()]?.apply { getDonationDetails(getClickedRequest()) }
        }
    }

    private fun showTryAgainSnackbar(whatToTry: () -> Unit){
        showMessage(resources.getString(R.string.fetching_data_error), Snackbar.LENGTH_INDEFINITE)
        snackbar!!.setAction(R.string.try_again) {
            whatToTry()
        }
    }

    private fun observeMapLocation(viewModel: RequestDetailsViewModel){
        mapInstance.apply {
            viewModel.donationDetails.observe(viewLifecycleOwner){
                it?.request?.bloodBank?.location?.let { location ->
                    val latLng = LatLng(location.latitude!!, location.longitude!!)
                    addMarker(MarkerOptions().position(latLng))
                    moveCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            }
        }
    }

    private fun observeViewModelEvents(viewModel: RequestDetailsViewModel){
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.eventsFlow.collect {
                when(it){
                    is RequestDetailsViewModel.RequestDetailsViewEvent.ShowSnackBar -> showDefiniteMessage(resources.getString(it.stringResourceId))
                    is RequestDetailsViewModel.RequestDetailsViewEvent.ShowTryAgainSnackBar -> showTryAgainSnackbar { fetchDonationDetails() }
                    RequestDetailsViewModel.RequestDetailsViewEvent.DismissFragment -> closeBottomSheetDialog()
                    is RequestDetailsViewModel.RequestDetailsViewEvent.CallPatient -> openDialerApp(it.phoneNumber)
                    is RequestDetailsViewModel.RequestDetailsViewEvent.RequestDetailsError -> handleError(it.error)
                }
            }
        }
    }

    private fun handleError(error: ServerError){
        if(error == ServerError.JWT_EXPIRED){
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                val response = tokensViewModel.getNewAccessToken(requireContext())
                // If an error happened when refreshing tokens, log user out
                response.error?.let{
                    AndroidUtility.forceLogOut(requireContext())
                }
            }
        }
        else if(error == ServerError.CONNECTION_ERROR){
            AndroidUtility.makeTryAgainSnackbar(
                binding.root.findViewById(R.id.dummyView) as View,
                resources.getString(error.errorStringResource),
                ::fetchDonationDetails
            ).show()
        }
        else{
            error.doErrorAction(binding.root.findViewById(R.id.dummyView) as View)
        }
    }

    private fun closeBottomSheetDialog() {
        requireParentFragment().childFragmentManager.findFragmentByTag("requestDetails")?.let {
            requireParentFragment().childFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun openDialerApp(phoneNumber: String){
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        requireActivity().startActivity(intent)
    }
}