package com.example.sharyan.ui

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharyan.R
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class LocationPickerViewModel: ViewModel() {

    val locationStringLiveData: MutableLiveData<String> = MutableLiveData("")
    var locationLatLng = LatLng(30.041517, 31.234525)

    fun setLocation(location: Address){
        locationStringLiveData.value = location.getAddressLine(0)?: ""
        locationLatLng = LatLng(location.latitude, location.longitude)
    }

     suspend fun getAddressFromLatLng(context: Context, position: LatLng): Address?{
         val geocoder = Geocoder(context, Locale("ar"))
         var address: Address? = null
         withContext(viewModelScope.coroutineContext) {
             withContext(Dispatchers.IO) {
                 try {
                     address = geocoder.getFromLocation(position.latitude, position.longitude, 1)[0]
                 } catch (e: Exception) {
                     Toast.makeText(
                         context,
                         context.resources.getString(R.string.internet_connection),
                         Toast.LENGTH_LONG
                     ).show()
                 }
             }
         }
         println(address)
         println(address?.subLocality)
         println(address?.featureName)
         println(address?.thoroughfare)
         println(address?.locality)
         println(address?.subAdminArea)
         println(address?.adminArea)
         return address
    }

    fun getCurrentSavedAddress(): String = locationStringLiveData.value!!
}