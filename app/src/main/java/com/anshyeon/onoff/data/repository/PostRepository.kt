package com.anshyeon.onoff.data.repository

import com.anshyeon.onoff.data.dataSource.ImageDataSource
import com.anshyeon.onoff.data.dataSource.UserDataSource
import com.anshyeon.onoff.data.model.ImageContent
import com.anshyeon.onoff.data.model.Post
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.network.FireBaseApiClient
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import com.anshyeon.onoff.util.DateFormatText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val fireBaseApiClient: FireBaseApiClient,
    private val userDataSource: UserDataSource,
    private val imageDataSource: ImageDataSource,
) {

    suspend fun createPost(
        title: String,
        body: String,
        location: String,
        currentUser: User,
        imageList: List<ImageContent>
    ): ApiResponse<Map<String, String>> {
        val currentTime = System.currentTimeMillis()
        val postId = userDataSource.getUid() + currentTime
        val imageLocations = imageDataSource.uploadImages(imageList)
        val post = Post(
            postId,
            title,
            body,
            location,
            currentUser,
            DateFormatText.getCurrentTime(),
            imageLocations
        )
        return try {
            fireBaseApiClient.createPost(
                userDataSource.getIdToken(),
                post
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    fun getPostList(
        location: String,
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<List<Post>> = flow {
        try {
            val response = fireBaseApiClient.getPostList(
                userDataSource.getIdToken(),
                "\"$location\""
            )
            response.onSuccess { data ->
                emit(data.map { entry ->
                    entry.value.run {
                        copy(
                            imageUrlList = imageLocations?.map { location ->
                                imageDataSource.downloadImage(location)
                            } ?: emptyList()
                        )
                    }
                })
            }.onError { code, message ->
                onError("code: $code, message: $message")
            }.onException {
                onError(it.message)
            }
        } catch (e: Exception) {
            onError(e.message)
        }
    }.onCompletion {
        onComplete()
    }.flowOn(Dispatchers.Default)
}