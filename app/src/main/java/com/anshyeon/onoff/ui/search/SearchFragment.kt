package com.anshyeon.onoff.ui.search

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

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(R.layout.fragment_search),
    OnMapReadyCallback {

    private val viewModel by viewModels<SearchViewModel>()
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view_search)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setNavigationOnClickListener()
        setSearchEnterClickListener()
        setSnackBarMessage()
        observeSearchedPlace()
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