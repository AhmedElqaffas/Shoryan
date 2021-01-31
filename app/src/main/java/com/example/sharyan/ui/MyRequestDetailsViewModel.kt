package com.example.sharyan.ui

import android.view.View
import androidx.lifecycle.*
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.data.CurrentAppUser
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.MyRequestDetailsRepo
import com.example.sharyan.repos.RequestFulfillmentRepo
import kotlinx.coroutines.Dispatchers


class MyRequestDetailsViewModel: ViewModel() {

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


    // Instead of the xml view observing the donationDetails liveData and performing subtraction itself,
    // this view should observe this liveData which simplifies the xml
    val numberOfRemainingBags: LiveData<String> = Transformations.map(donationDetails){
        EnglishToArabicConverter().convertDigits(
            donationDetails.value?.request?.numberOfBagsRequired
                ?.minus(donationDetails.value?.request?.numberOfBagsFulfilled!!)
                .toString()
        )
    }

    init {
        acceptedRequestMediatorLiveData.addSource(currentUserPendingRequest){
                value: String? -> acceptedRequestMediatorLiveData.setValue(value)
        }

        acceptedRequestMediatorLiveData.addSource(donationDetails){
            // When th details are loaded, set the request id as this liveData value
                value: DonationDetails -> acceptedRequestMediatorLiveData.setValue(value.request?.id)
        }
    }


    fun getDonationDetails(requestId: String) = liveData(Dispatchers.IO) {
        val details = MyRequestDetailsRepo.getDonationDetails(bloodDonationAPI, requestId)
        _donationDetails.postValue(details)
        emit(details)
        if(details != null){
            _areDonationDetailsLoaded.postValue(true)
            _isInLoadingState.postValue(false)
        }
    }


    fun cancelRequest(requestId: String) = liveData(Dispatchers.IO){
        _isInLoadingState.postValue(true)
        val processResultError = MyRequestDetailsRepo.cancelRequest(bloodDonationAPI, requestId)
        emit(processResultError)
        _isInLoadingState.postValue(false)
    }


}