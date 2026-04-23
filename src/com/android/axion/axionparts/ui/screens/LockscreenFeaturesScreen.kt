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
import android.os.SystemProperties
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.FeatureCard
import com.android.axion.compose.preferences.*
import com.android.axion.compose.scaffold.AxionScaffold

private enum class LockscreenSubScreen {
    MAIN,
    EDGE_LIGHT,
    MEDIA_ART,
    PULSE_VISUALIZER,
    AOD,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LockscreenFeaturesScreen(onBackClick: () -> Unit) {
    var currentScreen by rememberSaveable { mutableStateOf(LockscreenSubScreen.MAIN) }

    val screenTitle =
        when (currentScreen) {
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

    BackHandler(enabled = currentScreen != LockscreenSubScreen.MAIN, onBack = handleBack)

    AxionScaffold(title = screenTitle, onBackClick = handleBack) { innerPadding ->
        val motionScheme = MaterialTheme.motionScheme
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val isNavigatingForward = targetState != LockscreenSubScreen.MAIN

                if (isNavigatingForward) {

                    (slideInHorizontally(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetX = { fullWidth -> fullWidth },
                        ) + fadeIn(animationSpec = motionScheme.defaultEffectsSpec()))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = motionScheme.defaultSpatialSpec(),
                                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                            ) + fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
                        )
                } else {

                    (slideInHorizontally(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetX = { fullWidth -> -fullWidth / 3 },
                        ) + fadeIn(animationSpec = motionScheme.defaultEffectsSpec()))
                        .togetherWith(
                            slideOutHorizontally(
                                animationSpec = motionScheme.defaultSpatialSpec(),
                                targetOffsetX = { fullWidth -> fullWidth },
                            ) + fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
                        )
                }
            },
            label = "screenTransition",
        ) { screen ->
            when (screen) {
                LockscreenSubScreen.MAIN ->
                    LockscreenMainContent(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToEdgeLight = { currentScreen = LockscreenSubScreen.EDGE_LIGHT },
                        onNavigateToMediaArt = { currentScreen = LockscreenSubScreen.MEDIA_ART },
                        onNavigateToPulse = {
                            currentScreen = LockscreenSubScreen.PULSE_VISUALIZER
                        },
                        onNavigateToAod = { currentScreen = LockscreenSubScreen.AOD },
                    )
                LockscreenSubScreen.EDGE_LIGHT ->
                    EdgeLightContent(modifier = Modifier.padding(innerPadding))
                LockscreenSubScreen.MEDIA_ART ->
                    LockscreenMediaContent(modifier = Modifier.padding(innerPadding))
                LockscreenSubScreen.PULSE_VISUALIZER ->
                    PulseVisualizerContent(modifier = Modifier.padding(innerPadding))
                LockscreenSubScreen.AOD -> AodContent(modifier = Modifier.padding(innerPadding))
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
    onNavigateToAod: () -> Unit,
) {
    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeatureCard(
                    title = stringResource(R.string.edge_light),
                    subtitle = stringResource(R.string.edge_light_description),
                    icon = Icons.Filled.FlashOn,
                    onClick = onNavigateToEdgeLight,
                    illustrationColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                FeatureCard(
                    title = stringResource(R.string.media_art),
                    subtitle = stringResource(R.string.media_art_description),
                    icon = Icons.Filled.Album,
                    onClick = onNavigateToMediaArt,
                    illustrationColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeatureCard(
                    title = stringResource(R.string.pulse_visualizer),
                    subtitle = stringResource(R.string.pulse_visualizer_description),
                    icon = Icons.Filled.GraphicEq,
                    onClick = onNavigateToPulse,
                    illustrationColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                )
                FeatureCard(
                    title = stringResource(R.string.always_on_display),
                    subtitle = stringResource(R.string.always_on_display_description),
                    icon = Icons.Filled.Nightlight,
                    onClick = onNavigateToAod,
                    illustrationColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EdgeLightContent(modifier: Modifier = Modifier) {
    val edgeLightEnabled by rememberSettingBoolean("edge_light_enabled", SettingsType.SECURE, false)
    val colorMode by rememberSettingString("edge_light_color_mode", SettingsType.SECURE, "default")
    val customColor by rememberSettingInt("edge_light_custom_color", SettingsType.SECURE, AndroidColor.WHITE)

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                SecureSettingSwitch(
                    settingKey = "edge_light_enabled",
                    title = stringResource(R.string.enable_edge_light),
                    summary = stringResource(R.string.edge_light_summary),
                    defaultValue = false,
                )
            }
            item {
                SecureListPreference(
                    key = "edge_light_color_mode",
                    title = stringResource(R.string.color_mode),
                    summary =
                        when (colorMode) {
                            "default" -> "Notification accent"
                            "custom" -> "Custom color"
                            else -> "Notification accent"
                        },
                    options =
                        listOf("default" to "Notification accent", "custom" to "Custom color"),
                    defaultValue = "default",
                    dependencyKey = "edge_light_enabled",
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroup(title = stringResource(R.string.edge_light_appearance)) {
            item {
                SecureSettingSlider(
                    settingKey = "edge_light_spread",
                    title = stringResource(R.string.edge_light_spread),
                    summary = stringResource(R.string.edge_light_spread_summary),
                    min = 10,
                    max = 60,
                    defaultValue = 50,
                    unit = "%",
                    enabled = edgeLightEnabled,
                )
            }
            item {
                SecureSettingSlider(
                    settingKey = "edge_light_intensity",
                    title = stringResource(R.string.edge_light_intensity),
                    summary = stringResource(R.string.edge_light_intensity_summary),
                    min = 10,
                    max = 100,
                    defaultValue = 100,
                    unit = "%",
                    enabled = edgeLightEnabled,
                )
            }
        }

        AnimatedVisibility(
            visible = colorMode == "custom",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
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
                            dependencyKey = "edge_light_enabled",
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LockscreenMediaContent(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                SecureSettingSwitch(
                    settingKey = "ls_media_art_enabled",
                    title = stringResource(R.string.enable_media_art),
                    summary = stringResource(R.string.media_art_summary),
                    defaultValue = false,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroup(title = stringResource(R.string.appearance)) {
            item {
                SecureListPreference(
                    key = "ls_media_art_style",
                    title = stringResource(R.string.art_style),
                    summary = stringResource(R.string.art_style_summary),
                    options = listOf(
                        "0" to stringResource(R.string.art_style_blur),
                        "1" to stringResource(R.string.art_style_concept),
                    ),
                    defaultValue = "0",
                    dependencyKey = "ls_media_art_enabled",
                )
            }
            item {
                SecureSettingSlider(
                    settingKey = "ls_media_art_blur",
                    title = stringResource(R.string.blur_level),
                    summary = stringResource(R.string.blur_level_summary),
                    min = 0,
                    max = 100,
                    unit = "dp",
                    defaultValue = 0,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PulseVisualizerContent(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                SecureSettingSwitch(
                    settingKey = "visualizer_pulse_enabled",
                    title = stringResource(R.string.enable_pulse),
                    summary = stringResource(R.string.pulse_summary),
                    defaultValue = false,
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
                        "0" to stringResource(R.string.style_bars),
                        "1" to stringResource(R.string.style_fading_blocks),
                        "2" to stringResource(R.string.style_solid_line),
                        "3" to stringResource(R.string.style_center_mirror),
                    ),
                    defaultValue = "0",
                    dependencyKey = "visualizer_pulse_enabled",
                )
            }
            item {
                SecureListPreference(
                    key = "visualizer_pulse_color",
                    title = stringResource(R.string.color_mode),
                    summary = stringResource(R.string.visualizer_color_summary),
                    options =
                        listOf(
                            "lavalamp" to "Lava Lamp",
                            "album" to "Album Art",
                            "accent" to "System Accent",
                        ),
                    defaultValue = "lavalamp",
                    dependencyKey = "visualizer_pulse_enabled",
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
                    enabled = true,
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = "visualizer_pulse_rounded_bars_enabled",
                    title = stringResource(R.string.rounded_bars),
                    summary = stringResource(R.string.rounded_bars_summary),
                    defaultValue = true,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SaturationValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChange: (Float, Float) -> Unit,
) {
    var pickerSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(16.dp))
                .drawWithCache {
                    pickerSize = size
                    val hueColor = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
                    onDrawBehind {
                        drawRect(
                            brush =
                                Brush.horizontalGradient(
                                    colors = listOf(Color.White, Color(hueColor))
                                )
                        )

                        drawRect(
                            brush =
                                Brush.verticalGradient(
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
                    style = Stroke(width = 3.dp.toPx()),
                )

                drawCircle(
                    color = Color.Black.copy(alpha = 0.3f),
                    radius = 10.dp.toPx(),
                    center = Offset(indicatorX, indicatorY),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
    }
}

@Composable
private fun HueSlider(hue: Float, onHueChange: (Float) -> Unit) {
    val hueColors = remember {
        List(361) { h -> Color(AndroidColor.HSVToColor(floatArrayOf(h.toFloat(), 1f, 1f))) }
    }

    Box(
        modifier =
            Modifier.fillMaxWidth()
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
                style = Stroke(width = 3.dp.toPx()),
            )

            drawCircle(
                color = Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))),
                radius = 11.dp.toPx(),
                center = Offset(indicatorX, size.height / 2),
            )
        }
    }
}

@Composable
private fun HexColorInput(
    hexValue: String,
    onHexChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#",
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        BasicTextField(
            value = hexValue,
            onValueChange = { newValue ->
                val filtered =
                    newValue.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(6)
                onHexChange(filtered)
            },
            textStyle =
                LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PresetColorRow(selectedColor: Int, onColorSelected: (Int) -> Unit) {
    val presetColors =
        listOf(
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
            AndroidColor.parseColor("#FFAB40"),
        )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            presetColors.take(8).forEach { color ->
                ColorSwatch(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) },
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            presetColors.drop(8).forEach { color ->
                ColorSwatch(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) },
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Int, isSelected: Boolean, onClick: () -> Unit) {
    val isLightColor = run {
        val r = AndroidColor.red(color)
        val g = AndroidColor.green(color)
        val b = AndroidColor.blue(color)
        (0.299 * r + 0.587 * g + 0.114 * b) > 186
    }

    Box(
        modifier =
            Modifier.size(32.dp)
                .clip(CircleShape)
                .background(Color(color))
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape,
                        )
                    }
                )
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (isLightColor) Color.Black else Color.White,
                modifier = Modifier.size(16.dp),
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
    dependencyKey: String? = null,
) {
    val secureFlow = rememberSettingsFlow(SettingsType.SECURE)
    val color by rememberSettingInt(key, SettingsType.SECURE, defaultValue)
    val enabled =
        if (dependencyKey != null) {
            rememberSecureSettingBoolean(dependencyKey, true)
        } else {
            true
        }

    var showDialog by remember { mutableStateOf(false) }
    val shape = preferenceShape(position)

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable(enabled = enabled) { showDialog = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier =
                Modifier.size(40.dp)
                    .clip(CircleShape)
                    .background(Color(color).copy(alpha = if (enabled) 1f else 0.38f))
                    .border(
                        width = 2.dp,
                        color =
                            MaterialTheme.colorScheme.outline.copy(
                                alpha = if (enabled) 0.3f else 0.12f
                            ),
                        shape = CircleShape,
                    )
        )
    }

    if (showDialog && enabled) {
        ColorPickerDialog(
            initialColor = color,
            onColorSelected = { selectedColor ->
                secureFlow.putInt(key, selectedColor)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val hsv = remember { FloatArray(3) }
    AndroidColor.colorToHSV(initialColor, hsv)

    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var value by remember { mutableFloatStateOf(hsv[2]) }
    var hexInput by remember { mutableStateOf(String.format("%06X", initialColor and 0xFFFFFF)) }

    val currentColor =
        remember(hue, saturation, value) {
            AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
        }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .clip(ExpressiveShapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(24.dp)
        ) {
            Text(
                text = "Choose color",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SaturationValuePicker(
                hue = hue,
                saturation = saturation,
                value = value,
                onSaturationValueChange = { s, v ->
                    saturation = s
                    value = v
                    hexInput =
                        String.format(
                            "%06X",
                            AndroidColor.HSVToColor(floatArrayOf(hue, s, v)) and 0xFFFFFF,
                        )
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            HueSlider(
                hue = hue,
                onHueChange = { h ->
                    hue = h
                    hexInput =
                        String.format(
                            "%06X",
                            AndroidColor.HSVToColor(floatArrayOf(h, saturation, value)) and 0xFFFFFF,
                        )
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier.size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(currentColor))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp),
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
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Presets",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                },
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onColorSelected(currentColor) }) {
                    Text(text = "Select", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun AodContent(modifier: Modifier = Modifier) {
    val secureFlow = rememberSettingsFlow(SettingsType.SECURE)
    val scheduleMode by rememberSettingString("aod_schedule_mode", SettingsType.SECURE, "0")
    val screenOffAnimation by rememberSettingInt("screen_off_aod_animation", SettingsType.SYSTEM, 1)
    val screenOffAodEnabled by rememberSettingInt("screen_off_aod_enabled", SettingsType.SYSTEM, 0)

    val tapSupported = SystemProperties.getBoolean(
        "persist.sys.ax_doze_tap_pulse_supported", false)
    val doubleTapSupported = SystemProperties.getBoolean(
        "persist.sys.ax_doze_double_tap_pulse_supported", false)
    val pickupSupported = SystemProperties.getBoolean(
        "persist.sys.ax_doze_pickup_pulse_supported", false)
    val sideFpsSupported = SystemProperties.getBoolean(
        "persist.sys.ax_doze_side_fps_pulse_supported", false)

    LaunchedEffect(scheduleMode) {
        val modeInt = scheduleMode.toIntOrNull() ?: 0
        val shouldEnableDoze = modeInt != 0
        secureFlow.putInt(Settings.Secure.DOZE_ALWAYS_ON, if (shouldEnableDoze) 1 else 0)
    }

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.mode)) {
            item {
                SecureListPreference(
                    key = "aod_schedule_mode",
                    title = stringResource(R.string.aod_schedule),
                    summary =
                        when (scheduleMode) {
                            "0" -> stringResource(R.string.disabled)
                            "1" -> stringResource(R.string.always_on)
                            "2" -> stringResource(R.string.aod_charge_only)
                            "3" -> stringResource(R.string.scheduled)
                            "4" -> stringResource(R.string.aod_scheduled_charge)
                            else -> stringResource(R.string.disabled)
                        },
                    options =
                        listOf(
                            "0" to stringResource(R.string.disabled),
                            "1" to stringResource(R.string.always_on),
                            "2" to stringResource(R.string.aod_charge_only),
                            "3" to stringResource(R.string.scheduled),
                            "4" to stringResource(R.string.aod_scheduled_charge),
                        ),
                    defaultValue = "0",
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
                    defaultValue = true,
                )
            }
        }

        AnimatedVisibility(
            visible = screenOffAnimation == 1,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                PreferenceGroup(title = stringResource(R.string.screen_off)) {
                    item {
                        SystemSettingSwitch(
                            settingKey = "screen_off_aod_enabled",
                            title = stringResource(R.string.screen_off_aod),
                            summary = stringResource(R.string.screen_off_aod_summary),
                            defaultValue = false,
                        )
                    }
                    if (screenOffAodEnabled == 1) {
                        item {
                            SystemSettingSlider(
                                settingKey = "screen_off_aod_duration",
                                title = stringResource(R.string.screen_off_aod_duration),
                                summary = stringResource(R.string.screen_off_aod_duration_summary),
                                min = 1000,
                                max = 10000,
                                interval = 500,
                                defaultValue = 4000,
                                formatValue = { "${it / 1000f}s" },
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = scheduleMode == "3" || scheduleMode == "4",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                PreferenceGroup(title = stringResource(R.string.schedule)) {
                    item {
                        SecureTimePreference(
                            key = "aod_schedule_start_time",
                            title = stringResource(R.string.start_time),
                            summary = stringResource(R.string.start_time_summary),
                            defaultValue = "2300",
                        )
                    }
                    item {
                        SecureTimePreference(
                            key = "aod_schedule_end_time",
                            title = stringResource(R.string.end_time),
                            summary = stringResource(R.string.end_time_summary),
                            defaultValue = "0700",
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroup(title = stringResource(R.string.doze_pulse_gestures)) {
            item {
                SecureSettingSwitch(
                    settingKey = "ax_doze_notification_pulse",
                    title = stringResource(R.string.doze_pulse_notification),
                    summary = stringResource(R.string.doze_pulse_notification_summary),
                    icon = Icons.Filled.Notifications,
                    defaultValue = false,
                )
            }
            if (tapSupported) {
                item {
                    SecureSettingSwitch(
                        settingKey = "ax_doze_tap_pulse",
                        title = stringResource(R.string.doze_pulse_tap),
                        summary = stringResource(R.string.doze_pulse_tap_summary),
                        icon = Icons.Filled.TouchApp,
                        defaultValue = false,
                    )
                }
            }
            if (doubleTapSupported) {
                item {
                    SecureSettingSwitch(
                        settingKey = "ax_doze_double_tap_pulse",
                        title = stringResource(R.string.doze_pulse_double_tap),
                        summary = stringResource(R.string.doze_pulse_double_tap_summary),
                        icon = Icons.Filled.TouchApp,
                        defaultValue = false,
                    )
                }
            }
            if (pickupSupported) {
                item {
                    SecureSettingSwitch(
                        settingKey = "ax_doze_pickup_pulse",
                        title = stringResource(R.string.doze_pulse_pickup),
                        summary = stringResource(R.string.doze_pulse_pickup_summary),
                        defaultValue = false,
                    )
                }
            }
            if (sideFpsSupported) {
                item {
                    SecureSettingSwitch(
                        settingKey = "ax_doze_side_fps_pulse",
                        title = stringResource(R.string.doze_pulse_side_fps),
                        summary = stringResource(R.string.doze_pulse_side_fps_summary),
                        icon = Icons.Filled.Fingerprint,
                        defaultValue = false,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
