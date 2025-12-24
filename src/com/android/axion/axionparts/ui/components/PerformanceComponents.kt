/*
 * Copyright (C) 2025 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.axion.axionparts.ui.components

import android.os.SystemProperties
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.axion.axionparts.ui.theme.ExpressiveShapes
import kotlin.math.roundToInt

private const val POWER_MODE_KEY = "persist.sys.power_mode_perf"
private const val POWER_MODE_BY_USER_KEY = "persist.sys.power_mode_perf_by_user"

@Composable
fun PowerModeToggle(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    var isEnabled by remember {
        mutableStateOf(
            try {
                Settings.System.getIntForUser(
                    contentResolver, 
                    POWER_MODE_KEY, 
                    0, 
                    UserHandle.USER_CURRENT
                ) == 1
            } catch (e: Exception) {
                SystemProperties.getInt(POWER_MODE_KEY, 0) == 1
            }
        )
    }
    
    DisposableEffect(Unit) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isEnabled = try {
                    Settings.System.getIntForUser(
                        contentResolver, 
                        POWER_MODE_KEY, 
                        0, 
                        UserHandle.USER_CURRENT
                    ) == 1
                } catch (e: Exception) {
                    SystemProperties.getInt(POWER_MODE_KEY, 0) == 1
                }
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(POWER_MODE_KEY),
            false,
            observer
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "scale"
    )
    
    
    val gradientStart by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFFf12711) else Color(0xFF434343),
        animationSpec = tween(500),
        label = "gradientStart"
    )
    val gradientEnd by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFFf5af19) else Color(0xFF000000),
        animationSpec = tween(500),
        label = "gradientEnd"
    )
    
    
    val iconRotation by animateFloatAsState(
        targetValue = if (isEnabled) 360f else 0f,
        animationSpec = tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "iconRotation"
    )
    
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val statusColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
        animationSpec = tween(300),
        label = "statusColor"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .scale(scale)
            .clip(ExpressiveShapes.large)
            .background(Brush.linearGradient(colors = listOf(gradientStart, gradientEnd)))
            .then(
                if (isEnabled) {
                    Modifier.background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = pulseAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                } else Modifier
            )
            .clickable(interactionSource = interactionSource, indication = null) {
                val newValue = !isEnabled
                isEnabled = newValue
                val modeValue = if (newValue) 1 else 0
                SystemProperties.set(POWER_MODE_KEY, modeValue.toString())
                Settings.System.putIntForUser(
                    contentResolver,
                    POWER_MODE_KEY,
                    modeValue,
                    UserHandle.USER_CURRENT
                )
                Settings.System.putIntForUser(
                    contentResolver,
                    POWER_MODE_BY_USER_KEY,
                    modeValue,
                    UserHandle.USER_CURRENT
                )
            }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { rotationZ = iconRotation }
                )
                Column {
                    Text(
                        text = "Performance Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isEnabled) "Maximum performance enabled" else "Balanced mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
        }
    }
}


@Composable
fun BoostToggleCard(
    settingKey: String,
    title: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    defaultValue: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    var isEnabled by remember {
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
                isEnabled = try {
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
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "scale"
    )
    
    val gradientStart by animateColorAsState(
        targetValue = if (isEnabled) gradientColors.first() else Color(0xFF434343),
        animationSpec = tween(500),
        label = "gradientStart"
    )
    val gradientEnd by animateColorAsState(
        targetValue = if (isEnabled) gradientColors.last() else Color(0xFF000000),
        animationSpec = tween(500),
        label = "gradientEnd"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "boostPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val dotScale by animateFloatAsState(
        targetValue = if (isEnabled) 1.3f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label = "dotScale"
    )
    
    val statusColor by animateColorAsState(
        targetValue = if (isEnabled) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
        animationSpec = tween(300),
        label = "statusColor"
    )
    
    Box(
        modifier = modifier
            .height(100.dp)
            .scale(scale)
            .clip(ExpressiveShapes.large)
            .background(Brush.linearGradient(colors = listOf(gradientStart, gradientEnd)))
            .then(
                if (isEnabled) {
                    Modifier.background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = pulseAlpha * 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                } else Modifier
            )
            .clickable(interactionSource = interactionSource, indication = null) {
                val newValue = !isEnabled
                isEnabled = newValue
                Settings.Secure.putInt(contentResolver, settingKey, if (newValue) 1 else 0)
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isEnabled) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
fun FrequencySlider(
    settingKey: String,
    label: String,
    min: Int = 0,
    max: Int,
    interval: Int = 100000,
    defaultValue: Int = min,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    var currentValue by remember {
        mutableFloatStateOf(
            try {
                Settings.Secure.getInt(contentResolver, settingKey, defaultValue).toFloat()
            } catch (e: Exception) {
                defaultValue.toFloat()
            }
        )
    }
    
    DisposableEffect(settingKey) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                currentValue = try {
                    Settings.Secure.getInt(contentResolver, settingKey, defaultValue).toFloat()
                } catch (e: Exception) {
                    defaultValue.toFloat()
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
    
    val progress = if (max > min) (currentValue - min) / (max - min) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "progress"
    )
    
    Column(modifier = modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.4f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${currentValue.roundToInt() / 1000} MHz",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.6f), accentColor)))
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Slider(
            value = currentValue,
            onValueChange = { newValue ->
                val steppedValue = ((newValue - min) / interval).roundToInt() * interval + min
                currentValue = steppedValue.coerceIn(min, max).toFloat()
            },
            onValueChangeFinished = {
                Settings.Secure.putInt(contentResolver, settingKey, currentValue.roundToInt())
            },
            valueRange = min.toFloat()..max.toFloat(),
            steps = if (interval > 0) ((max - min) / interval) - 1 else 0,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.height(24.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${min / 1000} MHz",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "${max / 1000} MHz",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun LevelSlider(
    settingKey: String,
    label: String,
    min: Int = 0,
    max: Int,
    defaultValue: Int = 1,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    var currentValue by remember {
        mutableFloatStateOf(
            try {
                Settings.Secure.getInt(contentResolver, settingKey, defaultValue).toFloat()
            } catch (e: Exception) {
                defaultValue.toFloat()
            }
        )
    }
    
    DisposableEffect(settingKey) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                currentValue = try {
                    Settings.Secure.getInt(contentResolver, settingKey, defaultValue).toFloat()
                } catch (e: Exception) {
                    defaultValue.toFloat()
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
    
    val progress = if (max > min) (currentValue - min) / (max - min) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "progress"
    )
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Level ${currentValue.roundToInt()}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.6f), accentColor)))
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Slider(
            value = currentValue,
            onValueChange = { newValue ->
                currentValue = newValue.roundToInt().coerceIn(min, max).toFloat()
            },
            onValueChangeFinished = {
                Settings.Secure.putInt(contentResolver, settingKey, currentValue.roundToInt())
            },
            valueRange = min.toFloat()..max.toFloat(),
            steps = max - min - 1,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}
