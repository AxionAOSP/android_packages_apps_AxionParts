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

package com.android.axion.axionparts.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.theme.ExpressiveShapes
import com.android.axion.compose.applist.AppFilter
import com.android.axion.compose.applist.rememberFilteredAppList
import com.android.axion.compose.scaffold.AxionScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppInfo(val packageName: String, val label: String, val icon: Drawable)

enum class AppPickerMode {
    SINGLE,
    MULTI,
}

enum class AppFilterType {
    ALL,
    USER_ONLY,
    LAUNCHABLE_ONLY,
    LAUNCHABLE_USER_ONLY,
}

@Composable
fun AppPickerScreen(
    title: String = stringResource(R.string.select_apps),
    selectedApps: Set<String>,
    onBackClick: () -> Unit,
    onAppsSelected: (Set<String>) -> Unit,
    mode: AppPickerMode = AppPickerMode.MULTI,
    filterType: AppFilterType = AppFilterType.LAUNCHABLE_USER_ONLY,
    showSystemApps: Boolean = false,
    customFilter: ((ApplicationInfo) -> Boolean)? = null,
    maxSelection: Int? = null,
    maxSelectionMessage: String? = null,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    var searchQuery by remember { mutableStateOf("") }
    var tempSelectedApps by remember { mutableStateOf(selectedApps) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val sdkFilters: Array<AppFilter> = when (filterType) {
        AppFilterType.ALL -> arrayOf(AppFilter.ALL, AppFilter.NO_OVERLAYS)
        AppFilterType.USER_ONLY -> if (showSystemApps) arrayOf(AppFilter.ALL, AppFilter.NO_OVERLAYS) else arrayOf(AppFilter.USER_ONLY, AppFilter.NO_OVERLAYS)
        AppFilterType.LAUNCHABLE_ONLY -> arrayOf(AppFilter.LAUNCHABLE_ONLY, AppFilter.NO_OVERLAYS)
        AppFilterType.LAUNCHABLE_USER_ONLY -> if (showSystemApps) arrayOf(AppFilter.LAUNCHABLE_ONLY, AppFilter.NO_OVERLAYS) else arrayOf(AppFilter.USER_ONLY, AppFilter.LAUNCHABLE_ONLY, AppFilter.NO_OVERLAYS)
    }

    val sdkApps = rememberFilteredAppList(searchQuery, *sdkFilters)
    var customApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (customFilter != null) {
            withContext(Dispatchers.IO) {
                customApps = packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { appInfo -> customFilter(appInfo) }
                    .map { appInfo ->
                        AppInfo(
                            packageName = appInfo.packageName,
                            label = appInfo.loadLabel(packageManager).toString(),
                            icon = appInfo.loadIcon(packageManager),
                        )
                    }
                    .sortedBy { it.label.lowercase() }
            }
        }
    }

    val filteredCustomApps by remember(customApps, searchQuery) {
        mutableStateOf(
            if (searchQuery.isEmpty()) customApps
            else customApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        )
    }

    val filteredApps: List<AppInfo> = if (customFilter != null) {
        filteredCustomApps
    } else {
        sdkApps.value.map { entry ->
            AppInfo(packageName = entry.packageName, label = entry.label, icon = entry.icon)
        }
    }

    AxionScaffold(title = title, onBackClick = onBackClick) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_apps)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    shape = ExpressiveShapes.large,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor =
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.selected_count, tempSelectedApps.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppListItem(
                            app = app,
                            isSelected = tempSelectedApps.contains(app.packageName),
                            onClick = {
                                when (mode) {
                                    AppPickerMode.SINGLE -> {
                                        onAppsSelected(setOf(app.packageName))
                                        onBackClick()
                                    }
                                    AppPickerMode.MULTI -> {
                                        tempSelectedApps =
                                            if (tempSelectedApps.contains(app.packageName)) {
                                                tempSelectedApps - app.packageName
                                            } else if (maxSelection == null || tempSelectedApps.size < maxSelection) {
                                                tempSelectedApps + app.packageName
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        maxSelectionMessage
                                                            ?: context.resources.getQuantityString(
                                                                R.plurals.max_apps_selected,
                                                                maxSelection,
                                                                maxSelection,
                                                            )
                                                    )
                                                }
                                                tempSelectedApps
                                            }
                                    }
                                }
                            },
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )

            if (mode == AppPickerMode.MULTI) {
                FloatingActionButton(
                    onClick = { onAppsSelected(tempSelectedApps) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = ExpressiveShapes.large,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.save),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppListItem(app: AppInfo, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "scale",
        )

    val backgroundColor by
        animateColorAsState(
            targetValue =
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "bgColor",
        )

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .scale(scale)
                .clip(ExpressiveShapes.large)
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(44.dp).clip(ExpressiveShapes.medium),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isSelected) {
            Box(
                modifier =
                    Modifier.size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
