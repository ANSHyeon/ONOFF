package com.anshyeon.onoff.ui.home

import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentHomeBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.showMessage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home), OnMapReadyCallback {

    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationSource: FusedLocationSource
    private lateinit var client: FusedLocationProviderClient

    @UiThread
    override fun onMapReady(map: NaverMap) {
        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap = map.apply {
            locationSource = fusedLocationSource
            locationTrackingMode = LocationTrackingMode.Follow
            uiSettings.isLocationButtonEnabled = true
        }
        setNaverMapZoom()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        viewModel.getChatRooms()
        observeChatRoomList()
        setSnackBarMessage()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (fusedLocationSource.onRequestPermissionsResult(
                requestCode, permissions,
                grantResults
            )
        ) {
            if (!fusedLocationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            fusedLocationSource.lastLocation?.let {
                moveMapCamera(it.longitude, it.latitude)
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun observeChatRoomList() {
        lifecycleScope.launch {
            viewModel.chatRoomList.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it.forEach { chatRoom ->
                    val marker = Marker()
                    marker.position =
                        LatLng(chatRoom.latitude.toDouble(), chatRoom.longitude.toDouble())
                    marker.map = naverMap
                }
            }
        }
    }

    private fun setSnackBarMessage() {
        lifecycleScope.launch {
            viewModel.snackBarText.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                binding.root.showMessage(it)
            }
        }
    }

    private fun moveMapCamera(longitude: Double, latitude: Double) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
            .animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun setNaverMapZoom() {
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 7.0

        fusedLocationSource.lastLocation?.let {
            moveMapCamera(it.longitude, it.latitude)
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}