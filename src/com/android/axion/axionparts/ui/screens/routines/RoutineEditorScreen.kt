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
import androidx.compose.material.icons.filled.Wifi
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
    TypeOption(Trigger.TYPE_APP_LAUNCH, stringResource(R.string.routines_app_launch), Icons.Default.OpenInNew),
    TypeOption(Trigger.TYPE_APP_CLOSE, stringResource(R.string.routines_app_close), Icons.Default.Close),
    TypeOption(Trigger.TYPE_SENSOR_PRIVACY_STATE, stringResource(R.string.routines_sensor_privacy), Icons.Default.CameraAlt),
    TypeOption(Trigger.TYPE_LOCATION, stringResource(R.string.routines_location), Icons.Default.LocationOn),
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
    var showActionPicker by remember { mutableStateOf(false) }
    var configuringActionType by remember { mutableStateOf<String?>(null) }
    var showConditionPicker by remember { mutableStateOf(false) }
    var configuringConditionType by remember { mutableStateOf<String?>(null) }

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
                onRemove = { triggers = triggers.toMutableList().also { it.removeAt(index) } },
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
                onRemove = {
                    conditions = conditions.toMutableList().also { it.removeAt(index) }
                },
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
                onRemove = { actions = actions.toMutableList().also { it.removeAt(index) } },
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
        TriggerConfigDialog(
            type = type,
            onConfirm = { trigger ->
                triggers = triggers + trigger
                configuringTriggerType = null
            },
            onDismiss = { configuringTriggerType = null },
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
        ActionConfigDialog(
            type = type,
            onConfirm = { action ->
                actions = actions + action
                configuringActionType = null
            },
            onDismiss = { configuringActionType = null },
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
        ConditionConfigDialog(
            type = type,
            onConfirm = { condition ->
                conditions = conditions + condition
                configuringConditionType = null
            },
            onDismiss = { configuringConditionType = null },
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
private fun ItemCard(text: String, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
    onConfirm: (Trigger) -> Unit,
    onDismiss: () -> Unit,
) {
    when (type) {
        Trigger.TYPE_TIME_OF_DAY -> {
            val timeState = rememberTimePickerState(8, 0, true)
            var selectedDays by remember { mutableStateOf(Trigger.ALL_DAYS) }
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
                    }) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_INTERVAL -> {
            var minutes by remember { mutableIntStateOf(30) }
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
                    }) { Text(stringResource(R.string.add)) }
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
            var threshold by remember { mutableFloatStateOf(20f) }
            var isBelow by remember { mutableStateOf(true) }
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
                    }) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_WIFI_STATE -> {
            var connected by remember { mutableStateOf(true) }
            var ssid by remember { mutableStateOf("") }
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
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(
                            Trigger.WifiState(connected, ssid.takeIf { it.isNotBlank() })
                        )
                    }) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Trigger.TYPE_BLUETOOTH_STATE -> {
            var connected by remember { mutableStateOf(true) }
            var address by remember { mutableStateOf("") }
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
                    }) { Text(stringResource(R.string.add)) }
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
            onConfirm = { sensor, blocked ->
                onConfirm(Trigger.SensorPrivacyState(sensor, blocked))
            },
            onDismiss = onDismiss,
        )

        Trigger.TYPE_LOCATION -> LocationTriggerDialog(
            onConfirm = { lat, lng, radius, entering ->
                onConfirm(Trigger.Location(lat, lng, radius, entering))
            },
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun ActionConfigDialog(
    type: String,
    onConfirm: (Action) -> Unit,
    onDismiss: () -> Unit,
) {
    when (type) {
        Action.TYPE_SET_FEATURE -> FeatureSelectDialog(
            title = stringResource(R.string.routines_set_feature),
            showToggle = true,
            onConfirm = { feature, enabled ->
                onConfirm(Action.SetFeature(feature, enabled))
            },
            onDismiss = onDismiss,
        )

        Action.TYPE_TOGGLE_FEATURE -> FeatureSelectDialog(
            title = stringResource(R.string.routines_toggle_feature),
            showToggle = false,
            onConfirm = { feature, _ -> onConfirm(Action.ToggleFeature(feature)) },
            onDismiss = onDismiss,
        )

        Action.TYPE_SET_VOLUME -> {
            var streamType by remember { mutableIntStateOf(AudioManager.STREAM_MUSIC) }
            var level by remember { mutableFloatStateOf(50f) }
            val streams = listOf(
                AudioManager.STREAM_MUSIC to stringResource(R.string.routines_stream_media),
                AudioManager.STREAM_RING to stringResource(R.string.routines_stream_ring),
                AudioManager.STREAM_NOTIFICATION to stringResource(R.string.routines_stream_notification),
                AudioManager.STREAM_ALARM to stringResource(R.string.routines_stream_alarm),
                AudioManager.STREAM_SYSTEM to stringResource(R.string.routines_stream_system),
            )
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
                        Text("${level.toInt()}%")
                        Slider(
                            value = level,
                            onValueChange = { level = it },
                            valueRange = 0f..100f,
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirm(Action.SetVolume(streamType, level.toInt()))
                    }) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SET_BRIGHTNESS -> {
            var level by remember { mutableFloatStateOf(128f) }
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
                    }) { Text(stringResource(R.string.add)) }
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
            var action by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_send_broadcast)) },
                text = {
                    OutlinedTextField(
                        value = action,
                        onValueChange = { action = it },
                        label = { Text(stringResource(R.string.routines_broadcast_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { onConfirm(Action.SendBroadcast(action.trim())) },
                        enabled = action.isNotBlank(),
                    ) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SHOW_NOTIFICATION -> {
            var title by remember { mutableStateOf("") }
            var text by remember { mutableStateOf("") }
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
                    ) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_DELAY -> {
            var seconds by remember { mutableIntStateOf(5) }
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
                    }) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SET_SETTING -> {
            var table by remember {
                mutableStateOf(Action.SetSetting.SettingsTable.SECURE)
            }
            var key by remember { mutableStateOf("") }
            var value by remember { mutableStateOf("") }
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
                    ) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Action.TYPE_SET_SENSOR_PRIVACY -> SensorPrivacyActionDialog(
            onConfirm = { sensor, blocked ->
                onConfirm(Action.SetSensorPrivacy(sensor, blocked))
            },
            onDismiss = onDismiss,
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionConfigDialog(
    type: String,
    onConfirm: (Condition) -> Unit,
    onDismiss: () -> Unit,
) {
    when (type) {
        Condition.TYPE_TIME_RANGE -> {
            val startState = rememberTimePickerState(9, 0, true)
            val endState = rememberTimePickerState(17, 0, true)
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
                            if (showEnd) stringResource(R.string.add)
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
            var selectedDays by remember { mutableStateOf(Trigger.ALL_DAYS) }
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
                    ) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Condition.TYPE_BATTERY_RANGE -> {
            var min by remember { mutableFloatStateOf(20f) }
            var max by remember { mutableFloatStateOf(80f) }
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
                    }) { Text(stringResource(R.string.add)) }
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
            var ssid by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.routines_wifi_connected)) },
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
                        onConfirm(Condition.WifiConnected(ssid.takeIf { it.isNotBlank() }))
                    }) { Text(stringResource(R.string.add)) }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        Condition.TYPE_BLUETOOTH_CONNECTED -> {
            var address by remember { mutableStateOf("") }
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
                    }) { Text(stringResource(R.string.add)) }
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
            onConfirm = { feature, active ->
                onConfirm(Condition.FeatureActive(feature, active))
            },
            onDismiss = onDismiss,
        )

        Condition.TYPE_SENSOR_BLOCKED -> SensorPrivacyTriggerDialog(
            onConfirm = { sensor, blocked ->
                onConfirm(Condition.SensorBlocked(sensor, blocked))
            },
            onDismiss = onDismiss,
        )

        Condition.TYPE_LOCATION_NEAR -> LocationConditionDialog(
            onConfirm = { lat, lng, radius ->
                onConfirm(Condition.LocationNear(lat, lng, radius))
            },
            onDismiss = onDismiss,
        )
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
    onConfirm: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedFeature by remember { mutableStateOf<String?>(null) }
    var enabled by remember { mutableStateOf(true) }

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
            ) { Text(stringResource(R.string.add)) }
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
    is Condition.WifiConnected -> condition.ssid?.let { "WiFi: $it" } ?: "WiFi connected"
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
}

@Composable
private fun SensorPrivacyTriggerDialog(
    onConfirm: (Int, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var sensor by remember { mutableIntStateOf(SENSOR_CAMERA) }
    var blocked by remember { mutableStateOf(true) }
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
                Text(stringResource(R.string.add))
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
    onConfirm: (Int, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var sensor by remember { mutableIntStateOf(SENSOR_CAMERA) }
    var blocked by remember { mutableStateOf(true) }
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
                Text(stringResource(R.string.add))
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
    onConfirm: (Double, Double, Float, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("200") }
    var entering by remember { mutableStateOf(true) }
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
            ) { Text(stringResource(R.string.add)) }
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
    onConfirm: (Double, Double, Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("200") }
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
            ) { Text(stringResource(R.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
