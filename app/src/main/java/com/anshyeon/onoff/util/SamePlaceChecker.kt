package com.anshyeon.onoff.util

import com.anshyeon.onoff.data.model.PlaceInfo
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object SamePlaceChecker {
    private const val R = 6372.8 * 1000

    private fun getRoadAddress(address: String?): String {
        return address?.split(" ")?.takeLast(2)?.joinToString(" ") ?: ""
    }

    private fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val chordLengthSquare =
            sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(Math.toRadians(lat1)) * cos(
                Math.toRadians(lat2)
            )
        val angularDistance = 2 * asin(sqrt(chordLengthSquare))
        return (R * angularDistance).toInt()
    }

    fun isSamePlace(
        placeInfo: PlaceInfo,
        selectedAddress: String,
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Boolean {
        val currentPlaceInfo = placeInfo.documents.first().roadAddress
        val currentRoadAddress = getRoadAddress(currentPlaceInfo?.addressName)
        val selectedRoadAddress = getRoadAddress(selectedAddress)
        val distance = getDistance(lat1, lon1, lat2, lon2)
        return (currentRoadAddress == selectedRoadAddress || distance < 150)
    }
}