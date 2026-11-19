package com.recime.challenge.ui

import androidx.annotation.StringRes
import com.recime.challenge.R
import com.recime.challenge.domain.model.RecipeNotFoundException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

@StringRes
internal fun Throwable.toUserMessageRes(): Int = when (this) {
    is RecipeNotFoundException -> R.string.common_error_recipe_unavailable
    is SocketTimeoutException -> R.string.common_error_timeout
    is UnknownHostException, is ConnectException, is NoRouteToHostException -> R.string.common_error_offline
    is HttpException -> when {
        code() == HttpURLConnection.HTTP_CLIENT_TIMEOUT -> R.string.common_error_timeout
        code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> R.string.common_error_server
        else -> R.string.common_error_generic
    }
    is SerializationException -> R.string.common_error_parse
    else -> R.string.common_error_generic
}
