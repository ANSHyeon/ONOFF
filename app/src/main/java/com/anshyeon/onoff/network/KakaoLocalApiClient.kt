package com.anshyeon.onoff.network

import com.anshyeon.onoff.data.model.PlaceInfo
import com.anshyeon.onoff.data.model.SearchPlaceList
import com.anshyeon.onoff.network.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoLocalApiClient {
    @GET("search/keyword.json?page=1&size=3&sort=accuracy")
    suspend fun getPlace(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
    ): ApiResponse<SearchPlaceList>

    @GET("geo/coord2address.json")
    suspend fun getPlaceInfoByLocation(
        @Header("Authorization") authorization: String,
        @Query("y") latitude: String,
        @Query("x") longitude: String,
    ): ApiResponse<PlaceInfo>
}