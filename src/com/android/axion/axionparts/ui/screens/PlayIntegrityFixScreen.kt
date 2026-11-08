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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.axion.compose.preferences.ClickablePreference
import com.android.axion.compose.preferences.ListPreference
import com.android.axion.compose.preferences.LocalPreferencePosition
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.PreferencePosition
import com.android.axion.compose.preferences.SecureSettingSwitch
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.preferences.preferenceShape
import com.android.axion.axionparts.R
import com.android.axion.compose.scaffold.AxionScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.provider.Settings
import org.json.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.random.Random
import com.android.axion.axionparts.ui.theme.MaxContentWidth

private const val TAG = "PlayIntegrityFix"
private const val PIF_CONFIG_KEY = "spoof_pif_config"
private const val PIF_PHOTOS_KEY = "spoof_pif_photos"
private const val PIF_CONFIG_NAME = "pif.json"
private const val GOOGLE_URL = "https://developer.android.com"
private const val FLASH_URL = "https://flash.android.com"
private const val FLASH_API = "https://content-flashstation-pa.googleapis.com/v1/builds"
private const val PIXEL_BULLETIN_URL = "https://source.android.com/docs/security/bulletin/pixel"
private const val KEY_VENDING_BUILD = "spoofVendingBuild"
private const val KEY_VERBOSE_LOGS = "verboseLogs"
private const val DEFAULT_VERBOSE_LOGS = "0"

private val VERSIONS_REGEX =
    Regex("""(?:https://developer\.android\.com)?/about/versions/(\d+)""")
private val DEVICE_ROW_REGEX = Regex(
    """<tr id="([^"]+)">\s*<td[^>]*>([^<]+)</td>""",
    RegexOption.DOT_MATCHES_ALL
)
private val API_KEY_REGEX = Regex("""AIza[0-9A-Za-z_-]{35}""")

private val PRESERVED_KEYS = listOf(
    "spoofBuild", "spoofProps", "spoofProvider", "spoofSignature",
    KEY_VENDING_BUILD, "spoofVendingSdk", "DEBUG", KEY_VERBOSE_LOGS
)

private val DEFAULT_SPOOF_FLAGS = linkedMapOf(
    "spoofBuild" to "true",
    "spoofProps" to "true",
    "spoofProvider" to "false",
    "spoofSignature" to "false",
    KEY_VENDING_BUILD to "true",
    "spoofVendingSdk" to "false",
    "DEBUG" to "false"
)

private fun JSONObject.putDefaultSpoofFlags(): JSONObject = apply {
    DEFAULT_SPOOF_FLAGS.forEach { (key, value) -> if (!has(key)) put(key, value) }
}

private fun isTrue(value: String) = value == "1" || value.equals("true", ignoreCase = true)

data class ConfigFileState(
    val fileName: String,
    val exists: Boolean,
    val isActive: Boolean,
    val data: Map<String, String> = emptyMap()
)

