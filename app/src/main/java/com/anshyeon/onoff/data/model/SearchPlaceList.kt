package com.anshyeon.onoff.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchPlaceList(
    val documents: List<Place>,
)

@JsonClass(generateAdapter = true)
data class Place(
    @Json(name = "address_name") val addressName: String,
    val distance: String,
    val id: String,
    val phone: String,
    @Json(name = "place_name") val placeName: String,
    @Json(name = "place_url") val placeUrl: String,
    @Json(name = "road_address_name") val roadAddressName: String,
    val x: String,
    val y: String,
)