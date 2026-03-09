/*
 * Copyright (C) 2025-2026 AxionOS Project
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

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.axionparts.R
import com.android.axion.compose.applist.AppFilter
import com.android.axion.compose.applist.rememberAppList
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.PreferencePosition
import com.android.axion.compose.scaffold.AxionScaffold
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ALLOWLIST_PATH = "/data/adb/.custom_rom_hide_allowlist"

private data class HideAppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    var isAllowed: Boolean = false,
)

@Composable
fun CustomRomHideScreen(onBackClick: (() -> Unit)? = null) {
    AxionScaffold(
        title = stringResource(R.string.custom_rom_hide),
        onBackClick = { onBackClick?.invoke() },
    ) { innerPadding ->
        CustomRomHideContent(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
private fun CustomRomHideContent(modifier: Modifier = Modifier) {
    var allowedCount by remember { mutableStateOf(0) }
    var showAppPicker by remember { mutableStateOf(false) }

    fun refreshStatus() {
        val file = File(ALLOWLIST_PATH)
        allowedCount = if (file.exists()) {
            file.readLines().count { it.isNotBlank() }
        } else 0
    }

    LaunchedEffect(Unit) { refreshStatus() }

    if (showAppPicker) {
        HideAllowlistPicker(
            onDismiss = {
                showAppPicker = false
                refreshStatus()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        PreferenceGroup(title = stringResource(R.string.custom_rom_hide_allowlist)) {
            item {
                ClickablePreference(
                    title = stringResource(R.string.manage_allowlist_apps),
                    summary = if (allowedCount > 0)
                        stringResource(R.string.allowlist_apps_configured, allowedCount)
                    else stringResource(R.string.no_allowlist_apps),
                    icon = Icons.Default.Add,
                    position = PreferencePosition.Single,
                    onClick = { showAppPicker = true },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HideAllowlistPicker(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showSystemApps by remember { mutableStateOf(false) }
    val allApps = remember { mutableStateListOf<HideAppEntry>() }
    val sdkApps = rememberAppList(AppFilter.USER_ONLY, AppFilter.NO_OVERLAYS)

    fun loadAllowlist(): Set<String> {
        val file = File(ALLOWLIST_PATH)
        if (!file.exists()) return emptySet()
        return file.readLines().filter { it.isNotBlank() }.map { it.trim() }.toSet()
    }

    fun saveAllowlist() {
        try {
            val file = File(ALLOWLIST_PATH)
            val lines = allApps.filter { it.isAllowed }.map { it.packageName }
            file.writeText(lines.joinToString("\n"))
            file.setReadable(true, false)
        } catch (e: Exception) {
            Log.e("CustomRomHide", "Failed to save allowlist: ${e.message}")
        }
    }

    LaunchedEffect(sdkApps.value, showSystemApps) {
        val sourceApps = sdkApps.value
        if (sourceApps.isEmpty()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val allowed = loadAllowlist()
            val installedApps = sourceApps
                .map { entry ->
                    HideAppEntry(
                        packageName = entry.packageName,
                        label = entry.label,
                        icon = entry.icon,
                        isAllowed = allowed.contains(entry.packageName),
                    ) to entry.isSystem
                }
                .sortedWith(compareBy({ !it.first.isAllowed }, { it.first.label.lowercase() }))
            withContext(Dispatchers.Main) {
                allApps.clear()
                allApps.addAll(
                    installedApps
                        .filter { !it.second || showSystemApps || it.first.isAllowed }
                        .map { it.first }
                )
                isLoading = false
            }
        }
    }

    val filteredApps = remember(searchQuery, allApps.toList()) {
        val query = searchQuery.lowercase()
        allApps.filter { app ->
            query.isEmpty() ||
                app.label.lowercase().contains(query) ||
                app.packageName.lowercase().contains(query)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            saveAllowlist()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.custom_rom_hide_picker_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.custom_rom_hide_picker_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = showSystemApps,
                    onClick = {
                        showSystemApps = !showSystemApps
                    },
                    label = { Text(stringResource(R.string.show_system_apps)) },
                    leadingIcon = if (showSystemApps) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else null,
                )

                Text(
                    text = stringResource(R.string.selected_count, allApps.count { it.isAllowed }),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.loading_apps))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        HideAppListItem(
                            app = app,
                            onToggle = {
                                val index = allApps.indexOfFirst { it.packageName == app.packageName }
                                if (index >= 0) {
                                    allApps[index] = allApps[index].copy(isAllowed = !allApps[index].isAllowed)
                                    saveAllowlist()
                                }
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HideAppListItem(app: HideAppEntry, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (app.isAllowed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                )
            } else {
                Box(
                    modifier = Modifier.size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = app.label.firstOrNull()?.toString() ?: "?",
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Box(
                modifier = Modifier.size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (app.isAllowed) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (app.isAllowed) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}
