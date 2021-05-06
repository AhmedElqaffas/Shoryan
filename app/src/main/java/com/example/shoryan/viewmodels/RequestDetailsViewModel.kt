package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.data.DonationDetailsResponse
import com.example.shoryan.data.DonationRequest
import com.example.shoryan.data.ServerError
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RequestFulfillmentRepo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class RequestDetailsViewModel(protected val bloodDonationAPI: RetrofitBloodDonationInterface,
                                   protected val requestId: String): ViewModel() {

    sealed class RequestDetailsViewEvent{
        data class ShowSnackBar(val stringResourceId: Int): RequestDetailsViewEvent()
        data class ShowTryAgainSnackBar(val stringResourceId: Int): RequestDetailsViewEvent()
        object DismissFragment: RequestDetailsViewEvent()
        data class CallPatient(val phoneNumber: String): RequestDetailsViewEvent()
        data class RequestDetailsError(val error: ServerError): RequestDetailsViewEvent()
    }

    private val _donationDetails =  MutableLiveData<DonationDetailsResponse>()
    val donationDetails: LiveData<DonationDetailsResponse> = _donationDetails
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

    open suspend fun getDonationDetails(requestId: String): LiveData<DonationDetailsResponse> {
        val details = RequestFulfillmentRepo.getDonationDetails(bloodDonationAPI, requestId)
        if(areDetailsFetchedSuccessfully(details)){
            _donationDetails.value = details
            _areDonationDetailsLoaded.value = true
            _isInLoadingState.value = false
        }else{
            pushErrorToFragment(details.error?.message)
        }
        return MutableLiveData<DonationDetailsResponse>(details)
    }

    protected fun areDetailsFetchedSuccessfully(details: DonationDetailsResponse): Boolean {
        return details.request != null
    }

    fun refresh(){
        viewModelScope.launch {
            getDonationDetails(requestId)
        }
    }

    fun updateDonationDetails(update: DonationRequest){
        _donationDetails.postValue(createDonationDetailsFromUpdate(update))
    }

    private fun createDonationDetailsFromUpdate(update: DonationRequest): DonationDetailsResponse{
        val currentRequestDetails = _donationDetails.value!!.request
        val updatedRequestDetails = currentRequestDetails!!.copy(
            numberOfBagsFulfilled = update.numberOfBagsFulfilled,
            numberOfBagsRequired = update.numberOfBagsRequired,
            numberOfComingDonors = update.numberOfComingDonors
        )
        return DonationDetailsResponse(updatedRequestDetails, null)
    }

    protected suspend fun pushErrorToFragment(error: ServerError?) {
        error?.let {
            _eventsFlow.emit(RequestDetailsViewEvent.RequestDetailsError(it))
        }
    }
}