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
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Place
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.showMessage
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
        observeCurrentPlaceInfo()
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        setNaverMapZoom()
    }

    private fun observeSearchedPlace() {
        lifecycleScope.launch {
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
            setDefaultPlaceInfoButton(this)
        }
    }

    private fun setBindingVisibility() {
        binding.mapViewSearch.visibility = View.VISIBLE
        binding.placeInfoSearch.visibility = View.VISIBLE
    }

    private fun setSnackBarMessage() {
        lifecycleScope.launch {
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
        lifecycleScope.launch {
            viewModel.placeChatRoom.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let {
                    setPlaceInfoButton(getString(R.string.label_enter_chat_room)) {
                        viewModel.insertChatRoom(it)
                    }
                }
            }
        }
    }

    private fun observeIsSaved() {
        lifecycleScope.launch {
            viewModel.savedChatRoom.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let {
                    client.lastLocation.addOnSuccessListener { location ->
                        viewModel.getCurrentPlaceInfo(
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                    }
                }
            }
        }
    }

    private fun observeCurrentPlaceInfo() {
        lifecycleScope.launch {
            viewModel.currentPlaceInfo.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let { placeInfo ->
                    val selectedPlaceInfo = viewModel.savedChatRoom.value
                    selectedPlaceInfo?.let { chatRoom ->
                        if (SamePlaceChecker.isSamePlace(placeInfo, chatRoom)) {
                            val action =
                                SearchFragmentDirections.actionSearchToChatRoom(
                                    selectedPlaceInfo.placeName,
                                    selectedPlaceInfo.chatRoomId
                                )
                            findNavController().navigate(action)

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
            setClickListener {
                operation()
            }
        }
    }

    private fun setDefaultPlaceInfoButton(place: Place) {
        setPlaceInfoButton(getString(R.string.label_create_chat_room)) {
            viewModel.createChatRoom(
                ChatRoom(
                    place.y + place.x,
                    place.placeName,
                    place.roadAddressName.ifBlank { place.addressName },
                    place.y,
                    place.x
                )
            )
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