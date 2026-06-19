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

package com.android.axion.axionparts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.axion.axionparts.ui.DashboardScreen
import com.android.axion.axionparts.ui.theme.AxionPartsTheme
import com.google.android.material.color.DynamicColors

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        enableEdgeToEdge()

        val initialScreen = when (intent?.action) {
            "com.android.axion.axionparts.action.LOCKSCREEN" -> "lockscreen"
            "com.android.axion.axionparts.action.UI_FEATURES" -> "ui_features"
            "com.android.axion.axionparts.action.SOUND" -> "sound"
            "com.android.axion.axionparts.action.GESTURES" -> "gestures"
            "com.android.axion.axionparts.action.PCMODE" -> "pcmode"
            "com.android.axion.axionparts.action.PERFORMANCE" -> "performance"
            "com.android.axion.axionparts.action.TRICKYSTORE" -> "trickystore"
            "com.android.axion.axionparts.action.PLAYINTEGRITYFIX" -> "playintegrityfix"
            "com.android.axion.axionparts.action.GAMESPOOFING" -> "gamespoofing"
            "com.android.axion.axionparts.action.ROUTINES" -> "routines"
            "com.android.axion.axionparts.action.BACKGROUND_MANAGER" -> "background_manager"
            "com.android.axion.axionparts.action.BRAVIA" -> "bravia_engine"
            "com.android.axion.axionparts.action.DYNAMIC_BAR" -> "dynamic_bar"
            "com.android.axion.axionparts.action.ESSENTIALS" -> "essentials"
            "com.android.axion.axionparts.action.MULTITASKING" -> "multitasking"
            else -> null
        }

        setContent {
            AxionPartsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    DashboardScreen(initialDetailScreen = initialScreen)
                }
            }
        }
    }
}
