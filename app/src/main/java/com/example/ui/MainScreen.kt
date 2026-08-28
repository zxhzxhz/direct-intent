package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ShortcutEntity
import com.example.ui.components.AddToDesktopDialog
import com.example.ui.components.ControlCenterTileSheet
import com.example.ui.components.JumpReplayGeneratorDialog
import com.example.ui.components.RootStatusCard
import com.example.ui.components.ShortcutCard
import com.example.ui.components.ShortcutEditDialog
import com.example.ui.theme.HyperOSBlue
import com.example.ui.theme.HyperOSPurple
import com.example.utils.ShortcutHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val shortcuts by viewModel.filteredShortcuts.collectAsStateWithLifecycle()
    val allShortcuts by viewModel.allShortcuts.collectAsStateWithLifecycle()
    val rootState by viewModel.rootState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val lastExecutionLog by viewModel.lastExecutionLog.collectAsStateWithLifecycle()

    var isSettingsOpen by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var shortcutToEdit by remember { mutableStateOf<ShortcutEntity?>(null) }

    var showTileSheet by remember { mutableStateOf(false) }
    var showPresetsDialog by remember { mutableStateOf(false) }
    var shortcutForDesktop by remember { mutableStateOf<ShortcutEntity?>(null) }

    val categories = remember(allShortcuts) {
        listOf("全部") + allShortcuts.map { it.category }.distinct().filter { it.isNotBlank() && it != "全部" }
    }

    val boundTilesCount = remember(allShortcuts) {
        allShortcuts.count { it.tileSlot in 1..10 }
    }

    val listState = rememberLazyListState()

    // Derived state for folding header: Only true when scrolled completely to the top
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    if (isSettingsOpen) {
        SettingsScreen(
            rootState = rootState,
            lastExecutionLog = lastExecutionLog,
            allShortcuts = allShortcuts,
            onCheckRoot = { viewModel.checkRootStatus() },
            onClearLog = { viewModel.clearExecutionLog() },
            onImportJson = { jsonStr -> viewModel.importJsonShortcuts(jsonStr) },
            onExportJson = { list -> viewModel.exportJsonShortcuts(list) },
            onBack = { isSettingsOpen = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(HyperOSBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "快捷指令",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Intent & Scheme 一键直达 / 桌面磁贴",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showTileSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "控制中心 Quick Tile",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = { isSettingsOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "切换主题",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        shortcutToEdit = null
                        showEditDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("新建指令", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = HyperOSBlue,
                    contentColor = Color.White
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Collapsible Mode Status Banner: Folded on scroll down, only expands when scrolled back to top
                AnimatedVisibility(
                    visible = isAtTop,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(animationSpec = tween(180)),
                    exit = shrinkVertically(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut(animationSpec = tween(120))
                ) {
                    RootStatusCard(
                        isRootAvailable = rootState.isRootAvailable,
                        totalShortcutsCount = allShortcuts.size,
                        tilesCount = boundTilesCount,
                        onCheckRoot = { viewModel.checkRootStatus() },
                        onOpenTiles = { showTileSheet = true },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Pinned Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("搜索指令名称、Intent 语句或分类...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HyperOSBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除搜索")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pinned Category Filter Row & Presets Button (Genre)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedCategory(cat) },
                                label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HyperOSBlue.copy(alpha = 0.15f),
                                    selectedLabelColor = HyperOSBlue
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HyperOSPurple.copy(alpha = 0.12f),
                        modifier = Modifier.clickable { showPresetsDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = HyperOSPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("预设库", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HyperOSPurple)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Shortcuts List with high-performance item keys and recycled contentTypes
                if (shortcuts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(HyperOSBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = HyperOSBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "未找到匹配的快捷指令" else "暂无快捷指令",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "点击下方按钮一键导入预设常用指令，或点击右下角新建",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showPresetsDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HyperOSBlue)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("浏览预设库", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = shortcuts,
                            key = { it.id },
                            contentType = { "shortcut_item" }
                        ) { shortcut ->
                            ShortcutCard(
                                shortcut = shortcut,
                                onTrigger = {
                                    viewModel.triggerShortcut(context, shortcut)
                                    Toast.makeText(context, "正在触发: ${shortcut.alias}", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = {
                                    shortcutToEdit = shortcut
                                    showEditDialog = true
                                },
                                onDelete = {
                                    viewModel.deleteShortcut(shortcut)
                                    Toast.makeText(context, "已删除: ${shortcut.alias}", Toast.LENGTH_SHORT).show()
                                },
                                onAddToDesktop = {
                                    shortcutForDesktop = shortcut
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showEditDialog) {
        ShortcutEditDialog(
            shortcutToEdit = shortcutToEdit,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.addOrUpdateShortcut(updated)
                showEditDialog = false
            },
            onOpenPresets = {
                showEditDialog = false
                showPresetsDialog = true
            }
        )
    }

    if (showPresetsDialog) {
        JumpReplayGeneratorDialog(
            onDismiss = { showPresetsDialog = false },
            onSelectPreset = { preset ->
                shortcutToEdit = preset.copy(id = 0L)
                showPresetsDialog = false
                showEditDialog = true
            }
        )
    }

    if (showTileSheet) {
        ControlCenterTileSheet(
            allShortcuts = allShortcuts,
            onDismiss = { showTileSheet = false },
            onEditShortcut = { shortcut ->
                shortcutToEdit = shortcut
                showEditDialog = true
            }
        )
    }

    if (shortcutForDesktop != null) {
        AddToDesktopDialog(
            shortcut = shortcutForDesktop!!,
            onDismiss = { shortcutForDesktop = null }
        )
    }
}
