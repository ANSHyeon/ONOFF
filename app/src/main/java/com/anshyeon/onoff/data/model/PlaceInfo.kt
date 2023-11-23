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
)

@JsonClass(generateAdapter = true)
data class RoadAddress(
    @Json(name = "building_name") val buildingName: String = "",
    @Json(name = "road_name") val roadName: String = "",
    @Json(name = "main_building_no") val mainBuildingNo: String = "",
)