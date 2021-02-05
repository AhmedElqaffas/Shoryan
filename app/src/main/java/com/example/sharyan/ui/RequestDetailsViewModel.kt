package com.example.sharyan.ui

import android.view.View
import androidx.lifecycle.*
import com.example.sharyan.EnglishToArabicConverter
import com.example.sharyan.data.DonationDetails
import com.example.sharyan.networking.RetrofitBloodDonationInterface
import com.example.sharyan.networking.RetrofitClient
import com.example.sharyan.repos.RequestFulfillmentRepo

open class RequestDetailsViewModel: ViewModel() {

    // Private mutable liveData for the donation details obtained from the repository
    private val _donationDetails =  MutableLiveData<DonationDetails>()
    // A public liveData for the XML views to observe, the separation of the live data to two
    // (private mutable and public immutable) was used in a google codelab, so I thought it may
    // be best practice to do it
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

    // Publishes messages for the subscribed fragments to show
    protected val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    // Set to true when the current BottomSheetFragment should be dismissed
    protected val _shouldDismiss = MutableLiveData<Boolean>(false)
    val shouldDismissFragment: LiveData<Boolean> = _shouldDismiss

    protected var bloodDonationAPI: RetrofitBloodDonationInterface = RetrofitClient
        .getRetrofitClient()
        .create(RetrofitBloodDonationInterface::class.java)

    open suspend fun getDonationDetails(requestId: String): LiveData<DonationDetails?> {
        val details = RequestFulfillmentRepo.getDonationDetails(bloodDonationAPI, requestId)
        if(details?.request != null){
            _donationDetails.value = details
            _areDonationDetailsLoaded.value = true
            _isInLoadingState.value = false
        }
        return MutableLiveData<DonationDetails?>(details)
    }
}