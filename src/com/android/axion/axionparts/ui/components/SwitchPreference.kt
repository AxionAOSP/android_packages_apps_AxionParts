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

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class PreferencePosition {
    Single,
    Top,
    Middle,
    Bottom
}

val LocalPreferencePosition = compositionLocalOf { PreferencePosition.Single }

fun preferenceShape(position: PreferencePosition): Shape {
    return when (position) {
        PreferencePosition.Single -> RoundedCornerShape(28.dp)
        PreferencePosition.Top -> RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )
        PreferencePosition.Middle -> RoundedCornerShape(4.dp)
        PreferencePosition.Bottom -> RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 28.dp,
            bottomEnd = 28.dp
        )
    }
}

@Composable
fun SwitchPreference(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    position: PreferencePosition = LocalPreferencePosition.current
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = preferenceShape(position)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            thumbContent = {
                Icon(
                    imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            },
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SecureSettingSwitch(
    settingKey: String,
    title: String,
    summary: String,
    defaultValue: Boolean = false,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    position: PreferencePosition = LocalPreferencePosition.current
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    var isChecked by remember {
        mutableStateOf(
            try {
                Settings.Secure.getInt(contentResolver, settingKey, if (defaultValue) 1 else 0) == 1
            } catch (e: Exception) {
                defaultValue
            }
        )
    }
    
    DisposableEffect(settingKey) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isChecked = try {
                    Settings.Secure.getInt(contentResolver, settingKey, if (defaultValue) 1 else 0) == 1
                } catch (e: Exception) {
                    defaultValue
                }
            }
        }
        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(settingKey),
            false,
            observer
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }
    
    SwitchPreference(
        title = title,
        summary = summary,
        checked = isChecked,
        onCheckedChange = { newValue ->
            isChecked = newValue
            Settings.Secure.putInt(contentResolver, settingKey, if (newValue) 1 else 0)
        },
        modifier = modifier,
        enabled = enabled,
        position = position
    )
}

@Composable
fun SystemSettingSwitch(
    settingKey: String,
    title: String,
    summary: String,
    defaultValue: Boolean = false,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    position: PreferencePosition = LocalPreferencePosition.current
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    var isChecked by remember {
        mutableStateOf(
            try {
                Settings.System.getInt(contentResolver, settingKey, if (defaultValue) 1 else 0) == 1
            } catch (e: Exception) {
                defaultValue
            }
        )
    }
    
    DisposableEffect(settingKey) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isChecked = try {
                    Settings.System.getInt(contentResolver, settingKey, if (defaultValue) 1 else 0) == 1
                } catch (e: Exception) {
                    defaultValue
                }
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(settingKey),
            false,
            observer
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }
    
    SwitchPreference(
        title = title,
        summary = summary,
        checked = isChecked,
        onCheckedChange = { newValue ->
            isChecked = newValue
            Settings.System.putInt(contentResolver, settingKey, if (newValue) 1 else 0)
        },
        modifier = modifier,
        enabled = enabled,
        position = position
    )
}
