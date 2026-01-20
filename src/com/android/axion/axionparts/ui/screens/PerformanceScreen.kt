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
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.BoostToggleCard
import com.android.axion.axionparts.ui.components.FrequencySlider
import com.android.axion.axionparts.ui.components.LevelSlider
import com.android.axion.axionparts.ui.components.PowerModeToggle
import com.android.axion.axionparts.ui.theme.BottomNavPadding
import com.android.axion.axionparts.ui.theme.ExpressiveShapes

private data class ClusterConfig(
    val name: String,
    val id: String,
    val maxFreq: Int,
    val availableFreqs: List<Int>,
    val boostKey: String,
    val boostFreqKey: String,
    val minFreqKey: String,
    val maxFreqKey: String,
    val boostDefault: Boolean,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    if (showTopBar) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.performance),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        onBackClick?.let { onClick ->
                            IconButton(onClick = onClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            PerformanceContent(modifier = Modifier.padding(innerPadding))
        }
    } else {
        PerformanceContent(modifier = Modifier)
    }
}

@Composable
fun PerformanceContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val smallAvailableFreqs = remember {
        Settings.Secure.getString(contentResolver, "ax_cpu_small_freqs")?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }

    val bigAvailableFreqs = remember {
        Settings.Secure.getString(contentResolver, "ax_cpu_big_freqs")?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }

    val primeAvailableFreqs = remember {
        Settings.Secure.getString(contentResolver, "ax_cpu_prime_freqs")?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }
    
    val gpuFreqsPath = remember { SystemProperties.get("persist.sys.axion_gpu_freqs_path", "") }
    val gpuMinFreqFile = remember { SystemProperties.get("persist.sys.axion_gpu_minfreq_file", "") }
    val gpuMaxLevels = remember { SystemProperties.getInt("persist.sys.axion_gpu_levels", 0) }
    val showGpu = gpuFreqsPath.isNotEmpty() && gpuMinFreqFile.isNotEmpty() && gpuMaxLevels > 0
    
    val littleClusterName = stringResource(R.string.little_cluster)
    val bigClusterName = stringResource(R.string.big_cluster)
    val primeClusterName = stringResource(R.string.prime_cluster)
    
    val clusters = remember(littleClusterName, bigClusterName, primeClusterName, smallAvailableFreqs, bigAvailableFreqs, primeAvailableFreqs) {
        listOf(
            ClusterConfig(
                name = littleClusterName,
                id = "little",
                maxFreq = smallAvailableFreqs.maxOrNull() ?: 0,
                availableFreqs = smallAvailableFreqs,
                boostKey = "axion_cpu_boost",
                boostFreqKey = "axion_min_freq_boost",
                minFreqKey = "axion_min_freq",
                maxFreqKey = "axion_max_freq",
                boostDefault = true,
                accentColor = Color(0xFF667eea)
            ),
            ClusterConfig(
                name = bigClusterName,
                id = "big",
                maxFreq = bigAvailableFreqs.maxOrNull() ?: 0,
                availableFreqs = bigAvailableFreqs,
                boostKey = "axion_big_core_boost",
                boostFreqKey = "axion_min_freq_big_boost",
                minFreqKey = "axion_min_freq_big",
                maxFreqKey = "axion_max_freq_big",
                boostDefault = false,
                accentColor = Color(0xFFf5576c)
            ),
            ClusterConfig(
                name = primeClusterName,
                id = "prime",
                maxFreq = primeAvailableFreqs.maxOrNull() ?: 0,
                availableFreqs = primeAvailableFreqs,
                boostKey = "axion_prime_core_boost",
                boostFreqKey = "axion_min_freq_prime_boost",
                minFreqKey = "axion_min_freq_prime",
                maxFreqKey = "axion_max_freq_prime",
                boostDefault = false,
                accentColor = Color(0xFF4facfe)
            )
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        PowerModeToggle()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.boost_controls),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BoostToggleCard(
                settingKey = "axion_sf_boost",
                title = stringResource(R.string.display_boost),
                icon = Icons.Default.DisplaySettings,
                gradientColors = listOf(Color(0xFF11998e), Color(0xFF38ef7d)),
                defaultValue = true,
                modifier = Modifier.weight(1f)
            )
            BoostToggleCard(
                settingKey = "axion_touch_boost",
                title = stringResource(R.string.touch_boost),
                icon = Icons.Default.TouchApp,
                gradientColors = listOf(Color(0xFFee0979), Color(0xFFff6a00)),
                defaultValue = false,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.frequency_control),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
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
        
        Spacer(modifier = Modifier.height(BottomNavPadding))
    }
}

