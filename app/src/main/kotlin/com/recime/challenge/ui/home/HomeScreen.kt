package com.recime.challenge.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.recime.challenge.designsystem.component.ReciMeCategoryTabs
import com.recime.challenge.designsystem.component.ReciMeEmptyState
import com.recime.challenge.designsystem.component.ReciMeHomeBar
import com.recime.challenge.designsystem.component.ReciMeRecipeCard
import com.recime.challenge.designsystem.component.ReciMeRecipeCardSize
import com.recime.challenge.designsystem.component.ReciMeSearchBar
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.designsystem.theme.ReciMeTokens
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.R
import com.recime.challenge.ui.toUserMessageRes
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    onRecipeClick: (recipeId: String) -> Unit,
    onFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val recipes = viewModel.recipes.collectAsLazyPagingItems()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    HomeDrawer(
        drawerState = drawerState,
        onFavoritesClick = {
            scope.launch { drawerState.close() }
            onFavoritesClick()
        },
        onAllRecipesClick = { scope.launch { drawerState.close() } },
    ) {
        HomeScreen(
            filters = { filters },
            recipes = recipes,
            onFiltersChange = viewModel::onFiltersChange,
            onRecipeClick = onRecipeClick,
            onMenuClick = { scope.launch { drawerState.open() } },
            modifier = modifier,
        )
    }
}

@Composable
fun HomeScreen(
    filters: () -> SearchFilters,
    recipes: LazyPagingItems<Recipe>,
    onFiltersChange: (SearchFilters) -> Unit,
    onRecipeClick: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = GRID_MIN_CELL_WIDTH),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            fullSpanItem(key = "header") {
                HomeHeader(filtersProvider = filters, onFiltersChange = onFiltersChange, onMenuClick = onMenuClick)
            }
            recipeContent(recipes = recipes, onRecipeClick = onRecipeClick)
        }
    }
}

private fun LazyGridScope.fullSpanItem(key: String, content: @Composable () -> Unit) {
    item(key = key, span = { GridItemSpan(maxLineSpan) }, contentType = key) { content() }
}

private fun LazyGridScope.recipeContent(
    recipes: LazyPagingItems<Recipe>,
    onRecipeClick: (recipeId: String) -> Unit,
) {
    val refreshState = recipes.loadState.refresh
    when {
        recipes.itemCount > 0 -> {
            recipeItems(recipes, onRecipeClick)
            appendState(recipes)
        }
        refreshState is LoadState.Loading -> fullSpanItem(key = "loading") { LoadingIndicator() }
        refreshState is LoadState.Error -> fullSpanItem(key = "error") {
            ReciMeEmptyState(
                title = stringResource(R.string.common_error_title),
                message = stringResource(refreshState.error.toUserMessageRes()),
                icon = Icons.Default.Warning,
                actionLabel = stringResource(R.string.common_try_again),
                onAction = recipes::retry,
            )
        }
        else -> fullSpanItem(key = "empty") {
            ReciMeEmptyState(
                title = stringResource(R.string.home_empty_title),
                message = stringResource(R.string.home_empty_message),
                icon = Icons.Default.Search,
            )
        }
    }
}

/**
 * Stable keys keep scroll position and item state across refreshes; the content type lets Compose
 * reuse recipe-card compositions instead of recomposing them from scratch.
 */
private fun LazyGridScope.recipeItems(
    recipes: LazyPagingItems<Recipe>,
    onRecipeClick: (recipeId: String) -> Unit,
) {
    items(
        count = recipes.itemCount,
        key = recipes.itemKey { it.id },
        contentType = recipes.itemContentType { RECIPE_CONTENT_TYPE },
    ) { index ->
        recipes[index]?.let { recipe -> RecipeGridCard(recipe = recipe, onClick = onRecipeClick) }
    }
}

