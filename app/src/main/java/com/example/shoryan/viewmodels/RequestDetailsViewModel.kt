package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.R
import com.example.shoryan.data.*
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RequestFulfillmentRepo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class RequestDetailsViewModel(protected val bloodDonationAPI: RetrofitBloodDonationInterface,
                                   protected val requestId: String): ViewModel() {

    sealed class RequestDetailsViewEvent{
        data class ShowSnackBar(val stringResourceId: Int): RequestDetailsViewEvent()
        data class UserCantDonate(val reason: String): RequestDetailsViewEvent()
        data class ShowTryAgainSnackBar(val stringResourceId: Int): RequestDetailsViewEvent()
        object DismissFragment: RequestDetailsViewEvent()
        data class CallPatient(val phoneNumber: String): RequestDetailsViewEvent()
    }

    private val _donationDetails =  MutableLiveData<DonationDetails?>()
    val donationDetails: LiveData<DonationDetails?> = _donationDetails
    // The application is processing a query (contacting backend)
    protected val _isInLoadingState = MutableLiveData(true)
    val isInLoadingState: LiveData<Boolean> = _isInLoadingState
    // The donation details have been loaded
    private val _areDonationDetailsLoaded = MutableLiveData(false)
    val areDonationDetailsLoaded = _areDonationDetailsLoaded
    // Instead of the xml view observing the donationDetails liveData and performing subtraction itself,
    // this view should observe this liveData which simplifies the xml
    val numberOfRemainingBags: LiveData<Int> = Transformations.map(donationDetails){
            donationDetails.value?.request?.numberOfBagsRequired
                ?.minus(donationDetails.value?.request?.numberOfBagsFulfilled!!)
    }

    // A mechanism to push events to the fragment
    protected val _eventsFlow = MutableSharedFlow<RequestDetailsViewEvent>()
    val eventsFlow = _eventsFlow.asSharedFlow()

    open suspend fun getDonationDetails(requestId: String): LiveData<DonationDetails?> {
        val details = RequestFulfillmentRepo.getDonationDetails(bloodDonationAPI, requestId)
        if(areDetailsFetchedSuccessfully(details)){
            _donationDetails.value = details
            _areDonationDetailsLoaded.value = true
            _isInLoadingState.value = false
        }else{
            announceCommunicationFailure()
        }
        return MutableLiveData<DonationDetails?>(details)
    }

    protected fun areDetailsFetchedSuccessfully(details: DonationDetails?): Boolean {
        return details?.request != null
    }

    private suspend fun announceCommunicationFailure(){
        _eventsFlow.emit(RequestDetailsViewEvent.ShowTryAgainSnackBar(R.string.connection_error))
    }

    fun refresh(){
        viewModelScope.launch {
            getDonationDetails(requestId)
        }
    }

    fun updateDonationDetails(update: DonationRequestUpdate){
        _donationDetails.postValue(createDonationDetailsFromUpdate(update))
    }

    private fun createDonationDetailsFromUpdate(update: DonationRequestUpdate): DonationDetails{
        val currentRequestDetails = _donationDetails.value!!.request
        val updatedRequestDetails = currentRequestDetails!!.copy(
            numberOfBagsFulfilled = update.numberOfBagsFulfilled,
            numberOfBagsRequired = update.numberOfBagsRequired,
            numberOfComingDonors = update.numberOfComingDonors
        )
        return DonationDetails(updatedRequestDetails, DonationAbility(true))
    }
}