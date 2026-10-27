package com.recime.challenge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.recime.challenge.designsystem.theme.ReciMeTokens

@Composable
fun ReciMeCategoryTabs(
    categories: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        itemsIndexed(categories) { index, label ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .clickable { onSelect(index) }
                    .padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else ReciMeTokens.tabInactive,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                HorizontalDivider(
                    modifier = Modifier.width(87.dp),
                    thickness = 3.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                )
            }
        }
    }
}
