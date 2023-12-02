package com.anshyeon.onoff.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaceInfo(
    val documents: List<Documents>
)

@JsonClass(generateAdapter = true)
data class Documents(
    @Json(name = "road_address") val roadAddress: RoadAddress,
    val address: Address,
)

@JsonClass(generateAdapter = true)
data class RoadAddress(
    @Json(name = "address_name") val addressName: String = "",
    @Json(name = "building_name") val buildingName: String = "",
)

@JsonClass(generateAdapter = true)
data class Address(
    @Json(name = "region_2depth_name") val region2depthName: String = "",
    @Json(name = "region_3depth_name") val region3depthName: String = "",
)