package com.recime.challenge.ui.detail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recime.challenge.designsystem.component.ReciMeDetailBar
import com.recime.challenge.designsystem.component.ReciMeEmptyState
import com.recime.challenge.designsystem.component.ReciMeRecipeImage
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.designsystem.theme.ReciMeTokens
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.R
import com.recime.challenge.ui.home.previewRecipes

@Composable
fun RecipeDetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecipeDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::onRetry,
        onFavoriteClick = viewModel::onFavoriteClick,
        modifier = modifier,
    )
}

@Composable
fun RecipeDetailScreen(
    uiState: RecipeDetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ReciMeTokens.screenAlt,
        topBar = {
            ReciMeDetailBar(
                onBackClick = onBackClick,
                isFavorite = (uiState as? RecipeDetailUiState.Success)?.isFavorite == true,
                onFavoriteClick = onFavoriteClick,
            )
        },
    ) { padding ->
        when (uiState) {
            RecipeDetailUiState.Loading -> CenteredContent(Modifier.padding(padding)) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            is RecipeDetailUiState.Error -> ReciMeEmptyState(
                title = stringResource(R.string.common_error_title),
                message = stringResource(uiState.messageRes),
                icon = Icons.Default.Warning,
                actionLabel = stringResource(R.string.common_try_again),
                onAction = onRetry,
                modifier = Modifier.padding(padding),
            )
            is RecipeDetailUiState.Success -> RecipeDetailContent(
                recipe = uiState.recipe,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun RecipeDetailContent(recipe: Recipe, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 50.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ReciMeRecipeImage(
                imageUrl = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(241.dp)
                    .shadow(24.dp, CircleShape)
                    .clip(CircleShape),
            )
        }
        item {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.common_serves, recipe.servings),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                ),
                textAlign = TextAlign.Center,
            )
        }
        item { DetailSection(title = stringResource(R.string.detail_about), body = recipe.description) }
        item { DetailSection(title = stringResource(R.string.detail_ingredients), body = null) }
        itemsIndexed(recipe.ingredients) { _, ingredient ->
            IngredientRow(ingredient)
        }
        item { DetailSection(title = stringResource(R.string.detail_instructions), body = null) }
        itemsIndexed(recipe.instructions) { index, step ->
            Text(
                text = stringResource(R.string.detail_instruction_step, index + 1, step),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DetailSection(title: String, body: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (body != null) {
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun IngredientRow(ingredient: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall),
        )
        Text(text = ingredient, style = MaterialTheme.typography.bodyMedium.copy(color = ReciMeTokens.inkMuted))
    }
}

@Composable
private fun CenteredContent(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F9, heightDp = 900)
@Composable
private fun RecipeDetailSuccessPreview() {
    ReciMeTheme {
        RecipeDetailScreen(
            uiState = RecipeDetailUiState.Success(previewRecipes.first()),
            onBackClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F9, heightDp = 900)
@Composable
private fun RecipeDetailFavoritePreview() {
    ReciMeTheme {
        RecipeDetailScreen(
            uiState = RecipeDetailUiState.Success(previewRecipes.first(), isFavorite = true),
            onBackClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F8)
@Composable
private fun RecipeDetailErrorPreview() {
    ReciMeTheme {
        RecipeDetailScreen(
            uiState = RecipeDetailUiState.Error(R.string.common_error_recipe_unavailable),
            onBackClick = {},
            onRetry = {},
        )
    }
}
