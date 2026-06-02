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

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionparts.ui.screens

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.axionparts.R
import com.android.axion.compose.applist.AppFilter
import com.android.axion.compose.applist.rememberAppList
import com.android.axion.compose.preferences.*
import android.provider.Settings
import android.util.Base64
import com.android.axion.compose.scaffold.AxionScaffold
import com.android.axion.compose.sheet.BottomSheetDialog
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val KEYBOX_KEY = "spoof_trickystore_keybox"
private const val TARGET_KEY = "spoof_trickystore_target"
private const val VENDING_PACKAGE = "com.android.vending"

enum class TargetMode(val symbol: String) {
    AUTO(""),
    LEAF_HACK("?"),
    CERT_GEN("!"),
}

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    var targetMode: TargetMode = TargetMode.AUTO,
    var isInTarget: Boolean = false,
)

@Composable
fun TrickyStoreScreen(onBackClick: (() -> Unit)? = null, showTopBar: Boolean = true) {
    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.trickystore),
            onBackClick = { onBackClick?.invoke() },
        ) { innerPadding ->
            TrickyStoreContent(modifier = Modifier.padding(innerPadding))
        }
    } else {
        TrickyStoreContent(modifier = Modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrickyStoreContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var keyboxExists by remember { mutableStateOf(false) }
    var targetExists by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var targetAppCount by remember { mutableStateOf(0) }

    fun refreshStatus() {
        keyboxExists = !Settings.Secure.getString(context.contentResolver, KEYBOX_KEY).isNullOrEmpty()
        val targetContent = Settings.Secure.getString(context.contentResolver, TARGET_KEY)
        targetExists = !targetContent.isNullOrEmpty()
        targetAppCount = if (targetExists) {
            targetContent!!.lines().count { it.isNotBlank() }
        } else {
            0
        }
    }

    LaunchedEffect(Unit) {
        refreshStatus()
    }

    val keyboxPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: ByteArray(0)
                        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        Settings.Secure.putString(context.contentResolver, KEYBOX_KEY, encoded)

                        val am =
                            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        am.forceStopPackage(VENDING_PACKAGE)

                        Toast.makeText(
                                context,
                                context.getString(R.string.keybox_imported_success),
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                        refreshStatus()
                    } catch (e: Exception) {
                        Log.e("TrickyStore", "Failed to import keybox: ${e.message}")
                        Toast.makeText(
                                context,
                                context.getString(R.string.keybox_import_failed, e.message),
                                Toast.LENGTH_LONG,
                            )
                            .show()
                    }
                }
            }
        }

    val targetPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        val text = context.contentResolver.openInputStream(uri)?.use { input ->
                            input.readBytes().toString(StandardCharsets.UTF_8)
                        } ?: ""
                        Settings.Secure.putString(context.contentResolver, TARGET_KEY, text)

                        Toast.makeText(
                                context,
                                context.getString(R.string.target_list_imported),
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                        refreshStatus()
                    } catch (e: Exception) {
                        Toast.makeText(
                                context,
                                context.getString(R.string.target_list_import_failed, e.message),
                                Toast.LENGTH_LONG,
                            )
                            .show()
                    }
                }
            }
        }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_keybox_title)) },
            text = { Text(stringResource(R.string.delete_keybox_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            Settings.Secure.putString(context.contentResolver, KEYBOX_KEY, null)
                            Toast.makeText(
                                    context,
                                    context.getString(R.string.keybox_deleted),
                                    Toast.LENGTH_SHORT,
                                )
                                .show()
                            refreshStatus()
                        } catch (e: Exception) {
                            Toast.makeText(
                                    context,
                                    context.getString(R.string.keybox_delete_failed, e.message),
                                    Toast.LENGTH_LONG,
                                )
                                .show()
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showAppPicker) {
        AppPickerBottomSheet(
            onDismiss = {
                showAppPicker = false
                refreshStatus()
            }
        )
    }

    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        SettingsSection(
            title = stringResource(R.string.keybox_management),
            icon = Icons.Default.Key,
        ) {
            ClickablePreference(
                title = stringResource(R.string.import_keybox),
                summary =
                    if (keyboxExists) stringResource(R.string.keybox_installed)
                    else stringResource(R.string.no_keybox_found),
                icon = Icons.Default.Upload,
                position = PreferencePosition.Top,
                onClick = {
                    val intent =
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                        }
                    keyboxPicker.launch(intent)
                },
            )

            ClickablePreference(
                title = stringResource(R.string.delete_keybox),
                summary = stringResource(R.string.remove_keybox_file),
                icon = Icons.Default.Delete,
                enabled = keyboxExists,
                position = PreferencePosition.Bottom,
                onClick = { showDeleteDialog = true },
            )
        }

        SettingsSection(
            title = stringResource(R.string.target_configuration),
            icon = Icons.Default.Security,
        ) {
            ClickablePreference(
                title = stringResource(R.string.manage_target_apps),
                summary =
                    if (targetAppCount > 0)
                        stringResource(R.string.target_apps_configured, targetAppCount)
                    else stringResource(R.string.no_apps_configured),
                icon = Icons.Default.Add,
                position = PreferencePosition.Top,
                onClick = { showAppPicker = true },
            )

            ClickablePreference(
                title = stringResource(R.string.import_target_list),
                summary = stringResource(R.string.import_from_file),
                icon = Icons.Default.Upload,
                position = PreferencePosition.Bottom,
                onClick = {
                    val intent =
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "text/*"
                        }
                    targetPicker.launch(intent)
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerBottomSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showSystemApps by remember { mutableStateOf(false) }
    val allApps = remember { mutableStateListOf<AppEntry>() }
    val sdkApps = rememberAppList(AppFilter.ALL, AppFilter.NO_OVERLAYS)

    fun loadTargetFile(): Map<String, TargetMode> {
        val result = mutableMapOf<String, TargetMode>()
        val content = Settings.Secure.getString(context.contentResolver, TARGET_KEY) ?: return result
        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank()) {
                when {
                    trimmed.endsWith("?") -> {
                        result[trimmed.dropLast(1)] = TargetMode.LEAF_HACK
                    }
                    trimmed.endsWith("!") -> {
                        result[trimmed.dropLast(1)] = TargetMode.CERT_GEN
                    }
                    else -> {
                        result[trimmed] = TargetMode.AUTO
                    }
                }
            }
        }
        return result
    }

    fun saveTargetFile() {
        try {
            val lines =
                allApps
                    .filter { it.isInTarget }
                    .map { app -> app.packageName + app.targetMode.symbol }

            Settings.Secure.putString(
                context.contentResolver,
                TARGET_KEY,
                lines.joinToString("\n")
            )
        } catch (e: Exception) {
            Log.e("TrickyStore", "Failed to save target list: ${e.message}")
        }
    }

    LaunchedEffect(sdkApps.value, showSystemApps) {
        val sourceApps = sdkApps.value
        if (sourceApps.isEmpty()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val targetMap = loadTargetFile()
            val installedApps = sourceApps
                .map { entry ->
                    AppEntry(
                        packageName = entry.packageName,
                        label = entry.label,
                        icon = entry.icon,
                        targetMode = targetMap[entry.packageName] ?: TargetMode.AUTO,
                        isInTarget = targetMap.containsKey(entry.packageName),
                    ) to entry.isSystem
                }
                .sortedWith(compareBy({ !(it.first.isInTarget) }, { it.first.label.lowercase() }))
            withContext(Dispatchers.Main) {
                allApps.clear()
                allApps.addAll(
                    installedApps
                        .filter { !it.second || showSystemApps || it.first.isInTarget }
                        .map { it.first }
                )
                isLoading = false
            }
        }
    }

    val filteredApps =
        remember(searchQuery, allApps.toList()) {
            val query = searchQuery.lowercase()
            allApps.filter { app ->
                query.isEmpty() ||
                    app.label.lowercase().contains(query) ||
                    app.packageName.lowercase().contains(query)
            }
        }

    BottomSheetDialog(
        onDismiss = {
            saveTargetFile()
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.select_target_apps),
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.choose_apps_for_attestation),
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
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear),
                            )
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
                    leadingIcon =
                        if (showSystemApps) {
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
                    text = stringResource(R.string.selected_count, allApps.count { it.isInTarget }),
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
                    LoadingIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppListItem(
                            app = app,
                            onToggle = {
                                val index =
                                    allApps.indexOfFirst { it.packageName == app.packageName }
                                if (index >= 0) {
                                    allApps[index] =
                                        allApps[index].copy(isInTarget = !allApps[index].isInTarget)
                                    saveTargetFile()
                                }
                            },
                            onModeChange = { mode ->
                                val index =
                                    allApps.indexOfFirst { it.packageName == app.packageName }
                                if (index >= 0) {
                                    allApps[index] = allApps[index].copy(targetMode = mode)
                                    saveTargetFile()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListItem(app: AppEntry, onToggle: () -> Unit, onModeChange: (TargetMode) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color =
            if (app.isInTarget) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle() },
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
                        modifier =
                            Modifier.size(40.dp)
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
                    modifier =
                        Modifier.size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (app.isInTarget) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (app.isInTarget) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

            if (app.isInTarget) {
                Spacer(modifier = Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TargetMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = app.targetMode == mode,
                            onClick = { onModeChange(mode) },
                            shape =
                                SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = TargetMode.entries.size,
                                ),
                            label = {
                                Text(
                                    text =
                                        when (mode) {
                                            TargetMode.AUTO ->
                                                stringResource(R.string.target_mode_auto)
                                            TargetMode.LEAF_HACK ->
                                                stringResource(R.string.target_mode_leaf_hack)
                                            TargetMode.CERT_GEN ->
                                                stringResource(R.string.target_mode_cert_gen)
                                        },
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
