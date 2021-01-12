package com.example.sharyan.ui

import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharyan.R
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class LocationPickerViewModel: ViewModel() {

    val locationStringLiveData = MutableLiveData("")
    var locationLatLng = LatLng(30.041517, 31.234525)

    fun setLocation(location: Address){
        locationStringLiveData.value = location.getAddressLine(0)
        locationLatLng = LatLng(location.latitude, location.longitude)
    }

     suspend fun getAddressFromLatLng(activity: Activity, position: LatLng): Address?{
         val geocoder = Geocoder(activity)
         var address: Address? = null
         withContext(viewModelScope.coroutineContext) {
             withContext(Dispatchers.IO) {
                 try {
                     address = geocoder.getFromLocation(position.latitude, position.longitude, 1)[0]
                 } catch (e: Exception) {
                     Toast.makeText(
                         activity,
                         activity.resources.getString(R.string.internet_connection),
                         Toast.LENGTH_LONG
                     ).show()
                 }
             }
         }
         return address
    }
}