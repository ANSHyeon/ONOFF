package com.anshyeon.onoff

import com.anshyeon.onoff.data.source.ApiClient

class AppContainer {

    private var apiClient: ApiClient? = null

    fun provideApiClient(): ApiClient {
        return apiClient ?: ApiClient.create().apply {
            apiClient = this
        }
    }
}