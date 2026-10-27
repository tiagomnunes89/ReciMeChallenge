package com.recime.challenge.data.remote

import java.io.IOException
import java.io.InterruptedIOException
import kotlin.random.Random
import okhttp3.Interceptor
import okhttp3.Response

internal class RetryInterceptor(
    private val sleeper: Sleeper = Sleeper { millis -> Thread.sleep(millis) },
    private val random: () -> Double = { Random.nextDouble() },
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (chain.request().method != GET) return chain.proceed(chain.request())
        var attempt = 0
        while (true) {
            if (chain.call().isCanceled()) throw IOException("Canceled")
            try {
                val response = chain.proceed(chain.request())
                if (!response.isServerError() || attempt >= MAX_RETRIES) return response
                response.close()
            } catch (exception: IOException) {
                if (attempt >= MAX_RETRIES) throw exception
            }
            if (chain.call().isCanceled()) throw IOException("Canceled")
            val delay = BASE_DELAY_MILLIS * (1L shl attempt) +
                (random().coerceIn(0.0, 1.0) * JITTER_BOUND_MILLIS).toLong()
            try {
                sleeper.sleep(delay)
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                throw InterruptedIOException(exception.message)
            }
            attempt++
        }
    }

    private fun Response.isServerError(): Boolean = code in SERVER_ERROR_RANGE

    fun interface Sleeper {
        fun sleep(millis: Long)
    }

    private companion object {
        const val GET = "GET"
        const val MAX_RETRIES = 2
        const val BASE_DELAY_MILLIS = 100L
        const val JITTER_BOUND_MILLIS = BASE_DELAY_MILLIS
        val SERVER_ERROR_RANGE = 500..599
    }
}
