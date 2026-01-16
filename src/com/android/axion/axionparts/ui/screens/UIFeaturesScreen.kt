package com.android.axion.axionparts.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import androidx.compose.material.icons.filled.Tune
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.theme.ExpressiveShapes
import com.android.axion.compose.preferences.*

private enum class UIFeaturesSubScreen {
    MAIN,
    QUICK_SETTINGS,
    STATUS_BAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UIFeaturesScreen(
    onBackClick: () -> Unit
) {
    var currentScreen by rememberSaveable { mutableStateOf(UIFeaturesSubScreen.MAIN) }
    
    val screenTitle = when (currentScreen) {
        UIFeaturesSubScreen.MAIN -> stringResource(R.string.user_interface)
        UIFeaturesSubScreen.QUICK_SETTINGS -> stringResource(R.string.quick_settings)
        UIFeaturesSubScreen.STATUS_BAR -> stringResource(R.string.status_bar)
    }
    
    val handleBack: () -> Unit = {
        if (currentScreen == UIFeaturesSubScreen.MAIN) {
            onBackClick()
        } else {
            currentScreen = UIFeaturesSubScreen.MAIN
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
                val isNavigatingForward = targetState != UIFeaturesSubScreen.MAIN
                
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
                UIFeaturesSubScreen.MAIN -> UIFeaturesMainContent(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToQuickSettings = { currentScreen = UIFeaturesSubScreen.QUICK_SETTINGS },
                    onNavigateToStatusBar = { currentScreen = UIFeaturesSubScreen.STATUS_BAR }
                )
                UIFeaturesSubScreen.QUICK_SETTINGS -> QuickSettingsContent(
                    modifier = Modifier.padding(innerPadding)
                )
                UIFeaturesSubScreen.STATUS_BAR -> StatusBarContent(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun UIFeaturesMainContent(
    modifier: Modifier = Modifier,
    onNavigateToQuickSettings: () -> Unit,
    onNavigateToStatusBar: () -> Unit
) {
    val primaryColor = colorResource(id = android.R.color.system_accent1_600)
    val tertiaryColor = colorResource(id = android.R.color.system_accent3_600)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        
        AnimatedFeatureCard(
            title = stringResource(R.string.quick_settings),
            description = stringResource(R.string.quick_settings_description),
            illustrationBackground = primaryColor,
            onClick = onNavigateToQuickSettings
        ) {
            QuickSettingsIllustration()
        }
        
        AnimatedFeatureCard(
            title = stringResource(R.string.status_bar),
            description = stringResource(R.string.status_bar_description),
            illustrationBackground = tertiaryColor,
            onClick = onNavigateToStatusBar
        ) {
            StatusBarIllustration()
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
private fun QuickSettingsIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "qs")
    
    
    val sliderPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sliderPos"
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
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(phoneLeft + 3.dp.toPx(), phoneTop + 6.dp.toPx()),
            size = Size(phoneWidth - 6.dp.toPx(), phoneHeight - 12.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadius - 2.dp.toPx())
        )
        
        
        val sliderWidth = phoneWidth - 12.dp.toPx()
        val sliderHeight = 6.dp.toPx()
        val sliderLeft = phoneLeft + 6.dp.toPx()
        
        
        val sliderTopY = phoneTop + 10.dp.toPx()
        val sliderBottomY = phoneTop + phoneHeight - 16.dp.toPx()
        val sliderY = sliderTopY + (sliderBottomY - sliderTopY) * sliderPosition
        
        
        val tileSize = 8.dp.toPx()
        val tileSpacing = 3.dp.toPx()
        val tilesStartX = phoneLeft + 6.dp.toPx()
        
        
        val tilesTopY = phoneTop + 18.dp.toPx()
        val tilesBottomY = phoneTop + phoneHeight - 32.dp.toPx()
        
        val tilesY = tilesTopY + (tilesBottomY - tilesTopY) * (1f - sliderPosition)
        
        
        for (row in 0 until 2) {
            for (col in 0 until 2) {
                val tileX = tilesStartX + col * (tileSize + tileSpacing)
                val tileY = tilesY + row * (tileSize + tileSpacing)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.7f + (row + col) * 0.05f),
                    topLeft = Offset(tileX, tileY),
                    size = Size(tileSize, tileSize),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(sliderLeft, sliderY),
            size = Size(sliderWidth, sliderHeight),
            cornerRadius = CornerRadius(sliderHeight / 2)
        )
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(sliderLeft, sliderY),
            size = Size(sliderWidth * 0.6f, sliderHeight),
            cornerRadius = CornerRadius(sliderHeight / 2)
        )
        
        
        val thumbX = sliderLeft + (sliderWidth * 0.6f)
        val thumbY = sliderY + sliderHeight / 2
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(thumbX, thumbY)
        )
    }
}


@Composable
private fun QuickSettingsIllustrationLarge() {
    val infiniteTransition = rememberInfiniteTransition(label = "qsLarge")
    
    
    val sliderPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sliderPos"
    )
    
