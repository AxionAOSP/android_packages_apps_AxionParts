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
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.ViewSidebar
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
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.scaffold.AxionScaffold

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MultitaskingScreen(onBackClick: (() -> Unit)? = null) {
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }

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
        label = "multitaskingSubScreen",
    ) { subScreen ->
        when (subScreen) {
            null -> {
                AxionScaffold(
                    title = stringResource(R.string.multitasking),
                    onBackClick = { onBackClick?.invoke() },
                ) { innerPadding ->
                    MultitaskingContent(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToPcMode = { currentSubScreen = "pcmode" },
                    )
                }
            }
            "pcmode" -> {
                BackHandler { currentSubScreen = null }
                PcModeScreen(onBackClick = { currentSubScreen = null })
            }
        }
    }
}

@Composable
private fun MultitaskingContent(
    modifier: Modifier = Modifier,
    onNavigateToPcMode: () -> Unit = {},
) {
    val context = LocalContext.current

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.multitasking)) {
            item {
                ClickablePreference(
                    title = stringResource(R.string.sidebar),
                    summary = stringResource(R.string.sidebar_summary),
                    icon = Icons.Default.ViewSidebar,
                    showExternalIcon = true,
                    onClick = {
                        val intent =
                            Intent().apply {
                                component =
                                    ComponentName(
                                        "com.android.edge.bar",
                                        "com.android.edge.bar.settings.SettingsActivity",
                                    )
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        context.startActivity(intent)
                    },
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.pc_mode_settings),
                    summary = stringResource(R.string.pc_mode_summary),
                    icon = Icons.Filled.Splitscreen,
                    onClick = onNavigateToPcMode,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
