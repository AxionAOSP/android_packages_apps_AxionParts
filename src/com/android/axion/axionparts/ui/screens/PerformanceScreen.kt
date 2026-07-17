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

package com.android.axion.axionparts.ui.screens

import android.os.SystemProperties
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.FeatureCard
import com.android.axion.compose.scaffold.AxionScaffold

@Composable
fun PerformanceScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true,
    onNavigateToDetail: (String) -> Unit = {},
) {
    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.performance),
            onBackClick = { onBackClick?.invoke() },
        ) { innerPadding ->
            PerformanceContent(
                onBackgroundManagerClick = { onNavigateToDetail("background_manager") },
                onKernelManagerClick = { onNavigateToDetail("kernel_manager") },
                modifier = Modifier.padding(innerPadding),
            )
        }
    } else {
        PerformanceContent(
            onBackgroundManagerClick = { onNavigateToDetail("background_manager") },
            onKernelManagerClick = { onNavigateToDetail("kernel_manager") },
            modifier = Modifier,
        )
    }
}

@Composable
private fun PerformanceContent(
    onBackgroundManagerClick: () -> Unit,
    onKernelManagerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    }

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier.size(40.dp)
                        .clip(ExpressiveShapes.small)
                        .background(gpuColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = gpuColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            FeatureCard(
                title = stringResource(R.string.kernel_manager),
                summary = stringResource(R.string.kernel_manager_summary),
                onClick = onKernelManagerClick,
                modifier = Modifier.weight(1f),
                illustrationColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Filled.Sensors,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LevelSlider(
                settingKey = "axion_game_gpu_boost_level",
                label = stringResource(R.string.game_gpu_boost),
                min = 0,
                max = maxLevels,
                defaultValue = 1,
                accentColor = gpuColor,
            )

            LevelSlider(
                settingKey = "axion_sys_gpu_boost_level",
                label = stringResource(R.string.system_gpu_boost),
                min = 0,
                max = maxLevels,
                defaultValue = 1,
                accentColor = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
