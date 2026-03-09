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

package com.android.axion.axionparts.ui.components

import android.content.ContentResolver
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.AppListPreference
import com.android.axion.compose.preferences.LocalPreferencePosition
import com.android.axion.compose.preferences.PreferencePosition

const val ESSENTIAL_APP_LIST_KEY = "essential_app_list"

fun getEssentialApps(contentResolver: ContentResolver): Set<String> {
    val savedList = Settings.Secure.getString(contentResolver, ESSENTIAL_APP_LIST_KEY) ?: ""
    return if (savedList.isEmpty()) emptySet()
    else savedList.split(",").filter { it.isNotEmpty() }.toSet()
}

fun saveEssentialApps(contentResolver: ContentResolver, packages: Set<String>) {
    Settings.Secure.putString(contentResolver, ESSENTIAL_APP_LIST_KEY, packages.joinToString(","))
}

@Composable
fun EssentialAppsPreference(
    modifier: Modifier = Modifier,
    onNavigateToAppPicker: (selectedApps: Set<String>) -> Unit,
    position: PreferencePosition = LocalPreferencePosition.current,
) {
    AppListPreference(
        settingKey = ESSENTIAL_APP_LIST_KEY,
        title = stringResource(R.string.essential_notifications),
        description = stringResource(R.string.selected_apps_description),
        emptyText = stringResource(R.string.no_apps_selected),
        addButtonText = stringResource(R.string.add_apps),
        onAddClicked = onNavigateToAppPicker,
        position = position,
        modifier = modifier,
    )
}
