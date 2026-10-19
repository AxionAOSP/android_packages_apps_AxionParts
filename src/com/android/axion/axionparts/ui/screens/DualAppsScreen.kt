package com.android.axion.axionparts.ui.screens

import android.R as AndroidR
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.axionparts.R
import com.android.axion.compose.applist.AppFilter
import com.android.axion.compose.applist.rememberAppList
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.preferences.SwitchPreference
import com.android.axion.compose.scaffold.AxionScaffold
import com.android.internal.dualapps.AxDualAppsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DualAppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val uid: Int,
    val isCloned: Boolean,
)

@Composable
fun DualAppsScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true,
) {
    val manager = remember { AxDualAppsManager.getInstance() }
    val showDeleteSpaceDialogState = remember { mutableStateOf(false) }
    val dualSpaceExistsState = remember { mutableStateOf(manager.isDualSpaceExists) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val exists = manager.isDualSpaceExists
            withContext(Dispatchers.Main) {
                dualSpaceExistsState.value = exists
            }
        }
    }

    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.dual_apps),
            onBackClick = { onBackClick?.invoke() },
            actions = {
                if (dualSpaceExistsState.value) {
                    val icons = Icons.Default
                    IconButton(onClick = { showDeleteSpaceDialogState.value = true }) {
                        Icon(
                            imageVector = icons.Delete,
                            contentDescription = stringResource(R.string.dual_apps_clear_all),
                        )
                    }
                }
            },
        ) { innerPadding ->
            DualAppsContent(
                modifier = Modifier.padding(innerPadding),
                showDeleteSpaceDialogState = showDeleteSpaceDialogState,
                onDualSpaceExistsChanged = { dualSpaceExistsState.value = it },
            )
        }
    } else {
        DualAppsContent(
            showDeleteSpaceDialogState = showDeleteSpaceDialogState,
            onDualSpaceExistsChanged = { dualSpaceExistsState.value = it },
        )
    }
}

