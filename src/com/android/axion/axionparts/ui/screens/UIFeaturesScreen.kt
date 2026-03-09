package com.android.axion.axionparts.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.FeatureCard
import com.android.axion.compose.preferences.*
import com.android.axion.compose.scaffold.AxionScaffold

private enum class UIFeaturesSubScreen {
    MAIN,
    QUICK_SETTINGS,
    STATUS_BAR,
    DYNAMIC_BAR,
    DYNAMIC_BAR_GUIDE,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UIFeaturesScreen(onBackClick: () -> Unit) {
    var currentScreen by rememberSaveable { mutableStateOf(UIFeaturesSubScreen.MAIN) }

    val screenTitle =
        when (currentScreen) {
            UIFeaturesSubScreen.MAIN -> stringResource(R.string.user_interface)
            UIFeaturesSubScreen.QUICK_SETTINGS -> stringResource(R.string.quick_settings)
            UIFeaturesSubScreen.STATUS_BAR -> stringResource(R.string.status_bar)
            UIFeaturesSubScreen.DYNAMIC_BAR -> stringResource(R.string.dynamic_bar)
            UIFeaturesSubScreen.DYNAMIC_BAR_GUIDE -> stringResource(R.string.dynamic_bar_guide_title)
        }

    val handleBack: () -> Unit = {
        if (currentScreen == UIFeaturesSubScreen.MAIN) {
            onBackClick()
        } else {
            currentScreen = UIFeaturesSubScreen.MAIN
        }
    }

    BackHandler(onBack = handleBack)

    AxionScaffold(title = screenTitle, onBackClick = handleBack) { innerPadding ->
        val motionScheme = MaterialTheme.motionScheme
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val isNavigatingForward = targetState != UIFeaturesSubScreen.MAIN

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
                UIFeaturesSubScreen.MAIN ->
                    UIFeaturesMainContent(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToQuickSettings = {
                            currentScreen = UIFeaturesSubScreen.QUICK_SETTINGS
                        },
                        onNavigateToStatusBar = { currentScreen = UIFeaturesSubScreen.STATUS_BAR },
                        onNavigateToDynamicBar = { currentScreen = UIFeaturesSubScreen.DYNAMIC_BAR },
                    )
                UIFeaturesSubScreen.QUICK_SETTINGS ->
                    QuickSettingsContent(modifier = Modifier.padding(innerPadding))
                UIFeaturesSubScreen.STATUS_BAR ->
                    StatusBarContent(modifier = Modifier.padding(innerPadding))
                UIFeaturesSubScreen.DYNAMIC_BAR ->
                    DynamicBarMainContent(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateToGuide = { currentScreen = UIFeaturesSubScreen.DYNAMIC_BAR_GUIDE },
                    )
                UIFeaturesSubScreen.DYNAMIC_BAR_GUIDE ->
                    DynamicBarGuideContent(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
private fun UIFeaturesMainContent(
    modifier: Modifier = Modifier,
    onNavigateToQuickSettings: () -> Unit,
    onNavigateToStatusBar: () -> Unit,
    onNavigateToDynamicBar: () -> Unit,
) {
    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureCard(
                title = stringResource(R.string.status_bar),
                subtitle = stringResource(R.string.status_bar_description),
                icon = Icons.Filled.SignalCellularAlt,
                onClick = onNavigateToStatusBar,
                illustrationColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
            FeatureCard(
                title = stringResource(R.string.dynamic_bar),
                subtitle = stringResource(R.string.dynamic_bar_summary),
                icon = Icons.Filled.DynamicFeed,
                onClick = onNavigateToDynamicBar,
                illustrationColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun QuickSettingsContent(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.brightness_slider)) {
            item {
                SecureListPreference(
                    key = "qs_brightness_slider_enabled",
                    title = stringResource(R.string.brightness_slider),
                    summary = stringResource(R.string.brightness_slider_summary),
                    options =
                        listOf(
                            "0" to stringResource(R.string.brightness_hidden),
                            "1" to stringResource(R.string.brightness_show_expanded),
                            "2" to stringResource(R.string.brightness_always_visible),
                        ),
                    defaultValue = "2",
                )
            }
            item {
                SecureListPreference(
                    key = "qs_brightness_slider_top",
                    title = stringResource(R.string.brightness_slider_position),
                    summary = stringResource(R.string.brightness_slider_position_summary),
                    options =
                        listOf(
                            "0" to stringResource(R.string.position_bottom),
                            "1" to stringResource(R.string.position_top),
                        ),
                    defaultValue = "0",
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatusBarContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.status_bar_icons)) {
            item {
                ClickablePreference(
                    title = stringResource(R.string.status_bar_tuner),
                    summary = stringResource(R.string.status_bar_tuner_summary),
                    icon = Icons.Default.Tune,
                    showExternalIcon = true,
                    onClick = {
                        val intent =
                            Intent("com.android.settings.action.STATUS_BAR_TUNER").apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        context.startActivity(intent)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
