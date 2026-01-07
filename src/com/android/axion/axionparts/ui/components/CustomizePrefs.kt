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

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.axion.axionparts.ui.theme.ExpressiveShapes

@Composable
fun CustomizeIllustration(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "illustration")
    
    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )
    
    val float2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )
    
    val sliderAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slider"
    )
    
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            val phoneWidth = 55.dp.toPx()
            val phoneHeight = 95.dp.toPx()
            val phoneLeft = centerX - phoneWidth / 2
            val phoneTop = centerY - phoneHeight / 2
            
            drawRoundRect(
                color = onSurface.copy(alpha = 0.15f),
                topLeft = Offset(phoneLeft, phoneTop),
                size = Size(phoneWidth, phoneHeight),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
            
            drawRoundRect(
                color = primary.copy(alpha = 0.4f),
                topLeft = Offset(phoneLeft + 4.dp.toPx(), phoneTop + 8.dp.toPx()),
                size = Size(phoneWidth - 8.dp.toPx(), phoneHeight - 16.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            
            val sliderY = phoneTop + 25.dp.toPx()
            drawRoundRect(
                color = onSurface.copy(alpha = 0.2f),
                topLeft = Offset(phoneLeft + 10.dp.toPx(), sliderY),
                size = Size(35.dp.toPx(), 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            drawCircle(
                color = primary,
                radius = 5.dp.toPx(),
                center = Offset(phoneLeft + 15.dp.toPx() + (sliderAnim * 20.dp.toPx()), sliderY + 2.dp.toPx())
            )
            
            val toggleY = phoneTop + 45.dp.toPx()
            drawRoundRect(
                color = primary.copy(alpha = 0.6f),
                topLeft = Offset(phoneLeft + 10.dp.toPx(), toggleY),
                size = Size(18.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(5.dp.toPx())
            )
            drawCircle(
                color = primary,
                radius = 4.dp.toPx(),
                center = Offset(phoneLeft + 24.dp.toPx(), toggleY + 5.dp.toPx())
            )
            
            drawRoundRect(
                color = onSurface.copy(alpha = 0.15f),
                topLeft = Offset(phoneLeft + 32.dp.toPx(), toggleY + 2.dp.toPx()),
                size = Size(12.dp.toPx(), 6.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            
            val colorY = phoneTop + 65.dp.toPx()
            drawCircle(
                color = primary.copy(alpha = 0.8f),
                radius = 5.dp.toPx(),
                center = Offset(phoneLeft + 14.dp.toPx(), colorY)
            )
            drawCircle(
                color = primary,
                radius = 5.dp.toPx(),
                center = Offset(phoneLeft + 28.dp.toPx(), colorY)
            )
            drawCircle(
                color = onSurface.copy(alpha = 0.3f),
                radius = 5.dp.toPx(),
                center = Offset(phoneLeft + 42.dp.toPx(), colorY)
            )
            
            drawRoundRect(
                color = primary.copy(alpha = 0.5f),
                topLeft = Offset(centerX - 90.dp.toPx() + (float1 * 8.dp.toPx()), centerY - 30.dp.toPx() - (float2 * 10.dp.toPx())),
                size = Size(28.dp.toPx(), 28.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            
            drawCircle(
                color = primary.copy(alpha = 0.3f),
                radius = 18.dp.toPx(),
                center = Offset(centerX + 75.dp.toPx() - (float2 * 6.dp.toPx()), centerY - 20.dp.toPx() + (float1 * 8.dp.toPx()))
            )
            
            drawRoundRect(
                color = primary.copy(alpha = 0.25f),
                topLeft = Offset(centerX + 55.dp.toPx(), centerY + 25.dp.toPx() - (float1 * 5.dp.toPx())),
                size = Size(22.dp.toPx(), 22.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            
            drawCircle(
                color = primary.copy(alpha = 0.4f),
                radius = 12.dp.toPx(),
                center = Offset(centerX - 70.dp.toPx(), centerY + 30.dp.toPx() + (float2 * 6.dp.toPx()))
            )
        }
    }
}

@Composable
fun CustomizeCardStack(
    items: List<CustomizeItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            CustoimizePrefCard(
                item = item,
                isTopCard = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CustoimizePrefCard(
    item: CustomizeItem,
    isTopCard: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = item.onClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceBright
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            CardIllustration(type = item.illustrationType)
        }
    }
}

@Composable
private fun CardIllustration(
    type: IllustrationType,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.primaryContainer.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            IllustrationType.THEMES -> {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (-8).dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .offset(x = 8.dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(colors.tertiary.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 6.dp, y = (-8).dp)
                        .clip(CircleShape)
                        .background(colors.secondary.copy(alpha = 0.6f))
                )
            }
            IllustrationType.LOCKSCREEN -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary.copy(alpha = 0.5f))
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.primary.copy(alpha = 0.3f))
                    )
                }
            }
            IllustrationType.UI_FEATURES -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.primary.copy(alpha = 0.8f))
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.primary.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }
            IllustrationType.SOUND -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0.4f, 0.7f, 1f, 0.6f, 0.3f).forEach { height ->
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height((24.dp * height))
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.primary.copy(alpha = 0.5f + height * 0.3f))
                        )
                    }
                }
            }
            IllustrationType.GESTURES -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.6f))
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(width = 20.dp, height = 2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(colors.primary.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

enum class IllustrationType {
    THEMES,
    LOCKSCREEN,
    UI_FEATURES,
    SOUND,
    GESTURES
}

data class CustomizeItem(
    val title: String,
    val subtitle: String,
    val illustrationType: IllustrationType,
    val onClick: () -> Unit
)
