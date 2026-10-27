package com.recime.challenge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.recime.challenge.designsystem.R
import com.recime.challenge.designsystem.theme.ReciMeTokens

@Composable
fun ReciMeHomeBar(
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlignedIconButton(
            onClick = onMenuClick,
            contentDescription = stringResource(R.string.designsystem_menu),
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun ReciMeDetailBar(
    onBackClick: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 41.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlignedIconButton(onClick = onBackClick, contentDescription = stringResource(R.string.designsystem_back)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )
        }
        AlignedIconButton(
            onClick = onFavoriteClick,
            contentDescription = stringResource(
                if (isFavorite) R.string.designsystem_favorite_remove else R.string.designsystem_favorite_add,
            ),
            alignStart = false,
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) ReciMeTokens.orange else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun ReciMeTitleBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 41.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlignedIconButton(onClick = onBackClick, contentDescription = stringResource(R.string.designsystem_back)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun AlignedIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    alignStart: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
        contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        content()
    }
}
