package com.example.data

import kotlinx.coroutines.flow.Flow

class ShortcutRepository(private val dao: ShortcutDao) {
    val allShortcuts: Flow<List<ShortcutEntity>> = dao.getAllShortcuts()

    suspend fun insert(shortcut: ShortcutEntity): Long {
        if (shortcut.tileSlot in 1..10) {
            dao.clearTileSlot(shortcut.tileSlot, shortcut.id)
        }
        return dao.insertShortcut(shortcut)
    }

    suspend fun update(shortcut: ShortcutEntity) {
        if (shortcut.tileSlot in 1..10) {
            dao.clearTileSlot(shortcut.tileSlot, shortcut.id)
        }
        dao.updateShortcut(shortcut)
    }

    suspend fun delete(shortcut: ShortcutEntity) {
        dao.deleteShortcut(shortcut)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun getByTileSlotSync(slot: Int): ShortcutEntity? {
        return dao.getShortcutByTileSlotSync(slot)
    }
}
