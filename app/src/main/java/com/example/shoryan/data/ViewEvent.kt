package com.example.shoryan.data

import com.google.android.gms.maps.model.LatLng

sealed class ViewEvent{
    data class ShowSnackBar(val text: String): ViewEvent()
    data class ShowTryAgainSnackBar(val text: String = "فشل في الاتصال بالشبكة"): ViewEvent()
    data class UpdateMapLocation(val latlng: LatLng): ViewEvent()
    object DismissFragment: ViewEvent()
}