    Canvas(modifier = Modifier.size(100.dp, 140.dp)) {
        val phoneWidth = size.width * 0.75f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 16.dp.toPx()
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 3.dp.toPx())
        )
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.15f),
            topLeft = Offset(phoneLeft + 4.dp.toPx(), phoneTop + 8.dp.toPx()),
            size = Size(phoneWidth - 8.dp.toPx(), phoneHeight * 0.7f),
            cornerRadius = CornerRadius(12.dp.toPx())
        )
        
        
        val sliderWidth = phoneWidth - 16.dp.toPx()
        val sliderHeight = 10.dp.toPx()
        val sliderLeft = phoneLeft + 8.dp.toPx()
        
        val sliderTopY = phoneTop + 14.dp.toPx()
        val sliderBottomY = phoneTop + phoneHeight - 35.dp.toPx()
        val sliderY = sliderTopY + (sliderBottomY - sliderTopY) * sliderPosition
        
        
        val tileSize = 14.dp.toPx()
        val tileSpacing = 6.dp.toPx()
        val tilesStartX = phoneLeft + 10.dp.toPx()
        
        val tilesTopY = phoneTop + 28.dp.toPx()
        val tilesBottomY = phoneTop + phoneHeight - 75.dp.toPx()
        
        val tilesY = tilesTopY + (tilesBottomY - tilesTopY) * (1f - sliderPosition)
        
        
        for (row in 0 until 2) {
            for (col in 0 until 3) {
                val tileX = tilesStartX + col * (tileSize + tileSpacing)
                val tileY = tilesY + row * (tileSize + tileSpacing)
                val alpha = 0.6f + ((row + col) % 3) * 0.15f
                drawRoundRect(
                    color = Color.White.copy(alpha = alpha),
                    topLeft = Offset(tileX, tileY),
                    size = Size(tileSize, tileSize),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(sliderLeft, sliderY),
            size = Size(sliderWidth, sliderHeight),
            cornerRadius = CornerRadius(sliderHeight / 2)
        )
        
        
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(sliderLeft, sliderY),
            size = Size(sliderWidth * 0.65f, sliderHeight),
            cornerRadius = CornerRadius(sliderHeight / 2)
        )
        
        
        drawCircle(
            color = Color.White,
            radius = 7.dp.toPx(),
            center = Offset(sliderLeft + sliderWidth * 0.65f, sliderY + sliderHeight / 2)
        )
    }
}