@Composable
fun PlayIntegrityFixScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true
) {
    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.play_integrity_fix),
            onBackClick = { onBackClick?.invoke() },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                PlayIntegrityFixContent(
                    modifier = Modifier.widthIn(max = MaxContentWidth)
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            PlayIntegrityFixContent(
                modifier = Modifier.widthIn(max = MaxContentWidth)
            )
        }
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

    fun readCurrentConfig(): Map<String, String> {
        val content = Settings.Secure.getString(context.contentResolver, PIF_CONFIG_KEY)
            ?: return emptyMap()
        return try {
            val trimmed = content.trim()
            if (trimmed.isEmpty()) return emptyMap()
            val result = mutableMapOf<String, String>()
            if (trimmed.startsWith("{")) {
                val json = JSONObject(trimmed)
                json.keys().forEach { key -> result[key] = json.optString(key, "") }
            } else {
                trimmed.lines().forEach { line ->
                    val l = line.trim()
                    if (l.isNotEmpty() && !l.startsWith("#") && !l.startsWith("//")) {
                        val eq = l.indexOf('=')
                        if (eq > 0) {
                            result[l.substring(0, eq).trim()] =
                                l.substring(eq + 1).substringBefore('#').trim()
                        }
                    }
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse config: ${e.message}")
            emptyMap()
        }
    }

    fun refreshStatus() {
        val data = readCurrentConfig()
        val exists = data.isNotEmpty()
        configFiles = listOf(ConfigFileState(PIF_CONFIG_NAME, exists, exists, data))
        activeConfig = if (exists) PIF_CONFIG_NAME else ""
    }

    LaunchedEffect(Unit) {
        refreshStatus()
    }

    fun updateConfigFlag(key: String, value: String) {
        try {
            val existing = Settings.Secure.getString(context.contentResolver, PIF_CONFIG_KEY)
            if (existing.isNullOrBlank()) {
                Toast.makeText(context, context.getString(R.string.no_config_loaded), Toast.LENGTH_SHORT).show()
                return
            }
            val json = JSONObject(normalizePifPayload(existing))
            json.put(key, value)
            Settings.Secure.putString(context.contentResolver, PIF_CONFIG_KEY, json.toString(2))
            refreshStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update $key", e)
            Toast.makeText(context, context.getString(R.string.failed_to_update, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }
    
    fun fetchPixelBetaPif() {
        scope.launch {
            isFetching = true
            fetchStatus = context.getString(R.string.fetching)
            
            try {
                val result = withContext(Dispatchers.IO) {
                    fetchPifFromGoogle()
                }
                
                when (result) {
                    is PifFetchResult.Success -> {
                        val existing = readCurrentConfig()
                        PRESERVED_KEYS.forEach { key ->
                            existing[key]?.let { value -> result.pifData.put(key, value) }
                        }
                        result.pifData.putDefaultSpoofFlags()
                        Settings.Secure.putString(
                            context.contentResolver,
                            PIF_CONFIG_KEY,
                            result.pifData.toString(2)
                        )
                        Toast.makeText(context, context.getString(R.string.fetched_model, result.model), Toast.LENGTH_SHORT).show()
                        fetchStatus = ""
                        refreshStatus()
                    }
                    is PifFetchResult.Error -> {
                        Toast.makeText(context, context.getString(R.string.failed_message, result.message), Toast.LENGTH_LONG).show()
                        fetchStatus = ""
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch PIF config: ${e.message}")
                Toast.makeText(context, context.getString(R.string.failed_message, e.message ?: ""), Toast.LENGTH_LONG).show()
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
                    val raw = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes().toString(StandardCharsets.UTF_8)
                    } ?: ""

                    val normalized = normalizePifPayload(raw)
                    Settings.Secure.putString(context.contentResolver, PIF_CONFIG_KEY, normalized)
                    Toast.makeText(context, context.getString(R.string.imported_as, PIF_CONFIG_NAME), Toast.LENGTH_SHORT).show()
                    refreshStatus()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import PIF config: ${e.message}")
                    Toast.makeText(context, context.getString(R.string.failed_message, e.message ?: ""), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(context.getString(R.string.delete_file_title, deleteTargetFile)) },
            text = { Text(context.getString(R.string.delete_config_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            Settings.Secure.putString(context.contentResolver, PIF_CONFIG_KEY, null)
                            Toast.makeText(context, context.getString(R.string.deleted_file, deleteTargetFile), Toast.LENGTH_SHORT).show()
                            refreshStatus()
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.failed_message, e.message ?: ""), Toast.LENGTH_LONG).show()
                        }
                        showDeleteDialog = false
                        deleteTargetFile = ""
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; deleteTargetFile = "" }) {
                    Text(stringResource(R.string.cancel))
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
                containerColor = MaterialTheme.colorScheme.surfaceBright
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
                            text = stringResource(R.string.fingerprint_spoofing),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (activeConfig.isNotEmpty()) {
                                stringResource(R.string.active_config, activeConfig)
                            } else {
                                stringResource(R.string.no_config_loaded)
                            },
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
                        Text(fetchStatus.ifEmpty { stringResource(R.string.fetching) })
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.fetch_pixel_beta))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        val activeConfigFile = configFiles.find { it.isActive }

        SecureSettingSwitch(
            settingKey = PIF_PHOTOS_KEY,
            title = stringResource(R.string.spoof_google_photos),
            summary = stringResource(R.string.unlimited_backup),
            icon = Icons.Default.Image,
            defaultValue = true,
            position = PreferencePosition.Single,
        )

        if (activeConfigFile != null) {
            fun flagValue(key: String): Boolean =
                isTrue(activeConfigFile.data[key] ?: DEFAULT_SPOOF_FLAGS[key] ?: "false")

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceGroup(
                title = stringResource(R.string.pif_flags_title),
                collapsible = true,
                initiallyExpanded = false,
            ) {
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pif_flag_spoof_build_title),
                        summary = stringResource(R.string.pif_flag_spoof_build_summary),
                        checked = flagValue("spoofBuild"),
                        onCheckedChange = { updateConfigFlag("spoofBuild", it.toString()) },
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pif_flag_spoof_props_title),
                        summary = stringResource(R.string.pif_flag_spoof_props_summary),
                        checked = flagValue("spoofProps"),
                        onCheckedChange = { updateConfigFlag("spoofProps", it.toString()) },
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pif_flag_spoof_provider_title),
                        summary = stringResource(R.string.pif_flag_spoof_provider_summary),
                        checked = flagValue("spoofProvider"),
                        onCheckedChange = { updateConfigFlag("spoofProvider", it.toString()) },
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pif_flag_spoof_signature_title),
                        summary = stringResource(R.string.pif_flag_spoof_signature_summary),
                        checked = flagValue("spoofSignature"),
                        onCheckedChange = { updateConfigFlag("spoofSignature", it.toString()) },
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pif_flag_spoof_vending_build_title),
                        summary = stringResource(R.string.pif_flag_spoof_vending_build_summary),
                        checked = flagValue(KEY_VENDING_BUILD),
                        onCheckedChange = { updateConfigFlag(KEY_VENDING_BUILD, it.toString()) },
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pif_flag_spoof_vending_sdk_title),
                        summary = stringResource(R.string.pif_flag_spoof_vending_sdk_summary),
                        checked = flagValue("spoofVendingSdk"),
                        enabled = !flagValue(KEY_VENDING_BUILD),
                        onCheckedChange = { updateConfigFlag("spoofVendingSdk", it.toString()) },
                    )
                }
                item {
                    SwitchPreference(
                        title = stringResource(R.string.pif_flag_debug_title),
                        summary = stringResource(R.string.pif_flag_debug_summary),
                        checked = flagValue("DEBUG"),
                        onCheckedChange = { updateConfigFlag("DEBUG", it.toString()) },
                    )
                }
                item {
                    val verboseOptions = listOf(
                        "0" to stringResource(R.string.pif_verbose_logs_off),
                        "1" to stringResource(R.string.pif_verbose_logs_basic),
                        "2" to stringResource(R.string.pif_verbose_logs_detailed),
                        "3" to stringResource(R.string.pif_verbose_logs_full),
                    )
                    val storedVerbose = activeConfigFile.data[KEY_VERBOSE_LOGS] ?: DEFAULT_VERBOSE_LOGS
                    val verboseVal = if (verboseOptions.any { it.first == storedVerbose }) storedVerbose else DEFAULT_VERBOSE_LOGS
                    ListPreference(
                        title = stringResource(R.string.pif_flag_verbose_logs_title),
                        options = verboseOptions,
                        value = verboseVal,
                        onValueChange = { updateConfigFlag(KEY_VERBOSE_LOGS, it) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceGroup(
                title = stringResource(R.string.active_config_title),
                collapsible = true,
                initiallyExpanded = false,
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = preferenceShape(LocalPreferencePosition.current),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeConfigFile.fileName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                activeConfigFile.data["MODEL"]?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                if (activeConfigFile.data.isNotEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = preferenceShape(LocalPreferencePosition.current),
                            color = MaterialTheme.colorScheme.surfaceBright,
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                val displayKeys = listOf(
                                    "MODEL", "SECURITY_PATCH", "FINGERPRINT",
                                    "MANUFACTURER", "BRAND", "PRODUCT", "DEVICE", "DEVICE_INITIAL_SDK_INT",
                                )
                                displayKeys.forEach { key ->
                                    activeConfigFile.data[key]?.let { ConfigValueRow(key, it) }
                                }

                                val settingKeys = activeConfigFile.data.keys.filter {
                                    it.startsWith("spoof") || it == "DEBUG" || it == "verboseLogs"
                                }
                                if (settingKeys.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    settingKeys.forEach { key ->
                                        activeConfigFile.data[key]?.let { ConfigValueRow(key, it) }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    ClickablePreference(
                        title = stringResource(R.string.replace),
                        summary = stringResource(R.string.import_text_verb),
                        icon = Icons.Default.Upload,
                        onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }
                            pifPicker.launch(intent)
                        },
                    )
                }

                item {
                    ClickablePreference(
                        title = stringResource(R.string.delete),
                        summary = activeConfigFile.fileName,
                        icon = Icons.Default.Delete,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = {
                            deleteTargetFile = activeConfigFile.fileName
                            showDeleteDialog = true
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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

private fun normalizePifPayload(raw: String): String {
    val trimmed = raw.trim()
    require(trimmed.isNotEmpty()) { "Config file is empty" }

    val json = if (trimmed.startsWith("{")) {
        JSONObject(trimmed)
    } else {
        JSONObject().apply {
            trimmed.lines().forEach { line ->
                val stripped = line.trim()
                if (stripped.isEmpty() || stripped.startsWith("#") || stripped.startsWith("//")) return@forEach
                val eq = stripped.indexOf('=')
                if (eq > 0) {
                    val key = stripped.substring(0, eq).trim()
                    val value = stripped.substring(eq + 1).trim().substringBefore('#').trim()
                    if (key.isNotEmpty()) put(key, value)
                }
            }
        }
    }

    require(json.length() > 0) { "Config file has no valid entries" }
    return json.toString(2)
}

private fun parseDeviceTable(html: String): List<Triple<String, String, String>> {
    val devices = mutableListOf<Triple<String, String, String>>()
    val seen = mutableSetOf<String>()
    DEVICE_ROW_REGEX.findAll(html).forEach { match ->
        val device = match.groupValues[1]
        if (!seen.add(device)) return@forEach
        val model = match.groupValues[2].trim().ifEmpty { device }
        devices.add(Triple(model, "${device}_beta", device))
    }
    return devices
}

private fun extractFlashApiKey(html: String): String? {
    val clientConfig = Regex("""data-client-config="([^"]*)"""").find(html)?.groupValues?.get(1)
    return clientConfig?.let { API_KEY_REGEX.find(it)?.value }
        ?: API_KEY_REGEX.find(html)?.value
}

private fun fetchPifFromGoogle(): PifFetchResult {
    return try {
        Log.d(TAG, "Fetching Pixel Canary metadata...")

        val versionsHtml = URL("$GOOGLE_URL/about/versions").readText(StandardCharsets.UTF_8)
        val knownVersions = VERSIONS_REGEX.findAll(versionsHtml)
            .map { it.groupValues[1].toInt() }
            .toSet()
            .sortedDescending()
        val maxVersion = knownVersions.firstOrNull()
            ?: return PifFetchResult.Error("No Android version pages found")
        val versions = listOf(maxVersion + 1) + knownVersions

        var devices: List<Triple<String, String, String>> = emptyList()
        for (version in versions) {
            try {
                val latestHtml = URL("$GOOGLE_URL/about/versions/$version").readText(StandardCharsets.UTF_8)
                val pages = Regex(
                    """href="(/about/versions/$version(?:/qpr(\d+))?/download(?:-ota)?)""""
                ).findAll(latestHtml)
                    .map { it.groupValues[1] to (it.groupValues[2].toIntOrNull() ?: 0) }
                    .toList()
                if (pages.isEmpty()) continue

                val newestQpr = pages.maxOf { it.second }
                val found = pages.filter { it.second == newestQpr }
                    .map { it.first }
                    .distinct()
                    .mapNotNull { path ->
                        try {
                            parseDeviceTable(URL("$GOOGLE_URL$path").readText(StandardCharsets.UTF_8))
                        } catch (e: Exception) {
                            Log.d(TAG, "Failed to read $path: ${e.message}")
                            null
                        }
                    }
                    .maxByOrNull { it.size }
                    .orEmpty()
                if (found.isNotEmpty()) {
                    devices = found
                    break
                }
            } catch (_: Exception) {
                continue
            }
        }

        if (devices.isEmpty()) return PifFetchResult.Error("No beta devices found")

        val (model, product, device) = devices[Random.nextInt(devices.size)]
        Log.d(TAG, "Selected device: $model ($product)")

        val flashHtml = URL(FLASH_URL).readText(StandardCharsets.UTF_8)
        val apiKey = extractFlashApiKey(flashHtml)
            ?: return PifFetchResult.Error("Failed to extract Flash Tool API key")

        val buildsUrl = "$FLASH_API?product=$product&key=$apiKey"
        val buildsConn = URL(buildsUrl).openConnection().apply {
            setRequestProperty("Referer", FLASH_URL)
            setRequestProperty("X-Goog-Api-Key", apiKey)
            connectTimeout = 15000
            readTimeout = 15000
        }
        val buildsJson = buildsConn.getInputStream().use { it.readBytes().toString(StandardCharsets.UTF_8) }

        val root = JSONObject(buildsJson)
        val buildsArray = root.optJSONArray("flashstationBuild")
            ?: return PifFetchResult.Error("No flashstationBuild array in Flash Tool response")

        var id: String? = null
        var incremental: String? = null
        var androidVersion = ""
        var canaryId: String? = null

        for (i in buildsArray.length() - 1 downTo 0) {
            val b = buildsArray.optJSONObject(i) ?: continue
            val meta = b.optJSONObject("previewMetadata") ?: continue
            if (!meta.optBoolean("canary")) continue

            val rc = b.optString("releaseCandidateName")
            val bid = b.optString("buildId")
            if (rc.isEmpty() || bid.isEmpty()) continue

            id = rc
            incremental = bid
            androidVersion = meta.optString("releaseTrackVersionName")
            canaryId = meta.optString("id").takeIf { it.contains("canary-") }
            break
        }

        if (id == null || incremental == null) {
            return PifFetchResult.Error("No canary build found for $product")
        }

        val fingerprint = "google/$product/$device:CANARY/$id/$incremental:user/release-keys"
        Log.d(TAG, "Fingerprint: $fingerprint (Android $androidVersion)")

        val canaryMonth = canaryId?.let {
            Regex("""canary-(\d{4})(\d{2})""").find(it)?.let { m ->
                "${m.groupValues[1]}-${m.groupValues[2]}"
            }
        } ?: return PifFetchResult.Error("Failed to derive canary month id")

        val securityPatch = try {
            val bulletinHtml = URL(PIXEL_BULLETIN_URL).readText(StandardCharsets.UTF_8)
            Regex("""<td>($canaryMonth-\d{2})</td>""").find(bulletinHtml)?.groupValues?.get(1)
                ?: "$canaryMonth-05"
        } catch (e: Exception) {
            Log.d(TAG, "Bulletin fetch failed, using estimated patch: ${e.message}")
            "$canaryMonth-05"
        }

        Log.d(TAG, "Security Patch: $securityPatch")

        val initialSdkInt = when (device) {
            "oriole", "raven" -> "31"
            "bluejay" -> "32"
            "panther", "cheetah", "lynx", "felix", "tangorpro" -> "33"
            "shiba", "husky", "akita", "tokay", "caiman", "komodo", "comet" -> "34"
            "tegu" -> "35"
            "stallion", "frankel", "blazer", "mustang", "rango" -> "36"
            "cubs", "grizzly", "kodiak", "yogi" -> "37"
            else -> "37"
        }

        val pifJson = JSONObject().apply {
            put("MANUFACTURER", "Google")
            put("MODEL", model)
            put("PRODUCT", product)
            put("DEVICE", device)
            put("FINGERPRINT", fingerprint)
            put("SECURITY_PATCH", securityPatch)
            put("DEVICE_INITIAL_SDK_INT", initialSdkInt)
        }

        PifFetchResult.Success(model, pifJson)
    } catch (e: Exception) {
        Log.e(TAG, "Canary fetch failed", e)
        PifFetchResult.Error("Failed to fetch canary PIF: ${e.message}")
    }
}
