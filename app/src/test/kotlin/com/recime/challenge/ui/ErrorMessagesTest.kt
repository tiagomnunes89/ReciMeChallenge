package com.recime.challenge.ui

import com.recime.challenge.R
import com.recime.challenge.domain.model.RecipeNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ErrorMessagesTest {

    @Test
    fun `maps recipe not found`() {
        assertEquals(
            R.string.common_error_recipe_unavailable,
            RecipeNotFoundException("missing").toUserMessageRes(),
        )
    }

    @Test
    fun `maps timeout`() {
        assertEquals(
            R.string.common_error_timeout,
            SocketTimeoutException().toUserMessageRes(),
        )
    }

    @Test
    fun `maps network failures`() {
        assertEquals(R.string.common_error_offline, UnknownHostException().toUserMessageRes())
        assertEquals(R.string.common_error_offline, ConnectException().toUserMessageRes())
        assertEquals(R.string.common_error_offline, IOException().toUserMessageRes())
    }

    @Test
    fun `maps server errors`() {
        val error = HttpException(Response.error<Unit>(503, "unavailable".toResponseBody()))

        assertEquals(R.string.common_error_server, error.toUserMessageRes())
    }

    @Test
    fun `maps request timeout responses`() {
        val error = HttpException(Response.error<Unit>(408, "timeout".toResponseBody()))

        assertEquals(R.string.common_error_timeout, error.toUserMessageRes())
    }

    @Test
    fun `maps parse errors`() {
        assertEquals(
            R.string.common_error_parse,
            SerializationException("invalid response").toUserMessageRes(),
        )
    }

    @Test
    fun `maps unsupported errors to generic message`() {
        assertEquals(
            R.string.common_error_generic,
            IllegalStateException().toUserMessageRes(),
        )
        assertEquals(
            R.string.common_error_generic,
            HttpException(Response.error<Unit>(400, "bad request".toResponseBody())).toUserMessageRes(),
        )
    }
}
