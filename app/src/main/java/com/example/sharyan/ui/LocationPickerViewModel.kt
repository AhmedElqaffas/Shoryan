package com.example.sharyan.ui

import android.location.Address
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationPickerViewModel: ViewModel() {

    val locationStringLiveData = MutableLiveData("")
    var locationLatLng = LatLng(30.041517, 31.234525)

    fun setLocation(location: Address){
        locationStringLiveData.value = location.getAddressLine(0)
        locationLatLng = LatLng(location.latitude, location.longitude)
    }
}