package com.recime.challenge.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.net.URI

private const val ANDROID_ASSET_PREFIX = "file:///android_asset/"

@Composable
fun ReciMeRecipeImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val safeImageUrl = remember(imageUrl) { safeImageUrl(imageUrl) }
    val request = remember(safeImageUrl) {
        ImageRequest.Builder(context).data(safeImageUrl).crossfade(true).build()
    }
    val fallback = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = fallback,
        error = fallback,
        fallback = fallback,
        contentScale = ContentScale.Crop,
    )
}

internal fun safeImageUrl(imageUrl: String?): String? =
    imageUrl?.trim()?.takeIf(String::isNotBlank)?.let { candidate ->
        if (candidate.startsWith(ANDROID_ASSET_PREFIX)) {
            val assetPath = candidate.removePrefix(ANDROID_ASSET_PREFIX)
            if (
                assetPath.isNotBlank() &&
                !assetPath.contains("..") &&
                !assetPath.contains('\\')
            ) {
                candidate
            } else {
                null
            }
        } else {
            safeHttpsImageUrl(candidate)
        }
    }

private fun safeHttpsImageUrl(imageUrl: String): String? =
    runCatching { URI(imageUrl) }.getOrNull()?.takeIf { uri ->
        uri.scheme.equals("https", ignoreCase = true) &&
            !uri.host.isNullOrBlank() &&
            uri.userInfo == null &&
            uri.fragment == null
    }?.toString()
