/*
 * Copyright (C) 2026 AxionOS Project
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

package com.android.axion.axionparts.search

import com.android.axion.axionparts.R

data class PartFeature(
    val key: String,
    val titleRes: Int,
    val summaryRes: Int,
    val keywordsRes: Int,
    val action: String,
    val targetPackage: String? = null,
    val targetClass: String? = null
)

object FeatureRegistry {
    val FEATURES = listOf(
        PartFeature(
            key = "edge_light_settings",
            titleRes = R.string.edge_light,
            summaryRes = R.string.edge_light_description,
            keywordsRes = R.string.keywords_edge_light,
            action = "com.android.axion.axionparts.action.LOCKSCREEN"
        ),
        PartFeature(
            key = "lockscreen_media_art",
            titleRes = R.string.media_art,
            summaryRes = R.string.media_art_description,
            keywordsRes = R.string.keywords_media_art,
            action = "com.android.axion.axionparts.action.LOCKSCREEN"
        ),
        PartFeature(
            key = "pulse_visualizer",
            titleRes = R.string.pulse_visualizer,
            summaryRes = R.string.pulse_visualizer_description,
            keywordsRes = R.string.keywords_pulse,
            action = "com.android.axion.axionparts.action.LOCKSCREEN"
        ),
        PartFeature(
            key = "aod_settings",
            titleRes = R.string.always_on_display,
            summaryRes = R.string.always_on_display_description,
            keywordsRes = R.string.keywords_aod,
            action = "com.android.axion.axionparts.action.LOCKSCREEN"
        ),
        PartFeature(
            key = "ui_features_settings",
            titleRes = R.string.ui_features,
            summaryRes = R.string.ui_features_subtitle,
            keywordsRes = R.string.keywords_ui_features,
            action = "com.android.axion.axionparts.action.UI_FEATURES"
        ),
        PartFeature(
            key = "sound_settings",
            titleRes = R.string.sound,
            summaryRes = R.string.sound_subtitle,
            keywordsRes = R.string.keywords_sound,
            action = "com.android.axion.axionparts.action.SOUND"
        ),
        PartFeature(
            key = "gestures_settings",
            titleRes = R.string.gestures,
            summaryRes = R.string.gestures_subtitle,
            keywordsRes = R.string.keywords_gestures,
            action = "com.android.axion.axionparts.action.GESTURES"
        ),
        PartFeature(
            key = "pcmode_settings",
            titleRes = R.string.pc_mode,
            summaryRes = R.string.pc_mode_summary,
            keywordsRes = R.string.keywords_pcmode,
            action = "com.android.axion.axionparts.action.PCMODE"
        ),
        PartFeature(
            key = "performance_settings",
            titleRes = R.string.performance,
            summaryRes = R.string.performance_summary,
            keywordsRes = R.string.keywords_performance,
            action = "com.android.axion.axionparts.action.PERFORMANCE"
        ),
        PartFeature(
            key = "trickystore_settings",
            titleRes = R.string.trickystore,
            summaryRes = R.string.trickystore_summary,
            keywordsRes = R.string.keywords_trickystore,
            action = "com.android.axion.axionparts.action.TRICKYSTORE"
        ),
        PartFeature(
            key = "playintegrityfix_settings",
            titleRes = R.string.play_integrity_fix,
            summaryRes = R.string.play_integrity_fix_summary,
            keywordsRes = R.string.keywords_playintegrityfix,
            action = "com.android.axion.axionparts.action.PLAYINTEGRITYFIX"
        ),
        PartFeature(
            key = "gamespoofing_settings",
            titleRes = R.string.game_spoofing,
            summaryRes = R.string.game_spoofing_summary,
            keywordsRes = R.string.keywords_gamespoofing,
            action = "com.android.axion.axionparts.action.GAMESPOOFING"
        ),
        PartFeature(
            key = "routines_settings",
            titleRes = R.string.routines,
            summaryRes = R.string.routines_summary,
            keywordsRes = R.string.keywords_routines,
            action = "com.android.axion.axionparts.action.ROUTINES"
        ),
        PartFeature(
            key = "background_manager_settings",
            titleRes = R.string.background_manager,
            summaryRes = R.string.background_manager_summary,
            keywordsRes = R.string.keywords_background_manager,
            action = "com.android.axion.axionparts.action.BACKGROUND_MANAGER"
        ),
        PartFeature(
            key = "bravia_engine_settings",
            titleRes = R.string.ax_bravia_engine,
            summaryRes = R.string.ax_bravia_engine_summary,
            keywordsRes = R.string.keywords_bravia,
            action = "com.android.axion.axionparts.action.BRAVIA"
        ),
        PartFeature(
            key = "dynamic_bar_settings",
            titleRes = R.string.dynamic_bar,
            summaryRes = R.string.dynamic_bar_summary,
            keywordsRes = R.string.keywords_dynamic_bar,
            action = "com.android.axion.axionparts.action.DYNAMIC_BAR"
        ),
        PartFeature(
            key = "essentials_settings",
            titleRes = R.string.essentials,
            summaryRes = R.string.essentials_summary,
            keywordsRes = R.string.keywords_essentials,
            action = "com.android.axion.axionparts.action.ESSENTIALS"
        ),
        PartFeature(
            key = "multitasking_settings",
            titleRes = R.string.multitasking,
            summaryRes = R.string.multitasking_summary,
            keywordsRes = R.string.keywords_multitasking,
            action = "com.android.axion.axionparts.action.MULTITASKING"
        ),
        PartFeature(
            key = "themes_settings",
            titleRes = R.string.themes,
            summaryRes = R.string.themes_subtitle,
            keywordsRes = R.string.keywords_themes,
            action = "android.intent.action.MAIN",
            targetPackage = "com.android.axion.axthemestore",
            targetClass = "com.android.axion.axthemestore.MainActivity"
        ),
        PartFeature(
            key = "gamespace_settings",
            titleRes = R.string.gamespace,
            summaryRes = R.string.gamespace_summary,
            keywordsRes = R.string.keywords_gamespace,
            action = "android.intent.action.MAIN",
            targetPackage = "io.chaldeaprjkt.gamespace",
            targetClass = "io.chaldeaprjkt.gamespace.settings.SettingsActivity"
        )
    )
}
