package com.recime.challenge.ui

import androidx.annotation.StringRes
import com.recime.challenge.R
import com.recime.challenge.domain.model.RecipeNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

@StringRes
internal fun Throwable.toUserMessageRes(): Int = when (this) {
    is RecipeNotFoundException -> R.string.common_error_recipe_unavailable
    is SocketTimeoutException -> R.string.common_error_timeout
    is UnknownHostException, is ConnectException, is IOException -> R.string.common_error_offline
    is HttpException -> when {
        code() == 408 -> R.string.common_error_timeout
        code() >= 500 -> R.string.common_error_server
        else -> R.string.common_error_generic
    }
    is SerializationException -> R.string.common_error_parse
    else -> R.string.common_error_generic
}
