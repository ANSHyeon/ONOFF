package com.anshyeon.onoff.util

fun isValidNickname(nickname: String): Boolean {
    return nickname.isNotBlank() && "[가-힣a-zA-Z0-9]{2,6}".toRegex().matches(nickname)
}