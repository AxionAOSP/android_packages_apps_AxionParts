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

package com.android.axion.axionparts.ui.screens

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.theme.ExpressiveShapes
import com.android.axion.compose.preferences.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundFeaturesScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sound),
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
        SoundFeaturesContent(
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun SoundFeaturesContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        MultiAudioFocusCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PerAppVolumeCard()
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PerAppVolumeCard() {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val interactionSource = remember { MutableInteractionSource() }
    
    var isEnabled by remember {
        mutableStateOf(
            try {
                Settings.System.getInt(contentResolver, "show_app_volume", 0) == 1
            } catch (e: Exception) {
                false
            }
        )
    }
    
    DisposableEffect(Unit) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isEnabled = try {
                    Settings.System.getInt(contentResolver, "show_app_volume", 0) == 1
                } catch (e: Exception) {
                    false
                }
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.getUriFor("show_app_volume"),
            false,
            observer
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }
    
    val accentColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.surfaceBright
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.large)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                isEnabled = !isEnabled
                Settings.System.putInt(contentResolver, "show_app_volume", if (isEnabled) 1 else 0)
            }
            .padding(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(ExpressiveShapes.medium)
                    .background(accentColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                PerAppVolumeIllustration()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.per_app_volume),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.per_app_volume_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = null,
                    thumbContent = {
                        Icon(
                            imageVector = if (isEnabled) Icons.Filled.Check else Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}


@Composable
private fun PerAppVolumeIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "perAppVol")
    val accentColor = MaterialTheme.colorScheme.primary
    
    val slider1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slider1"
    )
    
    val slider2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slider2"
    )
    
    val slider3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slider3"
    )
    
    Canvas(modifier = Modifier.size(100.dp, 100.dp)) {
        val phoneWidth = size.width * 0.7f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 8.dp.toPx()
        
        drawRoundRect(
            color = accentColor.copy(alpha = 0.6f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 2.dp.toPx())
        )
        
        val panelWidth = phoneWidth * 0.6f
        val panelHeight = phoneHeight * 0.7f
        val panelRight = phoneLeft + phoneWidth - 4.dp.toPx()
        val panelTop = phoneTop + (phoneHeight - panelHeight) / 2
        
        drawRoundRect(
            color = accentColor.copy(alpha = 0.2f),
            topLeft = Offset(panelRight - panelWidth, panelTop),
            size = Size(panelWidth, panelHeight),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        
        val rowHeight = panelHeight / 4
        val iconSize = 8.dp.toPx()
        val sliderHeight = 4.dp.toPx()
        val sliderWidth = panelWidth - iconSize - 12.dp.toPx()
        val startX = panelRight - panelWidth + 6.dp.toPx()
        val startY = panelTop + rowHeight / 2
        
        val sliderValues = listOf(slider1, slider2, slider3)
        
        sliderValues.forEachIndexed { index, value ->
            val y = startY + index * rowHeight
            
            drawCircle(
                color = accentColor.copy(alpha = 0.7f),
                radius = iconSize / 2,
                center = Offset(startX + iconSize / 2, y)
            )
            
            val trackX = startX + iconSize + 6.dp.toPx()
            drawRoundRect(
                color = accentColor.copy(alpha = 0.3f),
                topLeft = Offset(trackX, y - sliderHeight / 2),
                size = Size(sliderWidth, sliderHeight),
                cornerRadius = CornerRadius(sliderHeight / 2)
            )
            
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(trackX, y - sliderHeight / 2),
                size = Size(sliderWidth * value, sliderHeight),
                cornerRadius = CornerRadius(sliderHeight / 2)
            )
        }
    }
}

@Composable
private fun MultiAudioFocusCard() {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val interactionSource = remember { MutableInteractionSource() }
    
    var isEnabled by remember {
        mutableStateOf(
            try {
                Settings.System.getInt(contentResolver, "multi_audio_focus_enabled", 0) == 1
            } catch (e: Exception) {
                false
            }
        )
    }
    
    DisposableEffect(Unit) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isEnabled = try {
                    Settings.System.getInt(contentResolver, "multi_audio_focus_enabled", 0) == 1
                } catch (e: Exception) {
                    false
                }
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.getUriFor("multi_audio_focus_enabled"),
            false,
            observer
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }
    
    val accentColor = MaterialTheme.colorScheme.secondary
    val containerColor = MaterialTheme.colorScheme.surfaceBright
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.large)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                isEnabled = !isEnabled
                Settings.System.putInt(contentResolver, "multi_audio_focus_enabled", if (isEnabled) 1 else 0)
            }
            .padding(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(ExpressiveShapes.medium)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                MultiAudioIllustration()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.multi_audio_focus),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.multi_audio_focus_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = null,
                    thumbContent = {
                        Icon(
                            imageVector = if (isEnabled) Icons.Filled.Check else Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}


@Composable
private fun MultiAudioIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "multiAudio")
    val accentColor = MaterialTheme.colorScheme.secondary
    
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    
    val barHeights = remember {
        listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.3f)
    }
    
    val animatedHeights = barHeights.mapIndexed { index, baseHeight ->
        infiniteTransition.animateFloat(
            initialValue = baseHeight * 0.3f,
            targetValue = baseHeight,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 400 + (index * 80),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
    }
    
    Canvas(modifier = Modifier.size(200.dp, 100.dp)) {
        val centerY = size.height / 2
        
        val leftSpeakerX = 30.dp.toPx()
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(leftSpeakerX - 8.dp.toPx(), centerY - 25.dp.toPx()),
            size = Size(12.dp.toPx(), 20.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        
        for (i in 0 until 3) {
            val waveOffset = 8.dp.toPx() + i * 8.dp.toPx()
            val alpha = (0.7f - i * 0.2f) * (1f - wave1 * 0.3f)
            drawArc(
                color = accentColor.copy(alpha = alpha),
                startAngle = -50f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(leftSpeakerX + waveOffset, centerY - 30.dp.toPx() - 5.dp.toPx()),
                size = Size(15.dp.toPx() + i * 8.dp.toPx(), 30.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        val rightSpeakerX = size.width - 30.dp.toPx()
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(rightSpeakerX - 4.dp.toPx(), centerY - 25.dp.toPx()),
            size = Size(12.dp.toPx(), 20.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        
        for (i in 0 until 3) {
            val waveOffset = 8.dp.toPx() + i * 8.dp.toPx()
            val alpha = (0.7f - i * 0.2f) * (1f - wave2 * 0.3f)
            drawArc(
                color = accentColor.copy(alpha = alpha),
                startAngle = 130f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(rightSpeakerX - waveOffset - 15.dp.toPx() - i * 8.dp.toPx(), centerY - 30.dp.toPx() - 5.dp.toPx()),
                size = Size(15.dp.toPx() + i * 8.dp.toPx(), 30.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        val barCount = animatedHeights.size
        val barWidth = 6.dp.toPx()
        val barSpacing = 4.dp.toPx()
        val totalWidth = barCount * barWidth + (barCount - 1) * barSpacing
        val startX = (size.width - totalWidth) / 2
        val maxBarHeight = 50.dp.toPx()
        
        animatedHeights.forEachIndexed { index, heightState ->
            val barHeight = heightState.value * maxBarHeight
            val barX = startX + index * (barWidth + barSpacing)
            
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(barX, centerY - barHeight / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(3.dp.toPx())
            )
        }
    }
}
