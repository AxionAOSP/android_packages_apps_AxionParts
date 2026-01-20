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

import android.content.Context

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private const val TAG = "GameSpoofing"
private const val CONFIG_PATH = "/data/adb/gameprops"
private const val CONFIG_FILE = "gameprops.json"

data class GameConfig(
    val packageName: String,
    val appName: String,
    val props: Map<String, String>
)

data class DeviceProfile(
    val name: String,
    val props: Map<String, String>,
    val isCustom: Boolean = false
)

private val PRESET_PROFILES = listOf(
    DeviceProfile("ROG Phone 8 Pro", mapOf("MODEL" to "ASUS_AI2401_A", "MANUFACTURER" to "asus")),
    DeviceProfile("Galaxy S24 Ultra", mapOf("MODEL" to "SM-S928B", "MANUFACTURER" to "samsung")),
    DeviceProfile("Xiaomi 13 Pro", mapOf("MODEL" to "2210132C", "MANUFACTURER" to "Xiaomi")),
    DeviceProfile("OnePlus 9 Pro", mapOf("MODEL" to "LE2101", "MANUFACTURER" to "OnePlus")),
    DeviceProfile("Black Shark 4", mapOf("MODEL" to "2SM-X706B", "MANUFACTURER" to "blackshark")),
    DeviceProfile("Lenovo Y700", mapOf("MODEL" to "Lenovo TB-9707F", "MANUFACTURER" to "Lenovo"))
)

private const val PRESETS_KEY = "game_spoofing_user_presets"

private fun loadCustomPresets(context: Context): List<DeviceProfile> {
    val jsonString = Settings.Secure.getString(context.contentResolver, PRESETS_KEY) ?: return emptyList()
    val profiles = mutableListOf<DeviceProfile>()
    try {
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val name = obj.getString("name")
            val propsObj = obj.getJSONObject("props")
            val props = mutableMapOf<String, String>()
            val keys = propsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                props[key] = propsObj.getString(key)
            }
            profiles.add(DeviceProfile(name, props, true))
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load custom presets", e)
    }
    return profiles
}

private fun saveCustomPreset(context: Context, profile: DeviceProfile) {
    val current = loadCustomPresets(context).toMutableList()
    current.add(profile)
    
    val jsonArray = JSONArray()
    current.forEach { p ->
        val obj = JSONObject()
        obj.put("name", p.name)
        val propsObj = JSONObject()
        p.props.forEach { (k, v) -> propsObj.put(k, v) }
        obj.put("props", propsObj)
        jsonArray.put(obj)
    }
    
    Settings.Secure.putString(context.contentResolver, PRESETS_KEY, jsonArray.toString())
}

private fun deleteCustomPreset(context: Context, profileName: String) {
    val current = loadCustomPresets(context).filter { it.name != profileName }
    
    val jsonArray = JSONArray()
    current.forEach { p ->
        val obj = JSONObject()
        obj.put("name", p.name)
        val propsObj = JSONObject()
        p.props.forEach { (k, v) -> propsObj.put(k, v) }
        obj.put("props", propsObj)
        jsonArray.put(obj)
    }
    
    Settings.Secure.putString(context.contentResolver, PRESETS_KEY, jsonArray.toString())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSpoofingScreen(
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
                            text = stringResource(R.string.app_spoofing),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        onBackClick?.let { onClick ->
                            IconButton(onClick = onClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
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
            GameSpoofingContent(
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        GameSpoofingContent(modifier = Modifier)
    }
}

@Composable
fun GameSpoofingContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(false) }
    var gameConfigs by remember { mutableStateOf(listOf<GameConfig>()) }
    var showAddGameDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingGame by remember { mutableStateOf<GameConfig?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }
    
    fun loadConfig() {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                loadGamePropsConfig(context)
            }
            enabled = result.first
            gameConfigs = result.second
        }
    }
    
    fun saveConfig() {
        scope.launch {
            withContext(Dispatchers.IO) {
                saveGamePropsConfig(enabled, gameConfigs)
            }
            Toast.makeText(context, context.getString(R.string.configuration_saved), Toast.LENGTH_SHORT).show()
        }
    }
    
    LaunchedEffect(Unit) {
        val configDir = File(CONFIG_PATH)
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
        loadConfig()
    }
    
    if (showAddGameDialog) {
        AddGameDialog(
            configuredGames = gameConfigs,
            onDismiss = { showAddGameDialog = false },
            onGameAdded = { newGame ->
                gameConfigs = gameConfigs + newGame
                saveConfig()
                showAddGameDialog = false
            }
        )
    }
    
    if (showEditDialog && editingGame != null) {
        EditGameDialog(
            game = editingGame!!,
            onDismiss = { 
                showEditDialog = false
                editingGame = null
            },
            onGameUpdated = { updatedGame ->
                gameConfigs = gameConfigs.map { 
                    if (it.packageName == updatedGame.packageName) updatedGame else it 
                }
                saveConfig()
                showEditDialog = false
                editingGame = null
            }
        )
    }
    
    if (showDeleteDialog && deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.remove_game_title)) },
            text = { Text(stringResource(R.string.remove_game_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        gameConfigs = gameConfigs.filter { it.packageName != deleteTarget }
                        saveConfig()
                        showDeleteDialog = false
                        deleteTarget = null
                    }
                ) {
                    Text(stringResource(R.string.remove), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    deleteTarget = null
                }) {
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
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = stringResource(R.string.app_spoofing),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (enabled) stringResource(R.string.apps_configured, gameConfigs.size) else stringResource(R.string.disabled),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Switch(
                    checked = enabled,
                    onCheckedChange = { 
                        enabled = it
                        saveConfig()
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddGameDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.add_app))
            }
            
            OutlinedButton(
                onClick = { loadConfig() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.reload))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (gameConfigs.isNotEmpty()) {
            Text(
                text = stringResource(R.string.configured_apps),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            
            gameConfigs.forEach { game ->
                GameConfigCard(
                    game = game,
                    onEdit = {
                        editingGame = game
                        showEditDialog = true
                    },
                    onDelete = {
                        deleteTarget = game.packageName
                        showDeleteDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Gamepad,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.no_apps_configured_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.add_apps_to_spoof),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun GameConfigCard(
    game: GameConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
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
                        text = game.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = game.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
    Icon(
        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
        contentDescription = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
            }
            
            if (expanded && game.props.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        game.props.forEach { (key, value) ->
                            ConfigValueRow(key, value)
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
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.remove))
                }
                
                FilledTonalButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.edit))
                }
            }
        }
    }
}

