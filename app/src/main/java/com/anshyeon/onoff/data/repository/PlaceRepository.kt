package com.anshyeon.onoff.data.repository

import com.anshyeon.onoff.BuildConfig
import com.anshyeon.onoff.data.model.SearchPlaceList
import com.anshyeon.onoff.network.KakaoLocalApiClient
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import javax.inject.Inject

class PlaceRepository @Inject constructor(
    private val kakaoLocalApiClient: KakaoLocalApiClient
) {

    suspend fun getSearchPlace(
        keyword: String
    ): ApiResponse<SearchPlaceList> {

        return try {
            kakaoLocalApiClient.getPlace(
                BuildConfig.KAKAO_CLIENT_ID,
                keyword
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }
}