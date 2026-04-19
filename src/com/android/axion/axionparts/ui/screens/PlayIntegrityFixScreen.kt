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
import android.app.ActivityManager
import android.content.Context
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Upload
import com.android.axion.compose.preferences.ExpressiveSwitch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
private const val PIF_CONFIG_NAME = "pif.json"
private const val GOOGLE_URL = "https://developer.android.com"
private const val FLASH_URL = "https://flash.android.com"
private const val FLASH_API = "https://content-flashstation-pa.googleapis.com/v1/builds"
private const val PIXEL_BULLETIN_URL = "https://source.android.com/docs/security/bulletin/pixel"
private const val VENDING_PACKAGE = "com.android.vending"

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
            title = "Play Integrity Fix",
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
            val json = JSONObject(content)
            val result = mutableMapOf<String, String>()
            json.keys().forEach { key -> result[key] = json.optString(key, "") }
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

    fun updateConfig(key: String, value: Any) {
        try {
            val existing = Settings.Secure.getString(context.contentResolver, PIF_CONFIG_KEY)
            val json = try { JSONObject(existing ?: "") } catch (e: Exception) { JSONObject() }
            json.put(key, value)
            Settings.Secure.putString(context.contentResolver, PIF_CONFIG_KEY, json.toString(2))
            refreshStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update config", e)
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
                        Settings.Secure.putString(
                            context.contentResolver,
                            PIF_CONFIG_KEY,
                            result.pifData.toString(2)
                        )

                        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        am.forceStopPackage(VENDING_PACKAGE)

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

                    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    am.forceStopPackage(VENDING_PACKAGE)

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
        var activeConfigExpanded by remember { mutableStateOf(false) }
        
        if (activeConfigFile != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Config",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                            color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.5f)
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
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                        
                        FilledTonalButton(
                            onClick = {
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

        val isSpoofPhotos = activeConfigFile?.data?.get("spoofPhotos")?.let {
            it == "true" || it == "1"
        } ?: false

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceBright
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
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
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Spoof Google Photos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Unlimited original quality backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ExpressiveSwitch(
                    checked = isSpoofPhotos,
                    onCheckedChange = { checked ->
                        updateConfig("spoofPhotos", checked.toString())
                    }
                )
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
    if (trimmed.isEmpty()) return "{}"
    if (trimmed.startsWith("{")) return trimmed
    val json = JSONObject()
    trimmed.lines().forEach { line ->
        val stripped = line.trim()
        if (stripped.isEmpty() || stripped.startsWith("#") || stripped.startsWith("//")) return@forEach
        val eq = stripped.indexOf('=')
        if (eq > 0) {
            val key = stripped.substring(0, eq).trim()
            val value = stripped.substring(eq + 1).trim().substringBefore('#').trim()
            if (key.isNotEmpty()) json.put(key, value)
        }
    }
    return json.toString(2)
}

private fun fetchPifFromGoogle(): PifFetchResult {
    return try {
        Log.d(TAG, "Fetching Pixel Canary metadata...")

        val versionsHtml = URL("$GOOGLE_URL/about/versions").readText(StandardCharsets.UTF_8)
        val latestVersion = Regex("""https://developer\.android\.com/about/versions/(\d+)""")
            .findAll(versionsHtml)
            .map { it.groupValues[1].toInt() }
            .toSortedSet()
            .maxOrNull()
            ?: return PifFetchResult.Error("No Android version pages found")

        val latestHtml = URL("$GOOGLE_URL/about/versions/$latestVersion").readText(StandardCharsets.UTF_8)
        val qprPath = Regex("""href="(/about/versions/$latestVersion/qpr(\d+)/download-ota)"""")
            .findAll(latestHtml)
            .map { it.groupValues[2].toInt() to it.groupValues[1] }
            .maxByOrNull { it.first }
            ?.second
            ?: return PifFetchResult.Error("No QPR download page found")

        val fiHtml = URL("$GOOGLE_URL$qprPath").readText(StandardCharsets.UTF_8)
        val rowPattern = Regex(
            """<tr id="([^"]+)">\s*<td[^>]*>([^<]+)</td>""",
            RegexOption.DOT_MATCHES_ALL
        )
        val devices = rowPattern.findAll(fiHtml)
            .map { Triple(it.groupValues[2].trim(), "${it.groupValues[1]}_beta", it.groupValues[1]) }
            .toList()

        if (devices.isEmpty()) return PifFetchResult.Error("No beta devices found")

        val (model, product, device) = devices[Random.nextInt(devices.size)]
        Log.d(TAG, "Selected device: $model ($product)")

        val flashHtml = URL(FLASH_URL).readText(StandardCharsets.UTF_8)
        val apiKey = Regex("""AIza[0-9A-Za-z_-]{35}""").find(flashHtml)?.value
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

        val pifJson = JSONObject().apply {
            put("MANUFACTURER", "Google")
            put("MODEL", model)
            put("PRODUCT", product)
            put("DEVICE", device)
            put("FINGERPRINT", fingerprint)
            put("SECURITY_PATCH", securityPatch)
            put("DEVICE_INITIAL_SDK_INT", "32")
        }

        PifFetchResult.Success(model, pifJson)
    } catch (e: Exception) {
        Log.e(TAG, "Canary fetch failed", e)
        PifFetchResult.Error("Failed to fetch canary PIF: ${e.message}")
    }
}
