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

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.EssentialAppsPreference
import com.android.axion.axionparts.ui.theme.BottomNavPadding
import com.android.axion.compose.preferences.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssentialsScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true,
    onNavigateToAppPicker: (selectedApps: Set<String>) -> Unit = {},
    onNavigateToTrickyStore: () -> Unit = {},
    onNavigateToPlayIntegrityFix: () -> Unit = {},
    onNavigateToGameSpoofing: () -> Unit = {}
) {
    if (showTopBar) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.essentials),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            EssentialsContent(
                modifier = Modifier.padding(innerPadding),
                onNavigateToAppPicker = onNavigateToAppPicker,
                onNavigateToTrickyStore = onNavigateToTrickyStore,
                onNavigateToPlayIntegrityFix = onNavigateToPlayIntegrityFix,
                onNavigateToGameSpoofing = onNavigateToGameSpoofing
            )
        }
    } else {
        EssentialsContent(
            modifier = Modifier,
            onNavigateToAppPicker = onNavigateToAppPicker,
            onNavigateToTrickyStore = onNavigateToTrickyStore,
            onNavigateToPlayIntegrityFix = onNavigateToPlayIntegrityFix,
            onNavigateToGameSpoofing = onNavigateToGameSpoofing
        )
    }
}

@Composable
fun EssentialsContent(
    modifier: Modifier = Modifier,
    onNavigateToAppPicker: (selectedApps: Set<String>) -> Unit = {},
    onNavigateToTrickyStore: () -> Unit = {},
    onNavigateToPlayIntegrityFix: () -> Unit = {},
    onNavigateToGameSpoofing: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        SettingsSection(
            title = stringResource(R.string.essential_notifications),
            icon = Icons.Default.Notifications
        ) {
            EssentialAppsPreference(
                onNavigateToAppPicker = onNavigateToAppPicker
            )
        }
        
        SettingsSection(
            title = stringResource(R.string.gaming),
            icon = Icons.Default.SportsEsports
        ) {
            ClickablePreference(
                title = stringResource(R.string.gamespace),
                summary = stringResource(R.string.gamespace_summary),
                icon = Icons.Default.SportsEsports,
                position = PreferencePosition.Top,
                showExternalIcon = true,
                onClick = {
                    val intent = Intent().apply {
                        component = ComponentName(
                            "io.chaldeaprjkt.gamespace",
                            "io.chaldeaprjkt.gamespace.settings.SettingsActivity"
                        )
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(intent)
                }
            )
            
            ClickablePreference(
                title = stringResource(R.string.game_spoofing),
                summary = stringResource(R.string.game_spoofing_summary),
                icon = Icons.Default.Gamepad,
                position = PreferencePosition.Bottom,
                onClick = onNavigateToGameSpoofing
            )
        }
        
        SettingsSection(
            title = stringResource(R.string.security),
            icon = Icons.Default.Key
        ) {
            ClickablePreference(
                title = stringResource(R.string.trickystore),
                summary = stringResource(R.string.trickystore_summary),
                icon = Icons.Default.Key,
                position = PreferencePosition.Top,
                onClick = onNavigateToTrickyStore
            )
            
            ClickablePreference(
                title = stringResource(R.string.play_integrity_fix),
                summary = stringResource(R.string.play_integrity_fix_summary),
                icon = Icons.Default.Fingerprint,
                position = PreferencePosition.Middle,
                onClick = onNavigateToPlayIntegrityFix
            )
            
            SecureSettingSwitch(
                settingKey = "window_ignore_secure",
                title = stringResource(R.string.ignore_window_secure),
                summary = stringResource(R.string.ignore_window_secure_summary),
                icon = Icons.Default.ScreenshotMonitor,
                position = PreferencePosition.Bottom
            )
        }
        
        Spacer(modifier = Modifier.height(BottomNavPadding))
    }
}
