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

package com.android.axion.axionparts.ui

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.*
import com.android.axion.axionparts.ui.screens.*
import com.android.axion.axionparts.ui.screens.routines.RoutinesScreen
import com.android.axion.axionparts.ui.theme.MaxContentWidth
import com.android.axion.compose.navigation.AxRouteAnimatedContent
import com.android.axion.compose.navigation.rememberAxRouteNavigator
import com.android.axion.compose.preferences.*
import com.android.axion.compose.scaffold.AxionScaffold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(initialDetailScreen: String? = null) {
    val activity = LocalContext.current as? Activity
    val windowSizeClass = rememberWindowSizeClass()
    val isExpandedLayout =
        windowSizeClass == WindowSizeClass.EXPANDED || windowSizeClass == WindowSizeClass.MEDIUM

    var appPickerTitleRes by rememberSaveable { mutableStateOf(R.string.select_apps) }
    var appPickerSelectedApps by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var appPickerSettingKey by rememberSaveable { mutableStateOf(ESSENTIAL_APP_LIST_KEY) }
    var appPickerMaxSelection by rememberSaveable { mutableStateOf<Int?>(null) }
    var appPickerMaxSelectionMessageRes by rememberSaveable { mutableStateOf<Int?>(null) }
    var appPickerFilterTypeName by rememberSaveable {
        mutableStateOf(AppFilterType.LAUNCHABLE_USER_ONLY.name)
    }
    var appPickerExcludedPackages by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    val detailNavigator = rememberAxRouteNavigator(initialRoute = initialDetailScreen)
    val currentDetailScreen = detailNavigator.route
    val dashboardScrollState = rememberLazyListState()

    fun navigateToDetail(screen: String) {
        detailNavigator.navigateTo(screen)
    }

    fun navigateToNestedDetail(screen: String) {
        detailNavigator.navigateToNested(screen)
    }

    fun navigateToAppPicker(
        titleRes: Int,
        selectedApps: Set<String>,
        settingKey: String,
        maxSelection: Int? = null,
        maxSelectionMessageRes: Int? = null,
        filterType: AppFilterType = AppFilterType.LAUNCHABLE_USER_ONLY,
        excludedPackages: Set<String> = emptySet(),
    ) {
        appPickerTitleRes = titleRes
        appPickerSelectedApps = selectedApps
        appPickerSettingKey = settingKey
        appPickerMaxSelection = maxSelection
        appPickerMaxSelectionMessageRes = maxSelectionMessageRes
        appPickerFilterTypeName = filterType.name
        appPickerExcludedPackages = excludedPackages
        navigateToNestedDetail("app_picker")
    }

    fun closeDetail() {
        if (!detailNavigator.goBack()) {
            activity?.finish()
        }
    }

    val appPickerCallback: (Set<String>) -> Unit = { selectedApps ->
        navigateToAppPicker(
            titleRes = R.string.select_essential_apps,
            selectedApps = selectedApps,
            settingKey = ESSENTIAL_APP_LIST_KEY,
        )
    }

    if (currentDetailScreen != null) {
        BackHandler { closeDetail() }
    }

    if (isExpandedLayout) {
        TwoPaneLayout(
            windowSizeClass = windowSizeClass,
            listPane = {
                DashboardContent(
                    scrollState = dashboardScrollState,
                    onNavigateToDetail = { navigateToDetail(it) },
                )
            },
            detailPane = {
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    if (currentDetailScreen != null) {
                        DetailPaneContent(
                            screen = currentDetailScreen!!,
                            onClose = { closeDetail() },
                            onNavigateToDetail = { navigateToNestedDetail(it) },
                            onNavigateToAppPicker = appPickerCallback,
                            onNavigateToManagedAppPicker = ::navigateToAppPicker,
                            appPickerTitleRes = appPickerTitleRes,
                            appPickerSelectedApps = appPickerSelectedApps,
                            appPickerSettingKey = appPickerSettingKey,
                            appPickerMaxSelection = appPickerMaxSelection,
                            appPickerMaxSelectionMessageRes = appPickerMaxSelectionMessageRes,
                            appPickerFilterTypeName = appPickerFilterTypeName,
                            appPickerExcludedPackages = appPickerExcludedPackages,
                        )
                    } else {
                        EmptyDetailPane()
                    }
                }
            },
            showDetailPane = currentDetailScreen != null,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        AxRouteAnimatedContent(
            targetRoute = currentDetailScreen,
            isForward = detailNavigator.isForward,
            label = "detailTransition",
        ) { detailScreen ->
            if (detailScreen != null) {
                DetailScreen(
                    screen = detailScreen,
                    onBackClick = { closeDetail() },
                    onNavigateToDetail = { navigateToNestedDetail(it) },
                    onNavigateToAppPicker = appPickerCallback,
                    onNavigateToManagedAppPicker = ::navigateToAppPicker,
                    appPickerTitleRes = appPickerTitleRes,
                    appPickerSelectedApps = appPickerSelectedApps,
                    appPickerSettingKey = appPickerSettingKey,
                    appPickerMaxSelection = appPickerMaxSelection,
                    appPickerMaxSelectionMessageRes = appPickerMaxSelectionMessageRes,
                    appPickerFilterTypeName = appPickerFilterTypeName,
                    appPickerExcludedPackages = appPickerExcludedPackages,
                )
            } else {
                DashboardContent(
                    scrollState = dashboardScrollState,
                    onNavigateToDetail = { navigateToDetail(it) },
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    scrollState: LazyListState,
    onNavigateToDetail: (String) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity

    AxionScaffold(
        title = stringResource(R.string.personalizations),
        onBackClick = { activity?.finish() },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            var revealPlayed by rememberSaveable { mutableStateOf(false) }
            val scaleIn = remember { Animatable(if (revealPlayed) 1f else 0.85f) }
            val alphaIn = remember { Animatable(if (revealPlayed) 1f else 0f) }
            LaunchedEffect(revealPlayed) {
                if (!revealPlayed) {
                    val scaleJob = launch {
                        scaleIn.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
                    }
                    val alphaJob = launch {
                        alphaIn.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
                    }
                    scaleJob.join()
                    alphaJob.join()
                    revealPlayed = true
                }
            }
            val revealModifier = Modifier.graphicsLayer {
                scaleX = scaleIn.value
                scaleY = scaleIn.value
                alpha = alphaIn.value
            }

            LazyColumn(
                state = scrollState,
                modifier =
                    Modifier.widthIn(max = MaxContentWidth)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "hero") {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(372.dp).then(revealModifier),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        WallpaperCard(
                            title = stringResource(R.string.lockscreen),
                            onClick = { onNavigateToDetail("lockscreen") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            VisualCard(
                                title = stringResource(R.string.themes),
                                onClick = {
                                    val intent =
                                        Intent().apply {
                                            component =
                                                ComponentName(
                                                    "com.android.axion.axthemestore",
                                                    "com.android.axion.axthemestore.MainActivity",
                                                )
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth().weight(1f),
                            ) {
                                ThemesIllustration()
                            }
                            VisualCard(
                                title = stringResource(R.string.ui_features),
                                onClick = { onNavigateToDetail("ui_features") },
                                modifier = Modifier.fillMaxWidth().weight(1f),
                            ) {
                                UIFeaturesIllustration()
                            }
                        }
                    }
                }

                item(key = "visuals") {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(148.dp).then(revealModifier),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        VisualCard(
                            title = stringResource(R.string.sound),
                            onClick = { onNavigateToDetail("sound") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        ) {
                            SoundIllustration()
                        }
                        VisualCard(
                            title = stringResource(R.string.gestures),
                            onClick = { onNavigateToDetail("gestures") },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        ) {
                            GesturesIllustration()
                        }
                    }
                }

                item(key = "features") {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(118.dp).then(revealModifier),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DashboardCard(
                            title = stringResource(R.string.routines),
                            icon = Icons.Filled.AutoMode,
                            onClick = { onNavigateToDetail("routines") },
                            modifier = Modifier.weight(1f),
                        )
                        DashboardCard(
                            title = stringResource(R.string.essentials),
                            icon = Icons.Filled.Workspaces,
                            onClick = { onNavigateToDetail("essentials") },
                            modifier = Modifier.weight(1f),
                        )
                        DashboardCard(
                            title = stringResource(R.string.performance),
                            icon = Icons.Filled.Bolt,
                            onClick = { onNavigateToDetail("performance") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item(key = "extraFeatures") {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(118.dp).then(revealModifier),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DashboardCard(
                            title = stringResource(R.string.multitasking),
                            icon = Icons.Filled.Splitscreen,
                            onClick = { onNavigateToDetail("multitasking") },
                            modifier = Modifier.weight(1f),
                        )
                        DashboardCard(
                            title = stringResource(R.string.ax_bravia_engine),
                            icon = Icons.Filled.Palette,
                            onClick = { onNavigateToDetail("bravia_engine") },
                            modifier = Modifier.weight(1f),
                        )
                        DashboardCard(
                            title = stringResource(R.string.diagnostics),
                            icon = Icons.Filled.Analytics,
                            onClick = {
                                val intent =
                                    Intent().apply {
                                        component =
                                            ComponentName(
                                                "com.axion.diagnostics",
                                                "com.axion.diagnostics.DiagnosticsActivity",
                                            )
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item(key = "navigationBars") {
                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailScreen(
    screen: String,
    onBackClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToAppPicker: (Set<String>) -> Unit = {},
    onNavigateToManagedAppPicker: BackgroundAppPickerNavigator = { _, _, _, _, _, _, _ -> },
    appPickerTitleRes: Int = R.string.select_apps,
    appPickerSelectedApps: Set<String> = emptySet(),
    appPickerSettingKey: String = ESSENTIAL_APP_LIST_KEY,
    appPickerMaxSelection: Int? = null,
    appPickerMaxSelectionMessageRes: Int? = null,
    appPickerFilterTypeName: String = AppFilterType.LAUNCHABLE_USER_ONLY.name,
    appPickerExcludedPackages: Set<String> = emptySet(),
) {
    when (screen) {
        "lockscreen" -> LockscreenFeaturesScreen(onBackClick = onBackClick)
        "ui_features" -> UIFeaturesScreen(onBackClick = onBackClick)
        "sound" -> SoundFeaturesScreen(onBackClick = onBackClick)
        "gestures" -> GesturesScreen(onBackClick = onBackClick)
        "trickystore" -> TrickyStoreScreen(onBackClick = onBackClick)
        "playintegrityfix" -> PlayIntegrityFixScreen(onBackClick = onBackClick)
        "gamespoofing" -> GameSpoofingScreen(onBackClick = onBackClick)
        "pcmode" -> PcModeScreen(onBackClick = onBackClick)
        "routines" -> RoutinesScreen(onBackClick = onBackClick)
        "performance" ->
            PerformanceScreen(
                onBackClick = onBackClick,
                onNavigateToDetail = onNavigateToDetail,
            )
        "background_manager" ->
            BackgroundManagerScreen(
                onBackClick = onBackClick,
                onNavigateToAppPicker = onNavigateToManagedAppPicker,
            )
        "kernel_manager" -> KernelManagerScreen(onBackClick = onBackClick)
        "app_picker" ->
            ManagedAppPickerScreen(
                titleRes = appPickerTitleRes,
                selectedApps = appPickerSelectedApps,
                settingKey = appPickerSettingKey,
                maxSelection = appPickerMaxSelection,
                maxSelectionMessageRes = appPickerMaxSelectionMessageRes,
                filterTypeName = appPickerFilterTypeName,
                excludedPackages = appPickerExcludedPackages,
                onBackClick = onBackClick,
            )
        "bravia_engine" -> AxBraviaEngineScreen(onBackClick = onBackClick)
        "dynamic_bar" -> DynamicBarScreen(onBackClick = onBackClick)
        "essentials" ->
            EssentialsScreen(
                onBackClick = onBackClick,
                onNavigateToAppPicker = onNavigateToAppPicker,
            )
        "multitasking" -> MultitaskingScreen(onBackClick = onBackClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailPaneContent(
    screen: String,
    onClose: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToAppPicker: (Set<String>) -> Unit = {},
    onNavigateToManagedAppPicker: BackgroundAppPickerNavigator = { _, _, _, _, _, _, _ -> },
    appPickerTitleRes: Int = R.string.select_apps,
    appPickerSelectedApps: Set<String> = emptySet(),
    appPickerSettingKey: String = ESSENTIAL_APP_LIST_KEY,
    appPickerMaxSelection: Int? = null,
    appPickerMaxSelectionMessageRes: Int? = null,
    appPickerFilterTypeName: String = AppFilterType.LAUNCHABLE_USER_ONLY.name,
    appPickerExcludedPackages: Set<String> = emptySet(),
) {
    when (screen) {
        "lockscreen" -> LockscreenFeaturesScreen(onBackClick = onClose)
        "ui_features" -> UIFeaturesScreen(onBackClick = onClose)
        "sound" -> SoundFeaturesScreen(onBackClick = onClose)
        "gestures" -> GesturesScreen(onBackClick = onClose)
        "trickystore" -> TrickyStoreScreen(onBackClick = onClose)
        "playintegrityfix" -> PlayIntegrityFixScreen(onBackClick = onClose)
        "gamespoofing" -> GameSpoofingScreen(onBackClick = onClose)
        "pcmode" -> PcModeScreen(onBackClick = onClose)
        "routines" -> RoutinesScreen(onBackClick = onClose)
        "performance" ->
            PerformanceScreen(
                onBackClick = onClose,
                onNavigateToDetail = onNavigateToDetail,
            )
        "background_manager" ->
            BackgroundManagerScreen(
                onBackClick = onClose,
                onNavigateToAppPicker = onNavigateToManagedAppPicker,
            )
        "kernel_manager" -> KernelManagerScreen(onBackClick = onClose)
        "app_picker" ->
            ManagedAppPickerScreen(
                titleRes = appPickerTitleRes,
                selectedApps = appPickerSelectedApps,
                settingKey = appPickerSettingKey,
                maxSelection = appPickerMaxSelection,
                maxSelectionMessageRes = appPickerMaxSelectionMessageRes,
                filterTypeName = appPickerFilterTypeName,
                excludedPackages = appPickerExcludedPackages,
                onBackClick = onClose,
            )
        "bravia_engine" -> AxBraviaEngineScreen(onBackClick = onClose)
        "dynamic_bar" -> DynamicBarScreen(onBackClick = onClose)
        "essentials" ->
            EssentialsScreen(
                onBackClick = onClose,
                onNavigateToAppPicker = onNavigateToAppPicker,
            )
        "multitasking" -> MultitaskingScreen(onBackClick = onClose)
    }
}

@Composable
private fun ManagedAppPickerScreen(
    titleRes: Int,
    selectedApps: Set<String>,
    settingKey: String,
    maxSelection: Int?,
    maxSelectionMessageRes: Int?,
    filterTypeName: String,
    excludedPackages: Set<String>,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val maxSelectionMessage =
        maxSelectionMessageRes?.let { resId ->
            maxSelection?.let { stringResource(resId, it) } ?: stringResource(resId)
        }

    val filterType = runCatching { AppFilterType.valueOf(filterTypeName) }
        .getOrDefault(AppFilterType.LAUNCHABLE_USER_ONLY)

    AppPickerScreen(
        title = stringResource(titleRes),
        selectedApps = selectedApps,
        onBackClick = onBackClick,
        onAppsSelected = { apps ->
            Settings.Secure.putString(context.contentResolver, settingKey, apps.joinToString(","))
            onBackClick()
        },
        maxSelection = maxSelection,
        maxSelectionMessage = maxSelectionMessage,
        filterType = filterType,
        excludedPackages = excludedPackages,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WallpaperCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val motionScheme = MaterialTheme.motionScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = motionScheme.defaultSpatialSpec(),
            label = "scale",
        )

    var wallpaperImage by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(Unit) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val drawable = wallpaperManager.drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
        if (bitmap != null) {
            val maxW = with(density) { 400.dp.roundToPx() }
            val maxH = with(density) { 600.dp.roundToPx() }
            wallpaperImage =
                withContext(Dispatchers.Default) {
                    val s =
                        minOf(
                            maxW.toFloat() / bitmap.width,
                            maxH.toFloat() / bitmap.height,
                            1f,
                        )
                    val sw = (bitmap.width * s).toInt().coerceAtLeast(1)
                    val sh = (bitmap.height * s).toInt().coerceAtLeast(1)
                    Bitmap.createScaledBitmap(bitmap, sw, sh, true).asImageBitmap()
                }
        }
    }

    val fallbackColors =
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
        )

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(28.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
    ) {
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
                    Modifier.fillMaxSize()
                        .background(Brush.linearGradient(fallbackColors))
            )
        }

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(72.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomStart).padding(18.dp),
        )
    }
}

@Composable
private fun ThemesIllustration() {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset(x = (-14).dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.7f)),
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset(x = 14.dp)
                .clip(CircleShape)
                .background(colors.tertiary.copy(alpha = 0.7f)),
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset(y = 14.dp)
                .clip(CircleShape)
                .background(colors.secondary.copy(alpha = 0.7f)),
        )
    }
}

@Composable
private fun UIFeaturesIllustration() {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primary),
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primaryContainer),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.tertiaryContainer),
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.secondaryContainer),
                )
            }
        }
    }
}

@Composable
private fun SoundIllustration() {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val heights = listOf(20f, 32f, 44f, 56f, 44f, 32f, 20f)
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.tertiary),
                )
            }
        }
    }
}

@Composable
private fun GesturesIllustration() {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Gesture,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun EmptyDetailPane() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Select an item",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}
