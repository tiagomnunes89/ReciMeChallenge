package com.recime.challenge.domain.model

import java.io.IOException
import kotlinx.coroutines.CancellationException

internal fun Throwable.rethrowIfCancellation() {
    if (this is CancellationException) throw this
    if (this is IOException && message == OKHTTP_CANCELED_MESSAGE) {
        throw CancellationException(message, this)
    }
}

private const val OKHTTP_CANCELED_MESSAGE = "Canceled"
