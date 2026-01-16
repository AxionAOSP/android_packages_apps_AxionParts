package com.android.axion.axionparts.ui.screens

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.R
import com.android.axion.axionparts.ui.components.*
import com.android.axion.axionparts.ui.theme.BottomNavPadding
import com.android.axion.axionparts.ui.theme.MaxContentWidth

@Composable
fun CustomizeContent(
    modifier: Modifier = Modifier,
    onNavigateToLockscreen: () -> Unit = {},
    onNavigateToUIFeatures: () -> Unit = {},
    onNavigateToSound: () -> Unit = {},
    onNavigateToGestures: () -> Unit = {}
) {
    val context = LocalContext.current
    
    val items = listOf(
        CustomizeItem(
            title = stringResource(R.string.themes),
            subtitle = stringResource(R.string.themes_subtitle),
            illustrationType = IllustrationType.THEMES,
            onClick = {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.android.axion.axthemestore",
                        "com.android.axion.axthemestore.MainActivity"
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        ),
        CustomizeItem(
            title = stringResource(R.string.lockscreen),
            subtitle = stringResource(R.string.lockscreen_subtitle),
            illustrationType = IllustrationType.LOCKSCREEN,
            onClick = onNavigateToLockscreen
        ),
        CustomizeItem(
            title = stringResource(R.string.ui_features),
            subtitle = stringResource(R.string.ui_features_subtitle),
            illustrationType = IllustrationType.UI_FEATURES,
            onClick = onNavigateToUIFeatures
        ),
        CustomizeItem(
            title = stringResource(R.string.sound),
            subtitle = stringResource(R.string.sound_subtitle),
            illustrationType = IllustrationType.SOUND,
            onClick = onNavigateToSound
        ),
        CustomizeItem(
            title = stringResource(R.string.gestures),
            subtitle = stringResource(R.string.gestures_subtitle),
            illustrationType = IllustrationType.GESTURES,
            onClick = onNavigateToGestures
        )
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            CustomizeIllustration()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CustomizeCardStack(items = items)
            
            Spacer(modifier = Modifier.height(BottomNavPadding))
        }
    }
}
