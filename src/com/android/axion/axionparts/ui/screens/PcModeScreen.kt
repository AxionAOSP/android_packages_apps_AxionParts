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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SecureSettingSlider
import com.android.axion.compose.preferences.SecureSettingSwitch
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.rememberSettingString
import com.android.axion.compose.preferences.rememberSettingsFlow
import com.android.axion.compose.scaffold.AxionScaffold

@Composable
fun PcModeScreen(onBackClick: () -> Unit) {
    AxionScaffold(title = stringResource(R.string.pc_mode_title), onBackClick = onBackClick) {
        paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreferenceGroup(title = stringResource(R.string.pref_category_general)) {
                item {
                    SecureSettingSwitch(
                        settingKey = "ax_pc_mode",
                        title = stringResource(R.string.pref_enable_pc_mode_title),
                        summary = stringResource(R.string.pref_enable_pc_mode_summary),
                    )
                }
            }

            PreferenceGroup(title = stringResource(R.string.pref_category_display)) {
                item { ResolutionPreference() }
                item {
                    SecureSettingSwitch(
                        settingKey = "ax_pc_mode_display_off",
                        title = stringResource(R.string.pref_pc_mode_screen_off_title),
                        summary = stringResource(R.string.pref_pc_mode_screen_off_summary),
                    )
                }
            }

            PreferenceGroup(title = stringResource(R.string.pref_category_taskbar)) {
                item {
                    SecureSettingSlider(
                        settingKey = "ax_pc_mode_taskbar_timeout",
                        title = stringResource(R.string.pref_taskbar_timeout_title),
                        summary = stringResource(R.string.pref_taskbar_timeout_summary),
                        min = 1000,
                        max = 10000,
                        interval = 500,
                        defaultValue = 3000,
                        formatValue = { value: Int ->
                            if (value >= 1000) {
                                val seconds = value / 1000f
                                if (seconds == seconds.toInt().toFloat()) {
                                    "${seconds.toInt()}s"
                                } else {
                                    String.format("%.1fs", seconds)
                                }
                            } else {
                                "${value}ms"
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResolutionPreference() {
    val flow = rememberSettingsFlow(SettingsType.SECURE)
    var showDialog by remember { mutableStateOf(false) }
    val currentResolution by rememberSettingString("ax_pc_mode_resolution_override", SettingsType.SECURE)

    ClickablePreference(
        title = stringResource(R.string.resolution_override_title),
        summary =
            if (currentResolution.isNotEmpty()) currentResolution
            else stringResource(R.string.resolution_default),
        onClick = { showDialog = true },
    )

    if (showDialog) {
        ResolutionDialog(
            currentValue = currentResolution,
            onDismiss = { showDialog = false },
            onConfirm = { newValue ->
                flow.putString("ax_pc_mode_resolution_override", newValue)
                showDialog = false
            },
        )
    }
}

@Composable
private fun ResolutionDialog(
    currentValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(currentValue) }
    var error by remember { mutableStateOf<String?>(null) }
    val invalidFormatError = stringResource(R.string.resolution_invalid_format)
    val maxLimitError = stringResource(R.string.resolution_max_limit_error)
    val minLimitError = stringResource(R.string.resolution_min_limit_error)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.resolution_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.resolution_input_label)) },
                    isError = error != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isEmpty()) {
                        onConfirm("")
                        return@TextButton
                    }
                    val parts = text.split("x", "X")
                    if (parts.size == 2) {
                        val width = parts[0].trim().toIntOrNull()
                        val height = parts[1].trim().toIntOrNull()

                        if (width != null && height != null) {
                            if (width > 4096 || height > 4096) {
                                error = maxLimitError
                            } else if (width < 320 || height < 240) {
                                error = minLimitError
                            } else {
                                onConfirm("${width}x${height}")
                            }
                        } else {
                            error = invalidFormatError
                        }
                    } else {
                        error = invalidFormatError
                    }
                }
            ) {
                Text(stringResource(R.string.action_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.action_cancel)) }
            TextButton(onClick = { onConfirm("") }) {
                Text(stringResource(R.string.action_reset_default))
            }
        },
    )
}
