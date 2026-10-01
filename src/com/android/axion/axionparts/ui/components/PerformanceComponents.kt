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

package com.android.axion.axionparts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.rememberSettingInt
import com.android.axion.compose.preferences.rememberSettingsFlow
import kotlin.math.roundToInt

@Composable
fun FrequencySlider(
    settingKey: String,
    label: String,
    availableFrequencies: List<Int>? = null,
    min: Int = 0,
    max: Int = 0,
    interval: Int = 100000,
    defaultValue: Int = min,
    value: Int? = null,
    minSelectableFreq: Int? = null,
    maxSelectableFreq: Int? = null,
    onValueCommitted: ((Int) -> Unit)? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val secureFlow = rememberSettingsFlow(SettingsType.SECURE)
    val persistedValue by rememberSettingInt(settingKey, SettingsType.SECURE, defaultValue)
    val sourceValue = value ?: persistedValue

    var currentValue by
        remember(settingKey) {
            val coerced = sourceValue.coerceFrequencyValue(availableFrequencies, min, max)
            val clamped = coerced.coerceIn(
                minSelectableFreq?.toFloat() ?: Float.NEGATIVE_INFINITY,
                maxSelectableFreq?.toFloat() ?: Float.POSITIVE_INFINITY,
            )
            mutableFloatStateOf(clamped)
        }

    LaunchedEffect(sourceValue, availableFrequencies, min, max, minSelectableFreq, maxSelectableFreq) {
        val coerced = sourceValue.coerceFrequencyValue(availableFrequencies, min, max)
        val clamped = coerced.coerceIn(
            minSelectableFreq?.toFloat() ?: Float.NEGATIVE_INFINITY,
            maxSelectableFreq?.toFloat() ?: Float.POSITIVE_INFINITY,
        )
        currentValue = clamped
    }

    Column(modifier = modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.4f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.mhz_format, currentValue.roundToInt().toMhz()),
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
            )
        }

        Slider(
            value =
                if (availableFrequencies != null && availableFrequencies.isNotEmpty()) {
                    val sortedFreqs = availableFrequencies.sorted()
                    val rawIndex = sortedFreqs.binarySearch(currentValue.toInt())
                    val index = if (rawIndex < 0) {
                        (-(rawIndex + 1)).coerceAtMost(sortedFreqs.size - 1)
                    } else {
                        rawIndex
                    }
                    index.coerceIn(0, sortedFreqs.size - 1).toFloat()
                } else {
                    currentValue.coerceIn(min.toFloat(), max.toFloat())
                },
            onValueChange = { newValue ->
                if (availableFrequencies != null && availableFrequencies.isNotEmpty()) {
                    val sortedFreqs = availableFrequencies.sorted()
                    val index = newValue.roundToInt().coerceIn(0, sortedFreqs.size - 1)
                    val freq = sortedFreqs[index]
                    val clamped = freq.coerceIn(
                        minSelectableFreq ?: sortedFreqs.first(),
                        maxSelectableFreq ?: sortedFreqs.last(),
                    )
                    currentValue = clamped.toFloat()
                } else {
                    val effectiveInterval = if (max >= HZ_THRESHOLD && interval < 1_000_000) interval * 1000 else interval
                    val steppedValue = ((newValue - min) / effectiveInterval).roundToInt() * effectiveInterval + min
                    val clamped = steppedValue.coerceIn(
                        minSelectableFreq ?: min,
                        maxSelectableFreq ?: max,
                    )
                    currentValue = clamped.toFloat()
                }
            },
            onValueChangeFinished = {
                val selectedValue = currentValue.roundToInt().coerceIn(
                    minSelectableFreq ?: Int.MIN_VALUE,
                    maxSelectableFreq ?: Int.MAX_VALUE,
                )
                if (onValueCommitted != null) {
                    onValueCommitted(selectedValue)
                } else {
                    secureFlow.putInt(settingKey, selectedValue)
                }
            },
            valueRange =
                if (availableFrequencies != null && availableFrequencies.isNotEmpty()) {
                    0f..(availableFrequencies.size - 1).toFloat()
                } else {
                    min.toFloat()..max.toFloat()
                },
            steps =
                if (availableFrequencies != null && availableFrequencies.isNotEmpty()) {
                    (availableFrequencies.size - 2).coerceAtLeast(0)
                } else {
                    val effectiveInterval = if (max >= HZ_THRESHOLD && interval < 1_000_000) interval * 1000 else interval
                    if (effectiveInterval > 0) ((max - min) / effectiveInterval) - 1 else 0
                },
            enabled = enabled,
            colors =
                SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val minDisplay =
                if (availableFrequencies != null && availableFrequencies.isNotEmpty()) {
                    availableFrequencies.minOrNull()?.toMhz() ?: 0
                } else {
                    min.toMhz()
                }
            val maxDisplay =
                if (availableFrequencies != null && availableFrequencies.isNotEmpty()) {
                    availableFrequencies.maxOrNull()?.toMhz() ?: 0
                } else {
                    max.toMhz()
                }

            Text(
                text = stringResource(R.string.mhz_format, minDisplay),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Text(
                text = stringResource(R.string.mhz_format, maxDisplay),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}

private const val HZ_THRESHOLD = 10_000_000

private fun Int.toMhz(): Int =
    if (this >= HZ_THRESHOLD) this / 1_000_000 else this / 1_000

private fun Int.coerceFrequencyValue(
    availableFrequencies: List<Int>?,
    min: Int,
    max: Int,
): Float =
    if (availableFrequencies != null && availableFrequencies.isNotEmpty()) {
        val sorted = availableFrequencies.sorted()
        toFloat().coerceIn(sorted.first().toFloat(), sorted.last().toFloat())
    } else if (max > min) {
        toFloat().coerceIn(min.toFloat(), max.toFloat())
    } else {
        toFloat()
    }
