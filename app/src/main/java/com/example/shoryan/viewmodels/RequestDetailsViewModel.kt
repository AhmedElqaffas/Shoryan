package com.example.shoryan.viewmodels

import androidx.lifecycle.*
import com.example.shoryan.EnglishToArabicConverter
import com.example.shoryan.data.DonationDetails
import com.example.shoryan.data.Location
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.RequestFulfillmentRepo
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class RequestDetailsViewModel(protected val bloodDonationAPI: RetrofitBloodDonationInterface,
                                   protected val requestId: String): ViewModel() {

    sealed class RequestDetailsViewEvent{
        data class ShowSnackBar(val text: String): RequestDetailsViewEvent()
        data class ShowTryAgainSnackBar(val text: String = "فشل في الاتصال بالشبكة"): RequestDetailsViewEvent()
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
    val numberOfRemainingBags: LiveData<String> = Transformations.map(donationDetails){
        EnglishToArabicConverter().convertDigits(
            donationDetails.value?.request?.numberOfBagsRequired
                ?.minus(donationDetails.value?.request?.numberOfBagsFulfilled!!)
                .toString()
        )
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
        _eventsFlow.emit(RequestDetailsViewEvent.ShowTryAgainSnackBar())
    }

      fun refresh(){
        viewModelScope.launch {
            getDonationDetails(requestId)
        }
    }
}