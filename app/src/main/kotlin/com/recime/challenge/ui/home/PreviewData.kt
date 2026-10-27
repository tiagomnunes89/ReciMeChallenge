package com.recime.challenge.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.recime.challenge.domain.model.DietaryAttribute
import com.recime.challenge.domain.model.Recipe
import kotlinx.coroutines.flow.flowOf

internal val previewRecipes = listOf(
    Recipe(
        id = "1",
        title = "Spaghetti Aglio e Olio",
        description = "Classic Italian pasta with garlic, chili and olive oil.",
        servings = 2,
        ingredients = listOf("spaghetti", "garlic", "olive oil", "chili flakes"),
        instructions = listOf(
            "Boil the spaghetti in salted water until al dente.",
            "Sauté sliced garlic and chili flakes in olive oil.",
            "Toss the pasta in the pan and serve.",
        ),
        dietaryAttributes = setOf(DietaryAttribute.VEGETARIAN, DietaryAttribute.VEGAN),
    ),
    Recipe(
        id = "2",
        title = "Chicken Caesar Salad",
        description = "Crisp romaine with grilled chicken, parmesan and croutons.",
        servings = 4,
        ingredients = listOf("chicken breast", "romaine lettuce", "parmesan"),
        instructions = listOf("Grill the chicken.", "Toss everything with dressing."),
        dietaryAttributes = emptySet(),
    ),
)

/** Builds a [LazyPagingItems] with a fixed content and load states, for previews and UI tests. */
@Composable
internal fun rememberPreviewPagingItems(
    recipes: List<Recipe> = previewRecipes,
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
): LazyPagingItems<Recipe> {
    val flow = remember(recipes, refresh, append) {
        val notLoading = LoadState.NotLoading(endOfPaginationReached = true)
        val loadStates = LoadStates(refresh = refresh, prepend = notLoading, append = append)
        flowOf(PagingData.from(recipes, sourceLoadStates = loadStates))
    }
    return flow.collectAsLazyPagingItems()
}
