package com.recime.challenge.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.recime.challenge.designsystem.R
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.designsystem.theme.ReciMeTokens

@Composable
fun ReciMeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.designsystem_search),
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge.copy(color = ReciMeTokens.inkMuted),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(30.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = ReciMeTokens.searchFill,
            unfocusedContainerColor = ReciMeTokens.searchFill,
            disabledContainerColor = ReciMeTokens.searchFill,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ReciMeSearchBarPreview() {
    ReciMeTheme {
        ReciMeSearchBar(query = "", onQueryChange = {})
    }
}
