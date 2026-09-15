package com.android.axion.axionparts.ui.screens

import android.R as AndroidR
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
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

    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.dual_apps),
            onBackClick = { onBackClick?.invoke() },
            actions = {
                if (manager.isDualSpaceExists) {
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
            )
        }
    } else {
        DualAppsContent(
            showDeleteSpaceDialogState = showDeleteSpaceDialogState,
        )
    }
}

@Composable
private fun DualAppsContent(
    modifier: Modifier = Modifier,
    showDeleteSpaceDialogState: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember { AxDualAppsManager.getInstance() }

    val userAppsState = rememberAppList(AppFilter.USER_ONLY, AppFilter.LAUNCHABLE_ONLY, AppFilter.NO_OVERLAYS)
    val userApps = userAppsState.value

    val isLoadingState = remember { mutableStateOf(true) }
    val searchQueryState = remember { mutableStateOf("") }
    val recommendedListState = remember { mutableStateOf<List<DualAppEntry>>(emptyList()) }
    val availableListState = remember { mutableStateOf<List<DualAppEntry>>(emptyList()) }
    val pendingDeleteAppState = remember { mutableStateOf<DualAppEntry?>(null) }

    val isLoading = isLoadingState.value
    val searchQuery = searchQueryState.value
    val recommendedList = recommendedListState.value
    val availableList = availableListState.value
    val pendingDeleteApp = pendingDeleteAppState.value

    fun refreshAppLists() {
        scope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val allowedSet = manager.queryAllowPackages().toSet()

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
            }
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

            if (filteredRecommended.isNotEmpty()) {
                PreferenceGroup(title = stringResource(R.string.dual_apps_recommended)) {
                    filteredRecommended.forEach { app ->
                        item {
                            DualAppPreferenceItem(
                                app = app,
                                onToggle = { enabled ->
                                    if (enabled) {
                                        if (!manager.isDualSpaceInitialized) {
                                            manager.prepareDualSpace(object : AxDualAppsManager.DualSpaceCreationCallback() {
                                                override fun onPrepareDone() {
                                                    manager.installApp(app.packageName, app.uid)
                                                    refreshAppLists()
                                                }

                                                override fun onFailure(errorCode: Int) {
                                                    refreshAppLists()
                                                }
                                            })
                                        } else {
                                            scope.launch(Dispatchers.IO) {
                                                manager.installApp(app.packageName, app.uid)
                                                refreshAppLists()
                                            }
                                        }
                                    } else {
                                        pendingDeleteAppState.value = app
                                    }
                                },
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
                                onToggle = { enabled ->
                                    if (enabled) {
                                        if (!manager.isDualSpaceInitialized) {
                                            manager.prepareDualSpace(object : AxDualAppsManager.DualSpaceCreationCallback() {
                                                override fun onPrepareDone() {
                                                    manager.installApp(app.packageName, app.uid)
                                                    refreshAppLists()
                                                }

                                                override fun onFailure(errorCode: Int) {
                                                    refreshAppLists()
                                                }
                                            })
                                        } else {
                                            scope.launch(Dispatchers.IO) {
                                                manager.installApp(app.packageName, app.uid)
                                                refreshAppLists()
                                            }
                                        }
                                    } else {
                                        pendingDeleteAppState.value = app
                                    }
                                },
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
                        manager.uninstallApp(target.packageName, target.uid, object : AxDualAppsManager.DualAppDeletionCallback() {
                            override fun onAppDeleted(packageName: String, code: Int) {
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
                        manager.clearDualSpace(object : AxDualAppsManager.DualSpaceDeletionCallback() {
                            override fun onDeleteDone() {
                                refreshAppLists()
                            }

                            override fun onFailure() {
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
) {
    SwitchPreference(
        title = app.label,
        summary = app.packageName,
        checked = app.isCloned,
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
