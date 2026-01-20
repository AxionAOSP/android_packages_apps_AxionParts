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

import android.graphics.Color as AndroidColor
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.*
import kotlin.math.sin

private enum class LockscreenSubScreen {
    MAIN,
    EDGE_LIGHT,
    MEDIA_ART,
    PULSE_VISUALIZER,
    AOD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockscreenFeaturesScreen(
    onBackClick: () -> Unit
) {
    var currentScreen by rememberSaveable { mutableStateOf(LockscreenSubScreen.MAIN) }
    
    val screenTitle = when (currentScreen) {
        LockscreenSubScreen.MAIN -> stringResource(R.string.lockscreen)
        LockscreenSubScreen.EDGE_LIGHT -> stringResource(R.string.edge_light)
        LockscreenSubScreen.MEDIA_ART -> stringResource(R.string.media_art)
        LockscreenSubScreen.PULSE_VISUALIZER -> stringResource(R.string.pulse_visualizer)
        LockscreenSubScreen.AOD -> stringResource(R.string.always_on_display)
    }
    
    val handleBack: () -> Unit = {
        if (currentScreen == LockscreenSubScreen.MAIN) {
            onBackClick()
        } else {
            currentScreen = LockscreenSubScreen.MAIN
        }
    }
    
    
    BackHandler(onBack = handleBack)
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
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
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val isNavigatingForward = targetState != LockscreenSubScreen.MAIN
                
                if (isNavigatingForward) {
                    
                    (slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) + fadeIn(animationSpec = tween(300))).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeOut(animationSpec = tween(150))
                    )
                } else {
                    
                    (slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> -fullWidth / 3 }
                    ) + fadeIn(animationSpec = tween(300))).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { fullWidth -> fullWidth }
                        ) + fadeOut(animationSpec = tween(150))
                    )
                }
            },
            label = "screenTransition"
        ) { screen ->
            when (screen) {
                LockscreenSubScreen.MAIN -> LockscreenMainContent(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToEdgeLight = { currentScreen = LockscreenSubScreen.EDGE_LIGHT },
                    onNavigateToMediaArt = { currentScreen = LockscreenSubScreen.MEDIA_ART },
                    onNavigateToPulse = { currentScreen = LockscreenSubScreen.PULSE_VISUALIZER },
                    onNavigateToAod = { currentScreen = LockscreenSubScreen.AOD }
                )
                LockscreenSubScreen.EDGE_LIGHT -> EdgeLightContent(
                    modifier = Modifier.padding(innerPadding)
                )
                LockscreenSubScreen.MEDIA_ART -> LockscreenMediaContent(
                    modifier = Modifier.padding(innerPadding)
                )
                LockscreenSubScreen.PULSE_VISUALIZER -> PulseVisualizerContent(
                    modifier = Modifier.padding(innerPadding)
                )
                LockscreenSubScreen.AOD -> AodContent(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun LockscreenMainContent(
    modifier: Modifier = Modifier,
    onNavigateToEdgeLight: () -> Unit,
    onNavigateToMediaArt: () -> Unit,
    onNavigateToPulse: () -> Unit,
    onNavigateToAod: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        
        
        AnimatedFeatureCard(
            title = stringResource(R.string.edge_light),
            description = stringResource(R.string.edge_light_description),
            illustrationBackground = Color(0xFF06B6D4),
            onClick = onNavigateToEdgeLight
        ) {
            EdgeLightIllustration()
        }
        
        
        AnimatedFeatureCard(
            title = stringResource(R.string.media_art),
            description = stringResource(R.string.media_art_description),
            illustrationBackground = Color(0xFFA855F7),
            onClick = onNavigateToMediaArt
        ) {
            MediaArtIllustration()
        }
        
        AnimatedFeatureCard(
            title = stringResource(R.string.pulse_visualizer),
            description = stringResource(R.string.pulse_visualizer_description),
            illustrationBackground = Color(0xFFEF4444),
            onClick = onNavigateToPulse
        ) {
            PulseVisualizerIllustration()
        }
        
        AnimatedFeatureCard(
            title = stringResource(R.string.always_on_display),
            description = stringResource(R.string.always_on_display_description),
            illustrationBackground = Color(0xFFFF9800),
            onClick = onNavigateToAod
        ) {
            AodIllustration()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun EdgeLightContent(
    modifier: Modifier = Modifier
) {
    val (colorMode, _) = rememberSecureSettingStringState("edge_light_color_mode", "default")
    val (customColor, _) = rememberSecureSettingIntState("edge_light_custom_color", AndroidColor.WHITE)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        
        FeatureIllustrationHeader(
            illustrationBackground = Color(0xFF06B6D4)
        ) {
            EdgeLightIllustrationLarge()
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                SecureSettingSwitch(
                    settingKey = "edge_light_enabled",
                    title = stringResource(R.string.enable_edge_light),
                    summary = stringResource(R.string.edge_light_summary),
                    defaultValue = false
                )
            }
            item {
                SecureListPreference(
                    key = "edge_light_color_mode",
                    title = stringResource(R.string.color_mode),
                    summary = when (colorMode) {
                        "default" -> stringResource(R.string.notification_accent)
                        "custom" -> stringResource(R.string.custom_color)
                        else -> stringResource(R.string.notification_accent)
                    },
                    options = listOf(
                        "default" to stringResource(R.string.notification_accent),
                        "custom" to stringResource(R.string.custom_color)
                    ),
                    defaultValue = "default",
                    dependencyKey = "edge_light_enabled"
                )
            }
        }
        
        
        
        AnimatedVisibility(
            visible = colorMode == "custom",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                
                PreferenceGroup(title = stringResource(R.string.customization)) {
                    item {
                        SecureColorPreference(
                            key = "edge_light_custom_color",
                            title = stringResource(R.string.custom_color),
                            summary = String.format("#%06X", customColor and 0xFFFFFF),
                            defaultValue = AndroidColor.WHITE,
                            dependencyKey = "edge_light_enabled"
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LockscreenMediaContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        
        FeatureIllustrationHeader(
            illustrationBackground = Color(0xFFA855F7)
        ) {
            MediaArtIllustrationLarge()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                SecureSettingSwitch(
                    settingKey = "ls_media_art_enabled",
                    title = stringResource(R.string.enable_media_art),
                    summary = stringResource(R.string.media_art_summary),
                    defaultValue = false
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PreferenceGroup(title = stringResource(R.string.appearance)) {
            item {
                SecureSettingSlider(
                    settingKey = "ls_media_art_blur",
                    title = stringResource(R.string.blur_level),
                    summary = stringResource(R.string.blur_level_summary),
                    min = 0,
                    max = 100,
                    unit = "dp",
                    defaultValue = 0
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PulseVisualizerContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        
        FeatureIllustrationHeader(
            illustrationBackground = Color(0xFFEF4444)
        ) {
            PulseVisualizerIllustrationLarge()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                SecureSettingSwitch(
                    settingKey = "visualizer_pulse_enabled",
                    title = stringResource(R.string.enable_pulse),
                    summary = stringResource(R.string.pulse_summary),
                    defaultValue = false
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PreferenceGroup(title = stringResource(R.string.style)) {
            item {
                SecureListPreference(
                    key = "pulse_view_style",
                    title = stringResource(R.string.render_style),
                    summary = stringResource(R.string.render_style_summary),
                    options = listOf(
                        "0" to stringResource(R.string.solid_lines),
                        "1" to stringResource(R.string.fading_blocks)
                    ),
                    defaultValue = "0",
                    dependencyKey = "visualizer_pulse_enabled"
                )
            }
            item {
                SecureListPreference(
                    key = "visualizer_pulse_color",
                    title = stringResource(R.string.color_mode),
                    summary = stringResource(R.string.visualizer_color_summary),
                    options = listOf(
                        "lavalamp" to stringResource(R.string.lava_lamp),
                        "album" to stringResource(R.string.album_art),
                        "accent" to stringResource(R.string.system_accent)
                    ),
                    defaultValue = "lavalamp",
                    dependencyKey = "visualizer_pulse_enabled"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PreferenceGroup(title = stringResource(R.string.bars)) {
            item {
                SecureSettingSlider(
                    settingKey = "visualizer_pulse_bar_count",
                    title = stringResource(R.string.bar_count),
                    summary = stringResource(R.string.bar_count_summary),
                    min = 8,
                    max = 64,
                    defaultValue = 32,
                    enabled = true
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = "visualizer_pulse_rounded_bars_enabled",
                    title = stringResource(R.string.rounded_bars),
                    summary = stringResource(R.string.rounded_bars_summary),
                    defaultValue = true
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AnimatedFeatureCard(
    title: String,
    description: String,
    illustrationBackground: Color,
    onClick: () -> Unit,
    illustration: @Composable () -> Unit
) {
    val containerColor = MaterialTheme.colorScheme.surfaceBright
    val contentColor = MaterialTheme.colorScheme.onSurface
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ExpressiveShapes.large)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(ExpressiveShapes.medium)
                .background(illustrationBackground.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            illustration()
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}


@Composable
private fun EdgeLightIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "edgeLight")
    
    val leftAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leftEdge"
    )
    
    val rightAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rightEdge"
    )
    
    Canvas(modifier = Modifier.size(60.dp, 70.dp)) {
        val phoneWidth = size.width * 0.7f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 8.dp.toPx()
        val edgeWidth = 3.dp.toPx()
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 2.dp.toPx())
        )
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(phoneLeft + 3.dp.toPx(), phoneTop + 6.dp.toPx()),
            size = Size(phoneWidth - 6.dp.toPx(), phoneHeight - 12.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadius - 2.dp.toPx())
        )
        
        
        drawLine(
            color = Color.Cyan.copy(alpha = leftAlpha),
            start = Offset(phoneLeft, phoneTop + cornerRadius),
            end = Offset(phoneLeft, phoneTop + phoneHeight - cornerRadius),
            strokeWidth = edgeWidth,
            cap = StrokeCap.Round
        )
        
        
        drawLine(
            color = Color.Cyan.copy(alpha = rightAlpha),
            start = Offset(phoneLeft + phoneWidth, phoneTop + cornerRadius),
            end = Offset(phoneLeft + phoneWidth, phoneTop + phoneHeight - cornerRadius),
            strokeWidth = edgeWidth,
            cap = StrokeCap.Round
        )
    }
}


@Composable
private fun MediaArtIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "mediaArt")
    
    val albumAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "albumArt"
    )
    
    Canvas(modifier = Modifier.size(60.dp, 70.dp)) {
        val phoneWidth = size.width * 0.7f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 8.dp.toPx()
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 2.dp.toPx())
        )
        
        
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF9333EA).copy(alpha = albumAlpha),
                    Color(0xFFDB2777).copy(alpha = albumAlpha)
                )
            ),
            topLeft = Offset(phoneLeft + 4.dp.toPx(), phoneTop + 8.dp.toPx()),
            size = Size(phoneWidth - 8.dp.toPx(), phoneHeight - 16.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadius - 2.dp.toPx())
        )
        
        
        val centerX = phoneLeft + phoneWidth / 2
        val centerY = phoneTop + phoneHeight / 2
        drawCircle(
            color = Color.White.copy(alpha = albumAlpha * 0.8f),
            radius = 8.dp.toPx(),
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color.White.copy(alpha = albumAlpha * 0.5f),
            radius = 12.dp.toPx(),
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}


@Composable
private fun PulseVisualizerIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val barHeights = remember {
        listOf(0.3f, 0.5f, 0.7f, 0.9f, 0.6f, 0.8f, 0.4f)
    }
    
    val animatedHeights = barHeights.mapIndexed { index, baseHeight ->
        infiniteTransition.animateFloat(
            initialValue = baseHeight * 0.4f,
            targetValue = baseHeight,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 300 + (index * 80),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
    }
    
    Canvas(modifier = Modifier.size(60.dp, 70.dp)) {
        val phoneWidth = size.width * 0.7f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 8.dp.toPx()
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 2.dp.toPx())
        )
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(phoneLeft + 3.dp.toPx(), phoneTop + 6.dp.toPx()),
            size = Size(phoneWidth - 6.dp.toPx(), phoneHeight - 12.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadius - 2.dp.toPx())
        )
        
        
        val barCount = animatedHeights.size
        val barAreaWidth = phoneWidth - 10.dp.toPx()
        val barWidth = barAreaWidth / (barCount * 2)
        val barSpacing = barWidth
        val maxBarHeight = phoneHeight * 0.5f
        val barBottom = phoneTop + phoneHeight - 10.dp.toPx()
        
        animatedHeights.forEachIndexed { index, heightState ->
            val barHeight = heightState.value * maxBarHeight
            val barX = phoneLeft + 5.dp.toPx() + index * (barWidth + barSpacing)
            
            drawRoundRect(
                color = Color(0xFFFF6B6B),
                topLeft = Offset(barX, barBottom - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
    }
}


@Composable
private fun FeatureIllustrationHeader(
    illustrationBackground: Color,
    illustration: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(ExpressiveShapes.large)
            .background(illustrationBackground.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(ExpressiveShapes.medium)
                .background(illustrationBackground.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            illustration()
        }
    }
}


@Composable
private fun EdgeLightIllustrationLarge() {
    val infiniteTransition = rememberInfiniteTransition(label = "edgeLightLarge")
    
    val leftAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leftEdgeLarge"
    )
    
    val rightAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rightEdgeLarge"
    )
    
    Canvas(modifier = Modifier.size(100.dp, 140.dp)) {
        val phoneWidth = size.width * 0.75f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 16.dp.toPx()
        val edgeWidth = 5.dp.toPx()
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 3.dp.toPx())
        )
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(phoneLeft + 5.dp.toPx(), phoneTop + 10.dp.toPx()),
            size = Size(phoneWidth - 10.dp.toPx(), phoneHeight - 20.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadius - 4.dp.toPx())
        )
        
        
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Cyan.copy(alpha = 0f),
                    Color.Cyan.copy(alpha = leftAlpha),
                    Color.Cyan.copy(alpha = leftAlpha),
                    Color.Cyan.copy(alpha = 0f)
                )
            ),
            start = Offset(phoneLeft - 2.dp.toPx(), phoneTop),
            end = Offset(phoneLeft - 2.dp.toPx(), phoneTop + phoneHeight),
            strokeWidth = edgeWidth,
            cap = StrokeCap.Round
        )
        
        
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Cyan.copy(alpha = 0f),
                    Color.Cyan.copy(alpha = rightAlpha),
                    Color.Cyan.copy(alpha = rightAlpha),
                    Color.Cyan.copy(alpha = 0f)
                )
            ),
            start = Offset(phoneLeft + phoneWidth + 2.dp.toPx(), phoneTop),
            end = Offset(phoneLeft + phoneWidth + 2.dp.toPx(), phoneTop + phoneHeight),
            strokeWidth = edgeWidth,
            cap = StrokeCap.Round
        )
    }
}


