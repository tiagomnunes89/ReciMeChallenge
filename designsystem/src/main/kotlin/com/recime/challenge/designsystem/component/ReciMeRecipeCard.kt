package com.recime.challenge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.recime.challenge.designsystem.R
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.designsystem.theme.ReciMeTokens

enum class ReciMeRecipeCardSize { Large, Compact }

private const val PHOTO_OVERLAP_DIVISOR = 3

@Composable
fun ReciMeRecipeCard(
    title: String,
    accent: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: ReciMeRecipeCardSize = ReciMeRecipeCardSize.Compact,
) {
    val isLarge = size == ReciMeRecipeCardSize.Large
    val photoSize = if (isLarge) ReciMeTokens.largeCardPhotoSize else ReciMeTokens.compactCardPhotoSize
    val cardHeight = if (isLarge) ReciMeTokens.largeCardHeight else ReciMeTokens.compactCardHeight
    val overlap = photoSize / PHOTO_OVERLAP_DIVISOR
    val widthModifier = if (isLarge) Modifier.width(ReciMeTokens.largeCardWidth) else Modifier.fillMaxWidth()
    Box(
        modifier = modifier
            .then(widthModifier)
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.designsystem_open_recipe, title),
                onClick = onClick,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier = Modifier
                .padding(top = overlap)
                .fillMaxWidth()
                .height(cardHeight)
                .shadow(
                    elevation = ReciMeTokens.cardElevation,
                    shape = MaterialTheme.shapes.large,
                    ambientColor = ReciMeTokens.cardShadow,
                    spotColor = ReciMeTokens.cardShadow,
                ),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ReciMeTokens.contentSpacing)
                    .padding(
                        top = photoSize - overlap + ReciMeTokens.inlinePadding,
                        bottom = ReciMeTokens.cardBottomPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(ReciMeTokens.contentSpacing))
                Text(
                    text = accent,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
        ReciMeRecipeImage(
            imageUrl = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(photoSize)
                .shadow(ReciMeTokens.cardPhotoElevation, CircleShape)
                .clip(CircleShape),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun ReciMeRecipeCardPreview() {
    ReciMeTheme {
        ReciMeRecipeCard(
            title = "Veggie tomato mix",
            accent = "Serves 2",
            onClick = {},
            size = ReciMeRecipeCardSize.Large,
        )
    }
}
