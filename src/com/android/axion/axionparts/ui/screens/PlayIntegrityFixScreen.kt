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
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.random.Random

private const val TAG = "PlayIntegrityFix"
private const val PIF_PATH = "/data/adb/playintegrityfix"
private val PIF_FILES = listOf("custom.pif.prop", "custom.pif.json", "pif.prop", "pif.json")
private const val GOOGLE_URL = "https://developer.android.com"

data class ConfigFileState(
    val fileName: String,
    val exists: Boolean,
    val isActive: Boolean,
    val data: Map<String, String> = emptyMap()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayIntegrityFixScreen(
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
                            text = "Play Integrity Fix",
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
            PlayIntegrityFixContent(
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        PlayIntegrityFixContent(modifier = Modifier)
    }
}

@Composable
fun PlayIntegrityFixContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var configFiles by remember { mutableStateOf(listOf<ConfigFileState>()) }
    var activeConfig by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTargetFile by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(false) }
    var fetchStatus by remember { mutableStateOf("") }
    var importTargetFile by remember { mutableStateOf("") }
    
    fun refreshStatus() {
        val pifDir = File(PIF_PATH)
        val files = mutableListOf<ConfigFileState>()
        var foundActive = false
        
        for (fileName in PIF_FILES) {
            val file = File(pifDir, fileName)
            val exists = file.exists()
            val isActive = exists && !foundActive
            if (isActive) foundActive = true
            
            val data = if (exists) readConfigData(file) else emptyMap()
            files.add(ConfigFileState(fileName, exists, isActive, data))
        }
        
        configFiles = files
        activeConfig = files.find { it.isActive }?.fileName ?: ""
    }
    
    LaunchedEffect(Unit) {
        val pifDir = File(PIF_PATH)
        if (!pifDir.exists()) {
            pifDir.mkdirs()
        }
        refreshStatus()
    }
    
    fun fetchPixelBetaPif() {
        scope.launch {
            isFetching = true
            fetchStatus = "Fetching..."
            
            try {
                val result = withContext(Dispatchers.IO) {
                    fetchBetaPifFromGoogle()
                }
                
                when (result) {
                    is PifFetchResult.Success -> {
                        val pifDir = File(PIF_PATH)
                        if (!pifDir.exists()) {
                            pifDir.mkdirs()
                        }
                        
                        val targetFile = File(pifDir, "pif.json")
                        targetFile.writeText(result.pifData.toString(2))
                        targetFile.setReadable(true, false)
                        
                        Toast.makeText(context, "Fetched: ${result.model}", Toast.LENGTH_SHORT).show()
                        fetchStatus = ""
                        refreshStatus()
                    }
                    is PifFetchResult.Error -> {
                        Toast.makeText(context, "Failed: ${result.message}", Toast.LENGTH_LONG).show()
                        fetchStatus = ""
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch PIF config: ${e.message}")
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                fetchStatus = ""
            } finally {
                isFetching = false
            }
        }
    }
    
    val pifPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val pifDir = File(PIF_PATH)
                    if (!pifDir.exists()) {
                        pifDir.mkdirs()
                    }
                    
                    val targetFileName = importTargetFile.ifEmpty {
                        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "pif.prop"
                        if (fileName.endsWith(".json")) "custom.pif.json" else "custom.pif.prop"
                    }
                    
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val pifFile = File(pifDir, targetFileName)
                    
                    inputStream?.use { input ->
                        pifFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    pifFile.setReadable(true, false)
                    
                    Toast.makeText(context, "Imported as $targetFileName", Toast.LENGTH_SHORT).show()
                    importTargetFile = ""
                    refreshStatus()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import PIF config: ${e.message}")
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        importTargetFile = ""
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete $deleteTargetFile?") },
            text = { Text("This config file will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val pifDir = File(PIF_PATH)
                            File(pifDir, deleteTargetFile).delete()
                            Toast.makeText(context, "Deleted $deleteTargetFile", Toast.LENGTH_SHORT).show()
                            refreshStatus()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        showDeleteDialog = false
                        deleteTargetFile = ""
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; deleteTargetFile = "" }) {
                    Text("Cancel")
                }
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
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Fingerprint Spoofing",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (activeConfig.isNotEmpty()) "Active: $activeConfig" else "No config loaded",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { fetchPixelBetaPif() },
                    enabled = !isFetching,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fetchStatus.ifEmpty { "Fetching..." })
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetch Pixel Beta")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val activeConfigFile = configFiles.find { it.isActive }
        var activeConfigExpanded by remember { mutableStateOf(false) }
        
        if (activeConfigFile != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeConfigExpanded = !activeConfigExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Config",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = activeConfigFile.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Icon(
                            if (activeConfigExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (activeConfigExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    if (activeConfigFile.data.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val model = activeConfigFile.data["MODEL"]
                        val securityPatch = activeConfigFile.data["SECURITY_PATCH"]
                        val fingerprint = activeConfigFile.data["FINGERPRINT"]
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeConfigExpanded = !activeConfigExpanded },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                if (model != null) {
                                    ConfigValueRow("MODEL", model)
                                }
                                if (securityPatch != null) {
                                    ConfigValueRow("SECURITY_PATCH", securityPatch)
                                }
                                if (fingerprint != null) {
                                    ConfigValueRow("FINGERPRINT", fingerprint)
                                }
                                
                                if (activeConfigExpanded) {
                                    val displayKeys = listOf(
                                        "MANUFACTURER", "BRAND", "PRODUCT", "DEVICE", "DEVICE_INITIAL_SDK_INT"
                                    )
                                    displayKeys.forEach { key ->
                                        activeConfigFile.data[key]?.let { value ->
                                            ConfigValueRow(key, value)
                                        }
                                    }
                                    
                                    val settingKeys = activeConfigFile.data.keys.filter { 
                                        it.startsWith("spoof") || it == "DEBUG" || it == "verboseLogs"
                                    }
                                    
                                    if (settingKeys.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = "Settings",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        settingKeys.forEach { key ->
                                            activeConfigFile.data[key]?.let { value ->
                                                ConfigValueRow(key, value)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                deleteTargetFile = activeConfigFile.fileName
                                showDeleteDialog = true 
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                        
                        FilledTonalButton(
                            onClick = { 
                                importTargetFile = activeConfigFile.fileName
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                }
                                pifPicker.launch(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replace")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        val inactiveConfigFiles = configFiles.filter { !it.isActive }
        var configSectionExpanded by remember { mutableStateOf(false) }
        val existingInactiveCount = inactiveConfigFiles.count { it.exists }
        
        if (inactiveConfigFiles.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { configSectionExpanded = !configSectionExpanded }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Other Config Files",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$existingInactiveCount of ${inactiveConfigFiles.size} configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Icon(
                            if (configSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (configSectionExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (configSectionExpanded) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            Text(
                                text = "Priority: custom.pif.* > pif.* (first found is used)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            inactiveConfigFiles.forEachIndexed { index, config ->
                                ConfigFileCard(
                                    config = config,
                                    isFirst = index == 0,
                                    isLast = index == inactiveConfigFiles.lastIndex,
                                    onImport = { fileName ->
                                        importTargetFile = fileName
                                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                            type = "*/*"
                                        }
                                        pifPicker.launch(intent)
                                    },
                                    onDelete = { fileName ->
                                        deleteTargetFile = fileName
                                        showDeleteDialog = true
                                    }
                                )
                                
                                if (index < inactiveConfigFiles.lastIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ConfigFileCard(
    config: ConfigFileState,
    isFirst: Boolean,
    isLast: Boolean,
    onImport: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(config.isActive) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = when {
            config.isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            config.exists -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (config.exists) expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusIcon(
                    exists = config.exists,
                    isActive = config.isActive
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (config.isActive) FontWeight.Bold else FontWeight.Medium
                    )
                    Text(
                        text = when {
                            config.isActive -> "Active - Currently in use"
                            config.exists -> "Available - Overridden by higher priority"
                            else -> "Not configured"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (config.exists) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (config.exists) {
                    OutlinedButton(
                        onClick = { onDelete(config.fileName) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
                
                FilledTonalButton(
                    onClick = { onImport(config.fileName) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (config.exists) "Replace" else "Import")
                }
            }
            
            if (expanded && config.exists && config.data.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        val displayKeys = listOf(
                            "FINGERPRINT", "MODEL", "MANUFACTURER", "BRAND",
                            "PRODUCT", "DEVICE", "SECURITY_PATCH", "DEVICE_INITIAL_SDK_INT"
                        )
                        
                        displayKeys.forEach { key ->
                            config.data[key]?.let { value ->
                                ConfigValueRow(key, value)
                            }
                        }
                        
                        val settingKeys = config.data.keys.filter { 
                            it.startsWith("spoof") || it == "DEBUG" 
                        }
                        if (settingKeys.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            settingKeys.forEach { key ->
                                config.data[key]?.let { value ->
                                    ConfigValueRow(key, value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIcon(exists: Boolean, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                when {
                    isActive -> Color(0xFF10B981)
                    exists -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when {
                isActive -> Icons.Default.Check
                exists -> Icons.Default.Description
                else -> Icons.Default.Close
            },
            contentDescription = null,
            tint = when {
                isActive -> Color.White
                exists -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.outline
            },
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun ConfigValueRow(key: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private sealed class PifFetchResult {
    data class Success(val model: String, val pifData: JSONObject) : PifFetchResult()
    data class Error(val message: String) : PifFetchResult()
}

private fun readConfigData(file: File): Map<String, String> {
    if (!file.exists()) return emptyMap()
    
    return try {
        val content = file.readText()
        val result = mutableMapOf<String, String>()
        
        if (file.name.endsWith(".json")) {
            val json = JSONObject(content)
            json.keys().forEach { key ->
                result[key] = json.optString(key, "")
            }
        } else {
            content.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("//")) {
                    val eqIndex = trimmed.indexOf('=')
                    if (eqIndex > 0) {
                        val key = trimmed.substring(0, eqIndex).trim()
                        val value = trimmed.substring(eqIndex + 1).trim()
                        result[key] = value
                    }
                }
            }
        }
        
        result
    } catch (e: Exception) {
        Log.e(TAG, "Failed to read config: ${e.message}")
        emptyMap()
    }
}

private fun fetchBetaPifFromGoogle(): PifFetchResult {
    try {
        Log.d(TAG, "Fetching Pixel Beta metadata from Google Developer...")

        val versionsHtml = URL("$GOOGLE_URL/about/versions").readText(StandardCharsets.UTF_8)

        val versionPattern = Regex("""https://developer\.android\.com/about/versions/(\d+)""")
        val versions = versionPattern.findAll(versionsHtml)
            .map { it.groupValues[1].toInt() }
            .toSet()
            .sortedDescending()

        if (versions.isEmpty()) {
            return PifFetchResult.Error("Could not find any Android version pages")
        }

        Log.d(TAG, "Found versions: $versions")

        for (version in versions) {
            val versionPage = "$GOOGLE_URL/about/versions/$version"
            Log.d(TAG, "Checking version page: $versionPage")
            
            try {
                val versionHtml = URL(versionPage).readText(StandardCharsets.UTF_8)

                val qprPattern = Regex("""href="(/about/versions/$version/qpr(\d+)/download-ota)"""")
                val qprMatches = qprPattern.findAll(versionHtml)
                    .map { match ->
                        val path = match.groupValues[1]
                        val qprNumber = match.groupValues[2].toInt()
                        qprNumber to path
                    }
                    .toList()
                    .sortedByDescending { it.first }

                if (qprMatches.isEmpty()) {
                    Log.d(TAG, "No QPR beta pages found for version $version")
                    continue
                }

                for ((qprNum, qprPath) in qprMatches) {
                    val otaPage = GOOGLE_URL + qprPath
                    Log.d(TAG, "Trying OTA page: $otaPage (QPR$qprNum)")

                    try {
                        val otaHtml = URL(otaPage).readText(StandardCharsets.UTF_8)

                        val otaUrlList = Regex("""href="(https://dl\.google\.com/[^"]*ota/([^/"]+_beta)[^"]*?)"""")
                            .findAll(otaHtml)
                            .map { it.groupValues[1] to it.groupValues[2] }
                            .toList()

                        if (otaUrlList.isEmpty()) {
                            Log.d(TAG, "No beta OTA URLs found on this page, trying next...")
                            continue
                        }

                        Log.d(TAG, "Found ${otaUrlList.size} beta devices")

                        val devices = mutableListOf<Triple<String, String, String>>()
                        
                        for ((otaUrl, product) in otaUrlList) {
                            val urlIndex = otaHtml.indexOf(otaUrl)
                            if (urlIndex == -1) continue
                            
                            val htmlBefore = otaHtml.substring(0, urlIndex)
                            
                            val tdPattern = Regex("""<td[^>]*>([^<]+)</td>""")
                            val tdMatches = tdPattern.findAll(htmlBefore).toList()
                            
                            if (tdMatches.isNotEmpty()) {
                                val model = tdMatches.last().groupValues[1].trim()
                                devices.add(Triple(model, product, otaUrl))
                                Log.d(TAG, "Matched: $model -> $product")
                            }
                        }

                        if (devices.isEmpty()) {
                            Log.d(TAG, "Could not match devices to OTA URLs, trying next...")
                            continue
                        }

                        val idx = Random.nextInt(devices.size)
                        val (model, product, otaUrl) = devices[idx]
                        val device = product.replace("_beta", "")

                        Log.d(TAG, "Selected: $model ($product) from Android $version QPR$qprNum")
                        Log.d(TAG, "OTA URL: $otaUrl")

                        Log.d(TAG, "Fetching first 4KB from OTA...")
                        val partialData = fetchPartialUrl(otaUrl, 4096)

                        val fingerprintMatch = Regex("""post-build=(.*)""")
                            .find(partialData)
                            ?: return PifFetchResult.Error("Could not extract fingerprint from OTA metadata")

                        val securityPatchMatch = Regex("""security-patch-level=(.*)""")
                            .find(partialData)
                            ?: return PifFetchResult.Error("Could not extract security patch from OTA metadata")

                        val fingerprint = fingerprintMatch.groupValues[1].trim()
                        val securityPatch = securityPatchMatch.groupValues[1].trim()

                        Log.d(TAG, "Fingerprint: $fingerprint")
                        Log.d(TAG, "Security Patch: $securityPatch")

                        val pifJson = JSONObject().apply {
                            put("MANUFACTURER", "Google")
                            put("MODEL", model)
                            put("PRODUCT", product)
                            put("DEVICE", device)
                            put("FINGERPRINT", fingerprint)
                            put("SECURITY_PATCH", securityPatch)
                            put("DEVICE_INITIAL_SDK_INT", "32")
                        }

                        return PifFetchResult.Success(model, pifJson)

                    } catch (e: Exception) {
                        Log.d(TAG, "Failed to fetch from QPR$qprNum: ${e.message}")
                        continue
                    }
                }

            } catch (e: Exception) {
                Log.d(TAG, "Failed to fetch version $version: ${e.message}")
                continue
            }
        }

        return PifFetchResult.Error("Could not find any valid beta OTA pages")

    } catch (e: Exception) {
        Log.e(TAG, "Error fetching from Google developer site", e)
        return PifFetchResult.Error("Failed to fetch from Google: ${e.message}")
    }
}

private fun fetchPartialUrl(url: String, maxBytes: Int): String {
    val connection = URL(url).openConnection()
    connection.connectTimeout = 15000
    connection.readTimeout = 15000

    connection.getInputStream().use { inputStream ->
        val buffer = ByteArray(512)
        val result = StringBuilder()
        var totalRead = 0

        while (totalRead < maxBytes) {
            val read = inputStream.read(buffer)
            if (read == -1) break

            val chunk = buffer.copyOf(read)
            result.append(String(chunk, StandardCharsets.ISO_8859_1))
            totalRead += read
        }

        return result.toString()
    }
}
