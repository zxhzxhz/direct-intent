package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ShortcutEntity
import com.example.data.ShortcutRepository
import com.example.utils.IntentLauncher
import com.example.utils.PresetTemplates
import com.example.utils.RootShell
import com.example.utils.TileHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(
    application: Application,
    private val repository: ShortcutRepository
) : AndroidViewModel(application) {

    data class RootState(
        val isChecking: Boolean = false,
        val isRootAvailable: Boolean = false,
        val suVersion: String = "未检测",
        val log: String = ""
    )

    private val _rootState = MutableStateFlow(RootState())
    val rootState: StateFlow<RootState> = _rootState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("全部")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _lastExecutionLog = MutableStateFlow<String?>(null)
    val lastExecutionLog: StateFlow<String?> = _lastExecutionLog.asStateFlow()

    val filteredShortcuts: StateFlow<List<ShortcutEntity>> = combine(
        repository.allShortcuts,
        _searchQuery,
        _selectedCategory
    ) { shortcuts, query, category ->
        var list = shortcuts
        if (category != "全部") {
            list = list.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.alias.lowercase().contains(q) ||
                it.intentUri.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allShortcuts: StateFlow<List<ShortcutEntity>> = repository.allShortcuts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        checkRootStatus()
        seedPresetsIfEmpty()
    }

    fun checkRootStatus() {
        viewModelScope.launch {
            _rootState.value = _rootState.value.copy(isChecking = true)
            val available = RootShell.isRootAvailable()
            val suVer = if (available) RootShell.getSuVersion() else "未找到 su 运行环境"
            val logMsg = if (available) "Root 权限已成功获取 ($suVer)" else "当前环境未检测到 Root 权限，部分需要 am start root 的指令将回退为标准 Intent 启动。"

            _rootState.value = RootState(
                isChecking = false,
                isRootAvailable = available,
                suVersion = suVer,
                log = logMsg
            )
        }
    }

    private fun seedPresetsIfEmpty() {
        viewModelScope.launch {
            repository.allShortcuts.collect { list ->
                if (list.isEmpty()) {
                    PresetTemplates.presets.forEach { preset ->
                        repository.insert(preset)
                    }
                    TileHelper.requestUpdateAllTiles(getApplication())
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addOrUpdateShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            if (shortcut.id == 0L) {
                repository.insert(shortcut)
            } else {
                repository.update(shortcut)
            }
            TileHelper.requestUpdateAllTiles(getApplication())
        }
    }

    fun deleteShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.delete(shortcut)
            TileHelper.requestUpdateAllTiles(getApplication())
        }
    }

    fun triggerShortcut(context: Context, shortcut: ShortcutEntity) {
        viewModelScope.launch {
            val result = IntentLauncher.launch(
                context = context,
                intentUriString = shortcut.intentUri,
                forceRoot = shortcut.useRoot
            )
            _lastExecutionLog.value = "[${shortcut.alias}] -> ${result.message}\n${result.logOutput}"
        }
    }

    fun clearExecutionLog() {
        _lastExecutionLog.value = null
    }

    fun importJsonShortcuts(jsonString: String): String {
        return try {
            val jsonArray = JSONArray(jsonString)
            var count = 0
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val entity = ShortcutEntity(
                    alias = obj.optString("alias", "导入指令"),
                    intentUri = obj.optString("intentUri", ""),
                    iconName = obj.optString("iconName", "bolt"),
                    customIconUri = if (obj.has("customIconUri") && !obj.isNull("customIconUri")) obj.getString("customIconUri") else null,
                    customColor = if (obj.has("customColor") && !obj.isNull("customColor")) obj.getLong("customColor") else null,
                    useRoot = obj.optBoolean("useRoot", true),
                    tileSlot = obj.optInt("tileSlot", 0),
                    category = obj.optString("category", "导入")
                )
                if (entity.intentUri.isNotBlank()) {
                    viewModelScope.launch {
                        repository.insert(entity)
                    }
                    count++
                }
            }
            if (count > 0) {
                TileHelper.requestUpdateAllTiles(getApplication())
            }
            "成功导入 $count 条快捷指令"
        } catch (e: Exception) {
            "JSON 解析失败: ${e.localizedMessage ?: e.message}"
        }
    }

    fun exportJsonShortcuts(shortcuts: List<ShortcutEntity>): String {
        val jsonArray = JSONArray()
        shortcuts.forEach {
            val obj = JSONObject().apply {
                put("alias", it.alias)
                put("intentUri", it.intentUri)
                put("iconName", it.iconName)
                it.customIconUri?.let { uri -> put("customIconUri", uri) }
                it.customColor?.let { color -> put("customColor", color) }
                put("useRoot", it.useRoot)
                put("tileSlot", it.tileSlot)
                put("category", it.category)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    class Factory(
        private val application: Application,
        private val repository: ShortcutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
