package com.recime.challenge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.recime.challenge.ui.detail.RecipeDetailRoute
import com.recime.challenge.ui.favorites.FavoritesRoute
import com.recime.challenge.ui.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
internal data object HomeDestination

@Serializable
internal data object FavoritesDestination

@Serializable
internal data class RecipeDetailDestination(val recipeId: String)

@Composable
fun HomeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
    ) {
        composable<HomeDestination> {
            HomeRoute(
                onRecipeClick = { id ->
                    navController.navigate(RecipeDetailDestination(id)) {
                        launchSingleTop = true
                    }
                },
                onFavoritesClick = {
                    navController.navigate(FavoritesDestination) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<FavoritesDestination> {
            FavoritesRoute(
                onBackClick = navController::popBackStack,
                onRecipeClick = { id ->
                    navController.navigate(RecipeDetailDestination(id)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<RecipeDetailDestination> {
            RecipeDetailRoute(onBackClick = navController::popBackStack)
        }
    }
}
