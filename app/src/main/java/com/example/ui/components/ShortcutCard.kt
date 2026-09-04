package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShortcutEntity
import com.example.ui.theme.HyperOSBlue
import com.example.ui.theme.HyperOSEmerald
import com.example.ui.theme.HyperOSOrange
import com.example.ui.theme.HyperOSPurple
import com.example.ui.theme.HyperOSRed
import com.example.utils.IconHelper
import com.example.utils.IntentLauncher

@Composable
fun ShortcutCard(
    shortcut: ShortcutEntity,
    onTrigger: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToDesktop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val parsedInfo = remember(shortcut.intentUri) {
        IntentLauncher.parseIntentUri(shortcut.intentUri)
    }

    val iconMeta = remember(shortcut.iconName) {
        IconHelper.getIconMeta(shortcut.iconName)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Icon + Title + Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // MIUI-style Squircle Icon Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (!shortcut.customIconUri.isNullOrBlank()) Color.Transparent
                            else iconMeta.composeColor.copy(alpha = 0.14f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!shortcut.customIconUri.isNullOrBlank()) {
                        AsyncImage(
                            model = shortcut.customIconUri,
                            contentDescription = shortcut.alias,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = iconMeta.vector,
                            contentDescription = null,
                            tint = iconMeta.composeColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shortcut.alias,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pill Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = shortcut.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Scheme vs Intent Pill
                        if (parsedInfo.isScheme) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HyperOSPurple.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Scheme",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HyperOSPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Root vs Normal Mode Pill
                        if (shortcut.useRoot) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HyperOSRed.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Root",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HyperOSRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HyperOSBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "标准",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = HyperOSBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Tile Slot Pill
                        if (shortcut.tileSlot in 1..10) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HyperOSEmerald.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "磁贴 ${shortcut.tileSlot}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HyperOSEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Intent / Scheme Detail Snippet Card (Clickable to expand)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (parsedInfo.isScheme) "URL Scheme 协议" else (parsedInfo.fullComponent ?: "Intent 指令"),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "折叠" else "展开",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (parsedInfo.dataUri != null) {
                                DetailItem(label = "Data / URL", value = parsedInfo.dataUri)
                            }
                            if (parsedInfo.action != null) {
                                DetailItem(label = "Action", value = parsedInfo.action)
                            }
                            if (parsedInfo.componentPackage != null && parsedInfo.fullComponent == null) {
                                DetailItem(label = "包名", value = parsedInfo.componentPackage)
                            }
                            if (parsedInfo.extrasMap.isNotEmpty()) {
                                val extrasStr = parsedInfo.extrasMap.entries.joinToString(", ") { "${it.key}=${it.value}" }
                                DetailItem(label = "附加参数", value = extrasStr)
                            }
                            if (parsedInfo.flagsHex != "0x0" && parsedInfo.flagsHex.isNotBlank()) {
                                DetailItem(label = "Flags", value = parsedInfo.flagsHex)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Secondary Actions: Desktop Icon, Edit, Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add to Desktop Action Icon (Clean icon only)
                    IconButton(
                        onClick = onAddToDesktop,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddHome,
                            contentDescription = "添加到桌面",
                            tint = HyperOSBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = HyperOSRed,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Primary Trigger Button
                Button(
                    onClick = onTrigger,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HyperOSBlue),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("一键触发", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label: ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = HyperOSBlue,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
