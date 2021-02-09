package com.example.shoryan.viewmodels

import android.view.View
import androidx.lifecycle.*
import com.example.shoryan.data.CurrentAppUser
import com.example.shoryan.data.DonationDetails
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.repos.RequestFulfillmentRepo
import kotlinx.coroutines.launch


class RequestFulfillmentViewModel: RequestDetailsViewModel() {

    // Instead of changing the CurrentAppUser repo to have a liveData, I created this liveData member
    // to observe changes
    private var currentUserPendingRequest = MutableLiveData<String>(CurrentAppUser.pendingRequestId)

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
                value: DonationDetails -> acceptedRequestMediatorLiveData.setValue(value.request?.id)
        }
    }

    override suspend fun getDonationDetails(requestId: String): LiveData<DonationDetails?> {
        val details = super.getDonationDetails(requestId)
        if(areDetailsFetchedSuccessfully(details.value)){
            _canUserDonate.postValue(details.value?.donationAbility?.canUserDonate)
            showDonationDisabilityReasonIfExists(details.value)
        }
        return details
    }

    private suspend fun showDonationDisabilityReasonIfExists(details: DonationDetails?) {
        details?.donationAbility?.reasonForDisability?.apply {
            _eventsFlow.emit(ViewEvent.ShowSnackBar(this))
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

    fun startDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val processResultError = RequestFulfillmentRepo.addUserToDonorsList(bloodDonationAPI, requestId)
        if(processResultError.isNullOrEmpty()){
            setUserPendingRequest(requestId)
        }
        processResultError?.apply { _eventsFlow.emit(ViewEvent.ShowSnackBar(this)) }
        _isInLoadingState.postValue(false)
    }

    fun confirmDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val processResultError = RequestFulfillmentRepo.confirmDonation(bloodDonationAPI, requestId)
        if(processResultError.isNullOrEmpty()){
            removeUserPendingRequest(true)
        }
        _eventsFlow.emit(ViewEvent.ShowSnackBar(processResultError?: "شكراً لتبرّعك"))
        _isInLoadingState.postValue(false)
    }

    fun cancelDonation(requestId: String) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val processResultError = RequestFulfillmentRepo.cancelDonation(bloodDonationAPI, requestId)
        if(processResultError.isNullOrEmpty()){
            removeUserPendingRequest(false)
        }
        _eventsFlow.emit(ViewEvent.ShowSnackBar(processResultError?: "تم الغاء التبرّع"))
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