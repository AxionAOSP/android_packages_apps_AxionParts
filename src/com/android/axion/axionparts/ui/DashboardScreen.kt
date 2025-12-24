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

package com.android.axion.axionparts.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.android.axion.axionparts.ui.components.BottomNavBar
import com.android.axion.axionparts.ui.components.NavItem
import com.android.axion.axionparts.ui.components.saveEssentialApps
import com.android.axion.axionparts.ui.screens.AppPickerScreen
import com.android.axion.axionparts.ui.screens.CustomizeContent
import com.android.axion.axionparts.ui.screens.EssentialsContent
import com.android.axion.axionparts.ui.screens.LockscreenFeaturesScreen
import com.android.axion.axionparts.ui.screens.SoundFeaturesScreen
import com.android.axion.axionparts.ui.screens.UIFeaturesScreen
import com.android.axion.axionparts.ui.screens.MultitaskingContent
import com.android.axion.axionparts.ui.screens.PerformanceContent

val navItems = listOf(
    NavItem(
        route = "customize",
        label = "Customize",
        icon = Icons.Filled.Palette,
        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    ),
    NavItem(
        route = "essentials",
        label = "Essentials",
        icon = Icons.Filled.Diamond,
        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
    ),
    NavItem(
        route = "performance",
        label = "Performance",
        icon = Icons.Filled.Bolt,
        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
    ),
    NavItem(
        route = "multitasking",
        label = "Multitasking",
        icon = Icons.Filled.Splitscreen,
        gradientColors = listOf(Color(0xFF0891B2), Color(0xFF06B6D4))
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var selectedRoute by rememberSaveable { mutableStateOf(navItems[0].route) }
    var previousIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAppPicker by rememberSaveable { mutableStateOf(false) }
    var appPickerSelectedApps by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var showLockscreenFeatures by rememberSaveable { mutableStateOf(false) }
    var showUIFeatures by rememberSaveable { mutableStateOf(false) }
    var showSoundFeatures by rememberSaveable { mutableStateOf(false) }
    
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    
    val currentIndex = navItems.indexOfFirst { it.route == selectedRoute }
    val isNavigatingForward = currentIndex >= previousIndex
    
    fun onNavSelected(route: String) {
        previousIndex = navItems.indexOfFirst { it.route == selectedRoute }
        selectedRoute = route
    }
    
    
    
    val currentTitle = navItems.find { it.route == selectedRoute }?.label ?: "Personalizations"
    
    
    if (showAppPicker) {
        BackHandler { showAppPicker = false }
        AppPickerScreen(
            title = "Select Essential Apps",
            selectedApps = appPickerSelectedApps,
            onBackClick = { showAppPicker = false },
            onAppsSelected = { apps ->
                saveEssentialApps(contentResolver, apps)
                showAppPicker = false
            }
        )
        return
    }
    
    
    val currentFeatureScreen = when {
        showUIFeatures -> "ui_features"
        showLockscreenFeatures -> "lockscreen"
        showSoundFeatures -> "sound"
        else -> "none"
    }
    
    AnimatedContent(
        targetState = currentFeatureScreen,
        transitionSpec = {
            if (targetState != "none") {
                
                (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))).togetherWith(
                    slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300))
                )
            } else {
                
                (slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300))).togetherWith(
                    slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
                )
            }
        },
        label = "featureScreenTransition"
    ) { featureScreen ->
        when (featureScreen) {
            "lockscreen" -> {
                BackHandler { showLockscreenFeatures = false }
                LockscreenFeaturesScreen(
                    onBackClick = { showLockscreenFeatures = false }
                )
            }
            "ui_features" -> {
                BackHandler { showUIFeatures = false }
                UIFeaturesScreen(
                    onBackClick = { showUIFeatures = false }
                )
            }
            "sound" -> {
                BackHandler { showSoundFeatures = false }
                SoundFeaturesScreen(
                    onBackClick = { showSoundFeatures = false }
                )
            }
            else -> {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            AnimatedContent(
                                targetState = currentTitle,
                                transitionSpec = {
                                    (fadeIn(tween(200)) + scaleIn(
                                        initialScale = 0.92f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )).togetherWith(
                                        fadeOut(tween(150)) + scaleOut(targetScale = 0.92f)
                                    )
                                },
                                label = "titleAnimation"
                            ) { title ->
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        )
                    )
                },
                bottomBar = {
                    BottomNavBar(
                        items = navItems,
                        selectedRoute = selectedRoute,
                        onItemSelected = { route -> onNavSelected(route) }
                    )
                }
            ) { innerPadding ->
                
                AnimatedContent(
                    targetState = selectedRoute,
                    transitionSpec = {
                        val slideDirection = if (isNavigatingForward) 1 else -1
                        
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> slideDirection * fullWidth / 4 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(
                            animationSpec = tween(250)
                        )).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -slideDirection * fullWidth / 4 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + fadeOut(
                                animationSpec = tween(200)
                            )
                        )
                    },
                    label = "screenTransition"
                ) { route ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (route) {
                            "customize" -> CustomizeContent(
                                onNavigateToLockscreen = { showLockscreenFeatures = true },
                                onNavigateToUIFeatures = { showUIFeatures = true },
                                onNavigateToSound = { showSoundFeatures = true }
                            )
                            "essentials" -> EssentialsContent(
                                onNavigateToAppPicker = { selectedApps ->
                                    appPickerSelectedApps = selectedApps
                                    showAppPicker = true
                                }
                            )
                            "performance" -> PerformanceContent()
                            "multitasking" -> MultitaskingContent()
                        }
                    }
                }
            }
            }
        }
    }
}
