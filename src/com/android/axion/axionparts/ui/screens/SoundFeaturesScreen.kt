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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MultipleStop
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.SwitchFeatureCard
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.rememberSettingBoolean
import com.android.axion.compose.preferences.rememberSettingsFlow
import com.android.axion.compose.scaffold.AxionScaffold

@Composable
fun SoundFeaturesScreen(onBackClick: () -> Unit) {
    AxionScaffold(title = stringResource(R.string.sound), onBackClick = onBackClick) { innerPadding
        ->
        val flow = rememberSettingsFlow(SettingsType.SYSTEM)
        val appVolumeEnabled by rememberSettingBoolean("show_app_volume", SettingsType.SYSTEM)
        val multiAudioEnabled by rememberSettingBoolean("multi_audio_focus_enabled", SettingsType.SYSTEM)

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SwitchFeatureCard(
                    title = stringResource(R.string.per_app_volume),
                    subtitle = stringResource(R.string.per_app_volume_summary),
                    icon = Icons.Filled.GraphicEq,
                    checked = appVolumeEnabled,
                    onCheckedChange = { enabled ->
                        flow.putInt("show_app_volume", if (enabled) 1 else 0)
                    },
                    illustrationColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                SwitchFeatureCard(
                    title = stringResource(R.string.multi_audio_focus),
                    subtitle = stringResource(R.string.multi_audio_focus_summary),
                    icon = Icons.Filled.MultipleStop,
                    checked = multiAudioEnabled,
                    onCheckedChange = { enabled ->
                        flow.putInt("multi_audio_focus_enabled", if (enabled) 1 else 0)
                    },
                    illustrationColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
