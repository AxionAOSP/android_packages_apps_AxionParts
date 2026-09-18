package com.android.axion.axionparts.ui.screens

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.SemiCircleFillView
import com.android.axion.compose.preferences.BasePreference
import com.android.axion.compose.preferences.PreferenceGroup
import com.android.axion.compose.scaffold.AxionScaffold
import com.android.internal.dexopt.AxUserDexoptManager

data class AppOptimizationEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val state: Int,
)

@Composable
fun AppOptimizationScreen(
    onBackClick: (() -> Unit)? = null,
    showTopBar: Boolean = true,
) {
    val context = LocalContext.current
    val currentState = remember { mutableIntStateOf(0) }
    val currentPkg = remember { mutableStateOf("") }
    val currentIndex = remember { mutableIntStateOf(0) }
    val totalCount = remember { mutableIntStateOf(0) }
    val appEntries = remember { mutableStateListOf<AppOptimizationEntry>() }

    fun refreshAppList(packageNames: List<String>, activePkg: String, state: Int) {
        val pm = context.packageManager
        val list = mutableListOf<AppOptimizationEntry>()
        for (pkg in packageNames) {
            try {
                val appInfo = pm.getApplicationInfoAsUser(pkg, 0, 0)
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getUserBadgedIcon(appInfo.loadUnbadgedIcon(pm), UserHandle.of(0))
                val entryState = if (state == 2 && pkg == activePkg) 1 else 0
                list.add(AppOptimizationEntry(pkg, label, icon, entryState))
            } catch (e: PackageManager.NameNotFoundException) {
                logError(pkg, e)
                continue
            }
        }
        appEntries.clear()
        appEntries.addAll(list)
    }

    DisposableEffect(Unit) {
        val callback = object : AxUserDexoptManager.BatchDexOptimizationCallback() {
            override fun onConnected(pkgs: List<String>?, state: Int, curOptPkg: String?) {
                val safePkgs = pkgs ?: emptyList()
                val safeCurPkg = curOptPkg ?: ""
                currentState.intValue = state
                currentPkg.value = safeCurPkg
                totalCount.intValue = safePkgs.size
                currentIndex.intValue = if (state == 2) {
                    safePkgs.indexOf(safeCurPkg).coerceAtLeast(0)
                } else {
                    0
                }
                if (state == 1 || state == 2) {
                    refreshAppList(safePkgs, safeCurPkg, state)
                }
            }

            override fun onProgress(current: Int, total: Int, pkgName: String?) {
                currentIndex.intValue = current
                totalCount.intValue = total
                currentPkg.value = pkgName ?: ""
                currentState.intValue = 2
                val targetIndex = appEntries.indexOfFirst { it.packageName == pkgName }
                if (targetIndex != -1) {
                    val existing = appEntries[targetIndex]
                    appEntries[targetIndex] = existing.copy(state = 2)
                }
            }

            override fun onCompleted() {
                currentState.intValue = 3
                currentIndex.intValue = totalCount.intValue
                for (i in 0 until appEntries.size) {
                    appEntries[i] = appEntries[i].copy(state = 2)
                }
            }

            override fun onError(error: String?) {
            }
        }

        AxUserDexoptManager.connect(callback)
        onDispose {
            AxUserDexoptManager.disconnect()
        }
    }

    if (showTopBar) {
        AxionScaffold(
            title = stringResource(R.string.app_optimization_title),
            onBackClick = { onBackClick?.invoke() },
        ) { innerPadding ->
            AppOptimizationContent(
                state = currentState.intValue,
                currentIndex = currentIndex.intValue,
                totalCount = totalCount.intValue,
                appEntries = appEntries,
                onStartClick = {
                    currentState.intValue = 2
                    AxUserDexoptManager.performDexOptimization()
                },
                modifier = Modifier.padding(innerPadding),
            )
        }
    } else {
        AppOptimizationContent(
            state = currentState.intValue,
            currentIndex = currentIndex.intValue,
            totalCount = totalCount.intValue,
            appEntries = appEntries,
            onStartClick = {
                currentState.intValue = 2
                AxUserDexoptManager.performDexOptimization()
            },
        )
    }
}

@Composable
private fun AppOptimizationContent(
    state: Int,
    currentIndex: Int,
    totalCount: Int,
    appEntries: List<AppOptimizationEntry>,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icons = Icons.Default
    val progressRatio = if (totalCount > 0) currentIndex.toFloat() / totalCount.toFloat() else if (state == 3 || state == 4) 1.0f else 0.0f
    val percentage = (progressRatio * 100).toInt().coerceIn(0, 100)
    val gaugeTextColor = if (isSystemInDarkTheme()) Color(0xFFF2F2F2) else Color(0xFF1C1C1C)
    val descriptionColor = if (isSystemInDarkTheme()) Color(0xFFADADAD) else Color(0xFF606060)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SpeedometerGauge(
                    progress = progressRatio,
                    percentage = percentage,
                    totalCount = totalCount,
                    isRunning = state == 2,
                    textColor = gaugeTextColor,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val descRes = when (state) {
                    2 -> R.string.app_optimization_desc_running
                    3, 4 -> R.string.app_optimization_desc_done
                    else -> R.string.app_optimization_desc_idle
                }
                Text(
                    text = stringResource(descRes),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        letterSpacing = 0.015.em,
                        textAlign = TextAlign.Center,
                    ),
                    color = descriptionColor,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp),
                )

                if (state == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStartClick,
                        modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                    ) {
                        Icon(
                            imageVector = icons.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.app_optimization_start),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        if (appEntries.isEmpty() || state == 3 || state == 4) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = icons.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                    Text(
                        text = stringResource(R.string.app_optimization_no_apps),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.app_optimization_no_apps_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            item {
                PreferenceGroup(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(R.string.app_optimization_pending_apps, appEntries.size),
                ) {
                    appEntries.forEach { entry ->
                        item {
                            BasePreference(
                                title = entry.label,
                                summary = entry.packageName,
                                customIcon = {
                                    if (entry.icon != null) {
                                        val bitmap = remember(entry.icon) {
                                            entry.icon.toBitmap(96, 96).asImageBitmap()
                                        }
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp).clip(CircleShape),
                                        )
                                    }
                                },
                                widget = {
                                    when (entry.state) {
                                        1 -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        2 -> {
                                            Icon(
                                                imageVector = icons.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = icons.HourglassEmpty,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedometerGauge(
    progress: Float,
    percentage: Int,
    totalCount: Int,
    isRunning: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxWidth().height(180.dp),
    ) {
        AndroidView(
            factory = { context -> SemiCircleFillView(context) },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.setAnimationDurationType(if (totalCount > 10) 2 else 1)
                if (isRunning) {
                    view.animateToProgress(progress)
                } else {
                    view.setProgressWithNoAnim(progress)
                }
            },
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 80.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = "$percentage",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.015.em,
                    color = textColor,
                )
                Text(
                    text = "%",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.015.em,
                    color = textColor,
                )
            }
        }
    }
}

private fun logError(pkg: String, e: Exception) {
    Log.w("AppOptimizationScreen", "Package not found: $pkg", e)
}
