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

package com.android.axion.axionparts.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.AppListPreference
import com.android.axion.compose.preferences.ListPreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SettingsFlow
import com.android.axion.compose.preferences.SettingsType
import com.android.axion.compose.preferences.rememberSettingInt
import com.android.axion.compose.preferences.rememberSettingString
import com.android.axion.compose.preferences.rememberSettingsFlow
import com.android.axion.compose.scaffold.AxionScaffold

private const val PACKAGE_FREEZER_KEY = "axion_perf_package_freezer"
private const val RESTRICT_BACKGROUND_KEY = "axion_perf_restrict_bg_auto_start"
private const val AGGRESSIVE_POLICY_KEY = "axion_perf_aggressive_policy"
private const val FREEZER_LEVEL_KEY = "axion_perf_freezer_level"

private val BACKGROUND_APP_BLACKLIST =
    setOf(
        "android",
        "com.android.launcher3",
        "com.android.systemui",
    )

typealias BackgroundAppPickerNavigator =
    (Int, Set<String>, String, Int?, Int?, AppFilterType, Set<String>) -> Unit

@Composable
fun BackgroundManagerScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true,
    onNavigateToAppPicker: BackgroundAppPickerNavigator = { _, _, _, _, _, _, _ -> },
) {
    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.background_manager),
            onBackClick = { onBackClick?.invoke() },
        ) { innerPadding ->
            BackgroundManagerContent(
                onNavigateToAppPicker = onNavigateToAppPicker,
                modifier = Modifier.padding(innerPadding),
            )
        }
    } else {
        BackgroundManagerContent(
            onNavigateToAppPicker = onNavigateToAppPicker,
            modifier = Modifier,
        )
    }
}

@Composable
private fun BackgroundManagerContent(
    onNavigateToAppPicker: BackgroundAppPickerNavigator,
    modifier: Modifier = Modifier,
) {
    val flow = rememberSettingsFlow(SettingsType.SECURE)
    val freezerPackages by rememberSettingString(PACKAGE_FREEZER_KEY, SettingsType.SECURE)
    val restrictedPackages by rememberSettingString(RESTRICT_BACKGROUND_KEY, SettingsType.SECURE)

    LaunchedEffect(freezerPackages) {
        flow.sanitizePackageList(PACKAGE_FREEZER_KEY, freezerPackages)
    }
    LaunchedEffect(restrictedPackages) {
        flow.sanitizePackageList(RESTRICT_BACKGROUND_KEY, restrictedPackages)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.background_manager_policy)) {
            item {
                AggressivePolicyPreference()
            }
            item {
                FreezerLevelPreference()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PreferenceGroup(title = stringResource(R.string.background_manager_apps)) {
            item {
                BackgroundAppListPreference(
                    settingKey = PACKAGE_FREEZER_KEY,
                    title = stringResource(R.string.package_freezer_apps),
                    description = stringResource(R.string.package_freezer_apps_summary),
                    onAddClicked = { packages ->
                        onNavigateToAppPicker(
                            R.string.package_freezer_apps,
                            packages.withoutBlacklistedApps(),
                            PACKAGE_FREEZER_KEY,
                            null,
                            null,
                            AppFilterType.ALL,
                            BACKGROUND_APP_BLACKLIST,
                        )
                    },
                )
            }
            item {
                BackgroundAppListPreference(
                    settingKey = RESTRICT_BACKGROUND_KEY,
                    title = stringResource(R.string.restricted_background_apps),
                    description = stringResource(R.string.restricted_background_apps_summary),
                    onAddClicked = { packages ->
                        onNavigateToAppPicker(
                            R.string.restricted_background_apps,
                            packages.withoutBlacklistedApps(),
                            RESTRICT_BACKGROUND_KEY,
                            null,
                            null,
                            AppFilterType.ALL,
                            BACKGROUND_APP_BLACKLIST,
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }
}

@Composable
private fun AggressivePolicyPreference() {
    val flow = rememberSettingsFlow(SettingsType.SECURE)
    val policy by rememberSettingInt(AGGRESSIVE_POLICY_KEY, SettingsType.SECURE, 0)

    ListPreference(
        title = stringResource(R.string.background_app_policy),
        summary = stringResource(R.string.background_app_policy_summary),
        options =
            listOf(
                "0" to stringResource(R.string.background_app_policy_normal),
                "1" to stringResource(R.string.background_app_policy_aggressive),
            ),
        value = policy.toString(),
        onValueChange = { flow.putInt(AGGRESSIVE_POLICY_KEY, it.toInt()) },
    )
}

@Composable
private fun FreezerLevelPreference() {
    val flow = rememberSettingsFlow(SettingsType.SECURE)
    val level by rememberSettingInt(FREEZER_LEVEL_KEY, SettingsType.SECURE, 2)

    ListPreference(
        title = stringResource(R.string.freezer_level),
        summary = stringResource(R.string.freezer_level_summary),
        options =
            listOf(
                "0" to stringResource(R.string.freezer_level_disabled),
                "1" to stringResource(R.string.freezer_level_process),
                "2" to stringResource(R.string.freezer_level_package),
            ),
        value = level.toString(),
        onValueChange = { flow.putInt(FREEZER_LEVEL_KEY, it.toInt()) },
    )
}

@Composable
private fun BackgroundAppListPreference(
    settingKey: String,
    title: String,
    description: String,
    onAddClicked: (Set<String>) -> Unit,
) {
    AppListPreference(
        settingKey = settingKey,
        title = title,
        description = description,
        emptyText = stringResource(R.string.no_apps_selected),
        addButtonText = stringResource(R.string.add_apps),
        onAddClicked = onAddClicked,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun String.toPackageList(): List<String> =
    split(',').map { it.trim() }.filter { it.isNotEmpty() }

private fun List<String>.withoutBlacklistedApps(): List<String> =
    filterNot { it in BACKGROUND_APP_BLACKLIST }

private fun Set<String>.withoutBlacklistedApps(): Set<String> =
    filterNot { it in BACKGROUND_APP_BLACKLIST }.toSet()

private fun SettingsFlow.sanitizePackageList(
    key: String,
    savedPackages: String,
) {
    val packages = savedPackages.toPackageList().withoutBlacklistedApps()
    val sanitizedValue = packages.joinToString(",")
    if (sanitizedValue != savedPackages) {
        putString(key, sanitizedValue)
    }
}
