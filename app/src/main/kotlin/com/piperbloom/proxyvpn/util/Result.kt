package com.piperbloom.proxyvpn.util

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun failure(exception: Throwable): Result<Nothing> = Failure(exception)
        fun loading(): Result<Nothing> = Loading
    }
}