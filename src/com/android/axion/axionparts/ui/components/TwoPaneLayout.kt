/*
 * Copyright (C) 2025 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.axion.axionparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    
    return when {
        screenWidthDp < 600 -> WindowSizeClass.COMPACT
        screenWidthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

@Composable
fun TwoPaneLayout(
    windowSizeClass: WindowSizeClass,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
    showDetailPane: Boolean,
    modifier: Modifier = Modifier
) {
    val isExpandedOrMedium = windowSizeClass == WindowSizeClass.EXPANDED || 
                             windowSizeClass == WindowSizeClass.MEDIUM
    
    if (isExpandedOrMedium) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
            ) {
                listPane()
            }
            
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
            ) {
                detailPane()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            if (showDetailPane) {
                detailPane()
            } else {
                listPane()
            }
        }
    }
}

@Composable
fun AdaptiveDetailContainer(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isExpandedOrMedium = windowSizeClass == WindowSizeClass.EXPANDED || 
                             windowSizeClass == WindowSizeClass.MEDIUM
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isExpandedOrMedium) {
                    Modifier.padding(horizontal = 16.dp)
                } else {
                    Modifier
                }
            ),
        contentAlignment = if (isExpandedOrMedium) Alignment.TopCenter else Alignment.TopStart
    ) {
        Box(
            modifier = if (isExpandedOrMedium) {
                Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }
        ) {
            content()
        }
    }
}
