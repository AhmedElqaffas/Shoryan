package com.example.sharyan.ui

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.sharyan.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment() {

    private lateinit var mapInstance: GoogleMap
    private lateinit var locationPickerViewModel: LocationPickerViewModel


    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { googleMap ->
        mapInstance = googleMap
        val initialMarker = locationPickerViewModel.locationLatLng
        googleMap.addMarker(MarkerOptions().position(initialMarker).draggable(true))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialMarker,15f))

        googleMap.setOnMapClickListener {
            placeMarker(it)
            confirmChosenLocation(it)
        }

        googleMap.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener{
            override fun onMarkerDragStart(p0: Marker?) {}

            override fun onMarkerDrag(p0: Marker?) {}

            override fun onMarkerDragEnd(p0: Marker?) {
                p0?.let {
                    confirmChosenLocation(it.position)
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        initializeViewModel()
    }

    private fun initializeViewModel(){
        locationPickerViewModel = ViewModelProvider(requireActivity()).get(LocationPickerViewModel::class.java)
    }

    private fun placeMarker(location: LatLng){
        mapInstance.clear()
        mapInstance.addMarker(MarkerOptions().position(location).draggable(true))
    }

    private fun confirmChosenLocation(location: LatLng){
        lifecycleScope.launch {
            val address = getAddressOfMarker(location)
            address?.let {
                showConfirmationDialog(it)
            }
        }
    }

    private suspend fun getAddressOfMarker(markerPosition: LatLng): Address?{
        val geocoder = Geocoder(requireActivity())
        var address: Address? = null
        withContext(lifecycleScope.coroutineContext) {
            withContext(Dispatchers.IO) {
                try {
                    address = geocoder.getFromLocation(markerPosition.latitude, markerPosition.longitude, 1)[0]
                } catch (e: Exception) {
                    Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.internet_connection),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        return address
    }

    private fun showConfirmationDialog(location: Address){
        AlertDialog.Builder(requireActivity())
            .setTitle("تأكيد العنوان")
            .setMessage("العنوان هو: ${location.getAddressLine(0)} ")
            .setNegativeButton("لا"){ dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("نعم"){ dialog, _ ->
                locationPickerViewModel.setLocation(location)
                dialog.dismiss()
                activity?.onBackPressed()
            }
            .show()
    }
}