@Composable
private fun MediaArtIllustrationLarge() {
    val infiniteTransition = rememberInfiniteTransition(label = "mediaArtLarge")
    
    val albumAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "albumArtLarge"
    )
    
    val albumScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "albumScaleLarge"
    )
    
    Canvas(modifier = Modifier.size(100.dp, 140.dp)) {
        val phoneWidth = size.width * 0.75f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 16.dp.toPx()
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 3.dp.toPx())
        )
        
        
        val artPadding = 8.dp.toPx()
        val artWidth = (phoneWidth - artPadding * 2) * albumScale
        val artHeight = (phoneHeight - artPadding * 2) * albumScale
        val artLeft = phoneLeft + (phoneWidth - artWidth) / 2
        val artTop = phoneTop + (phoneHeight - artHeight) / 2
        
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF9333EA).copy(alpha = albumAlpha),
                    Color(0xFFDB2777).copy(alpha = albumAlpha),
                    Color(0xFFEC4899).copy(alpha = albumAlpha)
                )
            ),
            topLeft = Offset(artLeft, artTop),
            size = Size(artWidth, artHeight),
            cornerRadius = CornerRadius(cornerRadius - 4.dp.toPx())
        )
        
        
        val centerX = phoneLeft + phoneWidth / 2
        val centerY = phoneTop + phoneHeight / 2
        drawCircle(
            color = Color.White.copy(alpha = albumAlpha * 0.9f),
            radius = 18.dp.toPx(),
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color(0xFF9333EA).copy(alpha = albumAlpha),
            radius = 12.dp.toPx(),
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color.White.copy(alpha = albumAlpha * 0.8f),
            radius = 5.dp.toPx(),
            center = Offset(centerX, centerY)
        )
        
        
        drawCircle(
            color = Color.White.copy(alpha = albumAlpha * 0.4f),
            radius = 24.dp.toPx(),
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}


