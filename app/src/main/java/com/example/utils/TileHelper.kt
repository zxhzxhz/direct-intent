package com.example.utils

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import android.util.Log
import com.example.tiles.QuickTile10Service
import com.example.tiles.QuickTile1Service
import com.example.tiles.QuickTile2Service
import com.example.tiles.QuickTile3Service
import com.example.tiles.QuickTile4Service
import com.example.tiles.QuickTile5Service
import com.example.tiles.QuickTile6Service
import com.example.tiles.QuickTile7Service
import com.example.tiles.QuickTile8Service
import com.example.tiles.QuickTile9Service

object TileHelper {

    private const val TAG = "TileHelper"

    val tileClasses: List<Class<out TileService>> = listOf(
        QuickTile1Service::class.java,
        QuickTile2Service::class.java,
        QuickTile3Service::class.java,
        QuickTile4Service::class.java,
        QuickTile5Service::class.java,
        QuickTile6Service::class.java,
        QuickTile7Service::class.java,
        QuickTile8Service::class.java,
        QuickTile9Service::class.java,
        QuickTile10Service::class.java
    )

    fun requestUpdateAllTiles(context: Context) {
        val appContext = context.applicationContext
        tileClasses.forEachIndexed { index, clazz ->
            try {
                TileService.requestListeningState(appContext, ComponentName(appContext, clazz))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request listening state for QuickTile${index + 1}Service", e)
            }
        }
    }

    fun requestUpdateTile(context: Context, slot: Int) {
        if (slot !in 1..10) return
        val appContext = context.applicationContext
        val clazz = tileClasses[slot - 1]
        try {
            TileService.requestListeningState(appContext, ComponentName(appContext, clazz))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request listening state for slot $slot", e)
        }
    }
}
