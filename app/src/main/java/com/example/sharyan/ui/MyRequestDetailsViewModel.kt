package com.example.sharyan.ui

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.example.sharyan.repos.MyRequestDetailsRepo
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
        _message.postValue(processResultError)
        _isInLoadingState.postValue(false)
        if(processResultError.isNullOrEmpty()){
            dismissFragment()
            showSuccessToast(view)
        }
    }

    private fun dismissFragment(){
        _shouldDismiss.postValue(true)
    }

    private fun showSuccessToast(view: View){
        Toast.makeText(view.context, "تم الغاء الطلب", Toast.LENGTH_LONG).show()
    }
}

