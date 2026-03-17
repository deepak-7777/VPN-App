package com.securevpn.app.utils

/**
 * A generic wrapper that represents a loading, success, or error state for any UI operation.
 */
sealed class Resource<out T> {

    /** Loading state — UI should show a progress indicator */
    object Loading : Resource<Nothing>()

    /** Success state with the result data */
    data class Success<T>(val data: T) : Resource<T>()

    /** Error state with an optional message and throwable */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : Resource<Nothing>()
}
