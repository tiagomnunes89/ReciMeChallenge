package com.recime.challenge.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recime.challenge.R
import com.recime.challenge.designsystem.component.ReciMeEmptyState
import com.recime.challenge.designsystem.component.ReciMeTitleBar
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.designsystem.theme.ReciMeTokens
import com.recime.challenge.ui.home.GRID_MIN_CELL_WIDTH
import com.recime.challenge.ui.home.RecipeGridCard
import com.recime.challenge.ui.home.previewRecipes

private const val RECIPE_CONTENT_TYPE = "recipe"

@Composable
fun FavoritesRoute(
    onBackClick: () -> Unit,
    onRecipeClick: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRecipeClick = onRecipeClick,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onBackClick: () -> Unit,
    onRecipeClick: (recipeId: String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { ReciMeTitleBar(title = stringResource(R.string.favorites_title), onBackClick = onBackClick) },
    ) { padding ->
        when (uiState) {
            FavoritesUiState.Loading -> Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            is FavoritesUiState.Error -> ReciMeEmptyState(
                title = stringResource(R.string.common_error_title),
                message = stringResource(uiState.messageRes),
                icon = Icons.Default.Warning,
                actionLabel = stringResource(R.string.common_try_again),
                onAction = onRetry,
                modifier = Modifier.padding(padding),
            )

            is FavoritesUiState.Success ->
                if (uiState.recipes.isEmpty()) {
                    ReciMeEmptyState(
                        title = stringResource(R.string.favorites_empty_title),
                        message = stringResource(R.string.favorites_empty_message),
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = GRID_MIN_CELL_WIDTH),
                        modifier = Modifier.padding(padding).fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = ReciMeTokens.screenPadding,
                            end = ReciMeTokens.screenPadding,
                            top = ReciMeTokens.screenPadding,
                            bottom = ReciMeTokens.screenBottomPadding,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(ReciMeTokens.gridSpacing),
                        verticalArrangement = Arrangement.spacedBy(ReciMeTokens.gridRowSpacing),
                    ) {
                        items(uiState.recipes, key = { it.id }, contentType = { RECIPE_CONTENT_TYPE }) { recipe ->
                            RecipeGridCard(recipe = recipe, onClick = onRecipeClick)
                        }
                    }
                }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2, heightDp = 800)
@Composable
private fun FavoritesPreview() {
    ReciMeTheme {
        FavoritesScreen(
            uiState = FavoritesUiState.Success(previewRecipes.take(2)),
            onBackClick = {},
            onRecipeClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun FavoritesEmptyPreview() {
    ReciMeTheme {
        FavoritesScreen(
            uiState = FavoritesUiState.Success(emptyList()),
            onBackClick = {},
            onRecipeClick = {},
            onRetry = {},
        )
    }
}