@Composable
private fun StatusBarIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "statusBar")
    
    val iconBlink by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )
    
    val batteryLevel by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "battery"
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
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(phoneLeft + 3.dp.toPx(), phoneTop + 6.dp.toPx()),
            size = Size(phoneWidth - 6.dp.toPx(), phoneHeight - 12.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadius - 2.dp.toPx())
        )
        
        
        val statusBarTop = phoneTop + 8.dp.toPx()
        val statusBarHeight = 6.dp.toPx()
        
        
        val clockX = phoneLeft + 5.dp.toPx()
        val dotSize = 1.5.dp.toPx()
        val dotSpacing = 2.dp.toPx()
        
        for (i in 0 until 4) {
            val xOffset = clockX + i * dotSpacing
            drawRoundRect(
                color = Color.White.copy(alpha = iconBlink),
                topLeft = Offset(xOffset, statusBarTop + 1.dp.toPx()),
                size = Size(dotSize, statusBarHeight - 2.dp.toPx()),
                cornerRadius = CornerRadius(0.5.dp.toPx())
            )
        }
        
        drawCircle(
            color = Color.White.copy(alpha = iconBlink),
            radius = 0.5.dp.toPx(),
            center = Offset(clockX + 2 * dotSpacing - 0.5.dp.toPx(), statusBarTop + 2.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = iconBlink),
            radius = 0.5.dp.toPx(),
            center = Offset(clockX + 2 * dotSpacing - 0.5.dp.toPx(), statusBarTop + 4.dp.toPx())
        )
        
        
        val batteryWidth = 8.dp.toPx()
        val batteryHeight = statusBarHeight
        val batteryX = phoneLeft + phoneWidth - 12.dp.toPx()
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(batteryX, statusBarTop),
            size = Size(batteryWidth, batteryHeight),
            cornerRadius = CornerRadius(1.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
        drawRoundRect(
            color = Color.White.copy(alpha = iconBlink),
            topLeft = Offset(batteryX + 1.dp.toPx(), statusBarTop + 1.dp.toPx()),
            size = Size((batteryWidth - 2.dp.toPx()) * batteryLevel, batteryHeight - 2.dp.toPx()),
            cornerRadius = CornerRadius(0.5.dp.toPx())
        )
        
        
        val barWidth = 1.5.dp.toPx()
        val barSpacing = 2.dp.toPx()
        val signalLeft = batteryX - 10.dp.toPx()
        
        for (i in 0 until 4) {
            val barHeight = (2 + i * 1.2f).dp.toPx()
            drawRoundRect(
                color = Color.White.copy(alpha = iconBlink),
                topLeft = Offset(signalLeft + i * barSpacing, statusBarTop + statusBarHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(0.5.dp.toPx())
            )
        }
    }
}