private fun LazyGridScope.appendState(recipes: LazyPagingItems<Recipe>) {
    when (val appendState = recipes.loadState.append) {
        is LoadState.Loading -> fullSpanItem(key = "append-loading") { LoadingIndicator(Modifier.padding(16.dp)) }
        is LoadState.Error -> fullSpanItem(key = "append-error") {
            AppendError(message = stringResource(appendState.error.toUserMessageRes()), onRetry = recipes::retry)
        }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
}

@Composable
private fun AppendError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRetry) { Text(stringResource(R.string.common_try_again)) }
    }
}

@Composable
private fun HomeHeader(
    filtersProvider: () -> SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onMenuClick: () -> Unit,
) {
    // Read here, not in HomeScreen, so typing only recomposes the header and never the recipe grid.
    val filters = filtersProvider()
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        ReciMeHomeBar(onMenuClick = onMenuClick, modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(R.string.home_headline),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        ReciMeSearchBar(
            query = filters.text,
            onQueryChange = { onFiltersChange(filters.copy(text = it)) },
        )
        ReciMeCategoryTabs(
            categories = listOf(
                stringResource(R.string.home_tab_all),
                stringResource(R.string.home_tab_vegetarian),
            ),
            selectedIndex = if (filters.vegetarianOnly) 1 else 0,
            onSelect = { index -> onFiltersChange(filters.copy(vegetarianOnly = index == 1)) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    text = stringResource(if (expanded) R.string.home_filters_less else R.string.home_filters_more),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        if (expanded) {
            AdvancedFilters(filters = filters, onFiltersChange = onFiltersChange)
        }
    }
}

@Composable
private fun AdvancedFilters(
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KitField(
            value = filters.servings,
            onValueChange = { onFiltersChange(filters.copy(servings = it)) },
            placeholder = stringResource(R.string.home_filter_servings),
            keyboardType = KeyboardType.Number,
        )
        KitField(
            value = filters.includeIngredients,
            onValueChange = { onFiltersChange(filters.copy(includeIngredients = it)) },
            placeholder = stringResource(R.string.home_filter_include),
        )
        KitField(
            value = filters.excludeIngredients,
            onValueChange = { onFiltersChange(filters.copy(excludeIngredients = it)) },
            placeholder = stringResource(R.string.home_filter_exclude),
        )
        KitField(
            value = filters.instructionText,
            onValueChange = { onFiltersChange(filters.copy(instructionText = it)) },
            placeholder = stringResource(R.string.home_filter_instructions),
        )
    }
}

@Composable
private fun KitField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = ReciMeTokens.inkMuted) },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = ReciMeTokens.searchFill,
            unfocusedContainerColor = ReciMeTokens.searchFill,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
internal fun RecipeGridCard(recipe: Recipe, onClick: (recipeId: String) -> Unit) {
    // `onClick` is passed as-is (not wrapped per item) so every card receives the same stable lambda.
    ReciMeRecipeCard(
        title = recipe.title,
        accent = stringResource(R.string.common_serves, recipe.servings),
        onClick = { onClick(recipe.id) },
        imageUrl = recipe.imageUrl,
        size = ReciMeRecipeCardSize.Compact,
        modifier = Modifier.fillMaxWidth(),
    )
}

internal val GRID_MIN_CELL_WIDTH = 160.dp
private const val RECIPE_CONTENT_TYPE = "recipe"

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2, heightDp = 900)
@Composable
private fun HomeScreenSuccessPreview() {
    ReciMeTheme {
        HomeScreen(
            filters = { SearchFilters() },
            recipes = rememberPreviewPagingItems(),
            onFiltersChange = {},
            onRecipeClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun HomeScreenEmptyPreview() {
    ReciMeTheme {
        HomeScreen(
            filters = { SearchFilters(text = "sushi") },
            recipes = rememberPreviewPagingItems(recipes = emptyList()),
            onFiltersChange = {},
            onRecipeClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F8)
@Composable
private fun HomeScreenErrorPreview() {
    ReciMeTheme {
        HomeScreen(
            filters = { SearchFilters() },
            recipes = rememberPreviewPagingItems(
                recipes = emptyList(),
                refresh = LoadState.Error(IllegalStateException("offline")),
            ),
            onFiltersChange = {},
            onRecipeClick = {},
        )
    }
}
