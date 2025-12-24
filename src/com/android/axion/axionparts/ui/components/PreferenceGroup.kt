package com.android.axion.axionparts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface PreferenceGroupScope {
    fun item(content: @Composable () -> Unit)
}

class PreferenceGroupScopeImpl : PreferenceGroupScope {
    private val items = mutableListOf<@Composable () -> Unit>()

    override fun item(content: @Composable () -> Unit) {
        items.add(content)
    }

    fun getItems(): List<@Composable () -> Unit> = items
}

@Composable
fun PreferenceGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    spaceBetween: Dp = 1.dp,
    content: PreferenceGroupScope.() -> Unit
) {
    val scope = PreferenceGroupScopeImpl()
    scope.content()
    val items = scope.getItems()

    Column(modifier = modifier) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp, top = 4.dp)
            )
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(spaceBetween)
        ) {
            items.forEachIndexed { index, composable ->
                val position = when {
                    items.size == 1 -> PreferencePosition.Single
                    index == 0 -> PreferencePosition.Top
                    index == items.size - 1 -> PreferencePosition.Bottom
                    else -> PreferencePosition.Middle
                }
                CompositionLocalProvider(LocalPreferencePosition provides position) {
                    composable()
                }
            }
        }
    }
}

