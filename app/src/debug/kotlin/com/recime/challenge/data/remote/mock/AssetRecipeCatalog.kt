package com.recime.challenge.data.remote.mock

import android.content.Context
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import kotlinx.serialization.json.Json

internal class AssetRecipeCatalog(
    private val context: Context,
    private val json: Json,
) {

    val recipes: List<RecipeDto> by lazy {
        val text: String = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        json.decodeFromString<RecipesResponseDto>(text).recipes.simulateCatalog(SIMULATED_CATALOG_SIZE)
    }

    private companion object {
        const val ASSET_NAME = "recipes.json"

        const val SIMULATED_CATALOG_SIZE = 100
    }
}
