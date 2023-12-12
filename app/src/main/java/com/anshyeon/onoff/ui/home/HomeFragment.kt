package com.anshyeon.onoff.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.core.content.PermissionChecker
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.databinding.FragmentHomeBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.showMessage
import com.anshyeon.onoff.util.SamePlaceChecker
import com.anshyeon.onoff.util.Constants
import com.anshyeon.onoff.util.NetworkConnection
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home), OnMapReadyCallback {

    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationSource: FusedLocationSource
    private lateinit var client: FusedLocationProviderClient
    private val locationPermissionRequest = setLocationPermissionRequest()
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionRequest.launch(
            permissions
        )
        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    @UiThread
    override fun onMapReady(map: NaverMap) {
        naverMap = map.also {
            it.locationSource = fusedLocationSource
            it.locationTrackingMode = LocationTrackingMode.Follow
            it.uiSettings.isLocationButtonEnabled = true
        }
        setNaverMapZoom()
        setLayout()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        verifySignInToken()
    }

    private fun verifySignInToken() {
        val localGoogleIdToken = viewModel.getLocalGoogleIdToken()
        if (localGoogleIdToken.isEmpty()) {
            val action =
                HomeFragmentDirections.actionHomeToSignIn()
            findNavController().navigate(action)
        }
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setMapView()
        setSearchPlaceBarClickListener()
        setNetworkErrorBar()
    }

    private fun setMapView() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                launch {
                    initializeMapWithPermissions()
                }
                launch {
                    setSnackBar()
                }
            }
        }
    }

    private suspend fun initializeMapWithPermissions() {
        viewModel.isPermissionGranted.collect { isPermissionGranted ->
            isPermissionGranted?.let {
                if (it) {
                    dismissDialog()
                    moveMapCamera()
                    viewModel.chatRoomList.collect { chatRoomList ->
                        chatRoomList.forEach { chatRoom ->
                            setMarker(chatRoom)
                        }
                    }
                } else {
                    val action =
                        HomeFragmentDirections.actionHomeToPermissionOffDialog()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private suspend fun setSnackBar() {
        viewModel.snackBarText.collect {
            Snackbar.make(
                binding.root,
                getString(it),
                Snackbar.LENGTH_SHORT,
            ).show()
        }
    }

    private fun setSearchPlaceBarClickListener() {
        binding.areaSearchBar.setOnClickListener {
            val action =
                HomeFragmentDirections.actionHomeToSearch()
            findNavController().navigate(action)
        }
    }

    private fun setPlaceInfoView(chatRoom: ChatRoom) {
        with(binding.placeInfoSearch) {
            setPlaceName(chatRoom.placeName)
            setAddress(chatRoom.address)
            setButtonText(getString(R.string.label_enter_chat_room))
            setClickListener(viewLifecycleOwner.lifecycleScope) {
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

    private fun observeCurrentPlaceInfo(currentLatitude: Double, currentLongitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                launch {
                    viewModel.currentPlaceInfo.collect {
                        it?.let { placeInfo ->
                            val selectedPlaceInfo = viewModel.selectedChatRoom
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
                                    handleSamePlace(chatRoom)
                                } else {
                                    binding.placeInfoSearch.showMessage(R.string.error_message_not_same_place)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleSamePlace(chatRoom: ChatRoom) {
        viewModel.insertChatRoom(chatRoom)

        viewModel.savedChatRoom.collect { savedChatRoom ->
            savedChatRoom?.let {
                val action =
                    HomeFragmentDirections.actionHomeToChatRoom(
                        chatRoom.placeName,
                        chatRoom.chatRoomId,
                        chatRoom.address,
                        chatRoom.latitude,
                        chatRoom.longitude
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            binding.networkErrorBar.visibility = if (it) View.GONE else View.VISIBLE
        }
    }

    private fun moveMapCamera() {
        client.lastLocation.addOnSuccessListener { startLocation ->
            val latLng = LatLng(startLocation.latitude, startLocation.longitude)
            val cameraUpdate = CameraUpdate.scrollTo(latLng).animate(CameraAnimation.Easing)
            naverMap.moveCamera(cameraUpdate)
        }
    }

    private fun setNaverMapZoom() {
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 7.0
    }

    private suspend fun dismissDialog() {
        val currentNavDestination = findNavController().currentDestination?.label.toString()
        val dialogDestination = getString(R.string.label_location_permission_off_destination)
        if (currentNavDestination == dialogDestination) {
            delay(Constants.DELAY_DURATION)
            navigateUp()
        }
    }

    private fun setLocationPermissionRequest() = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                viewModel.updateIsPermissionGranted(true)
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewModel.updateIsPermissionGranted(true)
            }

            else -> {
                viewModel.updateIsPermissionGranted(false)
            }
        }
    }

    private fun hasPermission(): Boolean {
        return PermissionChecker.checkSelfPermission(requireContext(), permissions[0]) ==
                PermissionChecker.PERMISSION_GRANTED ||
                PermissionChecker.checkSelfPermission(requireContext(), permissions[1]) ==
                PermissionChecker.PERMISSION_GRANTED
    }

    private fun setMarker(chatRoom: ChatRoom) {
        val marker = Marker().apply {
            position = LatLng(chatRoom.latitude.toDouble(), chatRoom.longitude.toDouble())
            map = naverMap
            setOnClickListener {
                viewModel.selectedChatRoom = chatRoom
                setPlaceInfoView(chatRoom)
                binding.placeInfoSearch.visibility = View.VISIBLE
                true
            }
        }
        viewModel.addMarker(marker)
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (hasPermission()) {
            viewModel.updateIsPermissionGranted(true)
        }
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