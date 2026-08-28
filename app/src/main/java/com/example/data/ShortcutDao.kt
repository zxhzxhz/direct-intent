package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY createdAt DESC")
    fun getAllShortcuts(): Flow<List<ShortcutEntity>>

    @Query("SELECT * FROM shortcuts WHERE tileSlot = :slot LIMIT 1")
    suspend fun getShortcutByTileSlotSync(slot: Int): ShortcutEntity?

    @Query("SELECT * FROM shortcuts WHERE tileSlot = :slot LIMIT 1")
    fun getShortcutByTileSlot(slot: Int): Flow<ShortcutEntity?>

    @Query("SELECT * FROM shortcuts WHERE id = :id")
    suspend fun getShortcutById(id: Long): ShortcutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: ShortcutEntity): Long

    @Update
    suspend fun updateShortcut(shortcut: ShortcutEntity)

    @Delete
    suspend fun deleteShortcut(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE shortcuts SET tileSlot = 0 WHERE tileSlot = :slot AND id != :exceptId")
    suspend fun clearTileSlot(slot: Int, exceptId: Long = -1)
}
