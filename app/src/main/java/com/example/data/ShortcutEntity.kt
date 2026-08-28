package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val intentUri: String,
    val iconName: String = "bolt",
    val customIconUri: String? = null,
    val customColor: Long? = null,
    val useRoot: Boolean = true,
    val tileSlot: Int = 0, // 0 = None, 1..10 = Quick Settings Tile Slot
    val category: String = "通用",
    val createdAt: Long = System.currentTimeMillis()
)
