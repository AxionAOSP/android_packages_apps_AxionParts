/*
 * Copyright (C) 2025-2026 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.android.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.axion.axionparts.ui.screens

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.theme.MaxContentWidth
import com.android.axion.compose.preferences.BasePreference
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.scaffold.AxionScaffold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

private const val SETTING_KEY = "system_adblock_enabled"
private const val SETTING_BLOCKED_COUNT_KEY = "system_adblock_blocked_count"
private const val DEFAULT_SOURCE = "https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt"

private fun getWhitelistFile(context: Context) = File(context.filesDir, "adblock_whitelist.txt")
private fun getBlacklistFile(context: Context) = File(context.filesDir, "adblock_blacklist.txt")
private fun getDownloadedFile(context: Context) = File(context.filesDir, "adblock_downloaded.txt")
private fun getSourcesFile(context: Context) = File(context.filesDir, "adblock_sources.txt")

@Composable
fun observeSecureIntState(context: Context, key: String, defaultValue: Int): androidx.compose.runtime.State<Int> {
    val cr = context.contentResolver
    return produceState(initialValue = Settings.Secure.getInt(cr, key, defaultValue)) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = Settings.Secure.getInt(cr, key, defaultValue)
            }
        }
        cr.registerContentObserver(Settings.Secure.getUriFor(key), false, observer)
        awaitDispose {
            cr.unregisterContentObserver(observer)
        }
    }
}

@Composable
fun observeSecureBooleanState(context: Context, key: String, defaultValue: Boolean): androidx.compose.runtime.State<Boolean> {
    val cr = context.contentResolver
    return produceState(initialValue = Settings.Secure.getInt(cr, key, if (defaultValue) 1 else 0) == 1) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = Settings.Secure.getInt(cr, key, if (defaultValue) 1 else 0) == 1
            }
        }
        cr.registerContentObserver(Settings.Secure.getUriFor(key), false, observer)
        awaitDispose {
            cr.unregisterContentObserver(observer)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AdBlockScreen(
    onBackClick: (() -> Unit)? = null
) {
    AxionScaffold(
        title = stringResource(R.string.system_adblock_title),
        onBackClick = { onBackClick?.invoke() },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            AdBlockContent(
                modifier = Modifier.widthIn(max = MaxContentWidth)
            )
        }
    }
}

@Composable
fun AdBlockContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var whitelist by remember { mutableStateOf(listOf<String>()) }
    var blacklist by remember { mutableStateOf(listOf<String>()) }
    var sources by remember { mutableStateOf(listOf<String>()) }
    var isDownloadedPresetActive by remember { mutableStateOf(false) }
    
    // Live Event-Driven Secure Settings Observers! (0% Lag, 100% Real-time updates!)
    val blockedCount by observeSecureIntState(context, SETTING_BLOCKED_COUNT_KEY, 0)
    val isAdBlockEnabled by observeSecureBooleanState(context, SETTING_KEY, false)

    var showAddDialog by remember { mutableStateOf(false) }
    var isAddingToWhitelist by remember { mutableStateOf(true) }
    var isAddingSource by remember { mutableStateOf(false) }
    var editingUrl by remember { mutableStateOf<String?>(null) }
    var newDomainText by remember { mutableStateOf("") }
    
    var isUpdatingList by remember { mutableStateOf(false) }

    // Load static list configs on launch
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val white = loadList(getWhitelistFile(context))
            val black = loadList(getBlacklistFile(context))
            
            // Setup default sources only if file does not exist (Allows explicit deletion!)
            val sourcesFile = getSourcesFile(context)
            if (!sourcesFile.exists()) {
                sourcesFile.writeText(DEFAULT_SOURCE)
            }
            val srcList = loadList(sourcesFile)
            
            val hasDownloaded = getDownloadedFile(context).exists()
            
            withContext(Dispatchers.Main) {
                whitelist = white
                blacklist = black
                sources = srcList
                isDownloadedPresetActive = hasDownloaded
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // AdBlock Master Toggle (With on-demand initial setup and dynamic status summary!)
        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isUpdatingList) {
                            val target = !isAdBlockEnabled
                            if (target) {
                                scope.launch(Dispatchers.IO) {
                                    val file = getDownloadedFile(context)
                                    if (!file.exists()) {
                                        isUpdatingList = true
                                        // Fetch Blocklists
                                        val success = downloadOnlineBlocklist(file, sources)
                                        if (success) {
                                            Settings.Secure.putInt(context.contentResolver, SETTING_KEY, 1)
                                            withContext(Dispatchers.Main) {
                                                isDownloadedPresetActive = true
                                                isUpdatingList = false
                                                Toast.makeText(context, "System AdBlock activated successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                isUpdatingList = false
                                                Toast.makeText(context, "Active connection required for initial setup.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Settings.Secure.putInt(context.contentResolver, SETTING_KEY, 1)
                                    }
                                }
                            } else {
                                Settings.Secure.putInt(context.contentResolver, SETTING_KEY, 0)
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isAdBlockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.system_adblock_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when {
                                isUpdatingList -> "Downloading initial blocklist..."
                                isAdBlockEnabled -> "Active (Blocked: $blockedCount domains)"
                                else -> "Inactive"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isUpdatingList) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        androidx.compose.material3.Switch(
                            checked = isAdBlockEnabled,
                            onCheckedChange = null // Click event is handled cleanly by parent Row
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multiple Sources List Manager (Extended beautifully using standard BasePreference items!)
        PreferenceGroup(title = "Blocklist Sources (URLs)") {
            item {
                ClickablePreference(
                    title = "Add Blocklist Source",
                    summary = "Enter a custom hosts file URL (AdAway, OISD, StevenBlack format)",
                    icon = Icons.Default.Add,
                    onClick = {
                        isAddingToWhitelist = false
                        isAddingSource = true
                        editingUrl = null
                        newDomainText = ""
                        showAddDialog = true
                    }
                )
            }
            sources.forEach { url ->
                item {
                    BasePreference(
                        title = url,
                        modifier = Modifier.clickable(enabled = !isUpdatingList) {
                            // Tap the preference card natively to edit it! (Super-premium UX!)
                            isAddingToWhitelist = false
                            isAddingSource = true
                            editingUrl = url
                            newDomainText = url
                            showAddDialog = true
                        },
                        widget = {
                            IconButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    removeFromList(getSourcesFile(context), url, context, scope)
                                    val updated = loadList(getSourcesFile(context))
                                    withContext(Dispatchers.Main) {
                                        sources = updated
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }

            // Integrated Updates and Downloads directly at the bottom of the Sources list!
            if (isDownloadedPresetActive) {
                item {
                    ClickablePreference(
                        title = if (isUpdatingList) "Updating Blocklists..." else "Check for Updates",
                        summary = "Using ${sources.size} combined online lists (Downloaded)",
                        icon = Icons.Default.CloudDownload,
                        enabled = !isUpdatingList,
                        onClick = {
                            isUpdatingList = true
                            scope.launch(Dispatchers.IO) {
                                val success = downloadOnlineBlocklist(getDownloadedFile(context), sources)
                                withContext(Dispatchers.Main) {
                                    isUpdatingList = false
                                    if (success) {
                                        isDownloadedPresetActive = true
                                        triggerRefresh(context, scope)
                                        Toast.makeText(context, "System AdBlock updated to online list!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to download update. Check connection.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            } else {
                item {
                    ClickablePreference(
                        title = "Download Online Blocklist",
                        summary = "Download and compile your active sources lists to activate adblocking",
                        icon = Icons.Default.CloudDownload,
                        enabled = !isUpdatingList,
                        onClick = {
                            isUpdatingList = true
                            scope.launch(Dispatchers.IO) {
                                val success = downloadOnlineBlocklist(getDownloadedFile(context), sources)
                                withContext(Dispatchers.Main) {
                                    isUpdatingList = false
                                    if (success) {
                                        isDownloadedPresetActive = true
                                        triggerRefresh(context, scope)
                                        Toast.makeText(context, "Online blocklist downloaded successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to download blocklist. Check connection.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Whitelist Manager (Using standard Preference elements to group natively!)
        PreferenceGroup(title = "Custom Whitelist (Allowed Domains)") {
            item {
                ClickablePreference(
                    title = "Add Whitelisted Domain",
                    summary = "Allow certain websites or trackers from being blocked",
                    icon = Icons.Default.Add,
                    onClick = {
                        isAddingToWhitelist = true
                        isAddingSource = false
                        newDomainText = ""
                        showAddDialog = true
                    }
                )
            }
            
            if (whitelist.isEmpty()) {
                item {
                    BasePreference(
                        title = "No domains whitelisted",
                        summary = "Tap Add to exclude a website.",
                        icon = Icons.Default.Info
                    )
                }
            } else {
                whitelist.forEach { domain ->
                    item {
                        BasePreference(
                            title = domain,
                            widget = {
                                IconButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        removeFromList(getWhitelistFile(context), domain, context, scope)
                                        val updated = loadList(getWhitelistFile(context))
                                        withContext(Dispatchers.Main) {
                                            whitelist = updated
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Blacklist Manager (Using standard Preference elements to group natively!)
        PreferenceGroup(title = "Custom Blacklist (Blocked Domains)") {
            item {
                ClickablePreference(
                    title = "Add Blacklisted Domain",
                    summary = "Block additional custom domains manually",
                    icon = Icons.Default.Add,
                    onClick = {
                        isAddingToWhitelist = false
                        isAddingSource = false
                        newDomainText = ""
                        showAddDialog = true
                    }
                )
            }
            
            if (blacklist.isEmpty()) {
                item {
                    BasePreference(
                        title = "No domains blacklisted",
                        summary = "Tap Add to block a website.",
                        icon = Icons.Default.Info
                    )
                }
            } else {
                blacklist.forEach { domain ->
                    item {
                        BasePreference(
                            title = domain,
                            widget = {
                                IconButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        removeFromList(getBlacklistFile(context), domain, context, scope)
                                        val updated = loadList(getBlacklistFile(context))
                                        withContext(Dispatchers.Main) {
                                            blacklist = updated
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Add dialog (for domains or sources)
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingUrl = null },
            title = {
                Text(
                    text = when {
                        editingUrl != null -> "Edit Blocklist Source"
                        isAddingSource -> "Add Blocklist Source"
                        isAddingToWhitelist -> "Add to Whitelist"
                        else -> "Add to Blacklist"
                    }
                )
            },
            text = {
                Column {
                    Text(
                        text = when {
                            editingUrl != null -> "Modify the URL of this hosts file blocklist."
                            isAddingSource -> "Type the URL of a hosts file blocklist (HTTP or HTTPS)."
                            isAddingToWhitelist -> "Type the domain name (e.g. adserver.com) to whitelist."
                            else -> "Type the domain name to block system-wide."
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newDomainText,
                        onValueChange = { newDomainText = it },
                        label = { Text(if (isAddingSource) "URL" else "Domain Name") },
                        singleLine = true,
                        placeholder = { Text(if (isAddingSource) "https://example.com/hosts.txt" else "domain.com") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val text = newDomainText.trim()
                    if (text.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            when {
                                isAddingSource -> {
                                    isUpdatingList = true
                                    showAddDialog = false
                                    
                                    val file = getSourcesFile(context)
                                    val list = loadList(file).toMutableList()
                                    
                                    if (editingUrl != null) {
                                        // Edit Mode: Replace the old URL with the new one!
                                        val index = list.indexOf(editingUrl)
                                        if (index != -1) {
                                            list[index] = text
                                        } else {
                                            if (!list.contains(text)) list.add(text)
                                        }
                                        editingUrl = null
                                    } else {
                                        // Add Mode: Append if unique
                                        if (!list.contains(text)) list.add(text)
                                    }
                                    
                                    // Save updated list
                                    file.writeText(list.joinToString("\n"))
                                    val updated = loadList(file)
                                    
                                    // Immediately download and compile the updated sources list!
                                    val success = downloadOnlineBlocklist(getDownloadedFile(context), updated)
                                    
                                    withContext(Dispatchers.Main) {
                                        sources = updated
                                        isUpdatingList = false
                                        if (success) {
                                            isDownloadedPresetActive = true
                                            triggerRefresh(context, scope)
                                            Toast.makeText(context, "Blocklist sources updated and compiled!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Sources saved, but download failed. Check connection.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                isAddingToWhitelist -> {
                                    val domain = text.lowercase()
                                    addToList(getWhitelistFile(context), domain, context, scope)
                                    val updated = loadList(getWhitelistFile(context))
                                    withContext(Dispatchers.Main) {
                                        whitelist = updated
                                        showAddDialog = false
                                    }
                                }
                                else -> {
                                    val domain = text.lowercase()
                                    addToList(getBlacklistFile(context), domain, context, scope)
                                    val updated = loadList(getBlacklistFile(context))
                                    withContext(Dispatchers.Main) {
                                        blacklist = updated
                                        showAddDialog = false
                                    }
                                }
                            }
                        }
                    }
                }) {
                    Text(if (editingUrl != null) "Save" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; editingUrl = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun loadList(file: File): List<String> {
    val list = mutableListOf<String>()
    try {
        if (file.exists()) {
            file.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    list.add(trimmed)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("AdBlockScreen", "Failed to load custom list: ${file.absolutePath}", e)
    }
    return list.sorted()
}

// Simple helper to append a line to a file without triggering any system refresh (for URLs!)
private fun addToListFile(file: File, data: String) {
    try {
        val list = loadList(file).toMutableList()
        if (!list.contains(data)) {
            list.add(data)
            file.writeText(list.joinToString("\n"))
        }
    } catch (e: Exception) {
        Log.e("AdBlockScreen", "Failed to append to file: ${file.absolutePath}", e)
    }
}

private fun addToList(file: File, domain: String, context: Context, scope: CoroutineScope) {
    try {
        val list = loadList(file).toMutableList()
        if (!list.contains(domain)) {
            list.add(domain)
            file.writeText(list.joinToString("\n"))
            triggerRefresh(context, scope)
        }
    } catch (e: Exception) {
        Log.e("AdBlockScreen", "Failed to add domain to list: ${file.absolutePath}", e)
    }
}

private fun removeFromList(file: File, domain: String, context: Context, scope: CoroutineScope) {
    try {
        val list = loadList(file).toMutableList()
        if (list.remove(domain)) {
            if (list.isEmpty()) {
                file.delete()
            } else {
                file.writeText(list.joinToString("\n"))
            }
            triggerRefresh(context, scope)
        }
    } catch (e: Exception) {
        Log.e("AdBlockScreen", "Failed to remove domain from list: ${file.absolutePath}", e)
    }
}

// Download and combine all active blocklist source URLs (Multi-Source Support!)
private fun downloadOnlineBlocklist(destination: File, sourceUrls: List<String>): Boolean {
    try {
        FileOutputStream(destination).use { outStream ->
            var successfulDownloads = 0
            for (sourceUrl in sourceUrls) {
                try {
                    val url = URL(sourceUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000
                    conn.readTimeout = 15000
                    
                    val responseCode = conn.getResponseCode()
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedInputStream(conn.inputStream).use { inStream ->
                            val buffer = ByteArray(4096)
                            var len: Int
                            while (inStream.read(buffer).also { len = it } != -1) {
                                outStream.write(buffer, 0, len)
                            }
                        }
                        // Write newline to separate hosts files safely
                        outStream.write("\n".toByteArray())
                        successfulDownloads++
                    }
                } catch (e: Exception) {
                    Log.e("AdBlockScreen", "Failed downloading blocklist from: $sourceUrl", e)
                }
            }
            return successfulDownloads > 0
        }
    } catch (e: Exception) {
        Log.e("AdBlockScreen", "Error compiling downloaded blocklists", e)
    }
    return false
}

private fun triggerRefresh(context: Context, scope: CoroutineScope) {
    val cr = context.contentResolver
    val isEnabled = Settings.Secure.getInt(cr, SETTING_KEY, 0) == 1
    if (isEnabled) {
        scope.launch(Dispatchers.IO) {
            Settings.Secure.putInt(cr, SETTING_KEY, 0)
            delay(200)
            Settings.Secure.putInt(cr, SETTING_KEY, 1)
            
            withContext(Dispatchers.Main) {
                HandlerThreadHelper.showToast(context, "System AdBlock lists re-compiled!")
            }
        }
    }
}

private object HandlerThreadHelper {
    fun showToast(context: Context, msg: String) {
        val handler = Handler(context.mainLooper)
        handler.post {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
