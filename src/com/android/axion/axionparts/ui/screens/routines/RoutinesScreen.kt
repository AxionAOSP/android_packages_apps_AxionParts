/*
 * Copyright (C) 2025-2026 AxionOS
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

package com.android.axion.axionparts.ui.screens.routines

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.android.axion.compose.preferences.ExpressiveSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SecureSettingSwitch
import com.android.axion.compose.scaffold.AxionScaffold
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SETTINGS_KEY = "ax_routines_data"
private const val ROUTINES_BACKUP_FILE_NAME = "axion-routines.json"
private const val JSON_MIME_TYPE = "application/json"

@Composable
fun RoutinesScreen(onBackClick: () -> Unit) {
    var currentView by rememberSaveable { mutableStateOf("list") }
    var editingRoutineId by rememberSaveable { mutableStateOf<String?>(null) }
    var isForward by rememberSaveable { mutableStateOf(true) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val serializer = remember { RoutineSerializer() }
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }

    LaunchedEffect(context, serializer) {
        routines = loadRoutines(context, serializer)
    }

    fun save(updated: List<Routine>) {
        routines = updated
        scope.launch {
            persistRoutines(context, serializer, updated)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        val imported = readRoutinesBackup(context, serializer, uri)
                        routines = imported
                        persistRoutines(context, serializer, imported)
                        Toast.makeText(
                            context,
                            context.resources.getQuantityString(
                                R.plurals.routines_imported_count,
                                imported.size,
                                imported.size,
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Toast.makeText(
                            context,
                            R.string.routines_import_failed,
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    try {
                        writeRoutinesBackup(context, serializer, uri, routines)
                        Toast.makeText(
                            context,
                            context.resources.getQuantityString(
                                R.plurals.routines_exported_count,
                                routines.size,
                                routines.size,
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Toast.makeText(
                            context,
                            R.string.routines_export_failed,
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }
    }

    val handleBack: () -> Unit = {
        if (currentView == "list") {
            onBackClick()
        } else {
            isForward = false
            currentView = "list"
        }
    }

    BackHandler(enabled = currentView != "list", onBack = handleBack)

    val screenTitle = when (currentView) {
        "editor" -> if (editingRoutineId != null)
            stringResource(R.string.routines_edit)
        else stringResource(R.string.routines_create)
        else -> stringResource(R.string.routines)
    }

    AxionScaffold(title = screenTitle, onBackClick = handleBack) { innerPadding ->
        AnimatedContent(
            targetState = currentView,
            transitionSpec = {
                val direction = if (isForward) 1 else -1
                (slideInHorizontally { it * direction / 3 } + fadeIn())
                    .togetherWith(slideOutHorizontally { -it * direction / 3 } + fadeOut())
            },
            label = "routines_screen_transition",
        ) { view ->
            when (view) {
                "list" -> RoutinesListContent(
                    modifier = Modifier.padding(innerPadding),
                    routines = routines,
                    onToggle = { id, enabled ->
                        save(routines.map {
                            if (it.id == id) it.copy(enabled = enabled) else it
                        })
                    },
                    onEdit = { id ->
                        editingRoutineId = id
                        isForward = true
                        currentView = "editor"
                    },
                    onDelete = { id -> save(routines.filter { it.id != id }) },
                    onDuplicate = { id ->
                        routines.find { it.id == id }?.let { routine ->
                            save(
                                routines + routine.copy(
                                    id = UUID.randomUUID().toString(),
                                    name = context.getString(
                                        R.string.routines_duplicate_name,
                                        routine.name,
                                    ),
                                    createdAt = System.currentTimeMillis(),
                                    lastTriggeredAt = null,
                                )
                            )
                        }
                    },
                    onCreate = {
                        editingRoutineId = null
                        isForward = true
                        currentView = "editor"
                    },
                    onImport = { importLauncher.launch(createRoutinesImportIntent()) },
                    onExport = { exportLauncher.launch(createRoutinesExportIntent()) },
                )
                "editor" -> key(editingRoutineId) {
                    RoutineEditorContent(
                        modifier = Modifier.padding(innerPadding),
                        routine = editingRoutineId?.let { id -> routines.find { it.id == id } },
                        onSave = { routine ->
                            if (routines.any { it.id == routine.id }) {
                                save(routines.map {
                                    if (it.id == routine.id) routine else it
                                })
                            } else {
                                save(routines + routine)
                            }
                            isForward = false
                            currentView = "list"
                        },
                        onCancel = {
                            isForward = false
                            currentView = "list"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutinesListContent(
    modifier: Modifier,
    routines: List<Routine>,
    onToggle: (String, Boolean) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onCreate: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
) {
    var routineToDelete by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        PreferenceGroup {
            item {
                SecureSettingSwitch(
                    settingKey = "ax_routines_enabled",
                    title = stringResource(R.string.routines_enabled),
                    summary = stringResource(R.string.routines_enabled_summary),
                    icon = Icons.Default.AutoMode,
                    defaultValue = false,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        PreferenceGroup(title = stringResource(R.string.routines_backup_restore)) {
            item {
                ClickablePreference(
                    title = stringResource(R.string.routines_export),
                    summary = if (routines.isEmpty()) {
                        stringResource(R.string.routines_export_empty_summary)
                    } else {
                        stringResource(R.string.routines_export_summary)
                    },
                    icon = Icons.Default.CloudDownload,
                    enabled = routines.isNotEmpty(),
                    onClick = onExport,
                )
            }
            item {
                ClickablePreference(
                    title = stringResource(R.string.routines_import),
                    summary = stringResource(R.string.routines_import_summary),
                    icon = Icons.Default.Upload,
                    onClick = onImport,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(visible = routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AutoMode,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.routines_no_routines),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.routines_no_routines_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }

        AnimatedVisibility(visible = routines.isNotEmpty()) {
            Column {
                routines.forEach { routine ->
                    RoutineCard(
                        routine = routine,
                        onToggle = { onToggle(routine.id, it) },
                        onClick = { onEdit(routine.id) },
                        onLongClick = { routineToDelete = routine.id },
                        onDuplicate = { onDuplicate(routine.id) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        FilledTonalButton(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.routines_create))
        }

        Spacer(Modifier.height(32.dp))
    }

    routineToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            title = { Text(stringResource(R.string.routines_delete_title)) },
            text = { Text(stringResource(R.string.routines_delete_message)) },
            confirmButton = {
                TextButton(onClick = { onDelete(id); routineToDelete = null }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

private suspend fun loadRoutines(
    context: Context,
    serializer: RoutineSerializer,
): List<Routine> = withContext(Dispatchers.IO) {
    serializer.deserializeRoutines(
        Settings.Secure.getStringForUser(
            context.contentResolver,
            SETTINGS_KEY,
            UserHandle.USER_CURRENT,
        ) ?: ""
    )
}

private suspend fun persistRoutines(
    context: Context,
    serializer: RoutineSerializer,
    routines: List<Routine>,
) = withContext(Dispatchers.IO) {
    Settings.Secure.putStringForUser(
        context.contentResolver,
        SETTINGS_KEY,
        serializer.serializeRoutines(routines),
        UserHandle.USER_CURRENT,
    )
}

private suspend fun readRoutinesBackup(
    context: Context,
    serializer: RoutineSerializer,
    uri: Uri,
): List<Routine> = withContext(Dispatchers.IO) {
    val json = context.contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes().toString(StandardCharsets.UTF_8)
    } ?: throw IOException()
    serializer.deserializeRoutines(json)
}

private suspend fun writeRoutinesBackup(
    context: Context,
    serializer: RoutineSerializer,
    uri: Uri,
    routines: List<Routine>,
) = withContext(Dispatchers.IO) {
    context.contentResolver.openOutputStream(uri)?.use { output ->
        output.write(serializer.serializeRoutines(routines).toByteArray(StandardCharsets.UTF_8))
    } ?: throw IOException()
}

private fun createRoutinesImportIntent(): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = JSON_MIME_TYPE
}

private fun createRoutinesExportIntent(): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = JSON_MIME_TYPE
    putExtra(Intent.EXTRA_TITLE, ROUTINES_BACKUP_FILE_NAME)
}

@Composable
private fun RoutineCard(
    routine: Routine,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDuplicate: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "card_press_scale",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    buildRoutineSummary(routine),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                routine.lastTriggeredAt?.let { ts ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        DateUtils.getRelativeTimeSpanString(
                            ts, System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                        ).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onDuplicate) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.routines_duplicate),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ExpressiveSwitch(
                checked = routine.enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun buildRoutineSummary(routine: Routine): String {
    val triggerTexts = mutableListOf<String>()
    for (trigger in routine.triggers) {
        triggerTexts += describeTrigger(trigger)
    }
    val triggerText = triggerTexts.joinToString(", ")
    val actionTexts = mutableListOf<String>()
    for (action in routine.actions) {
        actionTexts += describeAction(action)
    }
    val actionText = actionTexts.joinToString(", ")
    return "$triggerText → $actionText"
}

@Composable
internal fun describeTrigger(trigger: Trigger): String = when (trigger) {
    is Trigger.TimeOfDay -> describeTimeOfDay(trigger)
    is Trigger.Interval -> "Every ${trigger.intervalMinutes}m"
    is Trigger.ChargingState -> if (trigger.charging) "Charging" else "Unplugged"
    is Trigger.BatteryLevel -> "Battery ${trigger.direction.name.lowercase()} ${trigger.threshold}%"
    is Trigger.WifiState -> {
        val suffix = trigger.ssidPattern?.let { " (~$it)" }
            ?: trigger.ssid?.let { " ($it)" } ?: ""
        if (trigger.connected) "WiFi on$suffix" else "WiFi off$suffix"
    }
    is Trigger.BluetoothState -> if (trigger.connected) "BT on" else "BT off"
    is Trigger.ScreenState -> if (trigger.on) "Screen on" else "Screen off"
    is Trigger.FeatureState -> {
        val name = KNOWN_FEATURES[trigger.feature] ?: trigger.feature
        if (trigger.active) "$name on" else "$name off"
    }
    is Trigger.HeadphonesState -> if (trigger.connected) "Headphones in" else "Headphones out"
    is Trigger.RingerMode -> when (trigger.mode) {
        AudioManager.RINGER_MODE_SILENT -> "Silent mode"
        AudioManager.RINGER_MODE_VIBRATE -> "Vibrate mode"
        else -> "Normal mode"
    }
    is Trigger.IncomingCall -> if (trigger.phoneNumbers.isEmpty()) {
        stringResource(R.string.routines_incoming_call_any_summary)
    } else {
        stringResource(
            R.string.routines_incoming_call_numbers_summary,
            trigger.phoneNumbers.joinToString(", "),
        )
    }
    is Trigger.SmsMessage -> stringResource(R.string.routines_sms_message_summary, trigger.text)
    is Trigger.AppLaunch -> "Open ${trigger.packageName.substringAfterLast('.')}"
    is Trigger.AppClose -> "Close ${trigger.packageName.substringAfterLast('.')}"
    is Trigger.SensorPrivacyState -> {
        val sensor = if (trigger.sensor == SENSOR_CAMERA) "Camera" else "Mic"
        if (trigger.blocked) "$sensor blocked" else "$sensor unblocked"
    }
    is Trigger.Location -> {
        val action = if (trigger.entering) "Enter" else "Leave"
        "$action (${String.format("%.4f", trigger.latitude)}, ${String.format("%.4f", trigger.longitude)})"
    }
    is Trigger.CaptivePortal -> trigger.ssid?.let { "Captive portal ($it)" } ?: "Captive portal"
    is Trigger.NfcTag -> {
        val name = trigger.tagName?.takeIf { it.isNotBlank() } ?: trigger.tagId
        "Tap NFC \"$name\""
    }
}

@Composable
internal fun describeAction(action: Action): String = when (action) {
    is Action.SetFeature -> {
        val name = KNOWN_FEATURES[action.feature] ?: action.feature
        if (action.enabled) "Enable $name" else "Disable $name"
    }
    is Action.ToggleFeature -> "Toggle ${KNOWN_FEATURES[action.feature] ?: action.feature}"
    is Action.SetVolume -> {
        val audioManager = LocalContext.current.getSystemService(AudioManager::class.java)
        stringResource(
            R.string.routines_volume_action_summary,
            volumeStreamLabel(action.streamType),
            appliedVolumePercent(audioManager, action.streamType, action.level),
        )
    }
    is Action.SetBrightness -> "Brightness ${action.level * 100 / 255}%"
    is Action.SetRingerMode -> when (action.mode) {
        AudioManager.RINGER_MODE_SILENT -> "Silent"
        AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
        else -> "Normal ringer"
    }
    is Action.LaunchApp -> {
        val appName = action.packageName.substringAfterLast('.')
        when (action.launchMode) {
            Action.LaunchApp.LaunchMode.FREEFORM -> "Open $appName (Freeform)"
            Action.LaunchApp.LaunchMode.BUBBLE -> "Open $appName (Bubble)"
            else -> "Open $appName"
        }
    }
    is Action.SendBroadcast -> "Broadcast"
    is Action.ShowNotification -> "Notify: ${action.title}"
    is Action.Delay -> "Wait ${action.durationMs / 1000}s"
    is Action.SetSetting -> "Set ${action.key}"
    is Action.SetSensorPrivacy -> {
        val sensor = if (action.sensor == SENSOR_CAMERA) "Camera" else "Mic"
        if (action.blocked) "Block $sensor" else "Unblock $sensor"
    }
    is Action.PlaySound -> if (action.uri != null) {
        "Play custom sound"
    } else when (action.soundType) {
        SOUND_TYPE_ALARM -> "Play alarm"
        SOUND_TYPE_RINGTONE -> "Play ringtone"
        else -> "Play notification"
    }
    is Action.SendLocationSms -> action.phoneNumber?.takeIf { it.isNotBlank() }?.let {
        stringResource(R.string.routines_send_location_sms_number_summary, it)
    } ?: stringResource(R.string.routines_send_location_sms_summary)
    is Action.HttpRequest -> "${action.method} ${action.url.take(40)}"
}

@Composable
private fun volumeStreamLabel(streamType: Int): String = stringResource(
    when (streamType) {
        AudioManager.STREAM_MUSIC -> R.string.routines_stream_media
        AudioManager.STREAM_RING -> R.string.routines_stream_ring
        AudioManager.STREAM_NOTIFICATION -> R.string.routines_stream_notification
        AudioManager.STREAM_ALARM -> R.string.routines_stream_alarm
        else -> R.string.routines_stream_system
    }
)

private val WEEKDAYS = setOf(
    Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
    Calendar.THURSDAY, Calendar.FRIDAY,
)
private val WEEKENDS = setOf(Calendar.SATURDAY, Calendar.SUNDAY)

private val DAY_SHORT_NAMES = mapOf(
    Calendar.SUNDAY to "Sun", Calendar.MONDAY to "Mon",
    Calendar.TUESDAY to "Tue", Calendar.WEDNESDAY to "Wed",
    Calendar.THURSDAY to "Thu", Calendar.FRIDAY to "Fri",
    Calendar.SATURDAY to "Sat",
)

private fun describeTimeOfDay(trigger: Trigger.TimeOfDay): String {
    val time = "%02d:%02d".format(trigger.hour, trigger.minute)
    val days = trigger.daysOfWeek
    return when {
        days.size >= 7 || days == Trigger.ALL_DAYS -> "Daily at $time"
        days == WEEKDAYS -> "Weekdays at $time"
        days == WEEKENDS -> "Weekends at $time"
        else -> {
            val dayStr = listOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY,
            ).filter { it in days }.mapNotNull { DAY_SHORT_NAMES[it] }.joinToString(", ")
            "$dayStr at $time"
        }
    }
}