@Composable
private fun DualAppsContent(
    modifier: Modifier = Modifier,
    showDeleteSpaceDialogState: MutableState<Boolean>,
    onDualSpaceExistsChanged: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val manager = remember { AxDualAppsManager.getInstance() }

    val userAppsState = rememberAppList(AppFilter.USER_ONLY, AppFilter.LAUNCHABLE_ONLY, AppFilter.NO_OVERLAYS)
    val userApps = userAppsState.value

    val isLoadingState = remember { mutableStateOf(true) }
    val searchQueryState = remember { mutableStateOf("") }
    val recommendedListState = remember { mutableStateOf<List<DualAppEntry>>(emptyList()) }
    val availableListState = remember { mutableStateOf<List<DualAppEntry>>(emptyList()) }
    val pendingDeleteAppState = remember { mutableStateOf<DualAppEntry?>(null) }

    val busyPackagesState = remember { mutableStateOf<Set<String>>(emptySet()) }
    val enablingPackagesState = remember { mutableStateOf<Set<String>>(emptySet()) }
    val isPreparingSpaceState = remember { mutableStateOf(false) }
    val isClearingSpaceState = remember { mutableStateOf(false) }

    val isLoading = isLoadingState.value
    val searchQuery = searchQueryState.value
    val recommendedList = recommendedListState.value
    val availableList = availableListState.value
    val pendingDeleteApp = pendingDeleteAppState.value
    val busyPackages = busyPackagesState.value
    val enablingPackages = enablingPackagesState.value
    val isPreparingSpace = isPreparingSpaceState.value
    val isClearingSpace = isClearingSpaceState.value
    val controlsLocked = isPreparingSpace || isClearingSpace

    fun refreshAppLists() {
        scope.launch(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val allowedSet = manager.queryAllowPackages().toSet()
            val spaceExists = manager.isDualSpaceExists

            val rawRecommended = manager.queryRecommendedAppList(intent, 0)
            val rawAvailable = manager.queryAvailableAppList(intent, 0)
            val userAppMap = userApps.associateBy { it.packageName }

            val mappedRecommended = rawRecommended.mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val pkg = activityInfo.packageName
                val userApp = userAppMap[pkg] ?: return@mapNotNull null
                val uid = activityInfo.applicationInfo?.uid ?: 0
                DualAppEntry(pkg, userApp.label, userApp.icon, uid, allowedSet.contains(pkg))
            }.distinctBy { it.packageName }

            val mappedAvailable = rawAvailable.mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val pkg = activityInfo.packageName
                val userApp = userAppMap[pkg] ?: return@mapNotNull null
                val uid = activityInfo.applicationInfo?.uid ?: 0
                DualAppEntry(pkg, userApp.label, userApp.icon, uid, allowedSet.contains(pkg))
            }.distinctBy { it.packageName }

            withContext(Dispatchers.Main) {
                recommendedListState.value = mappedRecommended
                availableListState.value = mappedAvailable
                isLoadingState.value = false
                onDualSpaceExistsChanged(spaceExists)
            }
        }
    }

    fun handleEnable(app: DualAppEntry) {
        if (busyPackagesState.value.contains(app.packageName)) return
        if (isPreparingSpaceState.value || isClearingSpaceState.value) return
        busyPackagesState.value = busyPackagesState.value + app.packageName
        enablingPackagesState.value = enablingPackagesState.value + app.packageName

        if (manager.isDualSpaceInitialized) {
            scope.launch(Dispatchers.IO) {
                manager.installApp(app.packageName, app.uid)
                withContext(Dispatchers.Main) {
                    busyPackagesState.value = busyPackagesState.value - app.packageName
                    enablingPackagesState.value = enablingPackagesState.value - app.packageName
                    refreshAppLists()
                }
            }
            return
        }

        isPreparingSpaceState.value = true
        manager.prepareDualSpace(object : AxDualAppsManager.DualSpaceCreationCallback() {
            override fun onPrepareDone() {
                scope.launch(Dispatchers.IO) {
                    manager.installApp(app.packageName, app.uid)
                    withContext(Dispatchers.Main) {
                        busyPackagesState.value = busyPackagesState.value - app.packageName
                        enablingPackagesState.value = enablingPackagesState.value - app.packageName
                        isPreparingSpaceState.value = false
                        refreshAppLists()
                    }
                }
            }

            override fun onFailure(errorCode: Int) {
                busyPackagesState.value = busyPackagesState.value - app.packageName
                enablingPackagesState.value = enablingPackagesState.value - app.packageName
                isPreparingSpaceState.value = false
                refreshAppLists()
            }
        })
    }

    fun handleToggle(app: DualAppEntry, enabled: Boolean) {
        if (busyPackagesState.value.contains(app.packageName)) return
        if (isPreparingSpaceState.value || isClearingSpaceState.value) return
        if (enabled) {
            handleEnable(app)
        } else {
            pendingDeleteAppState.value = app
        }
    }

    LaunchedEffect(userApps) {
        if (userApps.isNotEmpty()) {
            refreshAppLists()
        } else {
            isLoadingState.value = false
        }
    }

    val filteredRecommended = remember(recommendedList, searchQuery) {
        if (searchQuery.isBlank()) recommendedList
        else recommendedList.filter {
            it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredAvailable = remember(availableList, searchQuery) {
        if (searchQuery.isBlank()) availableList
        else availableList.filter {
            it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        val icons = Icons.Default
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQueryState.value = it },
                placeholder = { Text(stringResource(R.string.dual_apps_search_hint)) },
                leadingIcon = { Icon(icons.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isPreparingSpace) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.dual_apps_creating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredRecommended.isNotEmpty()) {
                PreferenceGroup(title = stringResource(R.string.dual_apps_recommended)) {
                    filteredRecommended.forEach { app ->
                        item {
                            DualAppPreferenceItem(
                                app = app,
                                isBusy = busyPackages.contains(app.packageName),
                                isEnabling = enablingPackages.contains(app.packageName),
                                inputEnabled = !controlsLocked,
                                onToggle = { enabled -> handleToggle(app, enabled) },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredAvailable.isNotEmpty()) {
                PreferenceGroup(title = stringResource(R.string.dual_apps_all_apps)) {
                    filteredAvailable.forEach { app ->
                        item {
                            DualAppPreferenceItem(
                                app = app,
                                isBusy = busyPackages.contains(app.packageName),
                                isEnabling = enablingPackages.contains(app.packageName),
                                inputEnabled = !controlsLocked,
                                onToggle = { enabled -> handleToggle(app, enabled) },
                            )
                        }
                    }
                }
            }

            if (filteredRecommended.isEmpty() && filteredAvailable.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.dual_apps_no_apps),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }

    pendingDeleteApp?.let { app ->
        AlertDialog(
            onDismissRequest = { pendingDeleteAppState.value = null },
            title = { Text(stringResource(R.string.dual_apps_delete_dialog_title)) },
            text = { Text(stringResource(R.string.dual_apps_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = app
                        pendingDeleteAppState.value = null
                        if (busyPackagesState.value.contains(target.packageName)) return@TextButton
                        busyPackagesState.value = busyPackagesState.value + target.packageName
                        manager.uninstallApp(target.packageName, target.uid, object : AxDualAppsManager.DualAppDeletionCallback() {
                            override fun onAppDeleted(packageName: String, code: Int) {
                                busyPackagesState.value = busyPackagesState.value - target.packageName
                                refreshAppLists()
                            }
                        })
                    },
                ) {
                    Text(stringResource(AndroidR.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteAppState.value = null }) {
                    Text(stringResource(AndroidR.string.cancel))
                }
            },
        )
    }

    if (showDeleteSpaceDialogState.value) {
        AlertDialog(
            onDismissRequest = { showDeleteSpaceDialogState.value = false },
            title = { Text(stringResource(R.string.dual_apps_delete_space_dialog_title)) },
            text = { Text(stringResource(R.string.dual_apps_delete_space_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteSpaceDialogState.value = false
                        if (isClearingSpaceState.value || isPreparingSpaceState.value) return@TextButton
                        isClearingSpaceState.value = true
                        manager.clearDualSpace(object : AxDualAppsManager.DualSpaceDeletionCallback() {
                            override fun onDeleteDone() {
                                isClearingSpaceState.value = false
                                busyPackagesState.value = emptySet()
                                enablingPackagesState.value = emptySet()
                                refreshAppLists()
                            }

                            override fun onFailure() {
                                isClearingSpaceState.value = false
                                refreshAppLists()
                            }
                        })
                    },
                ) {
                    Text(stringResource(R.string.dual_apps_clear_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSpaceDialogState.value = false }) {
                    Text(stringResource(AndroidR.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun DualAppPreferenceItem(
    app: DualAppEntry,
    onToggle: (Boolean) -> Unit,
    isBusy: Boolean = false,
    isEnabling: Boolean = false,
    inputEnabled: Boolean = true,
) {
    val checked = if (isBusy) isEnabling else app.isCloned
    val summary = if (isBusy && isEnabling) {
        stringResource(R.string.dual_apps_creating)
    } else {
        app.packageName
    }
    SwitchPreference(
        title = app.label,
        summary = summary,
        checked = checked,
        enabled = inputEnabled && !isBusy,
        onCheckedChange = onToggle,
        customIcon = {
            if (app.icon != null) {
                val bitmap = remember(app.icon) { app.icon.toBitmap(96, 96).asImageBitmap() }
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                )
            }
        },
    )
}