@Composable
private fun PulseVisualizerIllustrationLarge() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseLarge")
    
    val barHeights = remember {
        listOf(0.2f, 0.4f, 0.6f, 0.9f, 0.7f, 0.5f, 0.8f, 0.6f, 0.3f)
    }
    
    val animatedHeights = barHeights.mapIndexed { index, baseHeight ->
        infiniteTransition.animateFloat(
            initialValue = baseHeight * 0.3f,
            targetValue = baseHeight,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 400 + (index * 60),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "barLarge$index"
        )
    }
    
    Canvas(modifier = Modifier.size(100.dp, 140.dp)) {
        val phoneWidth = size.width * 0.75f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 16.dp.toPx()
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 3.dp.toPx())
        )
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(phoneLeft + 5.dp.toPx(), phoneTop + 10.dp.toPx()),
            size = Size(phoneWidth - 10.dp.toPx(), phoneHeight - 20.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadius - 4.dp.toPx())
        )
        
        
        val barCount = animatedHeights.size
        val barAreaWidth = phoneWidth - 14.dp.toPx()
        val barWidth = barAreaWidth / (barCount * 1.5f)
        val barSpacing = barWidth * 0.5f
        val maxBarHeight = phoneHeight * 0.6f
        val barBottom = phoneTop + phoneHeight - 15.dp.toPx()
        
        animatedHeights.forEachIndexed { index, heightState ->
            val barHeight = heightState.value * maxBarHeight
            val barX = phoneLeft + 7.dp.toPx() + index * (barWidth + barSpacing)
            
            
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF6B6B),
                        Color(0xFFFF8E8E)
                    )
                ),
                topLeft = Offset(barX, barBottom - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(3.dp.toPx())
            )
        }
    }
}



