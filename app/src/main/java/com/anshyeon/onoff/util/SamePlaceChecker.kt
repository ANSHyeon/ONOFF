package com.anshyeon.onoff.util

import com.anshyeon.onoff.data.model.Place
import com.anshyeon.onoff.data.model.PlaceInfo

object SamePlaceChecker {

    private fun getRoadAddress(address: String): String {
        return address.split(" ").takeLast(2).joinToString(" ")
    }

    fun isSamePlace(placeInfo: PlaceInfo, selectedPlaceInfo: String): Boolean {
        val currentPlaceInfo = placeInfo.documents.first().roadAddress
        val currentRoadAddress = getRoadAddress(currentPlaceInfo.addressName)
        val selectedRoadAddress = getRoadAddress(selectedPlaceInfo)

        return currentRoadAddress == selectedRoadAddress
    }

    fun isSamePlace(placeInfo: PlaceInfo, selectedPlaceInfo: Place): Boolean {
        val currentPlaceInfo = placeInfo.documents.first().roadAddress
        val currentRoadAddress = getRoadAddress(currentPlaceInfo.addressName)
        val selectedRoadAddress = getRoadAddress(selectedPlaceInfo.roadAddressName)

        return currentRoadAddress == selectedRoadAddress
    }
}