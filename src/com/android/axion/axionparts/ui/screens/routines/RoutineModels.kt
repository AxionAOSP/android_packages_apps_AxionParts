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

import android.media.AudioManager
import java.util.Calendar
import java.util.UUID
import kotlin.math.roundToInt

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val enabled: Boolean = true,
    val triggers: List<Trigger>,
    val conditions: List<Condition> = emptyList(),
    val actions: List<Action>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long? = null,
)

sealed interface Trigger {

    data class TimeOfDay(
        val hour: Int,
        val minute: Int,
        val daysOfWeek: Set<Int> = ALL_DAYS,
    ) : Trigger

    data class Interval(val intervalMinutes: Int) : Trigger

    data class ChargingState(val charging: Boolean) : Trigger

    data class BatteryLevel(val threshold: Int, val direction: Direction) : Trigger {
        enum class Direction { ABOVE, BELOW }
    }

    data class WifiState(val connected: Boolean, val ssid: String? = null, val ssidPattern: String? = null) : Trigger

    data class BluetoothState(val connected: Boolean, val deviceAddress: String? = null) : Trigger

    data class ScreenState(val on: Boolean) : Trigger

    data class FeatureState(val feature: String, val active: Boolean) : Trigger

    data class HeadphonesState(val connected: Boolean) : Trigger

    data class RingerMode(val mode: Int) : Trigger

    data class IncomingCall(val phoneNumbers: Set<String> = emptySet()) : Trigger

    data class SmsMessage(val text: String, val senderNumbers: Set<String> = emptySet()) : Trigger

    data class AppLaunch(val packageName: String) : Trigger

    data class AppClose(val packageName: String) : Trigger

    data class SensorPrivacyState(val sensor: Int, val blocked: Boolean) : Trigger

    data class Location(
        val latitude: Double,
        val longitude: Double,
        val radiusMeters: Float,
        val entering: Boolean,
    ) : Trigger

    data class CaptivePortal(val ssid: String? = null) : Trigger

    companion object {
        val ALL_DAYS = setOf(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
            Calendar.SATURDAY,
        )

        const val TYPE_TIME_OF_DAY = "time_of_day"
        const val TYPE_INTERVAL = "interval"
        const val TYPE_CHARGING_STATE = "charging_state"
        const val TYPE_BATTERY_LEVEL = "battery_level"
        const val TYPE_WIFI_STATE = "wifi_state"
        const val TYPE_BLUETOOTH_STATE = "bluetooth_state"
        const val TYPE_SCREEN_STATE = "screen_state"
        const val TYPE_FEATURE_STATE = "feature_state"
        const val TYPE_HEADPHONES_STATE = "headphones_state"
        const val TYPE_RINGER_MODE = "ringer_mode"
        const val TYPE_INCOMING_CALL = "incoming_call"
        const val TYPE_SMS_MESSAGE = "sms_message"
        const val TYPE_APP_LAUNCH = "app_launch"
        const val TYPE_APP_CLOSE = "app_close"
        const val TYPE_SENSOR_PRIVACY_STATE = "sensor_privacy_state"
        const val TYPE_LOCATION = "location"
        const val TYPE_CAPTIVE_PORTAL = "captive_portal"
    }
}

sealed interface Condition {

    data class TimeRange(
        val startHour: Int,
        val startMinute: Int,
        val endHour: Int,
        val endMinute: Int,
    ) : Condition

    data class DayOfWeek(val days: Set<Int>) : Condition

    data class BatteryRange(val min: Int, val max: Int) : Condition

    data class ChargingState(val charging: Boolean) : Condition

    data class WifiConnected(val ssid: String? = null, val ssidPattern: String? = null) : Condition

    data class BluetoothConnected(val deviceAddress: String? = null) : Condition

    data class ScreenOn(val on: Boolean) : Condition

    data class FeatureActive(val feature: String, val active: Boolean) : Condition

    data class SensorBlocked(val sensor: Int, val blocked: Boolean) : Condition

    data class LocationNear(
        val latitude: Double,
        val longitude: Double,
        val radiusMeters: Float,
    ) : Condition

    data class IpAddress(val cidr: String, val isRegex: Boolean = false) : Condition

    companion object {
        const val TYPE_TIME_RANGE = "time_range"
        const val TYPE_DAY_OF_WEEK = "day_of_week"
        const val TYPE_BATTERY_RANGE = "battery_range"
        const val TYPE_CHARGING_STATE = "charging_state"
        const val TYPE_WIFI_CONNECTED = "wifi_connected"
        const val TYPE_BLUETOOTH_CONNECTED = "bluetooth_connected"
        const val TYPE_SCREEN_ON = "screen_on"
        const val TYPE_FEATURE_ACTIVE = "feature_active"
        const val TYPE_SENSOR_BLOCKED = "sensor_blocked"
        const val TYPE_LOCATION_NEAR = "location_near"
        const val TYPE_IP_ADDRESS = "ip_address"
    }
}

sealed interface Action {

    data class SetFeature(val feature: String, val enabled: Boolean) : Action

