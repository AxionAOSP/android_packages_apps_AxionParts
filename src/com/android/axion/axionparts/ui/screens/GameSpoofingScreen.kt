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
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val model: String,
    val manufacturer: String,
    val device: String = "",
    val product: String = ""
)

private val PRESET_PROFILES = listOf(
    DeviceProfile("ROG Phone 8 Pro", "ASUS_AI2401_A", "asus", "AI2401", "WW_AI2401"),
    DeviceProfile("Galaxy S24 Ultra", "SM-S928B", "samsung", "e3q", "e3qxxx"),
    DeviceProfile("Xiaomi 13 Pro", "2210132C", "Xiaomi", "nuwa", "nuwa_global"),
    DeviceProfile("OnePlus 9 Pro", "LE2101", "OnePlus", "lemonadep", "OnePlus9Pro"),
    DeviceProfile("Black Shark 4", "2SM-X706B", "blackshark", "shark", "shark_global"),
    DeviceProfile("Lenovo Y700", "Lenovo TB-9707F", "Lenovo", "TB-9707F", "TB-9707F")
)

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
                            text = "Game Spoofing",
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
                loadGamePropsConfig()
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
            Toast.makeText(context, "Configuration saved", Toast.LENGTH_SHORT).show()
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
            title = { Text("Remove Game?") },
            text = { Text("This will remove the spoofing configuration for this game.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        gameConfigs = gameConfigs.filter { it.packageName != deleteTarget }
                        saveConfig()
                        showDeleteDialog = false
                        deleteTarget = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    deleteTarget = null
                }) {
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
                            text = "Game Spoofing",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (enabled) "${gameConfigs.size} games configured" else "Disabled",
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
                Text("Add Game")
            }
            
            OutlinedButton(
                onClick = { loadConfig() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reload")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (gameConfigs.isNotEmpty()) {
            Text(
                text = "CONFIGURED GAMES",
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
                        text = "No games configured",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add games to start spoofing device properties",
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
            containerColor = MaterialTheme.colorScheme.surface
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
                    contentDescription = if (expanded) "Collapse" else "Expand",
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
                    Text("Remove")
                }
                
                FilledTonalButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
fun AddGameDialog(
    onDismiss: () -> Unit,
    onGameAdded: (GameConfig) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    var installedGames by remember { mutableStateOf(listOf<ApplicationInfo>()) }
    var selectedGame by remember { mutableStateOf<ApplicationInfo?>(null) }
    var selectedProfile by remember { mutableStateOf<DeviceProfile?>(null) }
    var showProfileSelector by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        installedGames = withContext(Dispatchers.IO) {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .sortedBy { pm.getApplicationLabel(it).toString() }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Game") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!showProfileSelector) {
                    Text("Select a game:", style = MaterialTheme.typography.labelMedium)
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
                    Text("Select device profile:", style = MaterialTheme.typography.labelMedium)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PRESET_PROFILES.forEach { profile ->
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
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${profile.manufacturer} ${profile.model}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                        props["MODEL"] = selectedProfile!!.model
                        props["MANUFACTURER"] = selectedProfile!!.manufacturer
                        if (selectedProfile!!.device.isNotEmpty()) {
                            props["DEVICE"] = selectedProfile!!.device
                        }
                        if (selectedProfile!!.product.isNotEmpty()) {
                            props["PRODUCT"] = selectedProfile!!.product
                        }
                        
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
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditGameDialog(
    game: GameConfig,
    onDismiss: () -> Unit,
    onGameUpdated: (GameConfig) -> Unit
) {
    var model by remember { mutableStateOf(game.props["MODEL"] ?: "") }
    var manufacturer by remember { mutableStateOf(game.props["MANUFACTURER"] ?: "") }
    var device by remember { mutableStateOf(game.props["DEVICE"] ?: "") }
    var product by remember { mutableStateOf(game.props["PRODUCT"] ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${game.appName}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("MODEL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = { Text("MANUFACTURER") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = device,
                    onValueChange = { device = it },
                    label = { Text("DEVICE (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("PRODUCT (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val props = mutableMapOf<String, String>()
                    if (model.isNotEmpty()) props["MODEL"] = model
                    if (manufacturer.isNotEmpty()) props["MANUFACTURER"] = manufacturer
                    if (device.isNotEmpty()) props["DEVICE"] = device
                    if (product.isNotEmpty()) props["PRODUCT"] = product
                    
                    onGameUpdated(game.copy(props = props))
                },
                enabled = model.isNotEmpty() && manufacturer.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun loadGamePropsConfig(): Pair<Boolean, List<GameConfig>> {
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
                games.add(GameConfig(packageName, packageName, props))
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
