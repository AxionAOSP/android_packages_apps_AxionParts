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

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.axionparts.ui.components.ClickablePreference
import com.android.axion.axionparts.ui.components.PreferencePosition
import com.android.axion.axionparts.ui.components.SettingsSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TRICKYSTORE_PATH = "/data/adb/tricky_store"
private const val KEYBOX_FILE = "keybox.xml"
private const val TARGET_FILE = "target.txt"

enum class TargetMode(val symbol: String, val description: String) {
    AUTO("", "Auto - Automatic mode selection"),
    LEAF_HACK("?", "Leaf Hack - Force leaf certificate hacking"),
    CERT_GEN("!", "Cert Gen - Force certificate generation")
}

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    var targetMode: TargetMode = TargetMode.AUTO,
    var isInTarget: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrickyStoreScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true
) {
    if (showTopBar) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "TrickyStore",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        onBackClick?.let { onClick ->
                            IconButton(onClick = onClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            TrickyStoreContent(
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        TrickyStoreContent(modifier = Modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrickyStoreContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var keyboxExists by remember { mutableStateOf(false) }
    var targetExists by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var targetAppCount by remember { mutableStateOf(0) }
    
    fun refreshStatus() {
        val trickyDir = File(TRICKYSTORE_PATH)
        keyboxExists = File(trickyDir, KEYBOX_FILE).exists()
        val targetFile = File(trickyDir, TARGET_FILE)
        targetExists = targetFile.exists()
        if (targetExists) {
            targetAppCount = targetFile.readLines().filter { it.isNotBlank() }.size
        } else {
            targetAppCount = 0
        }
    }
    
    LaunchedEffect(Unit) {
        val trickyDir = File(TRICKYSTORE_PATH)
        if (!trickyDir.exists()) {
            trickyDir.mkdirs()
        }
        refreshStatus()
    }
    
    val keyboxPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val trickyDir = File(TRICKYSTORE_PATH)
                    if (!trickyDir.exists()) {
                        trickyDir.mkdirs()
                    }
                    
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val keyboxFile = File(trickyDir, KEYBOX_FILE)
                    
                    inputStream?.use { input ->
                        keyboxFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    keyboxFile.setReadable(true, false)
                    
                    Toast.makeText(context, "Keybox imported successfully", Toast.LENGTH_SHORT).show()
                    refreshStatus()
                } catch (e: Exception) {
                    Log.e("TrickyStore", "Failed to import keybox: ${e.message}")
                    Toast.makeText(context, "Failed to import keybox: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    val targetPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val trickyDir = File(TRICKYSTORE_PATH)
                    if (!trickyDir.exists()) {
                        trickyDir.mkdirs()
                    }
                    
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val targetFile = File(trickyDir, TARGET_FILE)
                    
                    inputStream?.use { input ->
                        targetFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    targetFile.setReadable(true, false)
                    
                    Toast.makeText(context, "Target list imported successfully", Toast.LENGTH_SHORT).show()
                    refreshStatus()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to import target list: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Keybox?") },
            text = { Text("This will remove the keybox.xml file. Certificate spoofing will be disabled until a new keybox is imported.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val keyboxFile = File(TRICKYSTORE_PATH, KEYBOX_FILE)
                            if (keyboxFile.exists()) {
                                keyboxFile.delete()
                            }
                            Toast.makeText(context, "Keybox deleted", Toast.LENGTH_SHORT).show()
                            refreshStatus()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to delete keybox: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        SettingsSection(
            title = "Keybox Management",
            icon = Icons.Default.Key,
            gradientColors = listOf(
                Color(0xFF059669),
                Color(0xFF10B981),
                Color(0xFF34D399)
            )
        ) {
            ClickablePreference(
                title = "Import Keybox",
                summary = if (keyboxExists) "Keybox is installed" else "No keybox found - tap to import",
                icon = Icons.Default.Upload,
                position = PreferencePosition.Top,
                onClick = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }
                    keyboxPicker.launch(intent)
                }
            )
            
            ClickablePreference(
                title = "Delete Keybox",
                summary = "Remove the current keybox file",
                icon = Icons.Default.Delete,
                enabled = keyboxExists,
                position = PreferencePosition.Bottom,
                onClick = {
                    showDeleteDialog = true
                }
            )
        }
        
        SettingsSection(
            title = "Target Configuration", 
            icon = Icons.Default.Security,
            gradientColors = listOf(
                Color(0xFF7C3AED),
                Color(0xFFA855F7),
                Color(0xFFD946EF)
            )
        ) {
            ClickablePreference(
                title = "Manage Target Apps",
                summary = if (targetAppCount > 0) "$targetAppCount apps configured" else "No apps configured - tap to add",
                icon = Icons.Default.Add,
                position = PreferencePosition.Top,
                onClick = {
                    showAppPicker = true
                }
            )
            
            ClickablePreference(
                title = "Import Target List",
                summary = "Import from file",
                icon = Icons.Default.Upload,
                position = PreferencePosition.Bottom,
                onClick = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/*"
                    }
                    targetPicker.launch(intent)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showSystemApps by remember { mutableStateOf(false) }
    val allApps = remember { mutableStateListOf<AppEntry>() }
    
    fun loadTargetFile(): Map<String, TargetMode> {
        val result = mutableMapOf<String, TargetMode>()
        val targetFile = File(TRICKYSTORE_PATH, TARGET_FILE)
        if (targetFile.exists()) {
            targetFile.readLines().forEach { line ->
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
        }
        return result
    }
    
    fun saveTargetFile() {
        try {
            val trickyDir = File(TRICKYSTORE_PATH)
            if (!trickyDir.exists()) {
                trickyDir.mkdirs()
            }
            
            val targetFile = File(trickyDir, TARGET_FILE)
            val lines = allApps.filter { it.isInTarget }.map { app ->
                app.packageName + app.targetMode.symbol
            }
            
            targetFile.writeText(lines.joinToString("\n"))
            targetFile.setReadable(true, false)
        } catch (e: Exception) {
            Log.e("TrickyStore", "Failed to save target file: ${e.message}")
        }
    }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val targetMap = loadTargetFile()
            
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .map { appInfo ->
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    AppEntry(
                        packageName = appInfo.packageName,
                        label = appInfo.loadLabel(pm).toString(),
                        icon = try { appInfo.loadIcon(pm) } catch (e: Exception) { null },
                        targetMode = targetMap[appInfo.packageName] ?: TargetMode.AUTO,
                        isInTarget = targetMap.containsKey(appInfo.packageName)
                    ) to isSystem
                }
                .sortedWith(compareBy({ !(it.first.isInTarget) }, { it.first.label.lowercase() }))
            
            withContext(Dispatchers.Main) {
                allApps.clear()
                allApps.addAll(installedApps.filter { !it.second || showSystemApps || it.first.isInTarget }.map { it.first })
                isLoading = false
            }
        }
    }
    
    val filteredApps = remember(searchQuery, allApps.toList(), showSystemApps) {
        val pm = context.packageManager
        val query = searchQuery.lowercase()
        allApps.filter { app ->
            val isSystem = try {
                val appInfo = pm.getApplicationInfo(app.packageName, 0)
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (e: Exception) { false }
            
            val matchesSearch = query.isEmpty() || 
                app.label.lowercase().contains(query) || 
                app.packageName.lowercase().contains(query)
            
            val matchesFilter = showSystemApps || !isSystem || app.isInTarget
            
            matchesSearch && matchesFilter
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = {
            saveTargetFile()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select Target Apps",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Choose apps to enable attestation spoofing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showSystemApps,
                    onClick = { 
                        showSystemApps = !showSystemApps
                        scope.launch(Dispatchers.IO) {
                            val pm = context.packageManager
                            val targetMap = loadTargetFile()
                            
                            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                                .map { appInfo ->
                                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                    AppEntry(
                                        packageName = appInfo.packageName,
                                        label = appInfo.loadLabel(pm).toString(),
                                        icon = try { appInfo.loadIcon(pm) } catch (e: Exception) { null },
                                        targetMode = targetMap[appInfo.packageName] ?: TargetMode.AUTO,
                                        isInTarget = targetMap.containsKey(appInfo.packageName)
                                    ) to isSystem
                                }
                                .sortedWith(compareBy({ !(it.first.isInTarget) }, { it.first.label.lowercase() }))
                            
                            withContext(Dispatchers.Main) {
                                allApps.clear()
                                allApps.addAll(installedApps.filter { !it.second || showSystemApps || it.first.isInTarget }.map { it.first })
                            }
                        }
                    },
                    label = { Text("Show System Apps") },
                    leadingIcon = if (showSystemApps) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                
                Text(
                    text = "${allApps.count { it.isInTarget }} selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading apps...")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppListItem(
                            app = app,
                            onToggle = { 
                                val index = allApps.indexOfFirst { it.packageName == app.packageName }
                                if (index >= 0) {
                                    allApps[index] = allApps[index].copy(isInTarget = !allApps[index].isInTarget)
                                    saveTargetFile()
                                }
                            },
                            onModeChange = { mode ->
                                val index = allApps.indexOfFirst { it.packageName == app.packageName }
                                if (index >= 0) {
                                    allApps[index] = allApps[index].copy(targetMode = mode)
                                    saveTargetFile()
                                }
                            }
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
fun AppListItem(
    app: AppEntry,
    onToggle: () -> Unit,
    onModeChange: (TargetMode) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (app.isInTarget) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.label.firstOrNull()?.toString() ?: "?",
                            color = MaterialTheme.colorScheme.onPrimary
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
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (app.isInTarget) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.isInTarget) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            if (app.isInTarget) {
                Spacer(modifier = Modifier.height(8.dp))
                
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TargetMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = app.targetMode == mode,
                            onClick = { onModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = TargetMode.entries.size
                            ),
                            label = {
                                Text(
                                    text = when (mode) {
                                        TargetMode.AUTO -> "Auto"
                                        TargetMode.LEAF_HACK -> "Leaf ?"
                                        TargetMode.CERT_GEN -> "Gen !"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
