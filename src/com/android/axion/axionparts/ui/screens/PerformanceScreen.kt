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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.FrequencySlider
import com.android.axion.axionparts.ui.components.LevelSlider
import com.android.axion.axionparts.ui.components.PowerModeToggle
import com.android.axion.axionparts.ui.theme.ExpressiveShapes
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.rememberSettingsFlow
import com.android.axion.compose.scaffold.AxionScaffold

private data class ClusterConfig(
    val name: String,
    val maxFreq: Int,
    val availableFreqs: List<Int>,
    val minFreqKey: String,
    val maxFreqKey: String,
    val accentColor: Color,
)

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
                modifier = Modifier.padding(innerPadding),
            )
        }
    } else {
        PerformanceContent(
            onBackgroundManagerClick = { onNavigateToDetail("background_manager") },
            modifier = Modifier,
        )
    }
}

@Composable
fun PerformanceContent(
    onBackgroundManagerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val flow = rememberSettingsFlow(SettingsType.SECURE)

    val smallAvailableFreqs = remember {
        flow.getString("ax_cpu_small_freqs").split(",").mapNotNull { it.toIntOrNull() }
    }

    val bigAvailableFreqs = remember {
        flow.getString("ax_cpu_big_freqs").split(",").mapNotNull { it.toIntOrNull() }
    }

    val primeAvailableFreqs = remember {
        flow.getString("ax_cpu_prime_freqs").split(",").mapNotNull { it.toIntOrNull() }
    }

    val gpuFreqsPath = remember { SystemProperties.get("persist.sys.axion_gpu_freqs_path", "") }
    val gpuMinFreqFile = remember { SystemProperties.get("persist.sys.axion_gpu_minfreq_file", "") }
    val gpuMaxLevels = remember { SystemProperties.getInt("persist.sys.axion_gpu_levels", 0) }
    val showGpu = gpuFreqsPath.isNotEmpty() && gpuMinFreqFile.isNotEmpty() && gpuMaxLevels > 0

    val littleClusterName = stringResource(R.string.little_cluster)
    val bigClusterName = stringResource(R.string.big_cluster)
    val primeClusterName = stringResource(R.string.prime_cluster)
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val clusters =
        remember(
            littleClusterName,
            bigClusterName,
            primeClusterName,
            smallAvailableFreqs,
            bigAvailableFreqs,
            primeAvailableFreqs,
            primaryColor,
            tertiaryColor,
            secondaryColor,
        ) {
            listOf(
                ClusterConfig(
                    name = littleClusterName,
                    maxFreq = smallAvailableFreqs.maxOrNull() ?: 0,
                    availableFreqs = smallAvailableFreqs,
                    minFreqKey = "axion_min_freq",
                    maxFreqKey = "axion_max_freq",
                    accentColor = primaryColor,
                ),
                ClusterConfig(
                    name = bigClusterName,
                    maxFreq = bigAvailableFreqs.maxOrNull() ?: 0,
                    availableFreqs = bigAvailableFreqs,
                    minFreqKey = "axion_min_freq_big",
                    maxFreqKey = "axion_max_freq_big",
                    accentColor = tertiaryColor,
                ),
                ClusterConfig(
                    name = primeClusterName,
                    maxFreq = primeAvailableFreqs.maxOrNull() ?: 0,
                    availableFreqs = primeAvailableFreqs,
                    minFreqKey = "axion_min_freq_prime",
                    maxFreqKey = "axion_max_freq_prime",
                    accentColor = secondaryColor,
                ),
            )
        }

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ClickablePreference(
            title = stringResource(R.string.background_manager),
            summary = stringResource(R.string.background_manager_summary),
            icon = Icons.Outlined.Apps,
            onClick = onBackgroundManagerClick,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PowerModeToggle()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "FREQUENCY CONTROL",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
        )

        clusters.forEach { cluster ->
            if (cluster.maxFreq > 0) {
                ClusterCard(cluster = cluster)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showGpu) {
            GpuCard(maxLevels = gpuMaxLevels)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ClusterCard(cluster: ClusterConfig) {
    val icon =
        when {
            cluster.name.contains("Little") -> Icons.Outlined.Bolt
            cluster.name.contains("Big") -> Icons.Outlined.RocketLaunch
            else -> Icons.Outlined.Layers
        }

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .clip(ExpressiveShapes.large)
                .background(MaterialTheme.colorScheme.surfaceBright)
    ) {
        ClusterHeader(
            name = cluster.name,
            icon = icon,
            accentColor = cluster.accentColor,
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FrequencySlider(
                settingKey = cluster.minFreqKey,
                label = stringResource(R.string.minimum_frequency),
                availableFrequencies = cluster.availableFreqs.takeIf { it.isNotEmpty() },
                min = 0,
                max = cluster.maxFreq,
                interval = 100000,
                defaultValue = 0,
                accentColor = cluster.accentColor,
            )

            FrequencySlider(
                settingKey = cluster.maxFreqKey,
                label = stringResource(R.string.maximum_frequency),
                availableFrequencies = cluster.availableFreqs.takeIf { it.isNotEmpty() },
                min = 0,
                max = cluster.maxFreq,
                interval = 100000,
                defaultValue = cluster.maxFreq,
                accentColor = cluster.accentColor,
            )
        }
    }
}

@Composable
private fun ClusterHeader(
    name: String,
    icon: ImageVector,
    accentColor: Color,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth().background(accentColor.copy(alpha = 0.25f)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier.size(40.dp)
                    .clip(ExpressiveShapes.small)
                    .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun GpuCard(maxLevels: Int) {
    val gpuColor = MaterialTheme.colorScheme.tertiary

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .clip(ExpressiveShapes.large)
                .background(MaterialTheme.colorScheme.surfaceBright)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth().background(gpuColor.copy(alpha = 0.08f)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
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
            Column {
                Text(
                    text = "GPU",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Graphics Performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
