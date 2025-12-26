package com.android.axion.axionparts.ui.screens

import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.ui.components.LockscreenBanner
import com.android.axion.axionparts.ui.components.SoundBanner
import com.android.axion.axionparts.ui.components.ThemesBanner
import com.android.axion.axionparts.ui.components.UIFeaturesBanner

@Composable
fun CustomizeContent(
    modifier: Modifier = Modifier,
    onNavigateToLockscreen: () -> Unit = {},
    onNavigateToUIFeatures: () -> Unit = {},
    onNavigateToSound: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        ThemesBanner(
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
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LockscreenBanner(
            onClick = onNavigateToLockscreen
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        UIFeaturesBanner(
            onClick = onNavigateToUIFeatures
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SoundBanner(
            onClick = onNavigateToSound
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