@Composable
private fun SaturationValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChange: (Float, Float) -> Unit
) {
    var pickerSize by remember { mutableStateOf(Size.Zero) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(16.dp))
            .drawWithCache {
                pickerSize = size
                val hueColor = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
                onDrawBehind {
                    
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White, Color(hueColor))
                        )
                    )
                    
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black)
                        )
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                    onSaturationValueChange(s, v)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onSaturationValueChange(s, v)
                }
            }
    ) {
        
        if (pickerSize != Size.Zero) {
            val indicatorX = saturation * pickerSize.width
            val indicatorY = (1f - value) * pickerSize.height
            Canvas(modifier = Modifier.fillMaxSize()) {
                
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = Offset(indicatorX, indicatorY),
                    style = Stroke(width = 3.dp.toPx())
                )
                
                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = 10.dp.toPx(),
                    center = Offset(indicatorX, indicatorY),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit
) {
    val hueColors = remember {
        List(361) { h ->
            Color(AndroidColor.HSVToColor(floatArrayOf(h.toFloat(), 1f, 1f)))
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(hueColors))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val h = (offset.x / size.width * 360f).coerceIn(0f, 360f)
                    onHueChange(h)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val h = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                    onHueChange(h)
                }
            }
    ) {
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val indicatorX = (hue / 360f) * size.width
            
            drawCircle(
                color = Color.White,
                radius = 14.dp.toPx(),
                center = Offset(indicatorX, size.height / 2),
                style = Stroke(width = 3.dp.toPx())
            )
            
            drawCircle(
                color = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))),
                radius = 11.dp.toPx(),
                center = Offset(indicatorX, size.height / 2)
            )
        }
    }
}

