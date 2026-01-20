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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.*
import com.android.axion.axionparts.ui.screens.*

@Composable
fun getNavItems() = listOf(
    NavItem(
        route = "customize",
        label = stringResource(R.string.customize),
        icon = Icons.Filled.Palette,
        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
    ),
    NavItem(
        route = "essentials",
        label = stringResource(R.string.essentials),
        icon = Icons.Filled.Diamond,
        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFFA855F7))
    ),
    NavItem(
        route = "performance",
        label = stringResource(R.string.performance),
        icon = Icons.Filled.Bolt,
        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
    ),
    NavItem(
        route = "multitasking",
        label = stringResource(R.string.multitasking),
        icon = Icons.Filled.Splitscreen,
        gradientColors = listOf(Color(0xFF0891B2), Color(0xFF06B6D4))
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val navItems = getNavItems()
    val windowSizeClass = rememberWindowSizeClass()
    val isExpandedLayout = windowSizeClass == WindowSizeClass.EXPANDED || 
                           windowSizeClass == WindowSizeClass.MEDIUM
    
    var selectedRoute by rememberSaveable { mutableStateOf(navItems[0].route) }
    var previousIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAppPicker by rememberSaveable { mutableStateOf(false) }
    var appPickerSelectedApps by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var currentDetailScreen by rememberSaveable { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    
    val currentIndex = navItems.indexOfFirst { it.route == selectedRoute }
    val isNavigatingForward = currentIndex >= previousIndex
    
    fun onNavSelected(route: String) {
        previousIndex = navItems.indexOfFirst { it.route == selectedRoute }
        selectedRoute = route
        if (!isExpandedLayout) {
            currentDetailScreen = null
        }
    }
    
    fun navigateToDetail(screen: String) {
        currentDetailScreen = screen
    }
    
    fun closeDetail() {
        currentDetailScreen = null
    }
    
    val currentTitle = navItems.find { it.route == selectedRoute }?.label ?: stringResource(R.string.personalizations)
    
    if (showAppPicker) {
        BackHandler { showAppPicker = false }
        AppPickerScreen(
            title = stringResource(R.string.select_essential_apps),
            selectedApps = appPickerSelectedApps,
            onBackClick = { showAppPicker = false },
            onAppsSelected = { apps ->
                saveEssentialApps(contentResolver, apps)
                showAppPicker = false
            }
        )
        return
    }
    
    if (isExpandedLayout) {
        TwoPaneLayout(
            windowSizeClass = windowSizeClass,
            listPane = {
                ListPaneContent(
                    navItems = navItems,
                    selectedRoute = selectedRoute,
                    currentTitle = currentTitle,
                    isNavigatingForward = isNavigatingForward,
                    onNavSelected = { onNavSelected(it) },
                    onNavigateToDetail = { navigateToDetail(it) },
                    onNavigateToAppPicker = { selectedApps ->
                        appPickerSelectedApps = selectedApps
                        showAppPicker = true
                    }
                )
            },
            detailPane = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    if (currentDetailScreen != null) {
                        DetailPaneContent(
                            screen = currentDetailScreen!!,
                            onClose = { closeDetail() }
                        )
                    } else {
                        EmptyDetailPane()
                    }
                }
            },
            showDetailPane = currentDetailScreen != null,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        AnimatedContent(
            targetState = currentDetailScreen,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))).togetherWith(
                        slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300))
                    )
                } else {
                    (slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300))).togetherWith(
                        slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
                    )
                }
            },
            label = "detailTransition"
        ) { detailScreen ->
            if (detailScreen != null) {
                BackHandler { closeDetail() }
                DetailScreen(
                    screen = detailScreen,
                    onBackClick = { closeDetail() }
                )
            } else {
                ListPaneContent(
                    navItems = navItems,
                    selectedRoute = selectedRoute,
                    currentTitle = currentTitle,
                    isNavigatingForward = isNavigatingForward,
                    onNavSelected = { onNavSelected(it) },
                    onNavigateToDetail = { navigateToDetail(it) },
                    onNavigateToAppPicker = { selectedApps ->
                        appPickerSelectedApps = selectedApps
                        showAppPicker = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListPaneContent(
    navItems: List<NavItem>,
    selectedRoute: String,
    currentTitle: String,
    isNavigatingForward: Boolean,
    onNavSelected: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAppPicker: (Set<String>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                label = "screenTransition",
                modifier = Modifier.weight(1f)
            ) { route ->
                when (route) {
                    "customize" -> CustomizeContent(
                        onNavigateToLockscreen = { onNavigateToDetail("lockscreen") },
                        onNavigateToUIFeatures = { onNavigateToDetail("ui_features") },
                        onNavigateToSound = { onNavigateToDetail("sound") },
                        onNavigateToGestures = { onNavigateToDetail("gestures") }
                    )
                    "essentials" -> EssentialsContent(
                        onNavigateToAppPicker = onNavigateToAppPicker,
                        onNavigateToTrickyStore = { onNavigateToDetail("trickystore") },
                        onNavigateToPlayIntegrityFix = { onNavigateToDetail("playintegrityfix") },
                        onNavigateToGameSpoofing = { onNavigateToDetail("gamespoofing") }
                    )
                    "performance" -> PerformanceContent()
                    "multitasking" -> MultitaskingContent()
                }
            }
        }
        
        BottomNavBar(
            items = navItems,
            selectedRoute = selectedRoute,
            onItemSelected = onNavSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DetailScreen(
    screen: String,
    onBackClick: () -> Unit
) {
    when (screen) {
        "lockscreen" -> LockscreenFeaturesScreen(onBackClick = onBackClick)
        "ui_features" -> UIFeaturesScreen(onBackClick = onBackClick)
        "sound" -> SoundFeaturesScreen(onBackClick = onBackClick)
        "gestures" -> GesturesScreen(onBackClick = onBackClick)
        "trickystore" -> TrickyStoreScreen(onBackClick = onBackClick)
        "playintegrityfix" -> PlayIntegrityFixScreen(onBackClick = onBackClick)
        "gamespoofing" -> GameSpoofingScreen(onBackClick = onBackClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailPaneContent(
    screen: String,
    onClose: () -> Unit
) {
    when (screen) {
        "lockscreen" -> LockscreenFeaturesScreen(onBackClick = onClose)
        "ui_features" -> UIFeaturesScreen(onBackClick = onClose)
        "sound" -> SoundFeaturesScreen(onBackClick = onClose)
        "gestures" -> GesturesScreen(onBackClick = onClose)
        "trickystore" -> TrickyStoreScreen(onBackClick = onClose)
        "playintegrityfix" -> PlayIntegrityFixScreen(onBackClick = onClose)
        "gamespoofing" -> GameSpoofingScreen(onBackClick = onClose)
    }
}

@Composable
private fun EmptyDetailPane() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.select_item),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
