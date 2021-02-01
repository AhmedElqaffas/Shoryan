package com.example.sharyan.ui

import android.view.View
import androidx.lifecycle.*
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.RequestFulfillmentRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RequestFulfillmentViewModel: ViewModel() {

    private var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    // Private mutable liveData for the donation details obtained from the repository
    private val _donationDetails =  MutableLiveData<DonationDetails>()
    // A public liveData for the XML views to observe, the separation of the live data to two
    // (private mutable and public immutable) was used in a google codelab, so I thought it may
    // be best practice to do it
    val donationDetails: LiveData<DonationDetails> = _donationDetails
    // Instead of changing the CurrentAppUser repo to have a liveData, I created this liveData member
    // to observe changes
    private var currentUserPendingRequest = MutableLiveData<String>(CurrentAppUser.pendingRequestId)
    // The application is processing a query (contacting backend)
    private val _isInLoadingState = MutableLiveData(true)
    val isInLoadingState: LiveData<Boolean> = _isInLoadingState
    // The donation details have been loaded
    private val _areDonationDetailsLoaded = MutableLiveData(false)
    // The details layout should be visible when the donation details are loaded
    val donationDetailsLayoutVisibility: LiveData<Int> = Transformations.map(_areDonationDetailsLoaded){
        when(it){
            true -> View.VISIBLE
            false -> View.GONE
        }
    }
    // The shimmer layout should be hidden when the donation details are loaded
    val shimmerVisibility: LiveData<Int> = Transformations.map(_areDonationDetailsLoaded){
        when(it){
            true -> View.GONE
            false -> View.VISIBLE
        }
    }
    // LiveData to observe whether the user is allowed to donate or not, used to control XML views states
    private val _canUserDonate = MutableLiveData(false)
    val canUserDonate: LiveData<Boolean> = _canUserDonate

    // To trigger views states changes whenever the user pending request changes or whenever
    // the donation details are loaded, I used a mediator to combine those two liveData into one which
    // is triggered whenever one of the two sources is triggered
    private val acceptedRequestMediatorLiveData = MediatorLiveData<String>()

    // The donate button should have lower opacity when disabled (when user can't donate)
    val donateButtonAlpha: LiveData<Float> = Transformations.map(canUserDonate){
        when(it){
            true -> 1f
            false -> 0.5f
        }
    }

    // Instead of the xml view observing the donationDetails liveData and performing subtraction itself,
    // this view should observe this liveData which simplifies the xml
    val numberOfRemainingBags: LiveData<String> = Transformations.map(donationDetails){
        EnglishToArabicConverter().convertDigits(
            donationDetails.value?.request?.numberOfBagsRequired
                ?.minus(donationDetails.value?.request?.numberOfBagsFulfilled!!)
            .toString()
        )
    }

    // Publishes messages for the subscribed fragments to show
    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    init {
        acceptedRequestMediatorLiveData.addSource(currentUserPendingRequest){
                value: String? -> acceptedRequestMediatorLiveData.setValue(value)
        }

        acceptedRequestMediatorLiveData.addSource(donationDetails){
            // When th details are loaded, set the request id as this liveData value
                value: DonationDetails -> acceptedRequestMediatorLiveData.setValue(value.request?.id)
        }
    }

    /**
     * Maps changes in donationDetails and currentUserPendingRequest LiveData to an Integer LiveData
     * to be able to observe it in XML.
     */
    fun donateButtonVisibility(): LiveData<Int> = Transformations.map(acceptedRequestMediatorLiveData){
        when {
            it == null -> View.VISIBLE
            donationDetails.value?.request?.id == currentUserPendingRequest.value-> View.GONE
            else -> View.VISIBLE
        }
    }

    /**
     * Maps changes in donationDetails and currentUserPendingRequest LiveData to an Integer LiveData
     * to be able to observe it in XML. This method should be observed by views that are only visible
     * after the user clicks on "donate" button.
     */
    fun getWaitingDonationViewVisibility(): LiveData<Int> = Transformations.map(acceptedRequestMediatorLiveData){
        when (donationDetails.value?.request?.id) {
            null -> View.GONE
            currentUserPendingRequest.value -> View.VISIBLE
            else -> View.GONE
        }
    }

    fun getDonationDetails(requestId: String) = liveData(Dispatchers.IO) {
        val details = RequestFulfillmentRepo.getDonationDetails(bloodDonationAPI, requestId)
        emit(details)
        if(details?.request != null){
            _donationDetails.postValue(details)
            _areDonationDetailsLoaded.postValue(true)
            _isInLoadingState.postValue(false)
            _canUserDonate.postValue(details.donationAbility.canUserDonate)
            _message.postValue(details.donationAbility.reasonForDisability)
        }
    }

    fun startDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val processResultError = RequestFulfillmentRepo.addUserToDonorsList(bloodDonationAPI, requestId)
        if(processResultError.isNullOrEmpty()){
            setUserPendingRequest(requestId)
        }
        _message.postValue(processResultError)
        _isInLoadingState.postValue(false)
    }

    fun confirmDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val processResultError = RequestFulfillmentRepo.confirmDonation(bloodDonationAPI, requestId)
        if(processResultError.isNullOrEmpty()){
            removeUserPendingRequest(true)
        }
        _message.postValue(processResultError?: "شكراً لتبرّعك")
        _isInLoadingState.postValue(false)
    }

    fun cancelDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val processResultError = RequestFulfillmentRepo.cancelDonation(bloodDonationAPI, requestId)
        if(processResultError.isNullOrEmpty()){
            removeUserPendingRequest(false)
        }
        _message.postValue(processResultError?: "تم الغاء التبرّع")
        _isInLoadingState.postValue(false)
    }

    private fun setUserPendingRequest(requestId: String?){
        currentUserPendingRequest.postValue(requestId)
        CurrentAppUser.pendingRequestId = requestId
    }

    private fun removeUserPendingRequest(hasDonated: Boolean){
        setUserPendingRequest(null)
        // User can't donate again if they have already donated
        _canUserDonate.postValue(!hasDonated)
    }
}