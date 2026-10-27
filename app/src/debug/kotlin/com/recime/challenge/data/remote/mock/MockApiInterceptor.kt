package com.recime.challenge.data.remote.mock

import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import com.recime.challenge.domain.model.DEFAULT_PAGE_SIZE
import java.io.IOException
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

internal class MockApiInterceptor(
    private val recipes: () -> List<RecipeDto>,
    private val json: Json,
    private val latencyMillis: Long = NETWORK_LATENCY_MS,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        simulateLatency(chain)
        return route(chain.request())
    }

    private fun simulateLatency(chain: Interceptor.Chain) {
        try {
            if (latencyMillis > 0) Thread.sleep(latencyMillis)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw InterruptedIOException(exception.message)
        }
        if (chain.call().isCanceled()) throw IOException("Canceled")
    }

    private fun route(request: Request): Response {
        if (request.method != METHOD_GET) {
            return errorResponse(request, HttpURLConnection.HTTP_BAD_METHOD, "Method Not Allowed")
        }
        val segments: List<String> = request.url.pathSegments.filter(String::isNotEmpty)
        return when {
            segments == SEARCH_PATH -> searchResponse(request)
            segments.size == RECIPE_PATH_SIZE && segments.first() == RECIPES_SEGMENT ->
                recipeResponse(request, id = segments.last())
            else -> errorResponse(request, HttpURLConnection.HTTP_NOT_FOUND, "Not Found")
        }
    }

    private fun searchResponse(request: Request): Response {
        val url: HttpUrl = request.url
        val matching: List<RecipeDto> = recipes().filterBy(
            text = url.queryParameter("q"),
            vegetarian = url.queryParameter("vegetarian")?.toBooleanStrictOrNull(),
            servings = url.queryParameter("servings")?.toIntOrNull(),
            includeIngredients = url.queryParameterValues("include_ingredients").filterNotNull(),
            excludeIngredients = url.queryParameterValues("exclude_ingredients").filterNotNull(),
            instruction = url.queryParameter("instruction"),
        )
        val cursor = url.queryParameter("cursor")
        val limitValue = url.queryParameter("limit")
        val limit = limitValue?.toIntOrNull() ?: DEFAULT_PAGE_SIZE
        if (limitValue != null && limitValue.toIntOrNull() == null) {
            return errorResponse(request, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid limit")
        }
        if (limit !in 1..MAX_LIMIT) {
            return errorResponse(request, HttpURLConnection.HTTP_BAD_REQUEST, "Invalid limit")
        }
        return okResponse(request, matching.paginate(cursor = cursor, limit = limit))
    }

    private fun recipeResponse(request: Request, id: String): Response {
        val recipe: RecipeDto = recipes().firstOrNull { it.id == id }
            ?: return errorResponse(request, HttpURLConnection.HTTP_NOT_FOUND, "Recipe not found")
        val body: String = json.encodeToString(RecipeDto.serializer(), recipe)
        return jsonResponse(request, HttpURLConnection.HTTP_OK, "OK", body)
    }

    private fun okResponse(request: Request, response: RecipesResponseDto): Response {
        val body: String = json.encodeToString(RecipesResponseDto.serializer(), response)
        return jsonResponse(request, HttpURLConnection.HTTP_OK, "OK", body)
    }

    private fun errorResponse(request: Request, code: Int, message: String): Response {
        val body: String = json.encodeToString(buildJsonObject { put("error", message) })
        return jsonResponse(request, code, message, body)
    }

    private fun jsonResponse(request: Request, code: Int, message: String, body: String): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(body.toResponseBody(JSON_MEDIA_TYPE))
            .build()

    internal companion object {
        const val NETWORK_LATENCY_MS = 250L
        private const val METHOD_GET = "GET"
        private const val RECIPES_SEGMENT = "recipes"
        private const val RECIPE_PATH_SIZE = 2
        private const val MAX_LIMIT = 100
        private val SEARCH_PATH: List<String> = listOf(RECIPES_SEGMENT, "search")
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}

private fun List<RecipeDto>.paginate(cursor: String?, limit: Int): RecipesResponseDto {
    val afterCursor = cursor
        ?.let { value ->
            val cursorIndex = indexOfFirst { it.id == value }
            if (cursorIndex >= 0) drop(cursorIndex + 1) else this
        }
        ?: this
    val page = afterCursor.take(limit)
    return RecipesResponseDto(
        recipes = page,
        nextCursor = page.lastOrNull()?.id?.takeIf { page.size == limit && afterCursor.size > page.size },
    )
}