@Composable
private fun StatusBarIllustrationLarge() {
    val infiniteTransition = rememberInfiniteTransition(label = "statusBarLarge")
    
    val iconBlink by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )
    
    val batteryLevel by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "battery"
    )
    
    Canvas(modifier = Modifier.size(100.dp, 140.dp)) {
        val phoneWidth = size.width * 0.75f
        val phoneHeight = size.height * 0.9f
        val phoneLeft = (size.width - phoneWidth) / 2
        val phoneTop = (size.height - phoneHeight) / 2
        val cornerRadius = 16.dp.toPx()
        
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = 3.dp.toPx())
        )
        
        
        val screenLeft = phoneLeft + 5.dp.toPx()
        val screenTop = phoneTop + 10.dp.toPx()
        val screenWidth = phoneWidth - 10.dp.toPx()
        val screenHeight = phoneHeight - 20.dp.toPx()
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(screenLeft, screenTop),
            size = Size(screenWidth, screenHeight),
            cornerRadius = CornerRadius(cornerRadius - 5.dp.toPx())
        )
        
        
        val statusBarTop = screenTop + 4.dp.toPx()
        val statusBarHeight = 7.dp.toPx()
        
        
        val clockX = screenLeft + 6.dp.toPx()
        val digitWidth = 2.dp.toPx()
        val digitSpacing = 3.dp.toPx()
        
        for (i in 0 until 4) {
            val xOffset = clockX + i * digitSpacing
            
            val colonOffset = if (i >= 2) 1.5.dp.toPx() else 0f
            drawRoundRect(
                color = Color.White.copy(alpha = iconBlink),
                topLeft = Offset(xOffset + colonOffset, statusBarTop + 1.dp.toPx()),
                size = Size(digitWidth, statusBarHeight - 2.dp.toPx()),
                cornerRadius = CornerRadius(0.5.dp.toPx())
            )
        }
        
        val colonX = clockX + 2 * digitSpacing - 0.5.dp.toPx()
        drawCircle(
            color = Color.White.copy(alpha = iconBlink),
            radius = 0.8.dp.toPx(),
            center = Offset(colonX, statusBarTop + 2.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = iconBlink),
            radius = 0.8.dp.toPx(),
            center = Offset(colonX, statusBarTop + 5.dp.toPx())
        )
        
        
        val batteryWidth = 10.dp.toPx()
        val batteryHeight = statusBarHeight - 1.dp.toPx()
        val batteryX = screenLeft + screenWidth - 16.dp.toPx()
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(batteryX, statusBarTop + 0.5.dp.toPx()),
            size = Size(batteryWidth, batteryHeight),
            cornerRadius = CornerRadius(1.5.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
        drawRoundRect(
            color = Color.White.copy(alpha = iconBlink),
            topLeft = Offset(batteryX + 1.5.dp.toPx(), statusBarTop + 2.dp.toPx()),
            size = Size((batteryWidth - 3.dp.toPx()) * batteryLevel, batteryHeight - 3.dp.toPx()),
            cornerRadius = CornerRadius(0.5.dp.toPx())
        )
        
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(batteryX + batteryWidth + 0.5.dp.toPx(), statusBarTop + 2.dp.toPx()),
            size = Size(1.5.dp.toPx(), batteryHeight - 3.dp.toPx()),
            cornerRadius = CornerRadius(0.5.dp.toPx())
        )
        
        
        val barWidth = 2.dp.toPx()
        val barSpacing = 2.5.dp.toPx()
        val signalLeft = batteryX - 14.dp.toPx()
        
        for (i in 0 until 4) {
            val barHeight = (2 + i * 1.5f).dp.toPx()
            drawRoundRect(
                color = Color.White.copy(alpha = iconBlink),
                topLeft = Offset(signalLeft + i * barSpacing, statusBarTop + statusBarHeight - barHeight - 0.5.dp.toPx()),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(0.5.dp.toPx())
            )
        }
    }
}

@Composable
fun QuickSettingsContent(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        
        FeatureIllustrationHeader(
            illustrationBackground = primaryColor
        ) {
            QuickSettingsIllustrationLarge()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        
        PreferenceGroup(title = stringResource(R.string.brightness_slider)) {
            item {
                SecureListPreference(
                    key = "qs_brightness_slider_enabled",
                    title = stringResource(R.string.brightness_slider),
                    summary = stringResource(R.string.brightness_slider_summary),
                    options = listOf(
                        "0" to stringResource(R.string.brightness_hidden),
                        "1" to stringResource(R.string.brightness_show_expanded),
                        "2" to stringResource(R.string.brightness_always_visible)
                    ),
                    defaultValue = "2"
                )
            }
            item {
                SecureListPreference(
                    key = "qs_brightness_slider_top",
                    title = stringResource(R.string.brightness_slider_position),
                    summary = stringResource(R.string.brightness_slider_position_summary),
                    options = listOf(
                        "0" to stringResource(R.string.position_bottom),
                        "1" to stringResource(R.string.position_top)
                    ),
                    defaultValue = "0"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatusBarContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        
        FeatureIllustrationHeader(
            illustrationBackground = tertiaryColor
        ) {
            StatusBarIllustrationLarge()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PreferenceGroup(title = stringResource(R.string.status_bar_icons)) {
            item {
                ClickablePreference(
                    title = stringResource(R.string.status_bar_tuner),
                    summary = stringResource(R.string.status_bar_tuner_summary),
                    icon = Icons.Default.Tune,
                    showExternalIcon = true,
                    onClick = {
                        val intent = Intent("com.android.settings.action.STATUS_BAR_TUNER").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
