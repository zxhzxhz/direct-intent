package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ShortcutEntity
import com.example.ui.theme.HyperOSBlue
import com.example.ui.theme.HyperOSEmerald
import com.example.ui.theme.HyperOSOrange
import com.example.ui.theme.HyperOSPurple
import com.example.ui.theme.HyperOSRed
import com.example.utils.IconHelper
import com.example.utils.IntentLauncher
import com.example.utils.ShortcutHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutEditDialog(
    shortcutToEdit: ShortcutEntity?,
    onDismiss: () -> Unit,
    onSave: (ShortcutEntity) -> Unit,
    onOpenPresets: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var alias by remember { mutableStateOf(shortcutToEdit?.alias ?: "") }
    var intentUri by remember { mutableStateOf(shortcutToEdit?.intentUri ?: "") }
    var iconName by remember { mutableStateOf(shortcutToEdit?.iconName ?: "bolt") }
    var customIconUri by remember { mutableStateOf(shortcutToEdit?.customIconUri) }
    var iconTab by remember { mutableIntStateOf(if (shortcutToEdit?.customIconUri.isNullOrBlank()) 0 else 1) } // 0: 内置图标, 1: 相册外部图片
    var isImageProcessing by remember { mutableStateOf(false) }
    var customTileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(customIconUri) {
        if (!customIconUri.isNullOrBlank()) {
            val tileBmp = IconHelper.loadCustomTileBitmap(context, customIconUri!!, size = 96)
            customTileBitmap = tileBmp
        } else {
            customTileBitmap = null
        }
    }

    var useRoot by remember { mutableStateOf(shortcutToEdit?.useRoot ?: true) }
    var tileSlot by remember { mutableIntStateOf(shortcutToEdit?.tileSlot ?: 0) }
    var category by remember { mutableStateOf(shortcutToEdit?.category ?: "通用") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isImageProcessing = true
            coroutineScope.launch {
                val savedPath = ShortcutHelper.saveCustomImage(context, uri)
                if (savedPath != null) {
                    customIconUri = savedPath
                } else {
                    Toast.makeText(context, "图片加载与裁剪失败，请重试", Toast.LENGTH_SHORT).show()
                }
                isImageProcessing = false
            }
        }
    }

    val parsedInfo = remember(intentUri) {
        IntentLauncher.parseIntentUri(intentUri)
    }

    val isFormValid = alias.isNotBlank() && intentUri.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (shortcutToEdit == null) "新建快捷指令" else "编辑快捷指令",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HyperOSBlue.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onOpenPresets() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = HyperOSBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("预设库", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HyperOSBlue)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Alias Field
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("指令别名 (例如: 钉钉打卡 / 支付宝付款码)") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null, tint = HyperOSBlue) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Intent / Scheme URI Field
                OutlinedTextField(
                    value = intentUri,
                    onValueChange = { intentUri = it },
                    label = { Text("指令语句 (Intent URI 或 Scheme 协议)") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (parsedInfo.isScheme) Icons.Default.Link else Icons.Default.Code,
                            contentDescription = null,
                            tint = if (parsedInfo.isScheme) HyperOSPurple else HyperOSBlue
                        )
                    },
                    placeholder = { Text("例如: intent://... 或 alipays://platformapi/startapp?appId=20000056") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // Real-time Parser & Preview Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (parsedInfo.parseError != null) HyperOSRed.copy(alpha = 0.1f)
                    else if (parsedInfo.isScheme) HyperOSPurple.copy(alpha = 0.08f)
                    else HyperOSBlue.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = if (parsedInfo.parseError != null) HyperOSRed
                                else if (parsedInfo.isScheme) HyperOSPurple
                                else HyperOSBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (parsedInfo.parseError != null) "指令解析警告"
                                else if (parsedInfo.isScheme) "URL Scheme 协议解析成功"
                                else "Intent 结构解析成功",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (parsedInfo.parseError != null) HyperOSRed
                                else if (parsedInfo.isScheme) HyperOSPurple
                                else HyperOSBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (parsedInfo.parseError != null) {
                            Text(
                                text = parsedInfo.parseError,
                                fontSize = 11.sp,
                                color = HyperOSRed
                            )
                        } else if (parsedInfo.isScheme) {
                            Text(
                                text = "协议: Scheme (${parsedInfo.schemeName ?: "自定义"})\n目标: ${parsedInfo.dataUri ?: parsedInfo.rawUri}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        } else {
                            val extrasText = if (parsedInfo.extrasMap.isNotEmpty()) {
                                "\n携带参数 (${parsedInfo.extrasMap.size}个): " + parsedInfo.extrasMap.entries.joinToString(", ") { "${it.key}=${it.value}" }
                            } else ""

                            Text(
                                text = "组件: ${parsedInfo.fullComponent ?: "无"}\nAction: ${parsedInfo.action ?: "无"}\nFlags: ${parsedInfo.flagsHex}$extrasText",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Category Field
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("所属分类 (例如: 通用 / 办公 / 支付 / 系统)") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = HyperOSOrange) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon Configuration (Tab Switcher: Built-in vs External Gallery Image)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "指令图标设置",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    TabRow(
                        selectedTabIndex = iconTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = iconTab == 0,
                            onClick = { iconTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("内置图标", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        Tab(
                            selected = iconTab == 1,
                            onClick = { iconTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("相册自定义", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    if (iconTab == 0) {
                        // Built-in vector icon picker
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(IconHelper.iconMetas) { meta ->
                                val isSelected = iconName == meta.key
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) HyperOSBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, HyperOSBlue) else null,
                                    modifier = Modifier.clickable { iconName = meta.key }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = meta.vector,
                                            contentDescription = meta.label,
                                            tint = if (isSelected) HyperOSBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = meta.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) HyperOSBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // External Gallery Image Picker & Real-time Squircle Preview
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isImageProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(36.dp),
                                        color = HyperOSBlue,
                                        strokeWidth = 3.dp
                                    )
                                    Text("正在通过 Coil 加载并裁剪为圆角矩形...", fontSize = 11.sp, color = HyperOSBlue)
                                } else if (!customIconUri.isNullOrBlank()) {
                                    // Dual Preview: Desktop Squircle + Control Center Tile Simulation
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Desktop Icon Preview
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .shadow(8.dp, RoundedCornerShape(17.dp))
                                                    .clip(RoundedCornerShape(17.dp))
                                                    .border(1.5.dp, HyperOSBlue.copy(alpha = 0.4f), RoundedCornerShape(17.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = customIconUri,
                                                    contentDescription = "桌面圆角图标预览",
                                                    modifier = Modifier.size(68.dp),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("桌面图标", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        // 2. Control Center Tile Preview
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF25262B))
                                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (customTileBitmap != null) {
                                                    Image(
                                                        bitmap = customTileBitmap!!.asImageBitmap(),
                                                        contentDescription = "控制中心磁贴图标预览",
                                                        modifier = Modifier.size(40.dp)
                                                    )
                                                } else {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(22.dp),
                                                        strokeWidth = 2.dp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("控制中心磁贴", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Text(
                                        text = "已生成桌面彩色圆角图标 & 控制中心单色矢量磁贴",
                                        fontSize = 11.sp,
                                        color = HyperOSEmerald,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = HyperOSBlue),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("更换图片", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { customIconUri = null },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = HyperOSRed)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("清除图片", fontSize = 12.sp, color = HyperOSRed)
                                        }
                                    }
                                } else {
                                    // No custom icon yet
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = HyperOSBlue)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("从相册选择自定义图片", fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "支持 PNG、JPG、WEBP 格式，通过 Coil 自动裁剪为圆角矩形图标",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Tile Slot Picker
                Column {
                    Text(
                        text = "绑定至控制中心 Quick Settings Tile (1-10)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val slots = listOf(0 to "无") + (1..10).map { it to "T$it" }
                        items(slots) { (slot, label) ->
                            val isSelected = tileSlot == slot
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) HyperOSEmerald.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, HyperOSEmerald) else null,
                                modifier = Modifier.clickable { tileSlot = slot }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) HyperOSEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Root Toggle Switch (MIUI group style)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = HyperOSRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "使用 Root 权限执行",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "执行 su -c am start 突破未导出与后台限制",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useRoot,
                            onCheckedChange = { useRoot = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = HyperOSBlue
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        onSave(
                            ShortcutEntity(
                                id = shortcutToEdit?.id ?: 0L,
                                alias = alias.trim(),
                                intentUri = intentUri.trim(),
                                iconName = iconName,
                                customIconUri = if (iconTab == 1 && !customIconUri.isNullOrBlank()) customIconUri else null,
                                useRoot = useRoot,
                                tileSlot = tileSlot,
                                category = if (category.isBlank()) "通用" else category.trim()
                            )
                        )
                    }
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HyperOSBlue)
            ) {
                Text("保存指令", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