@Composable
fun AddGameDialog(
    configuredGames: List<GameConfig>,
    onDismiss: () -> Unit,
    onGameAdded: (GameConfig) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val scope = rememberCoroutineScope()
    var installedGames by remember { mutableStateOf(listOf<ApplicationInfo>()) }
    var selectedGame by remember { mutableStateOf<ApplicationInfo?>(null) }
    var selectedProfile by remember { mutableStateOf<DeviceProfile?>(null) }
    var showProfileSelector by remember { mutableStateOf(false) }
    
    var mergedProfiles by remember { mutableStateOf(PRESET_PROFILES) }
    var showCreatePresetDialog by remember { mutableStateOf(false) }

    fun refreshPresets() {
        scope.launch {
            val custom = withContext(Dispatchers.IO) { loadCustomPresets(context) }
            mergedProfiles = PRESET_PROFILES + custom
        }
    }

    LaunchedEffect(Unit) {
        val games = withContext(Dispatchers.IO) {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { 
                    val isSystem = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    !isSystem && configuredGames.none { config -> config.packageName == it.packageName }
                }
                .sortedBy { pm.getApplicationLabel(it).toString() }
        }
        installedGames = games
        refreshPresets()
    }

    if (showCreatePresetDialog) {
        CreatePresetDialog(
            onDismiss = { showCreatePresetDialog = false },
            onPresetCreated = { newProfile ->
                saveCustomPreset(context, newProfile)
                refreshPresets()
                showCreatePresetDialog = false
                selectedProfile = newProfile
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_app_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!showProfileSelector) {
                    Text(stringResource(R.string.select_an_app), style = MaterialTheme.typography.labelMedium)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        installedGames.forEach { app ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedGame = app
                                        showProfileSelector = true
                                    },
                                color = if (selectedGame == app) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    Color.Transparent
                            ) {
                                Text(
                                    text = pm.getApplicationLabel(app).toString(),
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.select_device_profile), style = MaterialTheme.typography.labelMedium)
                        TextButton(onClick = { showCreatePresetDialog = true }) {
                            Text(stringResource(R.string.create_new))
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        mergedProfiles.forEach { profile ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedProfile = profile },
                                color = if (selectedProfile == profile) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    Color.Transparent
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                        if (profile.isCustom) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                            Text(
                                                text = stringResource(R.string.custom),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                                IconButton(
                                                    onClick = {
                                                        deleteCustomPreset(context, profile.name)
                                                        refreshPresets()
                                                        if (selectedProfile == profile) selectedProfile = null
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = stringResource(R.string.delete_preset),
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (profile.props.containsKey("MANUFACTURER") && profile.props.containsKey("MODEL")) {
                                        Text(
                                            text = "${profile.props["MANUFACTURER"]} ${profile.props["MODEL"]}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedGame != null && selectedProfile != null) {
                        val props = mutableMapOf<String, String>()
                        props.putAll(selectedProfile!!.props)
                        
                        onGameAdded(
                            GameConfig(
                                packageName = selectedGame!!.packageName,
                                appName = pm.getApplicationLabel(selectedGame!!).toString(),
                                props = props
                            )
                        )
                    }
                },
                enabled = selectedGame != null && selectedProfile != null
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}


@Composable
fun PropertyEditor(
    props: MutableList<Pair<String, String>>,
    onAddProp: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        props.forEachIndexed { index, pair ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.property_number, index + 1),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { props.removeAt(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.remove),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = pair.first,
                        onValueChange = { newKey -> props[index] = newKey to pair.second },
                        label = { Text(stringResource(R.string.key)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = pair.second,
                        onValueChange = { newValue -> props[index] = pair.first to newValue },
                        label = { Text(stringResource(R.string.value)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
        
        FilledTonalButton(
            onClick = onAddProp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_property))
        }
    }
}

@Composable
fun CreatePresetDialog(
    onDismiss: () -> Unit,
    onPresetCreated: (DeviceProfile) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val editableProps = remember { mutableStateListOf<Pair<String, String>>().apply {
        add("MODEL" to "")
        add("MANUFACTURER" to "")
    } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_preset_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.preset_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                PropertyEditor(
                    props = editableProps,
                    onAddProp = { editableProps.add("" to "") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val props = mutableMapOf<String, String>()
                        editableProps.forEach { (k, v) -> if (k.isNotBlank()) props[k.trim()] = v.trim() }
                        onPresetCreated(DeviceProfile(name.trim(), props, true))
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun EditGameDialog(
    game: GameConfig,
    onDismiss: () -> Unit,
    onGameUpdated: (GameConfig) -> Unit
) {
    val propsList = remember { mutableListOf<Pair<String, String>>().apply {
        game.props.forEach { add(it.toPair()) }
        if (isEmpty()) {
            add("MODEL" to "")
            add("MANUFACTURER" to "")
        }
    } }

    val editableProps = remember { mutableStateListOf<Pair<String, String>>().apply { 
        addAll(propsList)
    } }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text(stringResource(R.string.save_as_preset_title)) },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text(stringResource(R.string.preset_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            val propsToSave = mutableMapOf<String, String>()
                            editableProps.forEach { (key, value) ->
                                if (key.isNotBlank()) propsToSave[key.trim()] = value.trim()
                            }
                            val newProfile = DeviceProfile(presetName.trim(), propsToSave, true)
                            saveCustomPreset(context, newProfile)
                            Toast.makeText(context, context.getString(R.string.preset_saved), Toast.LENGTH_SHORT).show()
                            showSavePresetDialog = false
                        }
                    },
                    enabled = presetName.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.edit_app, game.appName),
                textAlign = TextAlign.Center
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PropertyEditor(
                    props = editableProps,
                    onAddProp = { editableProps.add("" to "") }
                )
                
                OutlinedButton(
                    onClick = { showSavePresetDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_as_preset_title))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newProps = mutableMapOf<String, String>()
                    editableProps.forEach { (key, value) ->
                        if (key.isNotBlank()) { 
                            newProps[key.trim()] = value.trim()
                        }
                    }
                    onGameUpdated(game.copy(props = newProps))
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun loadGamePropsConfig(context: Context): Pair<Boolean, List<GameConfig>> {
    val configFile = File(CONFIG_PATH, CONFIG_FILE)
    if (!configFile.exists()) {
        return Pair(false, emptyList())
    }
    
    try {
        val json = JSONObject(configFile.readText())
        val enabled = json.optBoolean("enabled", false)
        val games = mutableListOf<GameConfig>()
        
        if (json.has("games")) {
            val gamesObj = json.getJSONObject("games")
            gamesObj.keys().forEach { packageName ->
                val gameProps = gamesObj.getJSONObject(packageName)
                val props = mutableMapOf<String, String>()
                gameProps.keys().forEach { key ->
                    props[key] = gameProps.getString(key)
                }
                val appName = try {
                    val pm = context.packageManager
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    packageName
                }
                games.add(GameConfig(packageName, appName, props))
            }
        }
        
        return Pair(enabled, games)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load config", e)
        return Pair(false, emptyList())
    }
}

private fun saveGamePropsConfig(enabled: Boolean, games: List<GameConfig>) {
    val configFile = File(CONFIG_PATH, CONFIG_FILE)
    
    try {
        val json = JSONObject()
        json.put("enabled", enabled)
        
        val gamesObj = JSONObject()
        games.forEach { game ->
            val gameProps = JSONObject()
            game.props.forEach { (key, value) ->
                gameProps.put(key, value)
            }
            gamesObj.put(game.packageName, gameProps)
        }
        json.put("games", gamesObj)
        
        configFile.writeText(json.toString(2))
        configFile.setReadable(true, false)
        
        Log.i(TAG, "Config saved successfully")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save config", e)
    }
}
