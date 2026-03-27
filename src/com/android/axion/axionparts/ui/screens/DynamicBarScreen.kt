/*
 * Copyright (C) 2025-2026 AxionOS Project
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

package com.android.axion.axionparts.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SecureSettingSwitch
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.rememberSettingBoolean
import com.android.axion.compose.preferences.rememberSettingString
import com.android.axion.compose.preferences.rememberSettingsFlow
import com.android.axion.compose.scaffold.AxionScaffold
import org.json.JSONArray

private enum class DynamicBarSubScreen { MAIN, GUIDE }
private enum class ChipType { RECORDING, MEDIA, TIMER }

private val CardShape = RoundedCornerShape(28.dp)
private val ChipShape = RoundedCornerShape(32.dp)
private val GuideCardShape = RoundedCornerShape(20.dp)

private val RedAccent = Color(0xFFFF453A)
private val PurpleAccent = Color(0xFFBF5AF2)
private val BlueAccent = Color(0xFF0A84FF)

private data class EventToggle(
    val typeId: String,
    val titleRes: Int,
    val summaryRes: Int,
    val icon: ImageVector,
)

private val EVENT_TOGGLES = listOf(
    EventToggle("screen_recording", R.string.dynamic_bar_event_recording, R.string.dynamic_bar_event_recording_summary, Icons.Filled.FiberManualRecord),
    EventToggle("privacy", R.string.dynamic_bar_event_privacy, R.string.dynamic_bar_event_privacy_summary, Icons.Filled.Videocam),
    EventToggle("audio_recording", R.string.dynamic_bar_event_audio_recording, R.string.dynamic_bar_event_audio_recording_summary, Icons.Filled.Mic),
    EventToggle("media", R.string.dynamic_bar_event_media, R.string.dynamic_bar_event_media_summary, Icons.Filled.MusicNote),
    EventToggle("notification", R.string.dynamic_bar_event_notification, R.string.dynamic_bar_event_notification_summary, Icons.Filled.Notifications),
    EventToggle("timer", R.string.dynamic_bar_event_timer, R.string.dynamic_bar_event_timer_summary, Icons.Filled.Timer),
    EventToggle("stopwatch", R.string.dynamic_bar_event_stopwatch, R.string.dynamic_bar_event_stopwatch_summary, Icons.Filled.Timer),
    EventToggle("alarm", R.string.dynamic_bar_event_alarm, R.string.dynamic_bar_event_alarm_summary, Icons.Filled.Alarm),
    EventToggle("charging", R.string.dynamic_bar_event_charging, R.string.dynamic_bar_event_charging_summary, Icons.Filled.BatteryChargingFull),
    EventToggle("bluetooth", R.string.dynamic_bar_event_bluetooth, R.string.dynamic_bar_event_bluetooth_summary, Icons.Filled.Bluetooth),
    EventToggle("hotspot", R.string.dynamic_bar_event_hotspot, R.string.dynamic_bar_event_hotspot_summary, Icons.Filled.Wifi),
    EventToggle("ringer", R.string.dynamic_bar_event_ringer, R.string.dynamic_bar_event_ringer_summary, Icons.Filled.Vibration),
    EventToggle("vpn", R.string.dynamic_bar_event_vpn, R.string.dynamic_bar_event_vpn_summary, Icons.Filled.VpnKey),
    EventToggle("clipboard", R.string.dynamic_bar_event_clipboard, R.string.dynamic_bar_event_clipboard_summary, Icons.Filled.ContentCopy),
    EventToggle("torch", R.string.dynamic_bar_event_torch, R.string.dynamic_bar_event_torch_summary, Icons.Filled.FlashlightOn),
    EventToggle("casting", R.string.dynamic_bar_event_casting, R.string.dynamic_bar_event_casting_summary, Icons.Filled.Cast),
    EventToggle("promoted_ongoing", R.string.dynamic_bar_event_ongoing, R.string.dynamic_bar_event_ongoing_summary, Icons.Filled.Notifications),
    EventToggle("sports", R.string.dynamic_bar_event_sports, R.string.dynamic_bar_event_sports_summary, Icons.Filled.SportsScore),
    EventToggle("app_switch", R.string.dynamic_bar_event_app_switch, R.string.dynamic_bar_event_app_switch_summary, Icons.Filled.SwapHoriz),
    EventToggle("biometric_unlock", R.string.dynamic_bar_event_biometric, R.string.dynamic_bar_event_biometric_summary, Icons.Filled.Fingerprint),
)

private const val SETTINGS_KEY_ENABLED = "ax_dynamic_bar_enabled"
private const val SETTINGS_KEY_KEYGUARD_ENABLED = "ax_dynamic_bar_keyguard_enabled"
private const val SETTINGS_KEY_EVENTS = "ax_dynamic_bar_events"

@Composable
fun DynamicBarScreen(onBackClick: () -> Unit) {
    var currentScreen by rememberSaveable { mutableStateOf(DynamicBarSubScreen.MAIN) }

    val screenTitle = when (currentScreen) {
        DynamicBarSubScreen.MAIN -> stringResource(R.string.dynamic_bar)
        DynamicBarSubScreen.GUIDE -> stringResource(R.string.dynamic_bar_guide_title)
    }

    val handleBack: () -> Unit = {
        if (currentScreen == DynamicBarSubScreen.MAIN) onBackClick()
        else currentScreen = DynamicBarSubScreen.MAIN
    }

    BackHandler(enabled = currentScreen != DynamicBarSubScreen.MAIN, onBack = handleBack)

    AxionScaffold(title = screenTitle, onBackClick = handleBack) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val forward = targetState != DynamicBarSubScreen.MAIN
                if (forward) {
                    (slideInHorizontally { it } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 3 } + fadeIn())
                        .togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "dynamicBarTransition",
        ) { screen ->
            when (screen) {
                DynamicBarSubScreen.MAIN -> DynamicBarMainContent(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToGuide = { currentScreen = DynamicBarSubScreen.GUIDE },
                )
                DynamicBarSubScreen.GUIDE -> DynamicBarGuideContent(
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
internal fun DynamicBarMainContent(
    modifier: Modifier = Modifier,
    onNavigateToGuide: () -> Unit,
) {
    val isEnabled by rememberSettingBoolean(SETTINGS_KEY_ENABLED, SettingsType.SECURE, false)
    val disabledEventsJson by rememberSettingString(SETTINGS_KEY_EVENTS, SettingsType.SECURE, "")
    val secureFlow = rememberSettingsFlow(SettingsType.SECURE)
    val disabledEvents = remember(disabledEventsJson) {
        if (disabledEventsJson.isBlank()) emptySet()
        else try {
            val arr = JSONArray(disabledEventsJson)
            (0 until arr.length()).mapTo(mutableSetOf()) { arr.getString(it) }
        } catch (_: Exception) { emptySet() }
    }

    fun toggleEvent(typeId: String, enabled: Boolean) {
        val updated = if (enabled) disabledEvents - typeId else disabledEvents + typeId
        val json = if (updated.isEmpty()) "" else JSONArray(updated.toList()).toString()
        secureFlow.putString(SETTINGS_KEY_EVENTS, json)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        HeroBanner(isEnabled)

        Spacer(Modifier.height(20.dp))

        PreferenceGroup {
            item {
                SecureSettingSwitch(
                    settingKey = SETTINGS_KEY_ENABLED,
                    title = stringResource(R.string.dynamic_bar_enable),
                    summary = stringResource(R.string.dynamic_bar_enable_summary),
                    defaultValue = false,
                )
            }
            item {
                SecureSettingSwitch(
                    settingKey = SETTINGS_KEY_KEYGUARD_ENABLED,
                    title = stringResource(R.string.dynamic_bar_keyguard_enable),
                    summary = stringResource(R.string.dynamic_bar_keyguard_enable_summary),
                    defaultValue = true,
                    enabled = isEnabled,
                )
            }
        }

        AnimatedVisibility(
            visible = isEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                Spacer(Modifier.height(20.dp))

                PreferenceGroup(title = stringResource(R.string.dynamic_bar_events)) {
                    EVENT_TOGGLES.forEach { toggle ->
                        item {
                            EventToggleItem(
                                toggle = toggle,
                                enabled = toggle.typeId !in disabledEvents,
                                onToggle = { toggleEvent(toggle.typeId, it) },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        GuideNavigationCard(onClick = onNavigateToGuide)

        Spacer(Modifier.height(24.dp))

        FeatureIntroFooter()

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HeroBanner(isEnabled: Boolean) {
    val transition = rememberInfiniteTransition(label = "hero")
    val dotAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "blink",
    )
    val chipIndex by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2.99f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "chipCycle",
    )

    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "wave",
    )
    val hourglassRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "hourglass",
    )

    val textMeasurer = rememberTextMeasurer()
    data class ChipDemo(val label: String, val accent: Color, val type: ChipType)
    val chips = listOf(
        ChipDemo("0:42", RedAccent, ChipType.RECORDING),
        ChipDemo("", PurpleAccent, ChipType.MEDIA),
        ChipDemo("5:23", BlueAccent, ChipType.TIMER),
    )
    val activeChip = chips[chipIndex.toInt().coerceIn(0, 2)]

    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val chipH = 24.dp.toPx()
                val chipPadH = 10.dp.toPx()
                val iconSize = 16.dp.toPx()
                val gap = 4.dp.toPx()

                val labelStyle = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )

                val waveBarCount = 4
                val waveBarW = 2.dp.toPx()
                val waveBarGap = 2.dp.toPx()
                val waveW = waveBarCount * (waveBarW + waveBarGap) - waveBarGap

                val chipW = when (activeChip.type) {
                    ChipType.RECORDING -> {
                        val m = textMeasurer.measure(activeChip.label, labelStyle)
                        chipPadH + 8.dp.toPx() + gap + m.size.width + chipPadH
                    }
                    ChipType.MEDIA -> {
                        chipPadH + iconSize + gap + waveW + chipPadH
                    }
                    ChipType.TIMER -> {
                        val m = textMeasurer.measure(activeChip.label, labelStyle)
                        chipPadH + iconSize + gap + m.size.width + chipPadH
                    }
                }

                val chipLeft = cx - chipW / 2f
                val chipTop = cy - chipH / 2f

                val chipPath = Path().apply {
                    addRoundRect(RoundRect(
                        Rect(chipLeft, chipTop, chipLeft + chipW, chipTop + chipH),
                        CornerRadius(chipH / 2f),
                    ))
                }
                drawPath(chipPath, activeChip.accent)

                var contentX = chipLeft + chipPadH

                when (activeChip.type) {
                    ChipType.RECORDING -> {
                        drawCircle(
                            color = Color.White.copy(alpha = dotAlpha),
                            radius = 4.dp.toPx(),
                            center = Offset(contentX + 4.dp.toPx(), cy),
                        )
                        contentX += 8.dp.toPx() + gap
                        val m = textMeasurer.measure(activeChip.label, labelStyle)
                        drawText(textMeasurer, activeChip.label, Offset(contentX, cy - m.size.height / 2f), labelStyle)
                    }
                    ChipType.MEDIA -> {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = iconSize / 2f,
                            center = Offset(contentX + iconSize / 2f, cy),
                        )
                        contentX += iconSize + gap
                        val maxBarH = 10.dp.toPx()
                        for (i in 0 until waveBarCount) {
                            val phase = wavePhase + i * 1.2f
                            val h = maxBarH * (0.22f + 0.56f * ((sin(phase) + 1f) / 2f))
                            val bx = contentX + i * (waveBarW + waveBarGap)
                            drawRoundRect(
                                Color.White.copy(alpha = 0.85f),
                                Offset(bx, cy - h / 2f),
                                Size(waveBarW, h),
                                CornerRadius(waveBarW / 2f),
                            )
                        }
                    }
                    ChipType.TIMER -> {
                        drawHourglass(contentX + iconSize / 2f, cy, iconSize * 0.35f, hourglassRotation, Color.White)
                        contentX += iconSize + gap
                        val m = textMeasurer.measure(activeChip.label, labelStyle)
                        drawText(textMeasurer, activeChip.label, Offset(contentX, cy - m.size.height / 2f), labelStyle)
                    }
                }

                val indY = chipTop + chipH + 10.dp.toPx()
                chips.forEachIndexed { i, _ ->
                    val dotX = cx + (i - 1) * 10.dp.toPx()
                    val isCurrent = i == chipIndex.toInt().coerceIn(0, 2)
                    drawCircle(
                        color = if (isCurrent) activeChip.accent else Color.White.copy(alpha = 0.2f),
                        radius = if (isCurrent) 3.dp.toPx() else 2.dp.toPx(),
                        center = Offset(dotX, indY),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(R.string.dynamic_bar),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.dynamic_bar_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EventToggleItem(
    toggle: EventToggle,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    SwitchPreference(
        title = stringResource(toggle.titleRes),
        summary = stringResource(toggle.summaryRes),
        checked = enabled,
        onCheckedChange = onToggle,
        customIcon = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    toggle.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
    )
}

@Composable
private fun GuideNavigationCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CardShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.dynamic_bar_guide_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.dynamic_bar_guide_chip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun FeatureIntroFooter() {
    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.dynamic_bar_footer_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.dynamic_bar_footer_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class GuideStep(
    val titleRes: Int,
    val bodyRes: Int,
    val icon: ImageVector,
)

private val GUIDE_STEPS = listOf(
    GuideStep(R.string.dynamic_bar_guide_chip_title, R.string.dynamic_bar_guide_chip, Icons.Filled.FiberManualRecord),
    GuideStep(R.string.dynamic_bar_guide_swipe_title, R.string.dynamic_bar_guide_swipe, Icons.Filled.SwipeLeft),
    GuideStep(R.string.dynamic_bar_guide_expand_title, R.string.dynamic_bar_guide_expand, Icons.Filled.TouchApp),
    GuideStep(R.string.dynamic_bar_guide_dismiss_title, R.string.dynamic_bar_guide_dismiss, Icons.Filled.SwipeLeft),
    GuideStep(R.string.dynamic_bar_guide_keyguard_title, R.string.dynamic_bar_guide_keyguard, Icons.Filled.Notifications),
)

@Composable
internal fun DynamicBarGuideContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        ChipSwipeDemoIllustration()

        Spacer(Modifier.height(16.dp))

        GUIDE_STEPS.forEachIndexed { index, step ->
            GuideStepCard(step = step, stepNumber = index + 1)
            if (index < GUIDE_STEPS.lastIndex) Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))

        ExpandedCardDemoIllustration()

        Spacer(Modifier.height(16.dp))

        KeyguardChipDemoIllustration()

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ChipSwipeDemoIllustration() {
    val transition = rememberInfiniteTransition(label = "swipe")
    val swipePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(4500, easing = LinearEasing)),
        label = "phase",
    )
    val textMeasurer = rememberTextMeasurer()

    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.dynamic_bar_guide_swipe_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(16.dp))

            Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val chipH = 24.dp.toPx()
                val iconSize = 16.dp.toPx()
                val gap = 4.dp.toPx()
                val padH = 10.dp.toPx()

                val waveBarCount = 4
                val waveBarW = 2.dp.toPx()
                val waveBarGap = 2.dp.toPx()
                val waveW = waveBarCount * (waveBarW + waveBarGap) - waveBarGap

                data class DemoChip(val text: String, val accent: Color, val type: ChipType)
                val demoChips = listOf(
                    DemoChip("0:42", RedAccent, ChipType.RECORDING),
                    DemoChip("", PurpleAccent, ChipType.MEDIA),
                    DemoChip("5:23", BlueAccent, ChipType.TIMER),
                )

                val phase = swipePhase.toInt().coerceIn(0, 2)
                val frac = swipePhase - swipePhase.toInt()
                val current = demoChips[phase]
                val next = demoChips[(phase + 1) % 3]

                fun DrawScope.drawSwipeChip(chip: DemoChip, centerX: Float, alpha: Float, scale: Float) {
                    val labelStyle = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = alpha),
                    )

                    val w = when (chip.type) {
                        ChipType.RECORDING -> {
                            val m = textMeasurer.measure(chip.text, labelStyle)
                            padH + 8.dp.toPx() + gap + m.size.width + padH
                        }
                        ChipType.MEDIA -> padH + iconSize + gap + waveW + padH
                        ChipType.TIMER -> {
                            val m = textMeasurer.measure(chip.text, labelStyle)
                            padH + iconSize + gap + m.size.width + padH
                        }
                    }

                    val h = chipH * scale
                    val left = centerX - w / 2f
                    val top = cy - h / 2f

                    val path = Path().apply {
                        addRoundRect(RoundRect(
                            Rect(left, top, left + w, top + h),
                            CornerRadius(h / 2f),
                        ))
                    }
                    drawPath(path, chip.accent.copy(alpha = alpha))

                    var x = left + padH
                    when (chip.type) {
                        ChipType.RECORDING -> {
                            drawCircle(Color.White.copy(alpha = alpha * 0.8f), 4.dp.toPx() * scale, Offset(x + 4.dp.toPx(), cy))
                            x += 8.dp.toPx() + gap
                            val m = textMeasurer.measure(chip.text, labelStyle)
                            drawText(textMeasurer, chip.text, Offset(x, cy - m.size.height / 2f), labelStyle)
                        }
                        ChipType.MEDIA -> {
                            drawCircle(Color.White.copy(alpha = alpha * 0.3f), iconSize / 2f * scale, Offset(x + iconSize / 2f, cy))
                            x += iconSize + gap
                            val maxBarH = 10.dp.toPx() * scale
                            for (i in 0 until waveBarCount) {
                                val barH = maxBarH * (0.3f + 0.5f * ((i % 2 == 0).let { if (it) 0.8f else 0.4f }))
                                val bx = x + i * (waveBarW + waveBarGap)
                                drawRoundRect(
                                    Color.White.copy(alpha = alpha * 0.85f),
                                    Offset(bx, cy - barH / 2f),
                                    Size(waveBarW, barH),
                                    CornerRadius(waveBarW / 2f),
                                )
                            }
                        }
                        ChipType.TIMER -> {
                            drawHourglass(x + iconSize / 2f, cy, iconSize * 0.35f * scale, 0f, Color.White.copy(alpha = alpha))
                            x += iconSize + gap
                            val m = textMeasurer.measure(chip.text, labelStyle)
                            drawText(textMeasurer, chip.text, Offset(x, cy - m.size.height / 2f), labelStyle)
                        }
                    }
                }

                val slideOffset = frac * size.width * 0.4f
                drawSwipeChip(current, cx - slideOffset, alpha = 1f - frac, scale = 1f)
                drawSwipeChip(next, cx + size.width * 0.4f - slideOffset, alpha = frac, scale = 0.92f + 0.08f * frac)

                val arrowAlpha = if (frac < 0.3f) frac / 0.3f else if (frac > 0.7f) (1f - frac) / 0.3f else 1f
                val arrowX = cx + chipH
                val arrowSize = 6.dp.toPx()
                drawLine(
                    Color.White.copy(alpha = arrowAlpha * 0.4f),
                    Offset(arrowX, cy - arrowSize),
                    Offset(arrowX + arrowSize, cy),
                    strokeWidth = 2.dp.toPx(),
                )
                drawLine(
                    Color.White.copy(alpha = arrowAlpha * 0.4f),
                    Offset(arrowX, cy + arrowSize),
                    Offset(arrowX + arrowSize, cy),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        }
    }
}

@Composable
private fun ExpandedCardDemoIllustration() {
    val transition = rememberInfiniteTransition(label = "expanded")
    val timerProgress by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "timer",
    )
    val textMeasurer = rememberTextMeasurer()
    val surfaceBright = MaterialTheme.colorScheme.surfaceBright
    val outline = MaterialTheme.colorScheme.outlineVariant
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.dynamic_bar_guide_expand_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(16.dp))

            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                val w = size.width
                val h = size.height
                val cardW = w * 0.85f
                val cardH = h * 0.88f
                val cardLeft = (w - cardW) / 2f
                val cardTop = (h - cardH) / 2f
                val cardRadius = 28.dp.toPx()

                val cardPath = Path().apply {
                    addRoundRect(RoundRect(
                        Rect(cardLeft, cardTop, cardLeft + cardW, cardTop + cardH),
                        CornerRadius(cardRadius),
                    ))
                }
                drawPath(cardPath, surfaceBright)
                drawPath(cardPath, outline.copy(alpha = 0.3f), style = Stroke(1.dp.toPx()))

                val headerH = cardH * 0.52f
                val headerRadius = 24.dp.toPx()
                val headerPad = 10.dp.toPx()
                val headerPath = Path().apply {
                    addRoundRect(RoundRect(
                        Rect(cardLeft + headerPad, cardTop + headerPad, cardLeft + cardW - headerPad, cardTop + headerPad + headerH),
                        CornerRadius(headerRadius),
                    ))
                }
                drawPath(headerPath, BlueAccent.copy(alpha = 0.08f))

                val headerCy = cardTop + headerPad + headerH / 2f
                val padX = cardLeft + headerPad + 12.dp.toPx()

                val ringR = 18.dp.toPx()
                val ringCx = padX + ringR
                drawCircle(BlueAccent.copy(alpha = 0.1f), ringR, Offset(ringCx, headerCy))
                drawArc(
                    color = BlueAccent,
                    startAngle = -90f,
                    sweepAngle = 360f * timerProgress,
                    useCenter = false,
                    topLeft = Offset(ringCx - ringR, headerCy - ringR),
                    size = Size(ringR * 2, ringR * 2),
                    style = Stroke(3.dp.toPx()),
                )

                val timerIcon = 8.dp.toPx()
                drawLine(BlueAccent, Offset(ringCx, headerCy - timerIcon * 0.6f), Offset(ringCx, headerCy), strokeWidth = 2.dp.toPx())
                drawLine(BlueAccent, Offset(ringCx, headerCy), Offset(ringCx + timerIcon * 0.4f, headerCy), strokeWidth = 2.dp.toPx())

                val textX = padX + ringR * 2 + 12.dp.toPx()
                val labelStyle = TextStyle(fontSize = 10.sp, color = Color(0xFF8E8E93))
                drawText(textMeasurer, "Timer", Offset(textX, headerCy - 18.dp.toPx()), labelStyle)

                val mins = (timerProgress * 25).toInt()
                val secs = ((timerProgress * 25 * 60) % 60).toInt()
                val timeStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueAccent,
                    fontFamily = FontFamily.Monospace,
                )
                drawText(textMeasurer, "%d:%02d".format(mins, secs), Offset(textX, headerCy - 6.dp.toPx()), timeStyle)

                val chipW2 = 56.dp.toPx()
                val chipH2 = 20.dp.toPx()
                val chipLeft2 = cardLeft + cardW - headerPad - 12.dp.toPx() - chipW2
                val chipTop2 = headerCy - chipH2 / 2f
                val chipPath = Path().apply {
                    addRoundRect(RoundRect(
                        Rect(chipLeft2, chipTop2, chipLeft2 + chipW2, chipTop2 + chipH2),
                        CornerRadius(chipH2 / 2f),
                    ))
                }
                drawPath(chipPath, BlueAccent.copy(alpha = 0.15f))
                val chipLabel = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent)
                val chipM = textMeasurer.measure("Running", chipLabel)
                drawText(
                    textMeasurer, "Running",
                    Offset(chipLeft2 + (chipW2 - chipM.size.width) / 2f, chipTop2 + (chipH2 - chipM.size.height) / 2f),
                    chipLabel,
                )

                val btnY = cardTop + headerPad + headerH + 10.dp.toPx()
                val btnH = 28.dp.toPx()
                val btnGap = 8.dp.toPx()
                val btnLeft = cardLeft + headerPad
                val btnW = (cardW - headerPad * 2 - btnGap) / 2f

                val btn1Path = Path().apply {
                    addRoundRect(RoundRect(
                        Rect(btnLeft, btnY, btnLeft + btnW, btnY + btnH),
                        CornerRadius(btnH / 2f),
                    ))
                }
                drawPath(btn1Path, primary)
                val btn1Style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = onPrimary)
                val btn1M = textMeasurer.measure("+1 min", btn1Style)
                drawText(textMeasurer, "+1 min", Offset(btnLeft + (btnW - btn1M.size.width) / 2f, btnY + (btnH - btn1M.size.height) / 2f), btn1Style)

                val btn2Left = btnLeft + btnW + btnGap
                val btn2Path = Path().apply {
                    addRoundRect(RoundRect(
                        Rect(btn2Left, btnY, btn2Left + btnW, btnY + btnH),
                        CornerRadius(btnH / 2f),
                    ))
                }
                drawPath(btn2Path, primary)
                val btn2M = textMeasurer.measure("Cancel", btn1Style)
                drawText(textMeasurer, "Cancel", Offset(btn2Left + (btnW - btn2M.size.width) / 2f, btnY + (btnH - btn2M.size.height) / 2f), btn1Style)
            }
        }
    }
}

@Composable
private fun KeyguardChipDemoIllustration() {
    val transition = rememberInfiniteTransition(label = "kg")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "glow",
    )
    val textMeasurer = rememberTextMeasurer()

    Surface(
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.dynamic_bar_guide_keyguard_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(16.dp))

            Canvas(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val chipH = 36.dp.toPx()

                val labelStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                val timeText = "2:34"
                val timeM = textMeasurer.measure(timeText, labelStyle)

                val padS = 6.dp.toPx()
                val padE = 8.dp.toPx()
                val dotR = 5.dp.toPx()
                val gap = 6.dp.toPx()
                val btnSize = 24.dp.toPx()
                val btnGap = 4.dp.toPx()

                val contentW = dotR * 2 + gap + timeM.size.width + gap + btnSize * 2 + btnGap
                val chipW = padS + contentW + padE

                val chipLeft = cx - chipW / 2f
                val chipTop = cy - chipH / 2f

                val chipPath = Path().apply {
                    addRoundRect(RoundRect(
                        Rect(chipLeft, chipTop, chipLeft + chipW, chipTop + chipH),
                        CornerRadius(chipH / 2f),
                    ))
                }
                drawPath(chipPath, RedAccent)

                var x = chipLeft + padS

                drawCircle(Color.White.copy(alpha = glowAlpha), dotR, Offset(x + dotR, cy))
                x += dotR * 2 + gap

                drawText(textMeasurer, timeText, Offset(x, cy - timeM.size.height / 2f), labelStyle)
                x += timeM.size.width + gap

                drawCircle(Color.White.copy(alpha = 0.2f), btnSize / 2f, Offset(x + btnSize / 2f, cy))
                val pauseBarW = 3.dp.toPx()
                val pauseBarH = 10.dp.toPx()
                drawRoundRect(Color.White, Offset(x + btnSize / 2f - pauseBarW - 1.dp.toPx(), cy - pauseBarH / 2f), Size(pauseBarW, pauseBarH), CornerRadius(1.dp.toPx()))
                drawRoundRect(Color.White, Offset(x + btnSize / 2f + 1.dp.toPx(), cy - pauseBarH / 2f), Size(pauseBarW, pauseBarH), CornerRadius(1.dp.toPx()))
                x += btnSize + btnGap

                drawCircle(Color.White.copy(alpha = 0.2f), btnSize / 2f, Offset(x + btnSize / 2f, cy))
                val stopSize = 8.dp.toPx()
                drawRoundRect(Color.White, Offset(x + btnSize / 2f - stopSize / 2f, cy - stopSize / 2f), Size(stopSize, stopSize), CornerRadius(2.dp.toPx()))
            }
        }
    }
}

@Composable
private fun GuideStepCard(step: GuideStep, stepNumber: Int) {
    Surface(
        shape = GuideCardShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$stepNumber",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(step.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(step.bodyRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun DrawScope.drawHourglass(cx: Float, cy: Float, r: Float, rotation: Float, color: Color) {
    withTransform({ rotate(rotation, Offset(cx, cy)) }) {
        val top = cy - r
        val bot = cy + r
        val left = cx - r * 0.6f
        val right = cx + r * 0.6f
        val sw = 1.5f.dp.toPx()
        drawLine(color, Offset(left, top), Offset(right, top), sw)
        drawLine(color, Offset(left, bot), Offset(right, bot), sw)
        drawLine(color, Offset(left, top), Offset(cx, cy), sw)
        drawLine(color, Offset(right, top), Offset(cx, cy), sw)
        drawLine(color, Offset(cx, cy), Offset(left, bot), sw)
        drawLine(color, Offset(cx, cy), Offset(right, bot), sw)
    }
}
