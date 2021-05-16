package com.example.shoryan.viewmodels

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.shoryan.R
import com.example.shoryan.networking.RetrofitBloodDonationInterface
import com.example.shoryan.repos.MyRequestDetailsRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class MyRequestDetailsViewModel@AssistedInject constructor(
        bloodDonationAPI: RetrofitBloodDonationInterface,
        @Assisted requestId: String
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
        val reponse = MyRequestDetailsRepo.cancelRequest(bloodDonationAPI, requestId)
        if(reponse.successfulResponse != null){
            dismissFragment()
            showSuccessToast(view)
        }
        else{
            pushErrorToFragment(reponse.error!!.message)
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

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestId: String): MyRequestDetailsViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            requestId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(requestId) as T
            }
        }
    }
}