@Composable
private fun HexColorInput(
    hexValue: String,
    onHexChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        BasicTextField(
            value = hexValue,
            onValueChange = { newValue ->
                
                val filtered = newValue.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(6)
                onHexChange(filtered)
            },
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters
            ),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PresetColorRow(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    val presetColors = listOf(
        AndroidColor.WHITE,
        AndroidColor.parseColor("#FF5252"),
        AndroidColor.parseColor("#FF4081"),
        AndroidColor.parseColor("#E040FB"),
        AndroidColor.parseColor("#7C4DFF"),
        AndroidColor.parseColor("#536DFE"),
        AndroidColor.parseColor("#448AFF"),
        AndroidColor.parseColor("#40C4FF"),
        AndroidColor.parseColor("#18FFFF"),
        AndroidColor.parseColor("#64FFDA"),
        AndroidColor.parseColor("#69F0AE"),
        AndroidColor.parseColor("#B2FF59"),
        AndroidColor.parseColor("#EEFF41"),
        AndroidColor.parseColor("#FFFF00"),
        AndroidColor.parseColor("#FFD740"),
        AndroidColor.parseColor("#FFAB40")
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            presetColors.take(8).forEach { color ->
                ColorSwatch(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            presetColors.drop(8).forEach { color ->
                ColorSwatch(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isLightColor = run {
        val r = AndroidColor.red(color)
        val g = AndroidColor.green(color)
        val b = AndroidColor.blue(color)
        (0.299 * r + 0.587 * g + 0.114 * b) > 186
    }
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(color))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.selected),
                tint = if (isLightColor) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SecureColorPreference(
    key: String,
    title: String,
    summary: String,
    defaultValue: Int,
    position: PreferencePosition = PreferencePosition.Single,
    dependencyKey: String? = null
) {
    val (color, setColor) = rememberSecureSettingIntState(key, defaultValue)
    val enabled = if (dependencyKey != null) {
        rememberSecureSettingBoolean(dependencyKey, true)
    } else {
        true
    }
    
    var showDialog by remember { mutableStateOf(false) }
    val shape = preferenceShape(position)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(enabled = enabled) { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        
        Spacer(modifier = Modifier.width(16.dp))
        
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(color).copy(alpha = if (enabled) 1f else 0.38f))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 0.3f else 0.12f),
                    shape = CircleShape
                )
        )
    }
    
    if (showDialog && enabled) {
        ColorPickerDialog(
            initialColor = color,
            onColorSelected = { selectedColor ->
                setColor(selectedColor)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val hsv = remember { FloatArray(3) }
    AndroidColor.colorToHSV(initialColor, hsv)
    
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    var hexInput by remember { mutableStateOf(String.format("%06X", initialColor and 0xFFFFFF)) }
    
    val currentColor = remember(hue, saturation, value) {
        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ExpressiveShapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.choose_color),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SaturationValuePicker(
                hue = hue,
                saturation = saturation,
                value = value,
                onSaturationValueChange = { s, v ->
                    saturation = s
                    value = v
                    hexInput = String.format("%06X", AndroidColor.HSVToColor(floatArrayOf(hue, s, v)) and 0xFFFFFF)
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            HueSlider(
                hue = hue,
                onHueChange = { h ->
                    hue = h
                    hexInput = String.format("%06X", AndroidColor.HSVToColor(floatArrayOf(h, saturation, value)) and 0xFFFFFF)
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(currentColor))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
                
                HexColorInput(
                    hexValue = hexInput,
                    onHexChange = { newHex ->
                        hexInput = newHex
                        if (newHex.length == 6) {
                            try {
                                val parsedColor = AndroidColor.parseColor("#$newHex")
                                AndroidColor.colorToHSV(parsedColor, hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                            } catch (_: Exception) { }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            
            Text(
                text = stringResource(R.string.presets),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PresetColorRow(
                selectedColor = currentColor,
                onColorSelected = { color ->
                    AndroidColor.colorToHSV(color, hsv)
                    hue = hsv[0]
                    saturation = hsv[1]
                    value = hsv[2]
                    hexInput = String.format("%06X", color and 0xFFFFFF)
                }
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Button(
                    onClick = { onColorSelected(currentColor) }
                ) {
                    Text(
                        text = stringResource(R.string.select_uppercase),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun AodContent(
    modifier: Modifier = Modifier
) {
    val (scheduleMode, _) = rememberSecureSettingStringState("aod_schedule_mode", "0")
    val (screenOffAnimation, _) = rememberSystemSettingIntState("screen_off_aod_animation", 1)
    val context = LocalContext.current

    LaunchedEffect(scheduleMode) {
        val modeInt = scheduleMode.toIntOrNull() ?: 0
        val shouldEnableDoze = modeInt != 0
        Settings.Secure.putInt(
            context.contentResolver, 
            Settings.Secure.DOZE_ALWAYS_ON, 
            if (shouldEnableDoze) 1 else 0
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        FeatureIllustrationHeader(
            illustrationBackground = Color(0xFFFF9800)
        ) {
            AodIllustrationLarge()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PreferenceGroup(title = stringResource(R.string.mode)) {
            item {
                SecureListPreference(
                    key = "aod_schedule_mode",
                    title = stringResource(R.string.aod_schedule),
                    summary = when (scheduleMode) {
                        "0" -> stringResource(R.string.disabled)
                        "1" -> stringResource(R.string.always_on)
                        "2" -> stringResource(R.string.aod_charge_only)
                        "3" -> stringResource(R.string.scheduled)
                        "4" -> stringResource(R.string.aod_scheduled_charge)
                        else -> stringResource(R.string.disabled)
                    },
                    options = listOf(
                        "0" to stringResource(R.string.disabled),
                        "1" to stringResource(R.string.always_on),
                        "2" to stringResource(R.string.aod_charge_only),
                        "3" to stringResource(R.string.scheduled),
                        "4" to stringResource(R.string.aod_scheduled_charge)
                    ),
                    defaultValue = "0"
                )
            }
        }
        
        
        Spacer(modifier = Modifier.height(12.dp))
        
        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                SystemSettingSwitch(
                    settingKey = "screen_off_aod_animation",
                    title = stringResource(R.string.screen_off_animation),
                    summary = stringResource(R.string.screen_off_animation_summary),
                    defaultValue = true
                )
            }
        }
        
        AnimatedVisibility(
            visible = screenOffAnimation == 1,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                
                PreferenceGroup(title = stringResource(R.string.screen_off)) {
                    item {
                        SystemSettingSwitch(
                            settingKey = "screen_off_aod_enabled",
                            title = stringResource(R.string.screen_off_aod),
                            summary = stringResource(R.string.screen_off_aod_summary),
                            defaultValue = false
                        )
                    }
                }
            }
        }
        
        AnimatedVisibility(
            visible = scheduleMode == "3" || scheduleMode == "4",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                
                PreferenceGroup(title = stringResource(R.string.schedule)) {
                    item {
                        SecureTimePreference(
                            key = "aod_schedule_start_time",
                            title = stringResource(R.string.start_time),
                            summary = stringResource(R.string.start_time_summary),
                            defaultValue = "2300"
                        )
                    }
                    item {
                        SecureTimePreference(
                            key = "aod_schedule_end_time",
                            title = stringResource(R.string.end_time),
                            summary = stringResource(R.string.end_time_summary),
                            defaultValue = "0700"
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AodIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "aod")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Canvas(modifier = Modifier.size(60.dp, 70.dp)) {
        val phoneWidth = size.width * 0.7f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 8.dp.toPx()
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 2.dp.toPx())
        )
        
        val centerX = phoneLeft + phoneWidth / 2
        val centerY = phoneTop + phoneHeight / 3
        
        drawCircle(
            color = Color(0xFFFF9800).copy(alpha = pulseAlpha),
            radius = 12.dp.toPx(),
            center = Offset(centerX, centerY)
        )
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(centerX - 15.dp.toPx(), centerY + 20.dp.toPx()),
            size = Size(30.dp.toPx(), 4.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
    }
}

@Composable
private fun AodIllustrationLarge() {
    val infiniteTransition = rememberInfiniteTransition(label = "aodLarge")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Canvas(modifier = Modifier.size(120.dp, 140.dp)) {
        val phoneWidth = size.width * 0.6f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 16.dp.toPx()
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 2.dp.toPx())
        )
        
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.3f),
            topLeft = Offset(phoneLeft + 4.dp.toPx(), phoneTop + 4.dp.toPx()),
            size = Size(phoneWidth - 8.dp.toPx(), phoneHeight - 8.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadius - 2.dp.toPx())
        )
        
        val centerX = phoneLeft + phoneWidth / 2
        val centerY = phoneTop + phoneHeight / 3
        
        drawCircle(
            color = Color(0xFFFF9800).copy(alpha = pulseAlpha),
            radius = 24.dp.toPx(),
            center = Offset(centerX, centerY)
        )
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(centerX - 30.dp.toPx(), centerY + 40.dp.toPx()),
            size = Size(60.dp.toPx(), 6.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(centerX - 20.dp.toPx(), centerY + 55.dp.toPx()),
            size = Size(40.dp.toPx(), 6.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
    }
}
