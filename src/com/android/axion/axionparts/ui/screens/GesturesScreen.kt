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

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.FeatureCard
import com.android.axion.compose.preferences.*
import com.android.axion.compose.scaffold.AxionScaffold

private enum class GesturesSubScreen {
    MAIN,
    SHAKE,
    THREE_FINGER,
}

@Composable
fun GesturesScreen(onBackClick: () -> Unit) {
    var currentScreen by rememberSaveable { mutableStateOf(GesturesSubScreen.MAIN) }

    val screenTitle =
        when (currentScreen) {
            GesturesSubScreen.MAIN -> stringResource(R.string.gestures)
            GesturesSubScreen.SHAKE -> stringResource(R.string.shake_gestures)
            GesturesSubScreen.THREE_FINGER -> stringResource(R.string.three_finger_gestures)
        }

    val handleBack: () -> Unit = {
        if (currentScreen == GesturesSubScreen.MAIN) {
            onBackClick()
        } else {
            currentScreen = GesturesSubScreen.MAIN
        }
    }

    BackHandler(onBack = handleBack)

    AxionScaffold(title = screenTitle, onBackClick = handleBack) { innerPadding ->
        when (currentScreen) {
            GesturesSubScreen.MAIN ->
                GesturesMainContent(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToShake = { currentScreen = GesturesSubScreen.SHAKE },
                    onNavigateToThreeFinger = { currentScreen = GesturesSubScreen.THREE_FINGER },
                )
            GesturesSubScreen.SHAKE ->
                ShakeGesturesContent(modifier = Modifier.padding(innerPadding))
            GesturesSubScreen.THREE_FINGER ->
                ThreeFingerGesturesContent(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun GesturesMainContent(
    modifier: Modifier = Modifier,
    onNavigateToShake: () -> Unit,
    onNavigateToThreeFinger: () -> Unit,
) {
    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureCard(
                title = stringResource(R.string.shake_gestures),
                subtitle = stringResource(R.string.shake_gestures_summary),
                icon = Icons.Filled.Vibration,
                onClick = onNavigateToShake,
                illustrationColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
            FeatureCard(
                title = stringResource(R.string.three_finger_gestures),
                subtitle = stringResource(R.string.three_finger_gestures_summary),
                icon = Icons.Filled.Swipe,
                onClick = onNavigateToThreeFinger,
                illustrationColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ShakeGesturesContent(modifier: Modifier = Modifier) {
    val gestureActions =
        listOf(
            "0" to stringResource(R.string.gesture_action_nothing),
            "2" to stringResource(R.string.gesture_action_app_switch),
            "3" to stringResource(R.string.gesture_action_search),
            "4" to stringResource(R.string.gesture_action_voice_search),
            "6" to stringResource(R.string.gesture_action_launch_camera),
            "7" to stringResource(R.string.gesture_action_sleep),
            "8" to stringResource(R.string.gesture_action_last_app),
            "10" to stringResource(R.string.gesture_action_close_app),
            "11" to stringResource(R.string.gesture_action_play_pause),
            "12" to stringResource(R.string.gesture_action_flashlight),
            "13" to stringResource(R.string.gesture_action_screenshot),
            "14" to stringResource(R.string.gesture_action_volume_panel),
            "15" to stringResource(R.string.gesture_action_clear_notifications),
            "16" to stringResource(R.string.gesture_action_notifications_panel),
            "17" to stringResource(R.string.gesture_action_expand_qs),
            "18" to stringResource(R.string.gesture_action_ringer_modes),
        )

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup {
            item {
                SecureSettingSwitch(
                    settingKey = "shake_gestures_enabled",
                    title = stringResource(R.string.enable_shake_gestures),
                    summary = stringResource(R.string.shake_gestures_summary),
                    icon = Icons.Filled.Vibration,
                    defaultValue = false,
                )
            }
            item {
                SystemSettingSlider(
                    settingKey = "shake_gestures_intensity",
                    title = stringResource(R.string.shake_gesture_intensity),
                    summary = stringResource(R.string.shake_intensity_summary),
                    min = 1,
                    max = 10,
                    unit = "",
                    defaultValue = 6,
                )
            }
            item {
                SecureListPreference(
                    key = "shake_gestures_action",
                    title = stringResource(R.string.shake_action),
                    summary = stringResource(R.string.shake_action_summary),
                    options = gestureActions,
                    defaultValue = "0",
                    dependencyKey = "shake_gestures_enabled",
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ThreeFingerGesturesContent(modifier: Modifier = Modifier) {
    val gestureActions =
        listOf(
            "0" to stringResource(R.string.gesture_action_nothing),
            "2" to stringResource(R.string.gesture_action_app_switch),
            "3" to stringResource(R.string.gesture_action_search),
            "4" to stringResource(R.string.gesture_action_voice_search),
            "6" to stringResource(R.string.gesture_action_launch_camera),
            "7" to stringResource(R.string.gesture_action_sleep),
            "8" to stringResource(R.string.gesture_action_last_app),
            "10" to stringResource(R.string.gesture_action_close_app),
            "11" to stringResource(R.string.gesture_action_play_pause),
            "12" to stringResource(R.string.gesture_action_flashlight),
            "13" to stringResource(R.string.gesture_action_screenshot),
            "14" to stringResource(R.string.gesture_action_volume_panel),
            "15" to stringResource(R.string.gesture_action_clear_notifications),
            "16" to stringResource(R.string.gesture_action_notifications_panel),
            "17" to stringResource(R.string.gesture_action_expand_qs),
            "18" to stringResource(R.string.gesture_action_ringer_modes),
        )

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup {
            item {
                SecureListPreference(
                    key = "nothing_three_finger_screenshot",
                    title = stringResource(R.string.three_finger_swipe),
                    summary = stringResource(R.string.three_finger_swipe_summary),
                    options = gestureActions,
                    defaultValue = "13",
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = "nothing_three_finger_long_press",
                    title = stringResource(R.string.three_finger_long_press),
                    summary = stringResource(R.string.three_finger_long_press_summary),
                    defaultValue = false,
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = "nt_disable_combination_screenshot",
                    title = stringResource(R.string.disable_button_screenshot),
                    summary = stringResource(R.string.disable_button_screenshot_summary),
                    defaultValue = false,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

