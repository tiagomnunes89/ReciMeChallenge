package com.recime.challenge.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class RetryInterceptorTest {
    private val server = MockWebServer()

    init {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `persistent server errors make three attempts`() {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(500))
        val delays = mutableListOf<Long>()
        val client = client(delays)

        val response = client.newCall(Request.Builder().url(server.url("/")).build()).execute()

        assertEquals(500, response.code)
        assertEquals(3, server.requestCount)
        assertEquals(listOf(100L, 200L), delays)
    }

    @Test
    fun `a later success is returned after retry`() {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = client(mutableListOf()).newCall(
            Request.Builder().url(server.url("/")).build(),
        ).execute()

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `client errors are not retried`() {
        server.enqueue(MockResponse().setResponseCode(400))

        val response = client(mutableListOf()).newCall(
            Request.Builder().url(server.url("/")).build(),
        ).execute()

        assertEquals(400, response.code)
        assertEquals(1, server.requestCount)
    }

    private fun client(delays: MutableList<Long>): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(
                RetryInterceptor(
                    sleeper = RetryInterceptor.Sleeper { delays += it },
                    random = { 0.0 },
                ),
            )
            .build()
}
