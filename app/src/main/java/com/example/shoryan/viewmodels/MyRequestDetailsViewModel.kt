package com.example.shoryan.viewmodels

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
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
            setTitle("الغاء الطلب")
            setMessage("هل انت متأكد من انك تريد الغاء الطلب؟")
            setPositiveButton("نعم") { dialog, which -> cancelRequest(view) }
            setNegativeButton("لا",null)
            show()
        }
    }

    private fun cancelRequest(view: View) = viewModelScope.launch{
        _isInLoadingState.postValue(true)
        val processResultError = MyRequestDetailsRepo.cancelRequest(bloodDonationAPI, requestId)
        processResultError?.apply { _eventsFlow.emit(RequestDetailsViewEvent.ShowSnackBar(this)) }
        _isInLoadingState.postValue(false)
        if(processResultError.isNullOrEmpty()){
            dismissFragment()
            showSuccessToast(view)
        }
    }

    private suspend fun dismissFragment(){
        _eventsFlow.emit(RequestDetailsViewEvent.DismissFragment)
    }

    private fun showSuccessToast(view: View){
        Toast.makeText(view.context, "تم الغاء الطلب", Toast.LENGTH_LONG).show()
    }
}