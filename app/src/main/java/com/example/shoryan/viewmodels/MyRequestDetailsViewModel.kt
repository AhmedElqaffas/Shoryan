package com.example.shoryan.viewmodels

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.example.shoryan.R
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.MyRequestDetailsRepo
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


class MyRequestDetailsViewModel@Inject constructor(
        bloodDonationAPI: RetrofitBloodDonationInterface,
        @Named("requestId") requestId: String
): RequestDetailsViewModel(bloodDonationAPI, requestId) {

    fun showAlertDialog(view: View){
        val builder = AlertDialog.Builder(view.context)
        with(builder) {
            setTitle(view.context.resources.getString(R.string.cancel_request))
            setMessage(view.context.resources.getString(R.string.are_you_sure_cancel_request))
            setPositiveButton(view.context.resources.getString(R.string.yes)) { dialog, which -> cancelRequest(view) }
            setNegativeButton(view.context.resources.getString(R.string.no),null)
            show()
        }
    }

    private fun cancelRequest(view: View) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val updatedDonationRequest = MyRequestDetailsRepo.cancelRequest(bloodDonationAPI, requestId)
        if(updatedDonationRequest != null){
            dismissFragment()
            showSuccessToast(view)
        }
        else{
            _eventsFlow.emit(RequestDetailsViewEvent.ShowSnackBar(R.string.connection_error))
        }
        _isInLoadingState.postValue(false)
    }

    private suspend fun dismissFragment(){
        _eventsFlow.emit(RequestDetailsViewEvent.DismissFragment)
    }

    private fun showSuccessToast(view: View){
        Toast.makeText(view.context,
            view.context.resources.getString(R.string.request_canceled),
            Toast.LENGTH_LONG).show()
    }
}