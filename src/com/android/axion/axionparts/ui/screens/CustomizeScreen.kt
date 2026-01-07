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
import androidx.compose.ui.unit.dp
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
            title = "Themes",
            subtitle = "Icons, shapes & more",
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
            title = "Lockscreen",
            subtitle = "Edge light, media & visualizer",
            illustrationType = IllustrationType.LOCKSCREEN,
            onClick = onNavigateToLockscreen
        ),
        CustomizeItem(
            title = "UI Features",
            subtitle = "Status bar, QS & more",
            illustrationType = IllustrationType.UI_FEATURES,
            onClick = onNavigateToUIFeatures
        ),
        CustomizeItem(
            title = "Sound",
            subtitle = "Per-app volume & multi audio",
            illustrationType = IllustrationType.SOUND,
            onClick = onNavigateToSound
        ),
        CustomizeItem(
            title = "Gestures",
            subtitle = "Shake & three finger actions",
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
