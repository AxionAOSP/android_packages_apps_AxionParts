/*
 * Copyright 2025-2026 AxionOS
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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.rememberSecureSettingStringState
import com.android.axion.compose.scaffold.AxionScaffold

private const val LR = 0.2126f
private const val LG = 0.7152f
private const val LB = 0.0722f

private const val BRAVIA_KEY = "ax_bravia_engine_mode"
private const val MODE_OFF = "0"
private const val MODE_VIVID = "1"
private const val MODE_NATURAL = "2"
private const val MODE_CINEMA = "3"

private val PREVIEW_IMAGES = intArrayOf(
    R.drawable.ax_bravia_preview_mountains,
    R.drawable.ax_bravia_preview_landscape,
    R.drawable.ax_bravia_preview_nature,
    R.drawable.ax_bravia_preview_sunset,
)

private val PREVIEW_ASPECT_RATIOS = floatArrayOf(
    800f / 533f,
    2048f / 1365f,
    2048f / 1365f,
    2048f / 1365f,
)

private fun buildSaturationRow(sat: Float): FloatArray {
    val d = 1.0f - sat
    return floatArrayOf(
        LR * d + sat, LG * d,       LB * d,       0f, 0f,
        LR * d,       LG * d + sat, LB * d,       0f, 0f,
        LR * d,       LG * d,       LB * d + sat, 0f, 0f,
        0f,           0f,           0f,           1f, 0f,
    )
}

private fun buildColorMatrix(mode: String): ColorMatrix {
    return when (mode) {
        MODE_VIVID -> {
            ColorMatrix(buildSaturationRow(1.35f))
        }
        MODE_NATURAL -> {
            val sat = ColorMatrix(buildSaturationRow(1.20f))
            val warm = ColorMatrix(floatArrayOf(
                1.06f, 0f,    0f,    0f, 0f,
                0f,    1.01f, 0f,    0f, 0f,
                0f,    0f,    0.90f, 0f, 0f,
                0f,    0f,    0f,    1f, 0f,
            ))
            warm.timesAssign(sat)
            warm
        }
        MODE_CINEMA -> {
            val sat = ColorMatrix(buildSaturationRow(1.15f))
            val warm = ColorMatrix(floatArrayOf(
                1.12f, 0f,    0f,    0f, 0f,
                0f,    0.98f, 0f,    0f, 0f,
                0f,    0f,    0.82f, 0f, 0f,
                0f,    0f,    0f,    1f, 0f,
            ))
            warm.timesAssign(sat)
            warm
        }
        else -> ColorMatrix()
    }
}

@Composable
fun AxBraviaEngineScreen(onBackClick: () -> Unit) {
    AxionScaffold(
        title = stringResource(R.string.ax_bravia_engine),
        onBackClick = onBackClick,
    ) { innerPadding ->
        val (currentMode, setMode) = rememberSecureSettingStringState(
            key = BRAVIA_KEY,
            defaultValue = MODE_OFF,
        )

        val colorMatrix = remember(currentMode) { buildColorMatrix(currentMode) }
        val colorFilter = if (currentMode != MODE_OFF) ColorFilter.colorMatrix(colorMatrix) else null
        val pagerState = rememberPagerState { PREVIEW_IMAGES.size }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) { page ->
                Image(
                    painter = painterResource(PREVIEW_IMAGES[page]),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(PREVIEW_ASPECT_RATIOS[page])
                        .clip(RoundedCornerShape(16.dp)),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(PREVIEW_IMAGES.size) { index ->
                    val color by animateColorAsState(
                        targetValue = if (pagerState.currentPage == index)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant,
                        animationSpec = tween(250),
                        label = "dot",
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val modes = remember {
                listOf(
                    MODE_OFF to R.string.ax_bravia_engine_off,
                    MODE_VIVID to R.string.ax_bravia_engine_vivid,
                    MODE_NATURAL to R.string.ax_bravia_engine_natural,
                    MODE_CINEMA to R.string.ax_bravia_engine_cinema,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            ) {
                modes.forEach { (value, labelRes) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .selectable(
                                selected = currentMode == value,
                                onClick = { setMode(value) },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = currentMode == value,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
