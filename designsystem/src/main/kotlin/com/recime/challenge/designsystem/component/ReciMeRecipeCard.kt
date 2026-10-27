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
import androidx.compose.ui.unit.dp
import com.recime.challenge.designsystem.R
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.designsystem.theme.ReciMeTokens

enum class ReciMeRecipeCardSize { Large, Compact }

@Composable
fun ReciMeRecipeCard(
    title: String,
    accent: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: ReciMeRecipeCardSize = ReciMeRecipeCardSize.Compact,
) {
    val cardWidth = if (size == ReciMeRecipeCardSize.Large) 220.dp else 156.dp
    val photoSize = if (size == ReciMeRecipeCardSize.Large) 164.dp else 129.dp
    val cardHeight = if (size == ReciMeRecipeCardSize.Large) 270.dp else 212.dp
    val overlap = photoSize / 3

    val widthModifier = if (size == ReciMeRecipeCardSize.Large) Modifier.width(cardWidth) else Modifier.fillMaxWidth()
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
                    elevation = 24.dp,
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
                    .padding(horizontal = 12.dp)
                    .padding(top = photoSize - overlap + 8.dp, bottom = 20.dp),
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
                Spacer(Modifier.height(12.dp))
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
            // The title is already read by the card, so the photo is decorative.
            contentDescription = null,
            modifier = Modifier
                .size(photoSize)
                .shadow(12.dp, CircleShape)
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
