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

package com.android.axion.axionparts.ui.components

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.android.axion.compose.preferences.ExpressiveSwitch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.axion.axionparts.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val GridCardShape = RoundedCornerShape(24.dp)
private val BarShape = RoundedCornerShape(3.5.dp)
private val IconGridShape = RoundedCornerShape(8.dp)

@Composable
fun PersonalizeGrid(
    onNavigateToThemes: () -> Unit,
    onNavigateToLockscreen: () -> Unit,
    onNavigateToUIFeatures: () -> Unit,
    onNavigateToSound: () -> Unit,
    onNavigateToGestures: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemesGridCard(
                onClick = onNavigateToThemes,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LockscreenGridCard(
                    onClick = onNavigateToLockscreen,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                UIFeaturesGridCard(
                    onClick = onNavigateToUIFeatures,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(130.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SoundGridCard(
                onClick = onNavigateToSound,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            GesturesGridCard(
                onClick = onNavigateToGestures,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

private suspend fun downscaleWallpaper(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): ImageBitmap {
    return withContext(Dispatchers.Default) {
        val scale =
            minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height, 1f)
        val scaledWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true).asImageBitmap()
    }
}

@Composable
private fun ThemesGridCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var wallpaperImage by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(Unit) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val drawable = wallpaperManager.drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
        if (bitmap != null) {

            val maxW = with(density) { 400.dp.roundToPx() }
            val maxH = with(density) { 560.dp.roundToPx() }
            wallpaperImage = downscaleWallpaper(bitmap, maxW, maxH)
        }
    }

    val fallbackColors =
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        )

    GridCard(onClick = onClick, modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            val img = wallpaperImage
            if (img != null) {
                Image(
                    bitmap = img,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier =
                        Modifier.fillMaxSize().background(Brush.linearGradient(fallbackColors))
                )
            }

            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
            )

            Text(
                text = stringResource(R.string.themes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            )
        }
    }
}

@Composable
private fun LockscreenGridCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val accentColor = remember { Color(context.getColor(android.R.color.system_accent1_100)) }
    val infiniteTransition = rememberInfiniteTransition(label = "lockscreenCard")

    val colonAlpha by
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "colonPulse",
        )

    val edgeGlow by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "edgeGlow",
        )

    val strokeWidthPx = with(LocalDensity.current) { 3.dp.toPx() }

    GridCard(onClick = onClick, modifier = modifier, containerColor = Color.Black) {
        Box(
            modifier =
                Modifier.fillMaxSize().drawBehind {
                    val glowColor = accentColor.copy(alpha = edgeGlow * 0.3f)
                    val brush =
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, glowColor, Color.Transparent)
                        )
                    drawLine(
                        brush = brush,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = strokeWidthPx,
                    )
                    drawLine(
                        brush = brush,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidthPx,
                    )
                }
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "12",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 36.sp,
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor.copy(alpha = colonAlpha),
                            lineHeight = 36.sp,
                        )
                        Text(
                            text = "30",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            lineHeight = 36.sp,
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.lockscreen),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun UIFeaturesGridCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    data class IconItem(val icon: ImageVector, val bg: Color, val tint: Color)
    val iconItems =
        remember(colors) {
            listOf(
                IconItem(Icons.Filled.Palette, colors.primaryContainer, colors.onPrimaryContainer),
                IconItem(
                    Icons.Filled.LightMode,
                    colors.tertiaryContainer,
                    colors.onTertiaryContainer,
                ),
                IconItem(Icons.Filled.Tune, colors.secondaryContainer, colors.onSecondaryContainer),
                IconItem(
                    Icons.Filled.SignalCellularAlt,
                    colors.primaryContainer,
                    colors.onPrimaryContainer,
                ),
                IconItem(Icons.Filled.DarkMode, colors.surfaceContainerHighest, colors.onSurface),
                IconItem(
                    Icons.Filled.BrightnessAuto,
                    colors.tertiaryContainer,
                    colors.onTertiaryContainer,
                ),
            )
        }

    GridCard(onClick = onClick, modifier = modifier, containerColor = colors.surfaceBright) {
        Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    iconItems.subList(0, 3).forEach { item ->
                        Box(
                            modifier = Modifier.size(30.dp).clip(IconGridShape).background(item.bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.tint,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    iconItems.subList(3, 6).forEach { item ->
                        Box(
                            modifier = Modifier.size(30.dp).clip(IconGridShape).background(item.bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.tint,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ui_features),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SoundGridCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "soundCard")

    val baseHeights = remember { floatArrayOf(0.3f, 0.5f, 0.8f, 1f, 0.7f, 0.9f, 0.5f, 0.3f) }
    val animatedHeights =
        baseHeights.mapIndexed { index, base ->
            infiniteTransition.animateFloat(
                initialValue = base * 0.4f,
                targetValue = base,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(durationMillis = 500 + (index * 70), easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "bar$index",
            )
        }

    val barColors =
        remember(colors) {
            listOf(
                colors.primary,
                colors.primary.copy(alpha = 0.85f),
                colors.secondary,
                colors.secondary.copy(alpha = 0.85f),
                colors.tertiary,
                colors.tertiary.copy(alpha = 0.85f),
                colors.primary.copy(alpha = 0.7f),
                colors.tertiary.copy(alpha = 0.7f),
            )
        }

    val barWidthPx = with(LocalDensity.current) { 7.dp.toPx() }
    val barSpacingPx = with(LocalDensity.current) { 4.dp.toPx() }
    val maxBarHeightPx = with(LocalDensity.current) { 44.dp.toPx() }
    val barCornerPx = with(LocalDensity.current) { 3.5.dp.toPx() }

    GridCard(onClick = onClick, modifier = modifier, containerColor = colors.surfaceBright) {
        Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Canvas(
                modifier =
                    Modifier.align(Alignment.Center)
                        .width(
                            ((barWidthPx * 8 + barSpacingPx * 7) / LocalDensity.current.density).dp
                        )
                        .height(44.dp)
            ) {
                val totalWidth = barWidthPx * 8 + barSpacingPx * 7
                val startX = (size.width - totalWidth) / 2f
                val centerY = size.height / 2f

                animatedHeights.forEachIndexed { index, heightState ->
                    val h = maxBarHeightPx * heightState.value
                    val x = startX + index * (barWidthPx + barSpacingPx)
                    val alpha = 0.5f + heightState.value * 0.5f
                    val color = barColors[index].copy(alpha = alpha)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, centerY - h / 2f),
                        size = Size(barWidthPx, h),
                        cornerRadius = CornerRadius(barCornerPx, barCornerPx),
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.sound),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun GesturesGridCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    GridCard(onClick = onClick, modifier = modifier, containerColor = colors.surfaceBright) {
        Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier =
                        Modifier.size(40.dp).clip(CircleShape).background(colors.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Gesture,
                        contentDescription = null,
                        tint = colors.onTertiaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Box(
                    modifier =
                        Modifier.size(40.dp).clip(CircleShape).background(colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.TouchApp,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.gestures),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun GridCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceBright,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
            label = "scale",
        )

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(GridCardShape)
                .background(containerColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
    ) {
        content()
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    illustrationColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceBright,
        onClick = onClick,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(illustrationColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SwitchFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    illustrationColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceBright,
        onClick = { onCheckedChange(!checked) },
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (checked) illustrationColor
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) iconTint else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (checked) "On" else "Off",
                    style = MaterialTheme.typography.labelMedium,
                    color =
                        if (checked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ExpressiveSwitch(checked = checked, onCheckedChange = onCheckedChange)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IllustratedCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    illustration: @Composable BoxScope.() -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.motionScheme
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = motionScheme.defaultSpatialSpec(),
            label = "scale",
        )

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surfaceBright)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onClick,
                )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier =
                    Modifier.size(100.dp).clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
                content = illustration,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VisualCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    illustration: @Composable BoxScope.() -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.motionScheme
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = motionScheme.defaultSpatialSpec(),
            label = "scale",
        )

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surfaceBright)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onClick,
                )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(14.dp),
                contentAlignment = Alignment.Center,
                content = illustration,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.motionScheme
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = motionScheme.defaultSpatialSpec(),
            label = "scale",
        )

    Box(
        modifier =
            modifier
                .fillMaxHeight()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surfaceBright)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onSurface,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 12.sp),
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