@Composable
private fun ClusterCard(cluster: ClusterConfig) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    var boostEnabled by remember {
        mutableStateOf(
            try {
                Settings.Secure.getInt(
                    contentResolver, 
                    cluster.boostKey, 
                    if (cluster.boostDefault) 1 else 0
                ) == 1
            } catch (e: Exception) {
                cluster.boostDefault
            }
        )
    }
    
    DisposableEffect(cluster.boostKey) {
        val observer = object : android.database.ContentObserver(
            android.os.Handler(android.os.Looper.getMainLooper())
        ) {
            override fun onChange(selfChange: Boolean) {
                boostEnabled = try {
                    Settings.Secure.getInt(
                        contentResolver, 
                        cluster.boostKey, 
                        if (cluster.boostDefault) 1 else 0
                    ) == 1
                } catch (e: Exception) {
                    cluster.boostDefault
                }
            }
        }
        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(cluster.boostKey),
            false,
            observer
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }
    
    val icon = when (cluster.id) {
        "little" -> Icons.Outlined.Bolt
        "big" -> Icons.Outlined.RocketLaunch
        else -> Icons.Outlined.Layers
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.large)
            .background(MaterialTheme.colorScheme.surfaceBright)
    ) {
        ClusterHeader(
            name = cluster.name,
            icon = icon,
            accentColor = cluster.accentColor,
            boostEnabled = boostEnabled,
            onBoostToggle = { newValue ->
                boostEnabled = newValue
                Settings.Secure.putInt(contentResolver, cluster.boostKey, if (newValue) 1 else 0)
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = boostEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FrequencySlider(
                    settingKey = cluster.boostFreqKey,
                    label = stringResource(R.string.boost_frequency),
                    availableFrequencies = cluster.availableFreqs.takeIf { it.isNotEmpty() },
                    min = 0,
                    max = cluster.maxFreq,
                    interval = 100000,
                    defaultValue = 1000000,
                    accentColor = cluster.accentColor,
                    enabled = boostEnabled
                )
            }
            
            FrequencySlider(
                settingKey = cluster.minFreqKey,
                label = stringResource(R.string.minimum_frequency),
                availableFrequencies = cluster.availableFreqs.takeIf { it.isNotEmpty() },
                min = 0,
                max = cluster.maxFreq,
                interval = 100000,
                defaultValue = 0,
                accentColor = cluster.accentColor
            )
            
            FrequencySlider(
                settingKey = cluster.maxFreqKey,
                label = stringResource(R.string.maximum_frequency),
                availableFrequencies = cluster.availableFreqs.takeIf { it.isNotEmpty() },
                min = 0,
                max = cluster.maxFreq,
                interval = 100000,
                defaultValue = cluster.maxFreq,
                accentColor = cluster.accentColor
            )
        }
    }
}

@Composable
private fun ClusterHeader(
    name: String,
    icon: ImageVector,
    accentColor: Color,
    boostEnabled: Boolean,
    onBoostToggle: (Boolean) -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = if (boostEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
        animationSpec = tween(300),
        label = "statusColor"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.08f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ExpressiveShapes.small)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
                Text(
                    text = if (boostEnabled) stringResource(R.string.boost_active) else stringResource(R.string.boost_disabled),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = boostEnabled,
            onCheckedChange = onBoostToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun GpuCard(maxLevels: Int) {
    val gpuColor = Color(0xFFfc4a1a)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.large)
            .background(MaterialTheme.colorScheme.surfaceBright)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gpuColor.copy(alpha = 0.08f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ExpressiveShapes.small)
                    .background(gpuColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = gpuColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.gpu),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.graphics_performance),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LevelSlider(
                settingKey = "axion_game_gpu_boost_level",
                label = stringResource(R.string.game_gpu_boost),
                min = 0,
                max = maxLevels,
                defaultValue = 1,
                accentColor = gpuColor
            )
            
            LevelSlider(
                settingKey = "axion_sys_gpu_boost_level",
                label = stringResource(R.string.system_gpu_boost),
                min = 0,
                max = maxLevels,
                defaultValue = 1,
                accentColor = Color(0xFFf7b733)
            )
        }
    }
}
