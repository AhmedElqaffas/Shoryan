package com.example.sharyan.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.sharyan.BuildConfig
import com.example.sharyan.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private val defaultCameraZoom = 15f
    private val TAG = javaClass.simpleName
    private lateinit var mapInstance: GoogleMap
    private lateinit var locationPickerViewModel: LocationPickerViewModel
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var marker: Marker
    /* If the user did not change pinned location, this variable will stay null.
       If the user changed the pinned location, this variable will contain the pin address
     */
    private var newlyMarkedAddress: Address? = null


    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback { googleMap ->
        mapInstance = googleMap
        val initialMarkerPosition = locationPickerViewModel.locationLatLng
        initializeMapMarker(googleMap, initialMarkerPosition)
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

    private fun initializeMapMarker(googleMap: GoogleMap, initialMarkerPosition: LatLng){
        marker = googleMap.addMarker(
            MarkerOptions()
                .position(initialMarkerPosition)
                .draggable(true)
        )
    }

    private fun moveCameraToLocation(latLng: LatLng) {
        mapInstance.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultCameraZoom))
    }

    private fun setLocation(latLng: LatLng) {
        lifecycleScope.launch {
            moveCameraToLocation(latLng)
            changeMarkerPosition(latLng)
            newlyMarkedAddress = getChosenLocation(latLng)
        }
    }

    private fun changeMarkerPosition(location: LatLng) {
        marker.position = location
    }

    private suspend fun getChosenLocation(location: LatLng) =
        locationPickerViewModel.getAddressFromLatLng(requireActivity(), location)


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
        setGetMyLocationButtonListener()
        setConfirmLocationButtonListener()
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
                Log.e(TAG, "An error occurred: $status")
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

    private fun setConfirmLocationButtonListener() {
        confirmLocationFAB.setOnClickListener {
            newlyMarkedAddress?.let {
                locationPickerViewModel.setLocation(it)
            }
            activity?.onBackPressed()
        }
    }

    private fun setGetMyLocationButtonListener(){
        getMyLocationFAB.setOnClickListener{
            askUserForLocation()
        }
    }

    private fun askUserForLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (!isLocationPermissionGranted()){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        else {
            getUserLastKnownLocation()
        }
    }

    private fun isLocationPermissionGranted() =
        ( ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED )

    // What happens when users accept or deny accessing their location
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isLocationPermissionGranted()){
                getUserLastKnownLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLastKnownLocation(){
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(requireActivity()) { location ->
            setLocation(LatLng(location.latitude, location.longitude))
        }
    }
}