package com.example.shoryan.viewmodels

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.example.shoryan.data.ViewEvent
import com.example.shoryan.repos.MyRequestDetailsRepo
import kotlinx.coroutines.launch


class MyRequestDetailsViewModel: RequestDetailsViewModel() {

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
        val processResultError = MyRequestDetailsRepo.cancelRequest(bloodDonationAPI, donationDetails.value!!.request!!.id)
        processResultError?.apply { _eventsFlow.emit(ViewEvent.ShowSnackBar(this)) }
        _isInLoadingState.postValue(false)
        if(processResultError.isNullOrEmpty()){
            dismissFragment()
            showSuccessToast(view)
        }
    }

    private suspend fun dismissFragment(){
        _eventsFlow.emit(ViewEvent.DismissFragment)
    }

    private fun showSuccessToast(view: View){
        Toast.makeText(view.context, "تم الغاء الطلب", Toast.LENGTH_LONG).show()
    }
}

