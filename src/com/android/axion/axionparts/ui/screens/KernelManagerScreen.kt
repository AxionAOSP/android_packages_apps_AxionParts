/*
 * Copyright 2025-2026 AxionOS
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

import android.os.AxKernelControl
import android.os.AxKernelManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.FrequencySlider
import com.android.axion.compose.preferences.ListPreference
import com.android.axion.compose.preferences.LocalPreferencePosition
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.preferenceShape
import com.android.axion.compose.preferences.rememberSettingsFlow
import com.android.axion.compose.scaffold.AxionScaffold

private const val CPU_CLUSTER_COUNT_KEY = "ax_cpu_cluster_count"
private const val CPU_CLUSTER_FREQS_PREFIX = "ax_cpu_cluster_"
private const val CPU_CLUSTER_FREQS_SUFFIX = "_freqs"
private const val MIN_FREQ = "axion_min_freq"
private const val MIN_FREQ_BIG = "axion_min_freq_big"
private const val MIN_FREQ_PRIME = "axion_min_freq_prime"
private const val MAX_FREQ = "axion_max_freq"
private const val MAX_FREQ_BIG = "axion_max_freq_big"
private const val MAX_FREQ_PRIME = "axion_max_freq_prime"
private const val MIN_FREQ_CLUSTER_PREFIX = "axion_min_freq_cluster_"
private const val MAX_FREQ_CLUSTER_PREFIX = "axion_max_freq_cluster_"

private enum class ClusterRole {
    LITTLE,
    BIG,
    MIDDLE,
    PRIME,
}

private data class ClusterConfig(
    val name: String,
    val maxFreq: Int,
    val availableFreqs: List<Int>,
    val minFreqKey: String,
    val maxFreqKey: String,
    val minControl: AxKernelControl? = null,
    val maxControl: AxKernelControl? = null,
    val governorControl: AxKernelControl? = null,
)

@Composable
fun KernelManagerScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true,
) {
    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.kernel_manager),
            onBackClick = { onBackClick?.invoke() },
        ) { innerPadding ->
            KernelManagerContent(modifier = Modifier.padding(innerPadding))
        }
    } else {
        KernelManagerContent(modifier = Modifier)
    }
}

@Composable
private fun KernelManagerContent(modifier: Modifier = Modifier) {
    val flow = rememberSettingsFlow(SettingsType.SECURE)
    val kernelManager = remember { AxKernelManager() }
    var kernelControls by remember { mutableStateOf<List<AxKernelControl>>(emptyList()) }
    val refreshKernelControls = {
        kernelControls = kernelManager.getControls()
    }

    LaunchedEffect(kernelManager) {
        refreshKernelControls()
    }

    val clusterCount = remember(flow) { flow.getInt(CPU_CLUSTER_COUNT_KEY, 0) }
    val legacyAvailableFreqs =
        remember(flow) {
            listOf(
                flow.getString("ax_cpu_small_freqs").toFrequencyList(),
                flow.getString("ax_cpu_big_freqs").toFrequencyList(),
                flow.getString("ax_cpu_prime_freqs").toFrequencyList(),
            )
        }
    val dynamicAvailableFreqs =
        remember(flow, clusterCount) {
            (0 until clusterCount).map { index ->
                flow.getString(clusterFreqsKey(index)).toFrequencyList()
            }
        }
    val clusterAvailableFreqs =
        if (clusterCount > 0) dynamicAvailableFreqs else legacyAvailableFreqs
    val resolvedClusterCount = if (clusterCount > 0) clusterCount else legacyAvailableFreqs.size

    val littleClusterName = stringResource(R.string.little_cluster)
    val bigClusterName = stringResource(R.string.big_cluster)
    val primeClusterName = stringResource(R.string.prime_cluster)

    val settingsClusters =
        (0 until resolvedClusterCount).map { index ->
            val role = clusterRole(index, resolvedClusterCount)
            val availableFreqs = clusterAvailableFreqs.getOrNull(index).orEmpty()
            val coresStr = flow.getString(clusterCoresKey(index))
            val coreLabel = if (coresStr.isNotEmpty()) formatCoresLabel(coresStr) else ""
            ClusterConfig(
                name =
                    if (coreLabel.isNotEmpty()) coreLabel
                    else when (role) {
                        ClusterRole.LITTLE -> littleClusterName
                        ClusterRole.BIG -> bigClusterName
                        ClusterRole.PRIME -> primeClusterName
                        ClusterRole.MIDDLE -> stringResource(R.string.cpu_cluster, index + 1)
                    },
                maxFreq = availableFreqs.maxOrNull() ?: 0,
                availableFreqs = availableFreqs,
                minFreqKey = minFreqKey(index, resolvedClusterCount),
                maxFreqKey = maxFreqKey(index, resolvedClusterCount),
            )
        }
    val kernelClusters = kernelControls.toCpuClusters()
    val clusters = kernelClusters.takeIf { it.isNotEmpty() } ?: settingsClusters
    val gpuMinControl =
        kernelControls.firstOrNull { it.type == AxKernelControl.TYPE_GPU_MIN_FREQ }
    val gpuMaxControl =
        kernelControls.firstOrNull { it.type == AxKernelControl.TYPE_GPU_MAX_FREQ }

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        clusters.forEach { cluster ->
            if (cluster.maxFreq > 0) {
                ClusterGroup(
                    cluster = cluster,
                    kernelManager = kernelManager,
                    onKernelControlsChanged = refreshKernelControls,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (gpuMinControl != null && gpuMaxControl != null) {
            GpuGroup(
                minControl = gpuMinControl,
                maxControl = gpuMaxControl,
                kernelManager = kernelManager,
                onKernelControlsChanged = refreshKernelControls,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }
}

@Composable
private fun ClusterGroup(
    cluster: ClusterConfig,
    kernelManager: AxKernelManager,
    onKernelControlsChanged: () -> Unit,
) {
    PreferenceGroup(title = cluster.name) {
        item {
            KernelFrequencyPreference(
                settingKey = cluster.minFreqKey,
                label = stringResource(R.string.minimum_frequency),
                availableFreqs = cluster.availableFreqs,
                maxFreq = cluster.maxFreq,
                defaultValue = cluster.availableFreqs.minOrNull() ?: 0,
                value = cluster.minControl?.currentValue,
                onCommit =
                    cluster.minControl?.let { control ->
                        { value: Int ->
                            setKernelControl(kernelManager, control, value, onKernelControlsChanged)
                        }
                    },
            )
        }
        item {
            KernelFrequencyPreference(
                settingKey = cluster.maxFreqKey,
                label = stringResource(R.string.maximum_frequency),
                availableFreqs = cluster.availableFreqs,
                maxFreq = cluster.maxFreq,
                defaultValue = cluster.maxFreq,
                value = cluster.maxControl?.currentValue,
                onCommit =
                    cluster.maxControl?.let { control ->
                        { value: Int ->
                            setKernelControl(kernelManager, control, value, onKernelControlsChanged)
                        }
                    },
            )
        }
        val governorControl = cluster.governorControl
        if (governorControl != null) {
            item {
                GovernorPreference(
                    control = governorControl,
                    kernelManager = kernelManager,
                    onKernelControlsChanged = onKernelControlsChanged,
                )
            }
        }
    }
}

@Composable
private fun KernelFrequencyPreference(
    settingKey: String,
    label: String,
    availableFreqs: List<Int>,
    maxFreq: Int,
    defaultValue: Int,
    value: Int?,
    onCommit: ((Int) -> Unit)?,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .clip(preferenceShape(LocalPreferencePosition.current))
                .background(MaterialTheme.colorScheme.surfaceBright)
                .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        FrequencySlider(
            settingKey = settingKey,
            label = label,
            availableFrequencies = availableFreqs.takeIf { it.isNotEmpty() },
            min = 0,
            max = maxFreq,
            interval = 100000,
            defaultValue = defaultValue,
            value = value,
            onValueCommitted = onCommit,
        )
    }
}

private fun String.toFrequencyList(): List<Int> =
    split(",").mapNotNull { it.trim().toIntOrNull() }

private fun clusterFreqsKey(index: Int): String =
    "$CPU_CLUSTER_FREQS_PREFIX$index$CPU_CLUSTER_FREQS_SUFFIX"

private fun clusterCoresKey(index: Int): String = when (index) {
    0 -> "ax_cpu_small_cores"
    1 -> "ax_cpu_big_cores"
    2 -> "ax_cpu_prime_cores"
    else -> "ax_cpu_cluster_${index}_cores"
}

private fun formatCoresLabel(cores: String): String {
    val parts = cores.split(",").mapNotNull { it.trim().toIntOrNull() }.sorted()
    if (parts.isEmpty()) return ""
    if (parts.size == 1) return "cpu ${parts[0]}"
    val consecutive = (1 until parts.size).all { parts[it] == parts[it - 1] + 1 }
    return if (consecutive) {
        "cpu ${parts.first()}-${parts.last()}"
    } else {
        "cpu ${parts.joinToString(",")}"
    }
}

private fun clusterRole(index: Int, clusterCount: Int): ClusterRole =
    when {
        index == 0 -> ClusterRole.LITTLE
        isPrimeCluster(index, clusterCount) -> ClusterRole.PRIME
        index == 1 -> ClusterRole.BIG
        else -> ClusterRole.MIDDLE
    }

private fun minFreqKey(index: Int, clusterCount: Int): String =
    when {
        index == 0 -> MIN_FREQ
        isPrimeCluster(index, clusterCount) -> MIN_FREQ_PRIME
        index == 1 -> MIN_FREQ_BIG
        else -> "$MIN_FREQ_CLUSTER_PREFIX$index"
    }

private fun maxFreqKey(index: Int, clusterCount: Int): String =
    when {
        index == 0 -> MAX_FREQ
        isPrimeCluster(index, clusterCount) -> MAX_FREQ_PRIME
        index == 1 -> MAX_FREQ_BIG
        else -> "$MAX_FREQ_CLUSTER_PREFIX$index"
    }

private fun isPrimeCluster(index: Int, clusterCount: Int): Boolean =
    clusterCount > 2 && index == clusterCount - 1

private fun List<AxKernelControl>.toCpuClusters(): List<ClusterConfig> {
    val groups =
        filter {
                it.type == AxKernelControl.TYPE_CPU_MIN_FREQ ||
                    it.type == AxKernelControl.TYPE_CPU_MAX_FREQ ||
                    it.type == AxKernelControl.TYPE_CPU_GOVERNOR
            }
            .groupBy { it.group }
            .toList()

    return groups.mapIndexedNotNull { index, entry ->
        val controls = entry.second
        val minControl = controls.firstOrNull { it.type == AxKernelControl.TYPE_CPU_MIN_FREQ }
        val maxControl = controls.firstOrNull { it.type == AxKernelControl.TYPE_CPU_MAX_FREQ }
        val governorControl = controls.firstOrNull { it.type == AxKernelControl.TYPE_CPU_GOVERNOR }
        val sourceControl = minControl ?: maxControl ?: return@mapIndexedNotNull null
        val availableFreqs = sourceControl.availableValues.toList()
        ClusterConfig(
            name = entry.first,
            maxFreq = availableFreqs.maxOrNull() ?: maxControl?.defaultValue ?: 0,
            availableFreqs = availableFreqs,
            minFreqKey = minControl?.id ?: minFreqKey(index, groups.size),
            maxFreqKey = maxControl?.id ?: maxFreqKey(index, groups.size),
            minControl = minControl,
            maxControl = maxControl,
            governorControl = governorControl,
        )
    }
}

private fun setKernelControl(
    kernelManager: AxKernelManager,
    control: AxKernelControl,
    value: Int,
    onKernelControlsChanged: () -> Unit,
) {
    if (kernelManager.setControlValue(control.id, value)) {
        onKernelControlsChanged()
    }
}

@Composable
private fun GpuGroup(
    minControl: AxKernelControl,
    maxControl: AxKernelControl,
    kernelManager: AxKernelManager,
    onKernelControlsChanged: () -> Unit,
) {
    val availableFreqs = minControl.availableValues.toList()
    val maxFreq = availableFreqs.maxOrNull() ?: maxControl.defaultValue

    PreferenceGroup(title = stringResource(R.string.gpu)) {
        item {
            KernelFrequencyPreference(
                settingKey = minControl.id,
                label = stringResource(R.string.minimum_frequency),
                availableFreqs = availableFreqs,
                maxFreq = maxFreq,
                defaultValue = minControl.defaultValue,
                value = minControl.currentValue,
                onCommit = { value ->
                    setKernelControl(kernelManager, minControl, value, onKernelControlsChanged)
                },
            )
        }
        item {
            KernelFrequencyPreference(
                settingKey = maxControl.id,
                label = stringResource(R.string.maximum_frequency),
                availableFreqs = availableFreqs,
                maxFreq = maxFreq,
                defaultValue = maxControl.defaultValue,
                value = maxControl.currentValue,
                onCommit = { value ->
                    setKernelControl(kernelManager, maxControl, value, onKernelControlsChanged)
                },
            )
        }
    }
}

@Composable
private fun GovernorPreference(
    control: AxKernelControl,
    kernelManager: AxKernelManager,
    onKernelControlsChanged: () -> Unit,
) {
    val values = control.availableValues.toList()
    val labels = control.valueLabels.toList()
    if (values.isEmpty() || labels.isEmpty()) {
        return
    }
    val currentLabel = labels.getOrNull(values.indexOf(control.currentValue)) ?: labels.first()
    ListPreference(
        title = stringResource(R.string.cpu_governor),
        summary = currentLabel,
        options =
            values.mapIndexed { index, value ->
                value.toString() to (labels.getOrNull(index) ?: value.toString())
            },
        value = control.currentValue.toString(),
        onValueChange = { value ->
            setKernelControl(kernelManager, control, value.toInt(), onKernelControlsChanged)
        },
    )
}
