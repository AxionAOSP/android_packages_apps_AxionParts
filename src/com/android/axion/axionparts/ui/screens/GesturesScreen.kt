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

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.*
import com.android.axion.axionparts.ui.theme.MaxContentWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GesturesScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.gestures),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            GesturesContent(
                modifier = Modifier.widthIn(max = MaxContentWidth)
            )
        }
    }
}

@Composable
private fun GesturesContent(
    modifier: Modifier = Modifier
) {
    val gestureActions = listOf(
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
        "18" to stringResource(R.string.gesture_action_ringer_modes)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        GesturesIllustration()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PreferenceGroup(title = stringResource(R.string.shake_gestures)) {
            item {
                SecureSettingSwitch(
                    settingKey = "shake_gestures_enabled",
                    title = stringResource(R.string.enable_shake_gestures),
                    summary = stringResource(R.string.shake_gestures_summary),
                    defaultValue = false
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
                    defaultValue = 6
                )
            }
            item {
                SecureListPreference(
                    key = "shake_gestures_action",
                    title = stringResource(R.string.shake_action),
                    summary = stringResource(R.string.shake_action_summary),
                    options = gestureActions,
                    defaultValue = "0",
                    dependencyKey = "shake_gestures_enabled"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PreferenceGroup(title = stringResource(R.string.three_finger_gestures)) {
            item {
                SecureListPreference(
                    key = "nothing_three_finger_screenshot",
                    title = stringResource(R.string.three_finger_swipe),
                    summary = stringResource(R.string.three_finger_swipe_summary),
                    options = gestureActions,
                    defaultValue = "13"
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = "nothing_three_finger_long_press",
                    title = stringResource(R.string.three_finger_long_press),
                    summary = stringResource(R.string.three_finger_long_press_summary),
                    defaultValue = false
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = "nt_disable_combination_screenshot",
                    title = stringResource(R.string.disable_button_screenshot),
                    summary = stringResource(R.string.disable_button_screenshot_summary),
                    defaultValue = false
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GesturesIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "gestures")
    
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )
    
    val swipeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "swipe"
    )
    
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceBright),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            val phoneWidth = 50.dp.toPx()
            val phoneHeight = 85.dp.toPx()
            val phoneLeft = centerX - 60.dp.toPx() - phoneWidth / 2 + (shakeOffset * 4.dp.toPx())
            val phoneTop = centerY - phoneHeight / 2
            
            drawRoundRect(
                color = onSurface.copy(alpha = 0.15f),
                topLeft = Offset(phoneLeft, phoneTop),
                size = Size(phoneWidth, phoneHeight),
                cornerRadius = CornerRadius(10.dp.toPx())
            )
            
            drawRoundRect(
                color = primary.copy(alpha = 0.4f),
                topLeft = Offset(phoneLeft + 4.dp.toPx(), phoneTop + 6.dp.toPx()),
                size = Size(phoneWidth - 8.dp.toPx(), phoneHeight - 12.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            
            val arrowStartX = phoneLeft - 15.dp.toPx()
            val arrowEndX = phoneLeft - 5.dp.toPx()
            drawLine(
                color = primary.copy(alpha = 0.6f),
                start = Offset(arrowStartX, centerY - 10.dp.toPx()),
                end = Offset(arrowEndX, centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = primary.copy(alpha = 0.6f),
                start = Offset(arrowStartX, centerY + 10.dp.toPx()),
                end = Offset(arrowEndX, centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            val rightArrowStartX = phoneLeft + phoneWidth + 5.dp.toPx()
            val rightArrowEndX = phoneLeft + phoneWidth + 15.dp.toPx()
            drawLine(
                color = primary.copy(alpha = 0.6f),
                start = Offset(rightArrowEndX, centerY - 10.dp.toPx()),
                end = Offset(rightArrowStartX, centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = primary.copy(alpha = 0.6f),
                start = Offset(rightArrowEndX, centerY + 10.dp.toPx()),
                end = Offset(rightArrowStartX, centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            val handCenterX = centerX + 60.dp.toPx()
            val handCenterY = centerY
            
            val fingerSpacing = 10.dp.toPx()
            for (i in -1..1) {
                drawCircle(
                    color = primary.copy(alpha = 0.6f),
                    radius = 6.dp.toPx(),
                    center = Offset(handCenterX + i * fingerSpacing, handCenterY - 25.dp.toPx() + (swipeProgress * 50.dp.toPx()))
                )
            }
            
            val trailAlpha = (1f - swipeProgress) * 0.3f
            for (i in -1..1) {
                drawLine(
                    color = primary.copy(alpha = trailAlpha),
                    start = Offset(handCenterX + i * fingerSpacing, handCenterY - 25.dp.toPx()),
                    end = Offset(handCenterX + i * fingerSpacing, handCenterY - 25.dp.toPx() + (swipeProgress * 40.dp.toPx())),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
