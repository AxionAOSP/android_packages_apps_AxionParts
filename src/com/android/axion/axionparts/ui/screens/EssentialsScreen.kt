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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ScreenshotMonitor
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.EssentialAppsPreference
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SecureSettingSwitch
import com.android.axion.compose.scaffold.AxionScaffold

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EssentialsScreen(
    onBackClick: (() -> Unit)? = null,
    onNavigateToAppPicker: (selectedApps: Set<String>) -> Unit = {},
) {
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }

    if (currentSubScreen != null) {
        BackHandler { currentSubScreen = null }
    }

    val motionScheme = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = currentSubScreen,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally(motionScheme.defaultSpatialSpec()) { it } + fadeIn(motionScheme.defaultEffectsSpec())).togetherWith(
                    slideOutHorizontally(motionScheme.defaultSpatialSpec()) { -it / 3 } + fadeOut(motionScheme.defaultEffectsSpec())
                )
            } else {
                (slideInHorizontally(motionScheme.defaultSpatialSpec()) { -it / 3 } + fadeIn(motionScheme.defaultEffectsSpec())).togetherWith(
                    slideOutHorizontally(motionScheme.defaultSpatialSpec()) { it } + fadeOut(motionScheme.defaultEffectsSpec())
                )
            }
        },
        label = "essentialsSubScreen",
    ) { subScreen ->
        when (subScreen) {
            null -> {
                AxionScaffold(
                    title = stringResource(R.string.essentials),
                    onBackClick = { onBackClick?.invoke() },
                ) { innerPadding ->
                    EssentialsContent(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToAppPicker = onNavigateToAppPicker,
                        onNavigateToTrickyStore = { currentSubScreen = "trickystore" },
                        onNavigateToPlayIntegrityFix = { currentSubScreen = "playintegrityfix" },
                        onNavigateToGameSpoofing = { currentSubScreen = "gamespoofing" },
                        onNavigateToAdBlock = { currentSubScreen = "system_adblock" },
                    )
                }
            }
            "trickystore" -> {
                TrickyStoreScreen(onBackClick = { currentSubScreen = null })
            }
            "playintegrityfix" -> {
                PlayIntegrityFixScreen(onBackClick = { currentSubScreen = null })
            }
            "gamespoofing" -> {
                GameSpoofingScreen(onBackClick = { currentSubScreen = null })
            }
            "system_adblock" -> {
                AdBlockScreen(onBackClick = { currentSubScreen = null })
            }
        }
    }
}

@Composable
private fun EssentialsContent(
    modifier: Modifier = Modifier,
    onNavigateToAppPicker: (selectedApps: Set<String>) -> Unit = {},
    onNavigateToTrickyStore: () -> Unit = {},
    onNavigateToPlayIntegrityFix: () -> Unit = {},
    onNavigateToGameSpoofing: () -> Unit = {},
    onNavigateToAdBlock: () -> Unit = {},
) {
    val context = LocalContext.current

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.essential_notifications)) {
            item { EssentialAppsPreference(onNavigateToAppPicker = onNavigateToAppPicker) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroup(title = stringResource(R.string.quicklook)) {
            item {
                ClickablePreference(
                    title = stringResource(R.string.quicklook),
                    summary = stringResource(R.string.quicklook_settings_summary),
                    showExternalIcon = true,
                    onClick = {
                        val intent = Intent("com.android.axion.quicklook.SETTINGS").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        context.startActivity(intent)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroup(title = stringResource(R.string.gaming)) {
            item {
                ClickablePreference(
                    title = stringResource(R.string.gamespace),
                    summary = stringResource(R.string.gamespace_summary),
                    icon = Icons.Default.SportsEsports,
                    showExternalIcon = true,
                    onClick = {
                        val intent =
                            Intent().apply {
                                component =
                                    ComponentName(
                                        "io.chaldeaprjkt.gamespace",
                                        "io.chaldeaprjkt.gamespace.settings.SettingsActivity",
                                    )
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                        context.startActivity(intent)
                    },
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.game_spoofing),
                    summary = stringResource(R.string.game_spoofing_summary),
                    icon = Icons.Default.Gamepad,
                    onClick = onNavigateToGameSpoofing,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroup(title = stringResource(R.string.security)) {
            item {
                ClickablePreference(
                    title = stringResource(R.string.trickystore),
                    summary = stringResource(R.string.trickystore_summary),
                    icon = Icons.Default.Key,
                    onClick = onNavigateToTrickyStore,
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.play_integrity_fix),
                    summary = stringResource(R.string.play_integrity_fix_summary),
                    icon = Icons.Default.Fingerprint,
                    onClick = onNavigateToPlayIntegrityFix,
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = "window_ignore_secure",
                    title = stringResource(R.string.ignore_window_secure),
                    summary = stringResource(R.string.ignore_window_secure_summary),
                    icon = Icons.Default.ScreenshotMonitor,
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = "no_storage_restrict",
                    title = stringResource(R.string.no_storage_restrict_title),
                    summary = stringResource(R.string.no_storage_restrict_summary),
                    icon = Icons.Default.Folder,
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.system_adblock_title),
                    summary = stringResource(R.string.system_adblock_summary),
                    icon = Icons.Default.Shield,
                    onClick = onNavigateToAdBlock,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
