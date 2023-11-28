package com.anshyeon.onoff.data.repository

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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val fireBaseApiClient: FireBaseApiClient,
    private val userDataSource: UserDataSource,
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
        val imageLocations = uploadImages(imageList)
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
        val response = fireBaseApiClient.getPostList(
            userDataSource.getIdToken(),
            "\"$location\""
        )
        response.onSuccess { data ->
            emit(data.map { entry ->
                entry.value.run {
                    copy(
                        imageUrlList = imageLocations?.map { location ->
                            getDownloadUrl(location)
                        } ?: emptyList()
                    )
                }
            })
        }.onError { code, message ->
            onError("code: $code, message: $message")
        }.onException {
            onError(it.message)
        }
    }.onCompletion {
        onComplete()
    }.flowOn(Dispatchers.Default)

    private suspend fun getDownloadUrl(location: String): String {
        return FirebaseStorage.getInstance()
            .getReference(location)
            .downloadUrl
            .await()
            .toString()
    }

    private suspend fun uploadImages(imageList: List<ImageContent>): List<String> = coroutineScope {
        imageList.map { image ->
            val storageRef = FirebaseStorage.getInstance().reference
            val location = "images/${image.fileName}"
            val imageRef = storageRef.child(location)
            imageRef
                .putFile(image.uri)
                .await()
            location
        }
    }
}