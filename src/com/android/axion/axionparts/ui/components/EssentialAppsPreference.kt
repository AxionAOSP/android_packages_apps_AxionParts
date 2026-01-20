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

package com.android.axion.axionparts.ui.components

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.android.axion.axionparts.R
import com.android.axion.compose.preferences.*
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.axionparts.ui.theme.ExpressiveShapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val ESSENTIAL_APP_LIST_KEY = "essential_app_list"

data class SelectedAppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

fun getEssentialApps(contentResolver: android.content.ContentResolver): Set<String> {
    val savedList = Settings.Secure.getString(contentResolver, ESSENTIAL_APP_LIST_KEY) ?: ""
    return if (savedList.isEmpty()) {
        emptySet()
    } else {
        savedList.split(",").filter { it.isNotEmpty() }.toSet()
    }
}

fun saveEssentialApps(contentResolver: android.content.ContentResolver, packages: Set<String>) {
    val value = packages.joinToString(",")
    Settings.Secure.putString(contentResolver, ESSENTIAL_APP_LIST_KEY, value)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EssentialAppsPreference(
    modifier: Modifier = Modifier,
    onNavigateToAppPicker: (selectedApps: Set<String>) -> Unit,
    position: PreferencePosition = PreferencePosition.Single
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val packageManager = context.packageManager
    val shape = preferenceShape(position)
    
    var selectedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedAppsInfo by remember { mutableStateOf<List<SelectedAppInfo>>(emptyList()) }
    
    fun loadSelectedApps() {
        selectedPackages = getEssentialApps(contentResolver)
    }
    
    LaunchedEffect(Unit) {
        loadSelectedApps()
    }
    
    DisposableEffect(ESSENTIAL_APP_LIST_KEY) {
        val observer = object : android.database.ContentObserver(
            android.os.Handler(android.os.Looper.getMainLooper())
        ) {
            override fun onChange(selfChange: Boolean) {
                loadSelectedApps()
            }
        }
        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(ESSENTIAL_APP_LIST_KEY),
            false,
            observer
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }
    
    LaunchedEffect(selectedPackages) {
        withContext(Dispatchers.IO) {
            selectedAppsInfo = selectedPackages.mapNotNull { packageName ->
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    SelectedAppInfo(
                        packageName = packageName,
                        label = appInfo.loadLabel(packageManager).toString(),
                        icon = appInfo.loadIcon(packageManager)
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
            }.sortedBy { it.label.lowercase() }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceBright)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.selected_apps),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = stringResource(R.string.selected_apps_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AnimatedVisibility(
            visible = selectedAppsInfo.isNotEmpty(),
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedAppsInfo.forEach { app ->
                    AppChip(
                        app = app,
                        onRemove = {
                            val newPackages = selectedPackages - app.packageName
                            saveEssentialApps(contentResolver, newPackages)
                            selectedPackages = newPackages
                        }
                    )
                }
            }
        }
        
        if (selectedAppsInfo.isEmpty()) {
            Text(
                text = stringResource(R.string.no_apps_selected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AddAppButton(onClick = { onNavigateToAppPicker(selectedPackages) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppChip(
    app: SelectedAppInfo,
    onRemove: () -> Unit
) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(app.label) },
        leadingIcon = {
            app.icon?.let { icon ->
                Image(
                    bitmap = icon.toBitmap(24, 24).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                )
            }
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.remove),
                modifier = Modifier.size(18.dp)
            )
        },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = InputChipDefaults.inputChipBorder(
            enabled = true,
            selected = true,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}

@Composable
private fun AddAppButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    Row(
        modifier = Modifier
            .scale(scale)
            .clip(ExpressiveShapes.large)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.add_apps),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
