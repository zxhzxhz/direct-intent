package com.example.tiles

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.example.data.AppDatabase
import com.example.utils.IconHelper
import com.example.utils.IntentLauncher
import com.example.utils.RootShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

abstract class BaseQuickTileService(val tileSlot: Int) : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        val performTileClick = {
            serviceScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                val shortcut = db.shortcutDao().getShortcutByTileSlotSync(tileSlot)

                val qsTile = qsTile ?: return@launch
                if (shortcut == null) {
                    qsTile.label = "快捷指令 $tileSlot"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        qsTile.subtitle = "点击在 App 内绑定"
                    }
                    qsTile.state = Tile.STATE_INACTIVE
                    qsTile.icon = IconHelper.getTileIcon(applicationContext, null)
                    qsTile.updateTile()

                    // Launch main activity to let user bind intent
                    val intent = applicationContext.packageManager.getLaunchIntentForPackage(packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            val pendingIntent = PendingIntent.getActivity(
                                this@BaseQuickTileService,
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            startActivityAndCollapse(pendingIntent)
                        } else {
                            @Suppress("DEPRECATION")
                            startActivityAndCollapse(intent)
                        }
                    }
                    return@launch
                }

                // Tile is active: indicate launching state
                qsTile.state = Tile.STATE_ACTIVE
                qsTile.updateTile()

                val result = IntentLauncher.launch(
                    context = applicationContext,
                    intentUriString = shortcut.intentUri,
                    forceRoot = shortcut.useRoot,
                    tileService = this@BaseQuickTileService
                )

                Log.d("QuickTileService", "Tile $tileSlot clicked. Launch result: ${result.message}")

                // Collapse status bar / control center shade
                try {
                    RootShell.executeCommand("cmd statusbar collapse")
                    RootShell.executeCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
                } catch (e: Exception) {
                    Log.e("QuickTileService", "Failed to collapse status bar via root", e)
                }

                try {
                    @Suppress("DEPRECATION")
                    applicationContext.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                } catch (e: Exception) {
                    Log.e("QuickTileService", "Failed to close system dialogs via broadcast", e)
                }

                // Reset state back to active/inactive
                qsTile.state = if (result.success) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                qsTile.updateTile()
            }
        }

        if (isLocked) {
            unlockAndRun {
                performTileClick()
            }
        } else {
            performTileClick()
        }
    }

    private fun updateTileState() {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val shortcut = db.shortcutDao().getShortcutByTileSlotSync(tileSlot)
            val qsTile = qsTile ?: return@launch

            val tileIcon = IconHelper.getTileIcon(applicationContext, shortcut)
            qsTile.icon = tileIcon

            if (shortcut != null) {
                qsTile.label = shortcut.alias
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    qsTile.subtitle = if (shortcut.useRoot) "Root 模式" else "标准模式"
                }
                qsTile.state = Tile.STATE_INACTIVE
            } else {
                qsTile.label = "快捷指令 $tileSlot"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    qsTile.subtitle = "未配置"
                }
                qsTile.state = Tile.STATE_UNAVAILABLE
            }
            qsTile.updateTile()
        }
    }
}
