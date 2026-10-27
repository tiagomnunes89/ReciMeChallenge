package com.recime.challenge.ui.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.recime.challenge.R
import com.recime.challenge.designsystem.theme.ReciMeTokens

@Composable
internal fun HomeDrawer(
    drawerState: DrawerState,
    onAllRecipesClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.background) {
                Spacer(Modifier.height(ReciMeTokens.drawerTopSpacing))
                Text(
                    text = stringResource(R.string.home_drawer_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = ReciMeTokens.orange,
                    modifier = Modifier.padding(
                        horizontal = ReciMeTokens.drawerHorizontalPadding,
                        vertical = ReciMeTokens.contentSpacing,
                    ),
                )
                DrawerItem(
                    label = stringResource(R.string.home_drawer_all_recipes),
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    onClick = onAllRecipesClick,
                    selected = true,
                )
                DrawerItem(
                    label = stringResource(R.string.home_drawer_favorites),
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    onClick = onFavoritesClick,
                )
            }
        },
        content = content,
    )
}

@Composable
private fun DrawerItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                color = if (selected) {
                    ReciMeTokens.orange
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
            )
        },
        icon = icon,
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = ReciMeTokens.orange.copy(alpha = 0.12f),
            selectedIconColor = ReciMeTokens.orange,
            selectedTextColor = ReciMeTokens.orange,
            unselectedIconColor = MaterialTheme.colorScheme.onBackground,
            unselectedTextColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}
