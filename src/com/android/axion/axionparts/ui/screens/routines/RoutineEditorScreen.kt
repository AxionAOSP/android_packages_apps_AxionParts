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
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import com.android.axion.compose.preferences.ExpressiveSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.compose.applist.AppFilter
import com.android.axion.compose.applist.rememberFilteredAppList
import java.util.Calendar
import java.util.UUID
import kotlin.math.roundToInt

private data class TypeOption(
    val type: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun triggerOptions() = listOf(
    TypeOption(Trigger.TYPE_TIME_OF_DAY, stringResource(R.string.routines_time_of_day), Icons.Default.AccessTime),
    TypeOption(Trigger.TYPE_INTERVAL, stringResource(R.string.routines_interval), Icons.Default.Timer),
    TypeOption(Trigger.TYPE_CHARGING_STATE, stringResource(R.string.routines_charging), Icons.Default.BatteryChargingFull),
    TypeOption(Trigger.TYPE_BATTERY_LEVEL, stringResource(R.string.routines_battery_level), Icons.Default.BatteryStd),
    TypeOption(Trigger.TYPE_WIFI_STATE, stringResource(R.string.routines_wifi), Icons.Default.Wifi),
    TypeOption(Trigger.TYPE_BLUETOOTH_STATE, stringResource(R.string.routines_bluetooth), Icons.Default.Bluetooth),
    TypeOption(Trigger.TYPE_SCREEN_STATE, stringResource(R.string.routines_screen), Icons.Default.PhoneAndroid),
    TypeOption(Trigger.TYPE_FEATURE_STATE, stringResource(R.string.routines_feature_state), Icons.Default.ToggleOn),
    TypeOption(Trigger.TYPE_HEADPHONES_STATE, stringResource(R.string.routines_headphones), Icons.Default.Headphones),
    TypeOption(Trigger.TYPE_RINGER_MODE, stringResource(R.string.routines_ringer_mode), Icons.Default.VolumeUp),
    TypeOption(Trigger.TYPE_INCOMING_CALL, stringResource(R.string.routines_incoming_call), Icons.Default.PhoneAndroid),
    TypeOption(Trigger.TYPE_SMS_MESSAGE, stringResource(R.string.routines_sms_message), Icons.Default.Send),
    TypeOption(Trigger.TYPE_APP_LAUNCH, stringResource(R.string.routines_app_launch), Icons.Default.OpenInNew),
    TypeOption(Trigger.TYPE_APP_CLOSE, stringResource(R.string.routines_app_close), Icons.Default.Close),
    TypeOption(Trigger.TYPE_SENSOR_PRIVACY_STATE, stringResource(R.string.routines_sensor_privacy), Icons.Default.CameraAlt),
    TypeOption(Trigger.TYPE_LOCATION, stringResource(R.string.routines_location), Icons.Default.LocationOn),
    TypeOption(Trigger.TYPE_CAPTIVE_PORTAL, stringResource(R.string.routines_captive_portal), Icons.Default.WifiOff),
)

@Composable
private fun actionOptions() = listOf(
    TypeOption(Action.TYPE_SET_FEATURE, stringResource(R.string.routines_set_feature), Icons.Default.ToggleOn),
    TypeOption(Action.TYPE_TOGGLE_FEATURE, stringResource(R.string.routines_toggle_feature), Icons.Default.ToggleOn),
    TypeOption(Action.TYPE_SET_VOLUME, stringResource(R.string.routines_set_volume), Icons.Default.VolumeUp),
    TypeOption(Action.TYPE_SET_BRIGHTNESS, stringResource(R.string.routines_set_brightness), Icons.Default.BrightnessHigh),
    TypeOption(Action.TYPE_SET_RINGER_MODE, stringResource(R.string.routines_set_ringer_mode), Icons.Default.VolumeUp),
    TypeOption(Action.TYPE_LAUNCH_APP, stringResource(R.string.routines_launch_app), Icons.Default.OpenInNew),
    TypeOption(Action.TYPE_SEND_BROADCAST, stringResource(R.string.routines_send_broadcast), Icons.Default.Send),
    TypeOption(Action.TYPE_SHOW_NOTIFICATION, stringResource(R.string.routines_show_notification), Icons.Default.Notifications),
    TypeOption(Action.TYPE_DELAY, stringResource(R.string.routines_delay), Icons.Default.HourglassEmpty),
    TypeOption(Action.TYPE_SET_SETTING, stringResource(R.string.routines_set_setting), Icons.Default.Settings),
    TypeOption(Action.TYPE_SET_SENSOR_PRIVACY, stringResource(R.string.routines_set_sensor_privacy), Icons.Default.CameraAlt),
    TypeOption(Action.TYPE_PLAY_SOUND, stringResource(R.string.routines_play_sound), Icons.Default.VolumeUp),
    TypeOption(Action.TYPE_SEND_LOCATION_SMS, stringResource(R.string.routines_send_location_sms), Icons.Default.LocationOn),
    TypeOption(Action.TYPE_HTTP_REQUEST, stringResource(R.string.routines_http_request), Icons.Default.Language),
)

@Composable
private fun conditionOptions() = listOf(
    TypeOption(Condition.TYPE_TIME_RANGE, stringResource(R.string.routines_time_range), Icons.Default.AccessTime),
    TypeOption(Condition.TYPE_DAY_OF_WEEK, stringResource(R.string.routines_day_of_week), Icons.Default.AccessTime),
    TypeOption(Condition.TYPE_BATTERY_RANGE, stringResource(R.string.routines_battery_range), Icons.Default.BatteryStd),
    TypeOption(Condition.TYPE_CHARGING_STATE, stringResource(R.string.routines_charging), Icons.Default.BatteryChargingFull),
    TypeOption(Condition.TYPE_WIFI_CONNECTED, stringResource(R.string.routines_wifi_connected), Icons.Default.Wifi),
    TypeOption(Condition.TYPE_BLUETOOTH_CONNECTED, stringResource(R.string.routines_bluetooth_connected), Icons.Default.Bluetooth),
    TypeOption(Condition.TYPE_SCREEN_ON, stringResource(R.string.routines_screen_on_condition), Icons.Default.PhoneAndroid),
    TypeOption(Condition.TYPE_FEATURE_ACTIVE, stringResource(R.string.routines_feature_active), Icons.Default.ToggleOn),
    TypeOption(Condition.TYPE_SENSOR_BLOCKED, stringResource(R.string.routines_sensor_blocked), Icons.Default.CameraAlt),
    TypeOption(Condition.TYPE_LOCATION_NEAR, stringResource(R.string.routines_location_near), Icons.Default.LocationOn),
    TypeOption(Condition.TYPE_IP_ADDRESS, stringResource(R.string.routines_ip_address), Icons.Default.Router),
)

@Composable
fun RoutineEditorContent(
    modifier: Modifier,
    routine: Routine?,
    onSave: (Routine) -> Unit,
    onCancel: () -> Unit,
) {
    val id = remember { routine?.id ?: UUID.randomUUID().toString() }
    var name by remember { mutableStateOf(routine?.name ?: "") }
    var triggers by remember { mutableStateOf(routine?.triggers ?: emptyList()) }
    var conditions by remember { mutableStateOf(routine?.conditions ?: emptyList()) }
    var actions by remember { mutableStateOf(routine?.actions ?: emptyList()) }

    var showTriggerPicker by remember { mutableStateOf(false) }
    var configuringTriggerType by remember { mutableStateOf<String?>(null) }
    var editingTriggerIndex by remember { mutableStateOf<Int?>(null) }
    var showActionPicker by remember { mutableStateOf(false) }
    var configuringActionType by remember { mutableStateOf<String?>(null) }
    var editingActionIndex by remember { mutableStateOf<Int?>(null) }
    var showConditionPicker by remember { mutableStateOf(false) }
    var configuringConditionType by remember { mutableStateOf<String?>(null) }
    var editingConditionIndex by remember { mutableStateOf<Int?>(null) }

    val canSave = name.isNotBlank() && triggers.isNotEmpty() && actions.isNotEmpty()
    val context = LocalContext.current

    val soundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI
            )
            if (uri != null) {
                actions = actions + Action.PlaySound(RingtoneManager.TYPE_ALL, uri.toString())
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.routines_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(Modifier.height(20.dp))

        SectionHeader(stringResource(R.string.routines_triggers))
        triggers.forEachIndexed { index, trigger ->
            ItemCard(
                text = describeTrigger(trigger),
                onClick = {
                    editingTriggerIndex = index
                    configuringTriggerType = triggerTypeOf(trigger)
                },
                onRemove = { triggers = triggers.toMutableList().also { it.removeAt(index) } },
                onMoveUp = if (index > 0) {
                    { triggers = triggers.moved(index, index - 1) }
                } else null,
                onMoveDown = if (index < triggers.lastIndex) {
                    { triggers = triggers.moved(index, index + 1) }
                } else null,
            )
        }
        AddItemButton(stringResource(R.string.routines_add_trigger)) {
            showTriggerPicker = true
        }

        Spacer(Modifier.height(16.dp))

        SectionHeader(stringResource(R.string.routines_conditions) + " (${stringResource(R.string.routines_optional)})")
        conditions.forEachIndexed { index, condition ->
            ItemCard(
                text = describeCondition(condition),
                onClick = {
                    editingConditionIndex = index
                    configuringConditionType = conditionTypeOf(condition)
                },
                onRemove = {
                    conditions = conditions.toMutableList().also { it.removeAt(index) }
                },
                onMoveUp = if (index > 0) {
                    { conditions = conditions.moved(index, index - 1) }
                } else null,
                onMoveDown = if (index < conditions.lastIndex) {
                    { conditions = conditions.moved(index, index + 1) }
                } else null,
            )
        }
        AddItemButton(stringResource(R.string.routines_add_condition)) {
            showConditionPicker = true
        }

        Spacer(Modifier.height(16.dp))

        SectionHeader(stringResource(R.string.routines_actions))
        actions.forEachIndexed { index, action ->
            ItemCard(
                text = describeAction(action),
                onClick = {
                    if (action is Action.PlaySound) return@ItemCard
                    editingActionIndex = index
                    configuringActionType = actionTypeOf(action)
                },
                onRemove = { actions = actions.toMutableList().also { it.removeAt(index) } },
                onMoveUp = if (index > 0) {
                    { actions = actions.moved(index, index - 1) }
                } else null,
                onMoveDown = if (index < actions.lastIndex) {
                    { actions = actions.moved(index, index + 1) }
                } else null,
            )
        }
        AddItemButton(stringResource(R.string.routines_add_action)) {
            showActionPicker = true
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(
                    Routine(
                        id = id,
                        name = name.trim(),
                        triggers = triggers,
                        conditions = conditions,
                        actions = actions,
                        createdAt = routine?.createdAt ?: System.currentTimeMillis(),
                        lastTriggeredAt = routine?.lastTriggeredAt,
                    )
                )
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save))
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showTriggerPicker) {
        TypePickerDialog(
            title = stringResource(R.string.routines_select_trigger),
            options = triggerOptions(),
            onSelect = { type ->
                showTriggerPicker = false
                configuringTriggerType = type
            },
            onDismiss = { showTriggerPicker = false },
        )
    }

    configuringTriggerType?.let { type ->
        val editIdx = editingTriggerIndex
        TriggerConfigDialog(
            type = type,
            initial = editIdx?.let { triggers.getOrNull(it) },
            onConfirm = { trigger ->
                triggers = if (editIdx != null) {
                    triggers.toMutableList().also { it[editIdx] = trigger }
                } else {
                    triggers + trigger
                }
                configuringTriggerType = null
                editingTriggerIndex = null
            },
            onDismiss = {
                configuringTriggerType = null
                editingTriggerIndex = null
            },
        )
    }

    if (showActionPicker) {
        TypePickerDialog(
            title = stringResource(R.string.routines_select_action),
            options = actionOptions(),
            onSelect = { type ->
                showActionPicker = false
                if (type == Action.TYPE_PLAY_SOUND) {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                    }
                    soundPicker.launch(intent)
                } else {
                    configuringActionType = type
                }
            },
            onDismiss = { showActionPicker = false },
        )
    }

    configuringActionType?.let { type ->
        val editIdx = editingActionIndex
        ActionConfigDialog(
            type = type,
            initial = editIdx?.let { actions.getOrNull(it) },
            onConfirm = { action ->
                actions = if (editIdx != null) {
                    actions.toMutableList().also { it[editIdx] = action }
                } else {
                    actions + action
                }
                configuringActionType = null
                editingActionIndex = null
            },
            onDismiss = {
                configuringActionType = null
                editingActionIndex = null
            },
        )
    }

    if (showConditionPicker) {
        TypePickerDialog(
            title = stringResource(R.string.routines_select_condition),
            options = conditionOptions(),
            onSelect = { type ->
                showConditionPicker = false
                configuringConditionType = type
            },
            onDismiss = { showConditionPicker = false },
        )
    }

    configuringConditionType?.let { type ->
        val editIdx = editingConditionIndex
        ConditionConfigDialog(
            type = type,
            initial = editIdx?.let { conditions.getOrNull(it) },
            onConfirm = { condition ->
                conditions = if (editIdx != null) {
                    conditions.toMutableList().also { it[editIdx] = condition }
                } else {
                    conditions + condition
                }
                configuringConditionType = null
                editingConditionIndex = null
            },
            onDismiss = {
                configuringConditionType = null
                editingConditionIndex = null
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun ItemCard(
    text: String,
    onClick: () -> Unit = {},
    onRemove: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (onMoveUp != null) {
                IconButton(onClick = { onMoveUp() }, enabled = true) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.routines_move_up),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (onMoveDown != null) {
                IconButton(onClick = { onMoveDown() }, enabled = true) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.routines_move_down),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from == to || from !in indices || to !in indices) return this
    val mutable = toMutableList()
    val item = mutable.removeAt(from)
    mutable.add(to, item)
    return mutable.toList()
}

@Composable
private fun AddItemButton(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
private fun TypePickerDialog(
    title: String,
    options: List<TypeOption>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(options) { option ->
                    ListItem(
                        headlineContent = { Text(option.label) },
                        leadingContent = {
                            Icon(option.icon, contentDescription = null)
                        },
                        modifier = Modifier.clickable { onSelect(option.type) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TriggerConfigDialog(
    type: String,
    initial: Trigger? = null,
    onConfirm: (Trigger) -> Unit,
    onDismiss: () -> Unit,
) {
    val confirmLabel = stringResource(if (initial != null) R.string.save else R.string.add)
    when (type) {
        Trigger.TYPE_TIME_OF_DAY -> {
            val init = initial as? Trigger.TimeOfDay
            val timeState = rememberTimePickerState(init?.hour ?: 8, init?.minute ?: 0, true)
            var selectedDays by remember {
                mutableStateOf(init?.daysOfWeek ?: Trigger.ALL_DAYS)
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Column {
                        Text(stringResource(R.string.routines_time_of_day))
                        Text(
                            stringResource(R.string.routines_schedule_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        TimePicker(state = timeState)
                        Spacer(Modifier.height(12.dp))
                        DayOfWeekSelector(
                            selectedDays = selectedDays,
                            onDaysChanged = { selectedDays = it },
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(
                            Trigger.TimeOfDay(
                                timeState.hour, timeState.minute, selectedDays,
                            )
                        )
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_INTERVAL -> {
            var minutes by remember {
                mutableIntStateOf((initial as? Trigger.Interval)?.intervalMinutes ?: 30)
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_interval)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = minutes.toString(),
                            onValueChange = { minutes = it.toIntOrNull() ?: minutes },
                            label = { Text(stringResource(R.string.routines_minutes)) },
                            singleLine = true,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(Trigger.Interval(minutes.coerceAtLeast(1)))
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_CHARGING_STATE -> BooleanTriggerDialog(
            title = stringResource(R.string.routines_charging),
            labelTrue = stringResource(R.string.routines_charging_starts),
            labelFalse = stringResource(R.string.routines_charging_stops),
            onConfirm = { onConfirm(Trigger.ChargingState(it)) },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_BATTERY_LEVEL -> {
            val init = initial as? Trigger.BatteryLevel
            var threshold by remember { mutableFloatStateOf(init?.threshold?.toFloat() ?: 20f) }
            var isBelow by remember {
                mutableStateOf(init?.direction == Trigger.BatteryLevel.Direction.BELOW ||
                    init == null)
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_battery_level)) },
                text = {
                    Column {
                        Text("${threshold.toInt()}%")
                        Slider(
                            value = threshold,
                            onValueChange = { threshold = it },
                            valueRange = 5f..95f,
                            steps = 17,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = isBelow,
                                onClick = { isBelow = true },
                                label = { Text(stringResource(R.string.routines_below)) },
                            )
                            FilterChip(
                                selected = !isBelow,
                                onClick = { isBelow = false },
                                label = { Text(stringResource(R.string.routines_above)) },
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(
                            Trigger.BatteryLevel(
                                threshold.toInt(),
                                if (isBelow) Trigger.BatteryLevel.Direction.BELOW
                                else Trigger.BatteryLevel.Direction.ABOVE,
                            )
                        )
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_WIFI_STATE -> {
            val init = initial as? Trigger.WifiState
            var connected by remember { mutableStateOf(init?.connected ?: true) }
            var ssid by remember { mutableStateOf(init?.ssid ?: "") }
            var ssidPattern by remember { mutableStateOf(init?.ssidPattern ?: "") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_wifi)) },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.routines_connected), modifier = Modifier.weight(1f))
                            ExpressiveSwitch(checked = connected, onCheckedChange = { connected = it })
                        }
                        OutlinedTextField(
                            value = ssid,
                            onValueChange = { ssid = it },
                            label = { Text(stringResource(R.string.routines_ssid_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ssidPattern,
                            onValueChange = { ssidPattern = it },
                            label = { Text(stringResource(R.string.routines_ssid_pattern_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(
                            Trigger.WifiState(
                                connected,
                                ssid.takeIf { it.isNotBlank() },
                                ssidPattern.takeIf { it.isNotBlank() },
                            )
                        )
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_BLUETOOTH_STATE -> {
            val init = initial as? Trigger.BluetoothState
            var connected by remember { mutableStateOf(init?.connected ?: true) }
            var address by remember { mutableStateOf(init?.deviceAddress ?: "") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_bluetooth)) },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.routines_connected), modifier = Modifier.weight(1f))
                            ExpressiveSwitch(checked = connected, onCheckedChange = { connected = it })
                        }
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(stringResource(R.string.routines_device_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(
                            Trigger.BluetoothState(connected, address.takeIf { it.isNotBlank() })
                        )
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_SCREEN_STATE -> BooleanTriggerDialog(
            title = stringResource(R.string.routines_screen),
            labelTrue = stringResource(R.string.routines_screen_on),
            labelFalse = stringResource(R.string.routines_screen_off),
            onConfirm = { onConfirm(Trigger.ScreenState(it)) },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_FEATURE_STATE -> FeatureSelectDialog(
            title = stringResource(R.string.routines_feature_state),
            showToggle = true,
            initialFeature = (initial as? Trigger.FeatureState)?.feature,
            initialEnabled = (initial as? Trigger.FeatureState)?.active ?: true,
            confirmLabel = confirmLabel,
            onConfirm = { feature, active ->
                onConfirm(Trigger.FeatureState(feature, active))
            },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_HEADPHONES_STATE -> BooleanTriggerDialog(
            title = stringResource(R.string.routines_headphones),
            labelTrue = stringResource(R.string.routines_headphones_connected),
            labelFalse = stringResource(R.string.routines_headphones_disconnected),
            onConfirm = { onConfirm(Trigger.HeadphonesState(it)) },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_RINGER_MODE -> RingerModeDialog(
            onConfirm = { onConfirm(Trigger.RingerMode(it)) },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_INCOMING_CALL -> {
            val init = initial as? Trigger.IncomingCall
            var phoneNumbers by remember {
                mutableStateOf(init?.phoneNumbers?.joinToString(", ") ?: "")
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_incoming_call)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = phoneNumbers,
                            onValueChange = { phoneNumbers = it },
                            label = { Text(stringResource(R.string.routines_phone_numbers_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.routines_phone_numbers_optional_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(Trigger.IncomingCall(parsePhoneNumbers(phoneNumbers)))
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_SMS_MESSAGE -> {
            val init = initial as? Trigger.SmsMessage
            var text by remember { mutableStateOf(init?.text ?: "") }
            var senderNumbers by remember {
                mutableStateOf(init?.senderNumbers?.joinToString(", ") ?: "")
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_sms_message)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text(stringResource(R.string.routines_sms_phrase_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = senderNumbers,
                            onValueChange = { senderNumbers = it },
                            label = { Text(stringResource(R.string.routines_sms_senders_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.routines_sms_senders_optional_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onConfirm(
                                Trigger.SmsMessage(
                                    text.trim(),
                                    parsePhoneNumbers(senderNumbers),
                                )
                            )
                        },
                        enabled = text.isNotBlank(),
                    ) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_APP_LAUNCH -> AppPickerDialog(
            title = stringResource(R.string.routines_app_launch),
            onConfirm = { onConfirm(Trigger.AppLaunch(it)) },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_APP_CLOSE -> AppPickerDialog(
            title = stringResource(R.string.routines_app_close),
            onConfirm = { onConfirm(Trigger.AppClose(it)) },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_SENSOR_PRIVACY_STATE -> SensorPrivacyTriggerDialog(
            initialSensor = (initial as? Trigger.SensorPrivacyState)?.sensor ?: SENSOR_CAMERA,
            initialBlocked = (initial as? Trigger.SensorPrivacyState)?.blocked ?: true,
            confirmLabel = confirmLabel,
            onConfirm = { sensor, blocked ->
                onConfirm(Trigger.SensorPrivacyState(sensor, blocked))
            },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_LOCATION -> LocationTriggerDialog(
            initial = initial as? Trigger.Location,
            confirmLabel = confirmLabel,
            onConfirm = { lat, lng, radius, entering ->
                onConfirm(Trigger.Location(lat, lng, radius, entering))
            },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_CAPTIVE_PORTAL -> {
            var ssid by remember {
                mutableStateOf((initial as? Trigger.CaptivePortal)?.ssid ?: "")
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_captive_portal)) },
                text = {
                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        label = { Text(stringResource(R.string.routines_ssid_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(Trigger.CaptivePortal(ssid.takeIf { it.isNotBlank() }))
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun ActionConfigDialog(
    type: String,
    initial: Action? = null,
    onConfirm: (Action) -> Unit,
    onDismiss: () -> Unit,
) {
    val confirmLabel = stringResource(if (initial != null) R.string.save else R.string.add)
    when (type) {
        Action.TYPE_SET_FEATURE -> FeatureSelectDialog(
            title = stringResource(R.string.routines_set_feature),
            showToggle = true,
            initialFeature = (initial as? Action.SetFeature)?.feature,
            initialEnabled = (initial as? Action.SetFeature)?.enabled ?: true,
            confirmLabel = confirmLabel,
            onConfirm = { feature, enabled ->
                onConfirm(Action.SetFeature(feature, enabled))
            },
            onDismiss = onDismiss,
        )

        Action.TYPE_TOGGLE_FEATURE -> FeatureSelectDialog(
            title = stringResource(R.string.routines_toggle_feature),
            showToggle = false,
            initialFeature = (initial as? Action.ToggleFeature)?.feature,
            confirmLabel = confirmLabel,
            onConfirm = { feature, _ -> onConfirm(Action.ToggleFeature(feature)) },
            onDismiss = onDismiss,
        )

        Action.TYPE_SET_VOLUME -> {
            val init = initial as? Action.SetVolume
            val context = LocalContext.current
            val audioManager = remember(context) {
                context.getSystemService(AudioManager::class.java)
            }
            var streamType by remember {
                mutableIntStateOf(init?.streamType ?: AudioManager.STREAM_MUSIC)
            }
            var level by remember { mutableIntStateOf(init?.level ?: 50) }
            val streams = listOf(
                AudioManager.STREAM_MUSIC to stringResource(R.string.routines_stream_media),
                AudioManager.STREAM_RING to stringResource(R.string.routines_stream_ring),
                AudioManager.STREAM_NOTIFICATION to stringResource(R.string.routines_stream_notification),
                AudioManager.STREAM_ALARM to stringResource(R.string.routines_stream_alarm),
                AudioManager.STREAM_SYSTEM to stringResource(R.string.routines_stream_system),
            )
            val minLevel = audioManager?.getStreamMinVolume(streamType) ?: 0
            val maxLevel = audioManager?.getStreamMaxVolume(streamType) ?: 100
            val streamLevel = streamLevelForVolumePercent(level, minLevel, maxLevel)
            val displayPercent = volumePercentForStreamLevel(streamLevel, minLevel, maxLevel)
            val streamSteps = (maxLevel - minLevel - 1).coerceAtLeast(0)
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_set_volume)) },
                text = {
                    Column {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            streams.forEach { (type, label) ->
                                FilterChip(
                                    selected = streamType == type,
                                    onClick = { streamType = type },
                                    label = { Text(label) },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("$displayPercent%")
                        Slider(
                            value = streamLevel.toFloat(),
                            onValueChange = {
                                level = volumePercentForStreamLevel(
                                    it.roundToInt(),
                                    minLevel,
                                    maxLevel,
                                )
                            },
                            valueRange = minLevel.toFloat()..maxLevel.toFloat(),
                            steps = streamSteps,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(Action.SetVolume(streamType, displayPercent))
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SET_BRIGHTNESS -> {
            var level by remember {
                mutableFloatStateOf((initial as? Action.SetBrightness)?.level?.toFloat() ?: 128f)
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_set_brightness)) },
                text = {
                    Column {
                        Text("${(level * 100 / 255).toInt()}%")
                        Slider(
                            value = level,
                            onValueChange = { level = it },
                            valueRange = 0f..255f,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(Action.SetBrightness(level.toInt()))
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SET_RINGER_MODE -> RingerModeDialog(
            onConfirm = { onConfirm(Action.SetRingerMode(it)) },
            onDismiss = onDismiss,
        )

        Action.TYPE_LAUNCH_APP -> AppPickerDialog(
            title = stringResource(R.string.routines_launch_app),
            onConfirm = { onConfirm(Action.LaunchApp(it)) },
            onDismiss = onDismiss,
        )

        Action.TYPE_SEND_BROADCAST -> {
            val init = initial as? Action.SendBroadcast
            var action by remember { mutableStateOf(init?.action ?: "") }
            var mode by remember { mutableStateOf(init?.mode ?: Action.SendBroadcast.Mode.BROADCAST) }
            var componentPackage by remember { mutableStateOf(init?.componentPackage ?: "") }
            var componentClass by remember { mutableStateOf(init?.componentClass ?: "") }
            var extras by remember { mutableStateOf(init?.extras ?: emptyMap()) }
            val canSave = action.isNotBlank() ||
                (componentPackage.isNotBlank() && componentClass.isNotBlank())
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_send_broadcast)) },
                text = {
                    Column(
                        modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = mode == Action.SendBroadcast.Mode.BROADCAST,
                                onClick = { mode = Action.SendBroadcast.Mode.BROADCAST },
                                label = { Text(stringResource(R.string.routines_intent_mode_broadcast)) },
                            )
                            FilterChip(
                                selected = mode == Action.SendBroadcast.Mode.START_SERVICE,
                                onClick = { mode = Action.SendBroadcast.Mode.START_SERVICE },
                                label = { Text(stringResource(R.string.routines_intent_mode_service)) },
                            )
                            FilterChip(
                                selected = mode == Action.SendBroadcast.Mode.START_FOREGROUND_SERVICE,
                                onClick = { mode = Action.SendBroadcast.Mode.START_FOREGROUND_SERVICE },
                                label = { Text(stringResource(R.string.routines_intent_mode_fg_service)) },
                            )
                        }
                        OutlinedTextField(
                            value = action,
                            onValueChange = { action = it },
                            label = { Text(stringResource(R.string.routines_broadcast_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = componentPackage,
                            onValueChange = { componentPackage = it },
                            label = { Text(stringResource(R.string.routines_intent_component_package)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = componentClass,
                            onValueChange = { componentClass = it },
                            label = { Text(stringResource(R.string.routines_intent_component_class)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = stringResource(R.string.routines_intent_extras_label),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        IntentExtrasEditor(
                            extras = extras,
                            onExtrasChange = { extras = it },
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onConfirm(
                                Action.SendBroadcast(
                                    action = action.trim().takeIf { it.isNotBlank() },
                                    mode = mode,
                                    componentPackage = componentPackage.trim().takeIf { it.isNotBlank() },
                                    componentClass = componentClass.trim().takeIf { it.isNotBlank() },
                                    extras = extras,
                                )
                            )
                        },
                        enabled = canSave,
                    ) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SHOW_NOTIFICATION -> {
            val init = initial as? Action.ShowNotification
            var title by remember { mutableStateOf(init?.title ?: "") }
            var text by remember { mutableStateOf(init?.text ?: "") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_show_notification)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(R.string.routines_notif_title_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text(stringResource(R.string.routines_notif_text_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onConfirm(Action.ShowNotification(title.trim(), text.trim()))
                        },
                        enabled = title.isNotBlank(),
                    ) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_DELAY -> {
            var seconds by remember {
                mutableIntStateOf(((initial as? Action.Delay)?.durationMs?.div(1000))?.toInt() ?: 5)
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_delay)) },
                text = {
                    OutlinedTextField(
                        value = seconds.toString(),
                        onValueChange = { seconds = it.toIntOrNull() ?: seconds },
                        label = { Text(stringResource(R.string.routines_seconds)) },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(Action.Delay(seconds.coerceAtLeast(1) * 1000L))
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SET_SETTING -> {
            val init = initial as? Action.SetSetting
            var table by remember {
                mutableStateOf(init?.table ?: Action.SetSetting.SettingsTable.SECURE)
            }
            var key by remember { mutableStateOf(init?.key ?: "") }
            var value by remember { mutableStateOf(init?.value ?: "") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_set_setting)) },
                text = {
                    Column {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Action.SetSetting.SettingsTable.entries.forEach { t ->
                                FilterChip(
                                    selected = table == t,
                                    onClick = { table = t },
                                    label = { Text(t.name) },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = key,
                            onValueChange = { key = it },
                            label = { Text(stringResource(R.string.routines_setting_key_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            label = { Text(stringResource(R.string.routines_setting_value_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onConfirm(
                                Action.SetSetting(table, key.trim(), value.trim())
                            )
                        },
                        enabled = key.isNotBlank(),
                    ) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SET_SENSOR_PRIVACY -> SensorPrivacyActionDialog(
            initialSensor = (initial as? Action.SetSensorPrivacy)?.sensor ?: SENSOR_CAMERA,
            initialBlocked = (initial as? Action.SetSensorPrivacy)?.blocked ?: true,
            confirmLabel = confirmLabel,
            onConfirm = { sensor, blocked ->
                onConfirm(Action.SetSensorPrivacy(sensor, blocked))
            },
            onDismiss = onDismiss,
        )

        Action.TYPE_SEND_LOCATION_SMS -> {
            val init = initial as? Action.SendLocationSms
            var phoneNumber by remember { mutableStateOf(init?.phoneNumber ?: "") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_send_location_sms)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text(stringResource(R.string.routines_phone_number_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.routines_send_location_sms_target_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(
                            Action.SendLocationSms(
                                phoneNumber.trim().takeIf { it.isNotBlank() }
                            )
                        )
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_HTTP_REQUEST -> {
            val init = initial as? Action.HttpRequest
            var url by remember { mutableStateOf(init?.url ?: "") }
            var method by remember { mutableStateOf(init?.method ?: Action.METHOD_GET) }
            var headersText by remember {
                mutableStateOf(init?.headers?.entries?.joinToString("\n") { "${it.key}: ${it.value}" } ?: "")
            }
            var body by remember { mutableStateOf(init?.body ?: "") }
            var timeoutSec by remember {
                mutableStateOf(((init?.timeoutMs ?: 15000) / 1000).toString())
            }
            var ignoreSsl by remember { mutableStateOf(init?.ignoreSslErrors ?: false) }
            var requireValidated by remember {
                mutableStateOf(init?.requireValidatedInternet ?: true)
            }
            val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_http_request)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text(stringResource(R.string.routines_url_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            methods.forEach { m ->
                                FilterChip(
                                    selected = method == m,
                                    onClick = { method = m },
                                    label = { Text(m) },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = headersText,
                            onValueChange = { headersText = it },
                            label = { Text("Headers (Key: Value)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                        )
                        if (method != "GET") {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = body,
                                onValueChange = { body = it },
                                label = { Text(stringResource(R.string.routines_body_hint)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4,
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = timeoutSec,
                            onValueChange = { timeoutSec = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(R.string.routines_timeout_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.routines_http_ignore_ssl),
                                modifier = Modifier.weight(1f),
                            )
                            ExpressiveSwitch(
                                checked = ignoreSsl,
                                onCheckedChange = { ignoreSsl = it },
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.routines_http_require_validated))
                                Text(
                                    stringResource(R.string.routines_http_require_validated_summary),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            ExpressiveSwitch(
                                checked = requireValidated,
                                onCheckedChange = { requireValidated = it },
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val headers = headersText.lines()
                                .filter { it.contains(":") }
                                .associate {
                                    val (k, v) = it.split(":", limit = 2)
                                    k.trim() to v.trim()
                                }
                            val timeout = (timeoutSec.toIntOrNull() ?: 15)
                                .coerceIn(1, Action.MAX_HTTP_TIMEOUT_MS / 1000) * 1000
                            onConfirm(
                                Action.HttpRequest(
                                    url = url.trim(),
                                    method = method,
                                    headers = headers,
                                    body = body.takeIf { it.isNotBlank() },
                                    timeoutMs = timeout,
                                    ignoreSslErrors = ignoreSsl,
                                    requireValidatedInternet = requireValidated,
                                )
                            )
                        },
                        enabled = url.isNotBlank(),
                    ) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionConfigDialog(
    type: String,
    initial: Condition? = null,
    onConfirm: (Condition) -> Unit,
    onDismiss: () -> Unit,
) {
    val confirmLabel = stringResource(if (initial != null) R.string.save else R.string.add)
    when (type) {
        Condition.TYPE_TIME_RANGE -> {
            val init = initial as? Condition.TimeRange
            val startState = rememberTimePickerState(init?.startHour ?: 9, init?.startMinute ?: 0, true)
            val endState = rememberTimePickerState(init?.endHour ?: 17, init?.endMinute ?: 0, true)
            var showEnd by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        if (showEnd) stringResource(R.string.routines_end_time)
                        else stringResource(R.string.routines_start_time)
                    )
                },
                text = {
                    if (showEnd) TimePicker(state = endState)
                    else TimePicker(state = startState)
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (showEnd) {
                            onConfirm(
                                Condition.TimeRange(
                                    startState.hour, startState.minute,
                                    endState.hour, endState.minute,
                                )
                            )
                        } else {
                            showEnd = true
                        }
                    }) {
                        Text(
                            if (showEnd) confirmLabel
                            else stringResource(R.string.routines_next)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Condition.TYPE_DAY_OF_WEEK -> {
            var selectedDays by remember {
                mutableStateOf((initial as? Condition.DayOfWeek)?.days ?: Trigger.ALL_DAYS)
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_day_of_week)) },
                text = {
                    DayOfWeekSelector(
                        selectedDays = selectedDays,
                        onDaysChanged = { selectedDays = it },
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirm(Condition.DayOfWeek(selectedDays)) },
                        enabled = selectedDays.isNotEmpty(),
                    ) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Condition.TYPE_BATTERY_RANGE -> {
            val init = initial as? Condition.BatteryRange
            var min by remember { mutableFloatStateOf(init?.min?.toFloat() ?: 20f) }
            var max by remember { mutableFloatStateOf(init?.max?.toFloat() ?: 80f) }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_battery_range)) },
                text = {
                    Column {
                        Text("Min: ${min.toInt()}%")
                        Slider(
                            value = min,
                            onValueChange = { min = it.coerceAtMost(max) },
                            valueRange = 0f..100f,
                        )
                        Text("Max: ${max.toInt()}%")
                        Slider(
                            value = max,
                            onValueChange = { max = it.coerceAtLeast(min) },
                            valueRange = 0f..100f,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(Condition.BatteryRange(min.toInt(), max.toInt()))
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Condition.TYPE_CHARGING_STATE -> BooleanTriggerDialog(
            title = stringResource(R.string.routines_charging),
            labelTrue = stringResource(R.string.routines_while_charging),
            labelFalse = stringResource(R.string.routines_while_not_charging),
            onConfirm = { onConfirm(Condition.ChargingState(it)) },
            onDismiss = onDismiss,
        )

        Condition.TYPE_WIFI_CONNECTED -> {
            val init = initial as? Condition.WifiConnected
            var ssid by remember { mutableStateOf(init?.ssid ?: "") }
            var ssidPattern by remember { mutableStateOf(init?.ssidPattern ?: "") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_wifi_connected)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = ssid,
                            onValueChange = { ssid = it },
                            label = { Text(stringResource(R.string.routines_ssid_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ssidPattern,
                            onValueChange = { ssidPattern = it },
                            label = { Text(stringResource(R.string.routines_ssid_pattern_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(
                            Condition.WifiConnected(
                                ssid.takeIf { it.isNotBlank() },
                                ssidPattern.takeIf { it.isNotBlank() },
                            )
                        )
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Condition.TYPE_BLUETOOTH_CONNECTED -> {
            var address by remember {
                mutableStateOf((initial as? Condition.BluetoothConnected)?.deviceAddress ?: "")
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_bluetooth_connected)) },
                text = {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(stringResource(R.string.routines_device_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(
                            Condition.BluetoothConnected(address.takeIf { it.isNotBlank() })
                        )
                    }) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Condition.TYPE_SCREEN_ON -> BooleanTriggerDialog(
            title = stringResource(R.string.routines_screen),
            labelTrue = stringResource(R.string.routines_screen_is_on),
            labelFalse = stringResource(R.string.routines_screen_is_off),
            onConfirm = { onConfirm(Condition.ScreenOn(it)) },
            onDismiss = onDismiss,
        )

        Condition.TYPE_FEATURE_ACTIVE -> FeatureSelectDialog(
            title = stringResource(R.string.routines_feature_active),
            showToggle = true,
            initialFeature = (initial as? Condition.FeatureActive)?.feature,
            initialEnabled = (initial as? Condition.FeatureActive)?.active ?: true,
            confirmLabel = confirmLabel,
            onConfirm = { feature, active ->
                onConfirm(Condition.FeatureActive(feature, active))
            },
            onDismiss = onDismiss,
        )

        Condition.TYPE_SENSOR_BLOCKED -> SensorPrivacyTriggerDialog(
            initialSensor = (initial as? Condition.SensorBlocked)?.sensor ?: SENSOR_CAMERA,
            initialBlocked = (initial as? Condition.SensorBlocked)?.blocked ?: true,
            confirmLabel = confirmLabel,
            onConfirm = { sensor, blocked ->
                onConfirm(Condition.SensorBlocked(sensor, blocked))
            },
            onDismiss = onDismiss,
        )

        Condition.TYPE_LOCATION_NEAR -> LocationConditionDialog(
            initial = initial as? Condition.LocationNear,
            confirmLabel = confirmLabel,
            onConfirm = { lat, lng, radius ->
                onConfirm(Condition.LocationNear(lat, lng, radius))
            },
            onDismiss = onDismiss,
        )

        Condition.TYPE_IP_ADDRESS -> {
            val initIp = initial as? Condition.IpAddress
            var cidr by remember { mutableStateOf(initIp?.cidr ?: "") }
            var useRegex by remember { mutableStateOf(initIp?.isRegex ?: false) }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_ip_address)) },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.routines_ip_use_regex),
                                modifier = Modifier.weight(1f),
                            )
                            ExpressiveSwitch(
                                checked = useRegex,
                                onCheckedChange = { useRegex = it },
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cidr,
                            onValueChange = { cidr = it },
                            label = {
                                Text(stringResource(
                                    if (useRegex) R.string.routines_ip_regex_hint
                                    else R.string.routines_cidr_hint
                                ))
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirm(Condition.IpAddress(cidr.trim(), useRegex)) },
                        enabled = cidr.isNotBlank(),
                    ) { Text(confirmLabel) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun BooleanTriggerDialog(
    title: String,
    labelTrue: String,
    labelFalse: String,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text(labelTrue) },
                    modifier = Modifier.clickable { onConfirm(true) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(labelFalse) },
                    modifier = Modifier.clickable { onConfirm(false) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun FeatureSelectDialog(
    title: String,
    showToggle: Boolean,
    initialFeature: String? = null,
    initialEnabled: Boolean = true,
    confirmLabel: String = stringResource(R.string.add),
    onConfirm: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedFeature by remember { mutableStateOf<String?>(initialFeature) }
    var enabled by remember { mutableStateOf(initialEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (showToggle && selectedFeature != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            KNOWN_FEATURES[selectedFeature] ?: selectedFeature!!,
                            modifier = Modifier.weight(1f),
                        )
                        ExpressiveSwitch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    Spacer(Modifier.height(8.dp))
                }
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(KNOWN_FEATURES.entries.toList()) { (key, label) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            modifier = Modifier.clickable { selectedFeature = key },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            trailingContent = {
                                if (selectedFeature == key) {
                                    Icon(
                                        Icons.Default.AppShortcut,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedFeature?.let { onConfirm(it, enabled) } },
                enabled = selectedFeature != null,
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun RingerModeDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.routines_ringer_mode)) },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.routines_silent)) },
                    modifier = Modifier.clickable {
                        onConfirm(AudioManager.RINGER_MODE_SILENT)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.routines_vibrate)) },
                    modifier = Modifier.clickable {
                        onConfirm(AudioManager.RINGER_MODE_VIBRATE)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.routines_normal)) },
                    modifier = Modifier.clickable {
                        onConfirm(AudioManager.RINGER_MODE_NORMAL)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun AppPickerDialog(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val sdkApps = rememberFilteredAppList(searchQuery, AppFilter.LAUNCHABLE_ONLY, AppFilter.NO_OVERLAYS)
    val filtered = sdkApps.value.map { it.packageName to it.label }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(filtered) { (pkg, label) ->
                        ListItem(
                            headlineContent = {
                                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                Text(
                                    pkg,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            modifier = Modifier.clickable { onConfirm(pkg) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayOfWeekSelector(
    selectedDays: Set<Int>,
    onDaysChanged: (Set<Int>) -> Unit,
) {
    val dayLabels = listOf(
        Calendar.SUNDAY to "Sun",
        Calendar.MONDAY to "Mon",
        Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed",
        Calendar.THURSDAY to "Thu",
        Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat",
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dayLabels.forEach { (day, label) ->
            FilterChip(
                selected = day in selectedDays,
                onClick = {
                    val newDays = if (day in selectedDays) {
                        if (selectedDays.size > 1) selectedDays - day else selectedDays
                    } else {
                        selectedDays + day
                    }
                    onDaysChanged(newDays)
                },
                label = { Text(label) },
            )
        }
    }
}

private fun describeCondition(condition: Condition): String = when (condition) {
    is Condition.TimeRange ->
        "Between %02d:%02d - %02d:%02d".format(
            condition.startHour, condition.startMinute,
            condition.endHour, condition.endMinute,
        )
    is Condition.DayOfWeek -> {
        val dayNames = mapOf(
            Calendar.SUNDAY to "Sun", Calendar.MONDAY to "Mon",
            Calendar.TUESDAY to "Tue", Calendar.WEDNESDAY to "Wed",
            Calendar.THURSDAY to "Thu", Calendar.FRIDAY to "Fri",
            Calendar.SATURDAY to "Sat",
        )
        condition.days.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }
    is Condition.BatteryRange -> "Battery ${condition.min}%-${condition.max}%"
    is Condition.ChargingState -> if (condition.charging) "While charging" else "While not charging"
    is Condition.WifiConnected -> {
        condition.ssidPattern?.let { "WiFi: ~$it" }
            ?: condition.ssid?.let { "WiFi: $it" }
            ?: "WiFi connected"
    }
    is Condition.BluetoothConnected ->
        condition.deviceAddress?.let { "BT: $it" } ?: "Bluetooth connected"
    is Condition.ScreenOn -> if (condition.on) "Screen on" else "Screen off"
    is Condition.FeatureActive -> {
        val name = KNOWN_FEATURES[condition.feature] ?: condition.feature
        if (condition.active) "$name active" else "$name inactive"
    }
    is Condition.SensorBlocked -> {
        val sensor = if (condition.sensor == SENSOR_CAMERA) "Camera" else "Mic"
        if (condition.blocked) "$sensor blocked" else "$sensor unblocked"
    }
    is Condition.LocationNear ->
        "Near (${String.format("%.4f", condition.latitude)}, ${String.format("%.4f", condition.longitude)}) ${condition.radiusMeters.toInt()}m"
    is Condition.IpAddress -> "IP: ${condition.cidr}"
}

@Composable
private fun SensorPrivacyTriggerDialog(
    initialSensor: Int = SENSOR_CAMERA,
    initialBlocked: Boolean = true,
    confirmLabel: String = stringResource(R.string.add),
    onConfirm: (Int, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var sensor by remember { mutableIntStateOf(initialSensor) }
    var blocked by remember { mutableStateOf(initialBlocked) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.routines_sensor_privacy)) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sensor == SENSOR_CAMERA,
                        onClick = { sensor = SENSOR_CAMERA },
                        label = { Text(stringResource(R.string.routines_sensor_camera)) },
                    )
                    FilterChip(
                        selected = sensor == SENSOR_MICROPHONE,
                        onClick = { sensor = SENSOR_MICROPHONE },
                        label = { Text(stringResource(R.string.routines_sensor_microphone)) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = blocked,
                        onClick = { blocked = true },
                        label = { Text(stringResource(R.string.routines_block)) },
                    )
                    FilterChip(
                        selected = !blocked,
                        onClick = { blocked = false },
                        label = { Text(stringResource(R.string.routines_unblock)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(sensor, blocked) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun SensorPrivacyActionDialog(
    initialSensor: Int = SENSOR_CAMERA,
    initialBlocked: Boolean = true,
    confirmLabel: String = stringResource(R.string.add),
    onConfirm: (Int, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var sensor by remember { mutableIntStateOf(initialSensor) }
    var blocked by remember { mutableStateOf(initialBlocked) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.routines_set_sensor_privacy)) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sensor == SENSOR_CAMERA,
                        onClick = { sensor = SENSOR_CAMERA },
                        label = { Text(stringResource(R.string.routines_sensor_camera)) },
                    )
                    FilterChip(
                        selected = sensor == SENSOR_MICROPHONE,
                        onClick = { sensor = SENSOR_MICROPHONE },
                        label = { Text(stringResource(R.string.routines_sensor_microphone)) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = blocked,
                        onClick = { blocked = true },
                        label = { Text(stringResource(R.string.routines_block)) },
                    )
                    FilterChip(
                        selected = !blocked,
                        onClick = { blocked = false },
                        label = { Text(stringResource(R.string.routines_unblock)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(sensor, blocked) }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun LocationTriggerDialog(
    initial: Trigger.Location? = null,
    confirmLabel: String = stringResource(R.string.add),
    onConfirm: (Double, Double, Float, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var latitude by remember { mutableStateOf(initial?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initial?.longitude?.toString() ?: "") }
    var radius by remember { mutableStateOf(initial?.radiusMeters?.toInt()?.toString() ?: "200") }
    var entering by remember { mutableStateOf(initial?.entering ?: true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.routines_location)) },
        text = {
            Column {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(stringResource(R.string.routines_latitude_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(stringResource(R.string.routines_longitude_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text(stringResource(R.string.routines_radius_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = entering,
                        onClick = { entering = true },
                        label = { Text(stringResource(R.string.routines_location_enter)) },
                    )
                    FilterChip(
                        selected = !entering,
                        onClick = { entering = false },
                        label = { Text(stringResource(R.string.routines_location_exit)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = latitude.toDoubleOrNull() ?: return@TextButton
                    val lng = longitude.toDoubleOrNull() ?: return@TextButton
                    val r = radius.toFloatOrNull()?.coerceAtLeast(50f) ?: return@TextButton
                    onConfirm(lat, lng, r, entering)
                },
                enabled = latitude.toDoubleOrNull() != null && longitude.toDoubleOrNull() != null,
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun LocationConditionDialog(
    initial: Condition.LocationNear? = null,
    confirmLabel: String = stringResource(R.string.add),
    onConfirm: (Double, Double, Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var latitude by remember { mutableStateOf(initial?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(initial?.longitude?.toString() ?: "") }
    var radius by remember { mutableStateOf(initial?.radiusMeters?.toInt()?.toString() ?: "200") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.routines_location_near)) },
        text = {
            Column {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(stringResource(R.string.routines_latitude_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(stringResource(R.string.routines_longitude_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text(stringResource(R.string.routines_radius_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = latitude.toDoubleOrNull() ?: return@TextButton
                    val lng = longitude.toDoubleOrNull() ?: return@TextButton
                    val r = radius.toFloatOrNull()?.coerceAtLeast(50f) ?: return@TextButton
                    onConfirm(lat, lng, r)
                },
                enabled = latitude.toDoubleOrNull() != null && longitude.toDoubleOrNull() != null,
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun triggerTypeOf(trigger: Trigger): String = when (trigger) {
    is Trigger.TimeOfDay -> Trigger.TYPE_TIME_OF_DAY
    is Trigger.Interval -> Trigger.TYPE_INTERVAL
    is Trigger.ChargingState -> Trigger.TYPE_CHARGING_STATE
    is Trigger.BatteryLevel -> Trigger.TYPE_BATTERY_LEVEL
    is Trigger.WifiState -> Trigger.TYPE_WIFI_STATE
    is Trigger.BluetoothState -> Trigger.TYPE_BLUETOOTH_STATE
    is Trigger.ScreenState -> Trigger.TYPE_SCREEN_STATE
    is Trigger.FeatureState -> Trigger.TYPE_FEATURE_STATE
    is Trigger.HeadphonesState -> Trigger.TYPE_HEADPHONES_STATE
    is Trigger.RingerMode -> Trigger.TYPE_RINGER_MODE
    is Trigger.IncomingCall -> Trigger.TYPE_INCOMING_CALL
    is Trigger.SmsMessage -> Trigger.TYPE_SMS_MESSAGE
    is Trigger.AppLaunch -> Trigger.TYPE_APP_LAUNCH
    is Trigger.AppClose -> Trigger.TYPE_APP_CLOSE
    is Trigger.SensorPrivacyState -> Trigger.TYPE_SENSOR_PRIVACY_STATE
    is Trigger.Location -> Trigger.TYPE_LOCATION
    is Trigger.CaptivePortal -> Trigger.TYPE_CAPTIVE_PORTAL
}

private fun conditionTypeOf(condition: Condition): String = when (condition) {
    is Condition.TimeRange -> Condition.TYPE_TIME_RANGE
    is Condition.DayOfWeek -> Condition.TYPE_DAY_OF_WEEK
    is Condition.BatteryRange -> Condition.TYPE_BATTERY_RANGE
    is Condition.ChargingState -> Condition.TYPE_CHARGING_STATE
    is Condition.WifiConnected -> Condition.TYPE_WIFI_CONNECTED
    is Condition.BluetoothConnected -> Condition.TYPE_BLUETOOTH_CONNECTED
    is Condition.ScreenOn -> Condition.TYPE_SCREEN_ON
    is Condition.FeatureActive -> Condition.TYPE_FEATURE_ACTIVE
    is Condition.SensorBlocked -> Condition.TYPE_SENSOR_BLOCKED
    is Condition.LocationNear -> Condition.TYPE_LOCATION_NEAR
    is Condition.IpAddress -> Condition.TYPE_IP_ADDRESS
}

private fun actionTypeOf(action: Action): String = when (action) {
    is Action.SetFeature -> Action.TYPE_SET_FEATURE
    is Action.ToggleFeature -> Action.TYPE_TOGGLE_FEATURE
    is Action.SetVolume -> Action.TYPE_SET_VOLUME
    is Action.SetBrightness -> Action.TYPE_SET_BRIGHTNESS
    is Action.SetRingerMode -> Action.TYPE_SET_RINGER_MODE
    is Action.LaunchApp -> Action.TYPE_LAUNCH_APP
    is Action.SendBroadcast -> Action.TYPE_SEND_BROADCAST
    is Action.ShowNotification -> Action.TYPE_SHOW_NOTIFICATION
    is Action.Delay -> Action.TYPE_DELAY
    is Action.SetSetting -> Action.TYPE_SET_SETTING
    is Action.SetSensorPrivacy -> Action.TYPE_SET_SENSOR_PRIVACY
    is Action.PlaySound -> Action.TYPE_PLAY_SOUND
    is Action.SendLocationSms -> Action.TYPE_SEND_LOCATION_SMS
    is Action.HttpRequest -> Action.TYPE_HTTP_REQUEST
}

private fun parsePhoneNumbers(value: String): Set<String> =
    value.split(",", "\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()

@Composable
private fun IntentExtrasEditor(
    extras: Map<String, Action.SendBroadcast.IntentExtra>,
    onExtrasChange: (Map<String, Action.SendBroadcast.IntentExtra>) -> Unit,
) {
    val orderedKeys = remember(extras) { extras.keys.toList() }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        orderedKeys.forEach { key ->
            val extra = extras[key] ?: return@forEach
            IntentExtraRow(
                key = key,
                extra = extra,
                onChange = { newKey, newExtra ->
                    val updated = extras.toMutableMap()
                    updated.remove(key)
                    if (newKey.isNotBlank()) {
                        updated[newKey] = newExtra
                    }
                    onExtrasChange(updated.toMap())
                },
                onRemove = {
                    onExtrasChange(extras - key)
                },
            )
        }
        TextButton(
            onClick = {
                val baseName = "key"
                var index = 1
                var candidate = baseName
                while (extras.containsKey(candidate)) {
                    index += 1
                    candidate = "$baseName$index"
                }
                onExtrasChange(
                    extras + (candidate to Action.SendBroadcast.IntentExtra(
                        Action.SendBroadcast.IntentExtra.ExtraType.STRING, "",
                    ))
                )
            },
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.routines_intent_extras_add))
        }
    }
}

@Composable
private fun IntentExtraRow(
    key: String,
    extra: Action.SendBroadcast.IntentExtra,
    onChange: (newKey: String, newExtra: Action.SendBroadcast.IntentExtra) -> Unit,
    onRemove: () -> Unit,
) {
    var localKey by remember(key) { mutableStateOf(key) }
    var localValue by remember(key, extra.value) { mutableStateOf(extra.value) }
    var localType by remember(key, extra.type) { mutableStateOf(extra.type) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            OutlinedTextField(
                value = localKey,
                onValueChange = {
                    localKey = it
                    onChange(it.trim(), Action.SendBroadcast.IntentExtra(localType, localValue))
                },
                label = { Text(stringResource(R.string.routines_intent_extras_key)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }
        OutlinedTextField(
            value = localValue,
            onValueChange = {
                localValue = it
                onChange(localKey.trim(), Action.SendBroadcast.IntentExtra(localType, it))
            },
            label = { Text(stringResource(R.string.routines_intent_extras_value)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Action.SendBroadcast.IntentExtra.ExtraType.values().forEach { type ->
                FilterChip(
                    selected = localType == type,
                    onClick = {
                        localType = type
                        onChange(localKey.trim(), Action.SendBroadcast.IntentExtra(type, localValue))
                    },
                    label = { Text(type.name.lowercase()) },
                )
            }
        }
    }
}
