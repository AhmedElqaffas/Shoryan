package com.example.shoryan.viewmodels

import android.view.View
import androidx.lifecycle.*
import com.example.shoryan.R
import com.example.shoryan.data.CurrentSession
import com.example.shoryan.data.DonationDetailsResponse
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RequestFulfillmentRepo
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


class RequestFulfillmentViewModel @Inject constructor(
        bloodDonationAPI: RetrofitBloodDonationInterface,
        @Named("requestId") requestId: String
): RequestDetailsViewModel(bloodDonationAPI, requestId) {

    // Instead of changing the CurrentSession repo to have a liveData, I created this liveData member
    // to observe changes
    private var currentUserPendingRequest = MutableLiveData<String?>(CurrentSession.pendingRequestId)

    // LiveData to observe whether the user is allowed to donate or not, used to control XML views states
    private val _canUserDonate = MutableLiveData(false)
    val canUserDonate: LiveData<Boolean> = _canUserDonate

    // The donate button should have lower opacity when disabled (when user can't donate)
    val donateButtonAlpha: LiveData<Float> = Transformations.map(canUserDonate){
        when(it){
            true -> 1f
            false -> 0.5f
        }
    }
    // To trigger views states changes whenever the user pending request changes or whenever
    // the donation details are loaded, I used a mediator to combine those two liveData into one which
    // is triggered whenever one of the two sources is triggered
    private val acceptedRequestMediatorLiveData = MediatorLiveData<String>()

    init {
        acceptedRequestMediatorLiveData.addSource(currentUserPendingRequest){
                value: String? -> acceptedRequestMediatorLiveData.setValue(value)
        }

        acceptedRequestMediatorLiveData.addSource(donationDetails){
            // When th details are loaded, set the request id as this liveData value
                value: DonationDetailsResponse -> acceptedRequestMediatorLiveData.setValue(value.request?.id)
        }
    }

    override suspend fun getDonationDetails(requestId: String): LiveData<DonationDetailsResponse> {
        val details = super.getDonationDetails(requestId)
        if(areDetailsFetchedSuccessfully(details.value!!)){
            _canUserDonate.postValue(details.value?.error == null)
            pushErrorToFragment(details.value?.error?.message)
        }
        return details
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

    fun startDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val updatedDonationRequest = RequestFulfillmentRepo.addUserToDonorsList(bloodDonationAPI, requestId)
        updatedDonationRequest.request?.let{
            println(it.toString())
            setUserPendingRequest(requestId)
            super.updateDonationDetails(it)
        }
        updatedDonationRequest.error?.message?.let{
            pushErrorToFragment(it)
        }
        _isInLoadingState.postValue(false)
    }

    fun confirmDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val updatedDonationRequest = RequestFulfillmentRepo.confirmDonation(bloodDonationAPI, requestId)
        updatedDonationRequest.request?.let{
            removeUserPendingRequest(true)
            _eventsFlow.emit(RequestDetailsViewEvent.ShowSnackBar(R.string.thanks_donation))
            super.updateDonationDetails(it)
        }
        updatedDonationRequest.error?.message?.let{
            pushErrorToFragment(it)
        }
        _isInLoadingState.postValue(false)
    }

    fun cancelDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val updatedDonationRequest = RequestFulfillmentRepo.cancelDonation(bloodDonationAPI, requestId)
        updatedDonationRequest.request?.let{
            removeUserPendingRequest(false)
            _eventsFlow.emit(RequestDetailsViewEvent.ShowSnackBar(R.string.donation_canceled))
            super.updateDonationDetails(it)
        }
        updatedDonationRequest.error?.message?.let{
            pushErrorToFragment(it)
        }
        _isInLoadingState.postValue(false)
    }

    private fun setUserPendingRequest(requestId: String?){
        currentUserPendingRequest.postValue(requestId)
        CurrentSession.pendingRequestId = requestId
    }

    private fun removeUserPendingRequest(hasDonated: Boolean){
        setUserPendingRequest(null)
        // User can't donate again if they have already donated
        _canUserDonate.postValue(!hasDonated)
    }


    fun callPatient(phoneNumber: String){
        viewModelScope.launch {
            _eventsFlow.emit(RequestDetailsViewEvent.CallPatient(phoneNumber))
        }
    }
}