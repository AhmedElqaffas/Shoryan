package com.example.sharyan.ui

import android.annotation.SuppressLint
import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.sharyan.BuildConfig
import com.example.sharyan.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private lateinit var mapInstance: GoogleMap
    private lateinit var locationPickerViewModel: LocationPickerViewModel
    private lateinit var marker: Marker
    /* If the user did not change pinned location, this variable will stay null.
       If the user changed the pinned location, this variable will contain the location address
     */
    private var newlyMarkedAddress: Address? = null


    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { googleMap ->
        mapInstance = googleMap
        val initialMarkerPosition = locationPickerViewModel.locationLatLng
        marker = googleMap.addMarker(MarkerOptions()
            .position(initialMarkerPosition)
            .draggable(true)
        )
        moveCameraToLocation(initialMarkerPosition)
        googleMap.setOnMapClickListener {
            setLocation(it)
        }

        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(p0: Marker?) {}

            override fun onMarkerDrag(p0: Marker?) {}

            override fun onMarkerDragEnd(p0: Marker?) {
                p0?.let {
                    setLocation(it.position)
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
        initializeGoogleMap()
        initializeAutoComplete()
        initializeViewModel()
    }

    private fun initializeGoogleMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun initializeAutoComplete() {
        initializePlacesAPI()
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocompleteFragment)
                as AutocompleteSupportFragment

        autocompleteFragment.setCountries("EG")
        autocompleteFragment.setPlaceFields(listOf(Place.Field.LAT_LNG, Place.Field.NAME))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    setLocation(it)
                }
            }

            override fun onError(status: Status) {
                println("An error occurred: $status")
            }
        })
    }

    private fun initializePlacesAPI() {
        Places.initialize(requireActivity(), BuildConfig.PLACES_API_KEY)
    }

    private fun initializeViewModel() {
        locationPickerViewModel =
            ViewModelProvider(requireActivity()).get(LocationPickerViewModel::class.java)
    }

    private fun setLocation(latLng: LatLng) =
        lifecycleScope.launch {
            moveCameraToLocation(latLng)
            changeMarkerPosition(latLng)
            newlyMarkedAddress = getChosenLocation(latLng)
        }

    private fun moveCameraToLocation(latLng: LatLng) {
        mapInstance.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun changeMarkerPosition(location: LatLng) {
        marker.position = location
    }

    private suspend fun getChosenLocation(location: LatLng) =
        locationPickerViewModel.getAddressFromLatLng(requireActivity(), location)

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