package com.example.shoryan.viewmodels

import android.view.View
import androidx.lifecycle.*
import com.example.shoryan.EnglishToArabicConverter
import com.example.shoryan.data.DonationDetails
import com.example.shoryan.data.Location
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.networking.RetrofitClient
import com.example.shoryan.repos.RequestFulfillmentRepo
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

open class RequestDetailsViewModel: ViewModel() {

    private val _donationDetails =  MutableLiveData<DonationDetails>()
    val donationDetails: LiveData<DonationDetails> = _donationDetails
    // The application is processing a query (contacting backend)
    protected val _isInLoadingState = MutableLiveData(true)
    val isInLoadingState: LiveData<Boolean> = _isInLoadingState
    // The donation details have been loaded
    private val _areDonationDetailsLoaded = MutableLiveData(false)
    val areDonationDetailsLoaded = _areDonationDetailsLoaded
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
    protected val _eventsFlow = MutableSharedFlow<ViewEvent>()
    val eventsFlow = _eventsFlow.asSharedFlow()

    protected var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    open suspend fun getDonationDetails(requestId: String): LiveData<DonationDetails?> {
        val details = RequestFulfillmentRepo.getDonationDetails(bloodDonationAPI, requestId)
        if(areDetailsFetchedSuccessfully(details)){
            _donationDetails.value = details
            _areDonationDetailsLoaded.value = true
            _isInLoadingState.value = false
            updateMapLocation(details!!.request!!.bloodBank!!.location)
        }else{
            announceCommunicationFailure()
        }
        return MutableLiveData<DonationDetails?>(details)
    }

    protected fun areDetailsFetchedSuccessfully(details: DonationDetails?): Boolean {
        return details?.request != null
    }

    private suspend fun updateMapLocation(location: Location){
        _eventsFlow.emit(ViewEvent.UpdateMapLocation(LatLng(location.latitude, location.longitude)))
    }

    private suspend fun announceCommunicationFailure(){
        _eventsFlow.emit(ViewEvent.ShowTryAgainSnackBar())
    }
}