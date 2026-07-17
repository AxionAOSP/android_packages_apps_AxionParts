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

import android.os.SystemProperties
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.BasePreference
import com.android.axion.compose.preferences.ListPreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.scaffold.AxionScaffold
import kotlin.math.roundToInt

private const val SIZE_PROPERTY = "persist.sys.ax_rp_size_mb"
private const val ACTIVE_SIZE_PROPERTY = "persist.sys.ax_rp_active_kb"
private const val STATUS_PROPERTY = "persist.sys.ax_rp_status"
private val RAM_PLUS_SIZES_MIB = listOf(0, 2048, 4096, 6144, 8192)

@Composable
fun RamPlusScreen(onBackClick: (() -> Unit)? = null, showTopBar: Boolean = true) {
    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.ram_plus),
            onBackClick = { onBackClick?.invoke() },
        ) { innerPadding ->
            RamPlusContent(modifier = Modifier.padding(innerPadding))
        }
    } else {
        RamPlusContent()
    }
}

@Composable
private fun RamPlusContent(modifier: Modifier = Modifier) {
    val initialSize = remember {
        SystemProperties.getInt(SIZE_PROPERTY, 0).takeIf(RAM_PLUS_SIZES_MIB::contains) ?: 0
    }
    var selectedSize by remember { mutableIntStateOf(initialSize) }
    var showRestartDialog by remember { mutableStateOf(false) }
    val activeSize = remember {
        (SystemProperties.getLong(ACTIVE_SIZE_PROPERTY, 0) / 1024.0).roundToInt()
    }
    val status = remember { SystemProperties.get(STATUS_PROPERTY, "disabled") }
    val statusSummary = ramPlusStatusSummary(status, selectedSize, activeSize)
    val options =
        RAM_PLUS_SIZES_MIB.map { size ->
            size.toString() to
                if (size == 0) {
                    stringResource(R.string.ram_plus_off)
                } else {
                    stringResource(R.string.ram_plus_size_gb, size / 1024)
                }
        }

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.ram_plus_configuration)) {
            item {
                ListPreference(
                    title = stringResource(R.string.ram_plus_size),
                    summary = stringResource(R.string.ram_plus_summary),
                    options = options,
                    value = selectedSize.toString(),
                    onValueChange = { value ->
                        val size = value.toInt()
                        if (size != selectedSize) {
                            SystemProperties.set(SIZE_PROPERTY, value)
                            selectedSize = size
                            showRestartDialog = true
                        }
                    },
                )
            }
            item {
                BasePreference(
                    title = stringResource(R.string.ram_plus_status),
                    summary = statusSummary,
                )
            }
        }

        Text(
            text = stringResource(R.string.ram_plus_warning),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
        )

        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(R.string.ram_plus_restart_title)) },
            text = { Text(stringResource(R.string.ram_plus_restart_message)) },
            confirmButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(stringResource(R.string.ram_plus_restart_confirm))
                }
            },
        )
    }
}

@Composable
private fun ramPlusStatusSummary(status: String, selectedSize: Int, activeSize: Int): String {
    if ((status == "active" || status == "disabled") && selectedSize != activeSize) {
        return stringResource(R.string.ram_plus_restart_pending)
    }
    return when (status) {
        "active" -> stringResource(R.string.ram_plus_active_size, activeSize / 1024)
        "disabled" -> stringResource(R.string.disabled)
        "unsupported" -> stringResource(R.string.ram_plus_unsupported)
        "encryption_required" -> stringResource(R.string.ram_plus_encryption_required)
        "zram_not_ready" -> stringResource(R.string.ram_plus_zram_not_ready)
        "insufficient_space" -> stringResource(R.string.ram_plus_insufficient_space)
        "priority_error" -> stringResource(R.string.ram_plus_priority_error)
        else -> stringResource(R.string.ram_plus_setup_failed)
    }
}
