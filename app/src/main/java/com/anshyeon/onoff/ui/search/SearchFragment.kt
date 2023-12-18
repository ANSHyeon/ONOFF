package com.anshyeon.onoff.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentSearchBinding
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.data.model.Place
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.showMessage
import com.anshyeon.onoff.util.NetworkConnection
import com.anshyeon.onoff.util.SamePlaceChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(R.layout.fragment_search),
    OnMapReadyCallback {

    private val viewModel by viewModels<SearchViewModel>()
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var client: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMapView(view, savedInstanceState)
        setLayout()
    }

    private fun setMapView(view: View, savedInstanceState: Bundle?) {
        mapView = view.findViewById(R.id.map_view_search)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setNavigationOnClickListener()
        setSearchEnterClickListener()
        setSnackBarMessage()
        observeSearchedPlace()
        observePlaceChatRoom()
        observeIsSaved()
        setNetworkErrorBar()
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            binding.networkErrorBar.visibility = if (it) View.GONE else View.VISIBLE
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        setNaverMapZoom()
    }

    private fun observeSearchedPlace() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchedPlace.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                if (it != null) {
                    setMarker(it)
                    moveMapCamera(it)
                    setBindingVisibility()
                    setPlaceInfoView(it)
                }
            }
        }
    }

    private fun setPlaceInfoView(place: Place) {
        with(place) {
            val address = roadAddressName.ifBlank { addressName }
            binding.placeInfoSearch.setPlaceName(placeName)
            binding.placeInfoSearch.setAddress(address)
            setDefaultPlaceInfoButton()
        }
    }

    private fun setBindingVisibility() {
        binding.mapViewSearch.visibility = View.VISIBLE
        binding.placeInfoSearch.visibility = View.VISIBLE
    }

    private fun setSnackBarMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.snackBarText.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                Snackbar.make(
                    binding.root,
                    getString(it),
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun observePlaceChatRoom() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.placeChatRoom.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let {
                    setPlaceInfoButton(getString(R.string.label_enter_chat_room)) {
                        client.lastLocation.addOnSuccessListener { location ->
                            val currentLatitude = location.latitude
                            val currentLongitude = location.longitude
                            viewModel.getCurrentPlaceInfo(
                                currentLatitude.toString(),
                                currentLongitude.toString()
                            )
                            observeCurrentPlaceInfo(currentLatitude, currentLongitude)
                        }
                    }
                }
            }
        }
    }

    private fun observeIsSaved() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSaved.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                if (it) {
                    val searchedPlace = viewModel.searchedPlace.value
                    searchedPlace?.let {
                        val action =
                            SearchFragmentDirections.actionSearchToChatRoom(
                                searchedPlace.placeName,
                                searchedPlace.y + searchedPlace.x,
                                searchedPlace.roadAddressName,
                                searchedPlace.y,
                                searchedPlace.x
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun observeCurrentPlaceInfo(currentLatitude: Double, currentLongitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentPlaceInfo.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let { placeInfo ->
                    val selectedPlaceInfo = viewModel.placeChatRoom.value
                    selectedPlaceInfo?.let { chatRoom ->
                        if (SamePlaceChecker.isSamePlace(
                                placeInfo,
                                chatRoom.address,
                                currentLatitude,
                                currentLongitude,
                                chatRoom.latitude.toDouble(),
                                chatRoom.longitude.toDouble()
                            )
                        ) {
                            viewModel.addMemberToChatRoom(chatRoom)
                        } else {
                            binding.placeInfoSearch.showMessage(R.string.error_message_not_same_place)
                        }
                    } ?: run {
                        val searchedPlace = viewModel.searchedPlace.value ?: Place()

                        if (SamePlaceChecker.isSamePlace(
                                placeInfo,
                                searchedPlace.roadAddressName,
                                currentLatitude,
                                currentLongitude,
                                searchedPlace.y.toDouble(),
                                searchedPlace.x.toDouble()
                            )
                        ) {
                            viewModel.createChatRoom(searchedPlace)
                        } else {
                            binding.placeInfoSearch.showMessage(R.string.error_message_not_same_place)
                        }
                    }
                }
            }
        }
    }

    private fun setPlaceInfoButton(text: String, operation: () -> Unit) {
        with(binding.placeInfoSearch) {
            setButtonText(text)
            setClickListener(viewLifecycleOwner.lifecycleScope) {
                operation()
            }
        }
    }

    private fun setDefaultPlaceInfoButton() {
        setPlaceInfoButton(getString(R.string.label_create_chat_room)) {
            client.lastLocation.addOnSuccessListener { location ->
                val currentLatitude = location.latitude
                val currentLongitude = location.longitude
                viewModel.getCurrentPlaceInfo(
                    currentLatitude.toString(),
                    currentLongitude.toString()
                )
                observeCurrentPlaceInfo(currentLatitude, currentLongitude)
            }
        }
    }

    private fun moveMapCamera(place: Place) {
        val latLng = LatLng(place.y.toDouble(), place.x.toDouble())
        val cameraUpdate = CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun setNaverMapZoom() {
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 14.0
    }

    private fun setMarker(place: Place) {
        Marker().apply {
            position = LatLng(place.y.toDouble(), place.x.toDouble())
            map = naverMap
            tag = place.placeName
        }
    }

    private fun setNavigationOnClickListener() {
        binding.toolbarSearch.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setSearchEnterClickListener() {
        binding.etSearchPlaceName.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                viewModel.getSearchPlace()
                true
            } else {
                false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}