    data class ToggleFeature(val feature: String) : Action

    data class SetVolume(val streamType: Int, val level: Int) : Action

    data class SetBrightness(val level: Int) : Action

    data class SetRingerMode(val mode: Int) : Action

    data class LaunchApp(val packageName: String) : Action

    data class SendBroadcast(
        val action: String? = null,
        val mode: Mode = Mode.BROADCAST,
        val componentPackage: String? = null,
        val componentClass: String? = null,
        val extras: Map<String, IntentExtra> = emptyMap(),
    ) : Action {
        enum class Mode { BROADCAST, START_SERVICE, START_FOREGROUND_SERVICE }

        data class IntentExtra(val type: ExtraType, val value: String) {
            enum class ExtraType { STRING, INT, LONG, BOOLEAN, FLOAT, DOUBLE }
        }
    }

    data class ShowNotification(val title: String, val text: String) : Action

    data class Delay(val durationMs: Long) : Action

    data class SetSetting(
        val table: SettingsTable,
        val key: String,
        val value: String,
    ) : Action {
        enum class SettingsTable { SECURE, GLOBAL, SYSTEM }
    }

    data class SetSensorPrivacy(val sensor: Int, val blocked: Boolean) : Action

    data class PlaySound(val soundType: Int, val uri: String? = null) : Action

    data class SendLocationSms(val phoneNumber: String? = null) : Action

    data class HttpRequest(
        val url: String,
        val method: String = METHOD_GET,
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        val timeoutMs: Int = DEFAULT_HTTP_TIMEOUT_MS,
        val ignoreSslErrors: Boolean = false,
        val requireValidatedInternet: Boolean = true,
    ) : Action

    companion object {
        const val TYPE_SET_FEATURE = "set_feature"
        const val TYPE_TOGGLE_FEATURE = "toggle_feature"
        const val TYPE_SET_VOLUME = "set_volume"
        const val TYPE_SET_BRIGHTNESS = "set_brightness"
        const val TYPE_SET_RINGER_MODE = "set_ringer_mode"
        const val TYPE_LAUNCH_APP = "launch_app"
        const val TYPE_SEND_BROADCAST = "send_broadcast"
        const val TYPE_SHOW_NOTIFICATION = "show_notification"
        const val TYPE_DELAY = "delay"
        const val TYPE_SET_SETTING = "set_setting"
        const val TYPE_SET_SENSOR_PRIVACY = "set_sensor_privacy"
        const val TYPE_PLAY_SOUND = "play_sound"
        const val TYPE_SEND_LOCATION_SMS = "send_location_sms"
        const val TYPE_HTTP_REQUEST = "http_request"
        const val METHOD_GET = "GET"
        const val DEFAULT_HTTP_TIMEOUT_MS = 15_000
        const val MAX_HTTP_TIMEOUT_MS = 30_000
    }
}

const val SENSOR_MICROPHONE = 1
const val SENSOR_CAMERA = 2

const val SOUND_TYPE_RINGTONE = 1
const val SOUND_TYPE_NOTIFICATION = 2
const val SOUND_TYPE_ALARM = 4

internal fun streamLevelForVolumePercent(percent: Int, minLevel: Int, maxLevel: Int): Int {
    if (maxLevel <= minLevel) return minLevel
    val fraction = percent.coerceIn(0, 100) / 100f
    return (minLevel + fraction * (maxLevel - minLevel))
        .roundToInt()
        .coerceIn(minLevel, maxLevel)
}

internal fun volumePercentForStreamLevel(level: Int, minLevel: Int, maxLevel: Int): Int {
    if (maxLevel <= minLevel) return 0
    return ((level.coerceIn(minLevel, maxLevel) - minLevel) * 100f / (maxLevel - minLevel))
        .roundToInt()
        .coerceIn(0, 100)
}

internal fun appliedVolumePercent(audioManager: AudioManager?, streamType: Int, percent: Int): Int {
    if (audioManager == null) return percent.coerceIn(0, 100)
    val minLevel = audioManager.getStreamMinVolume(streamType)
    val maxLevel = audioManager.getStreamMaxVolume(streamType)
    return volumePercentForStreamLevel(
        streamLevelForVolumePercent(percent, minLevel, maxLevel),
        minLevel,
        maxLevel,
    )
}

val KNOWN_FEATURES = linkedMapOf(
    "wifi" to "WiFi",
    "bluetooth" to "Bluetooth",
    "mobile_data" to "Mobile Data",
    "airplane_mode" to "Airplane Mode",
    "do_not_disturb" to "Do Not Disturb",
    "dark_mode" to "Dark Mode",
    "auto_rotate" to "Auto Rotate",
    "battery_saver" to "Battery Saver",
    "data_saver" to "Data Saver",
    "hotspot" to "Hotspot",
    "location" to "Location",
    "nfc" to "NFC",
    "flashlight" to "Flashlight",
    "reading_mode" to "Reading Mode",
    "ambient_display" to "Ambient Display",
    "heads_up" to "Heads Up",
)
