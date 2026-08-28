package com.example.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.service.quicksettings.TileService
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IntentLauncher {

    private val SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")
    private val parsedUriCache = androidx.collection.LruCache<String, ParsedIntentInfo>(300)

    data class ParsedIntentInfo(
        val rawUri: String,
        val action: String?,
        val componentPackage: String?,
        val componentClass: String?,
        val fullComponent: String?,
        val flagsHex: String,
        val dataUri: String?,
        val type: String?,
        val categories: List<String>,
        val extrasMap: Map<String, Any?>,
        val isScheme: Boolean = false,
        val schemeName: String? = null,
        val parseError: String? = null
    )

    data class LaunchResult(
        val success: Boolean,
        val message: String,
        val isRootUsed: Boolean,
        val logOutput: String
    )

    fun escapeShellArg(arg: String): String {
        return "'" + arg.replace("'", "'\\''") + "'"
    }

    fun parseIntentUri(uriString: String): ParsedIntentInfo {
        val cached = parsedUriCache.get(uriString)
        if (cached != null) {
            return cached
        }

        val cleanUri = uriString.trim()
        if (cleanUri.isBlank()) {
            val emptyInfo = ParsedIntentInfo(
                rawUri = uriString,
                action = null,
                componentPackage = null,
                componentClass = null,
                fullComponent = null,
                flagsHex = "0x0",
                dataUri = null,
                type = null,
                categories = emptyList(),
                extrasMap = emptyMap(),
                isScheme = false,
                schemeName = null,
                parseError = "指令 URI 不能为空"
            )
            parsedUriCache.put(uriString, emptyInfo)
            return emptyInfo
        }

        val isScheme = (cleanUri.contains("://") || cleanUri.matches(SCHEME_REGEX)) &&
                !cleanUri.startsWith("intent:", ignoreCase = true) &&
                !cleanUri.contains("#Intent;", ignoreCase = true)

        val schemeName = if (isScheme) {
            cleanUri.substringBefore("://").substringBefore(":").lowercase()
        } else null

        val result = try {
            val intent = if (isScheme) {
                Intent(Intent.ACTION_VIEW, Uri.parse(cleanUri)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent.parseUri(cleanUri, Intent.URI_INTENT_SCHEME)
            }

            val comp = intent.component
            val pkg = comp?.packageName ?: intent.`package`
            val cls = comp?.className
            val fullComp = comp?.flattenToShortString() ?: if (pkg != null && cls != null) "$pkg/$cls" else null

            val flagsInt = intent.flags
            val flagsHex = "0x" + Integer.toHexString(flagsInt)

            val extras = intent.extras
            val extrasMap = mutableMapOf<String, Any?>()
            extras?.keySet()?.forEach { key ->
                extrasMap[key] = extras.get(key)
            }
            val categories = intent.categories?.toList() ?: emptyList()

            ParsedIntentInfo(
                rawUri = cleanUri,
                action = intent.action ?: if (isScheme) Intent.ACTION_VIEW else null,
                componentPackage = pkg,
                componentClass = cls,
                fullComponent = fullComp,
                flagsHex = flagsHex,
                dataUri = intent.dataString ?: if (isScheme) cleanUri else null,
                type = intent.type,
                categories = categories,
                extrasMap = extrasMap,
                isScheme = isScheme,
                schemeName = schemeName
            )
        } catch (e: Exception) {
            if (isScheme) {
                try {
                    val androidUri = Uri.parse(cleanUri)
                    ParsedIntentInfo(
                        rawUri = cleanUri,
                        action = Intent.ACTION_VIEW,
                        componentPackage = null,
                        componentClass = null,
                        fullComponent = null,
                        flagsHex = "0x10000000",
                        dataUri = cleanUri,
                        type = null,
                        categories = emptyList(),
                        extrasMap = emptyMap(),
                        isScheme = true,
                        schemeName = androidUri.scheme
                    )
                } catch (ex: Exception) {
                    ParsedIntentInfo(
                        rawUri = uriString,
                        action = null,
                        componentPackage = null,
                        componentClass = null,
                        fullComponent = null,
                        flagsHex = "0x0",
                        dataUri = null,
                        type = null,
                        categories = emptyList(),
                        extrasMap = emptyMap(),
                        isScheme = false,
                        schemeName = null,
                        parseError = "解析错误: ${e.localizedMessage ?: e.message}"
                    )
                }
            } else {
                ParsedIntentInfo(
                    rawUri = uriString,
                    action = null,
                    componentPackage = null,
                    componentClass = null,
                    fullComponent = null,
                    flagsHex = "0x0",
                    dataUri = null,
                    type = null,
                    categories = emptyList(),
                    extrasMap = emptyMap(),
                    isScheme = false,
                    schemeName = null,
                    parseError = "解析错误: ${e.localizedMessage ?: e.message}"
                )
            }
        }

        parsedUriCache.put(uriString, result)
        return result
    }

    suspend fun launch(
        context: Context,
        intentUriString: String,
        forceRoot: Boolean = true,
        tileService: TileService? = null
    ): LaunchResult = withContext(Dispatchers.IO) {
        val parsedInfo = parseIntentUri(intentUriString)
        if (parsedInfo.parseError != null) {
            return@withContext LaunchResult(
                success = false,
                message = parsedInfo.parseError,
                isRootUsed = false,
                logOutput = "无法解析指令: ${parsedInfo.parseError}"
            )
        }

        if (forceRoot) {
            val rootCmd = buildAmStartCommand(parsedInfo, intentUriString)
            Log.d("IntentLauncher", "Executing root command: $rootCmd")
            var shellResult = RootShell.executeCommand(rootCmd)

            // Fallback 1: if primary am start fails and it's a URL scheme or has action+data
            if (!shellResult.success && !parsedInfo.dataUri.isNullOrBlank()) {
                val action = parsedInfo.action ?: "android.intent.action.VIEW"
                val schemeFallbackCmd = "am start -a ${escapeShellArg(action)} -d ${escapeShellArg(parsedInfo.dataUri)} -f 0x10000000"
                Log.w("IntentLauncher", "Primary root command failed. Trying scheme am start fallback: $schemeFallbackCmd")
                shellResult = RootShell.executeCommand(schemeFallbackCmd)
            }

            // Fallback 2: raw URI am start
            if (!shellResult.success) {
                val rawFallbackCmd = "am start ${escapeShellArg(intentUriString.trim())}"
                Log.w("IntentLauncher", "Fallback failed. Trying raw am start: $rawFallbackCmd")
                shellResult = RootShell.executeCommand(rawFallbackCmd)
            }

            if (shellResult.success) {
                return@withContext LaunchResult(
                    success = true,
                    message = "Root 启动成功",
                    isRootUsed = true,
                    logOutput = "命令: $rootCmd\n输出: ${shellResult.output}"
                )
            } else {
                Log.w("IntentLauncher", "Root command failed. Falling back to standard launch...")
                return@withContext tryStandardLaunch(
                    context = context,
                    intentUriString = intentUriString,
                    tileService = tileService,
                    prefixMsg = "Root 脚本执行异常 (${shellResult.error.ifBlank { shellResult.output }})，已自动转为标准模式"
                )
            }
        } else {
            return@withContext tryStandardLaunch(
                context = context,
                intentUriString = intentUriString,
                tileService = tileService
            )
        }
    }

    private fun tryStandardLaunch(
        context: Context,
        intentUriString: String,
        tileService: TileService? = null,
        prefixMsg: String = ""
    ): LaunchResult {
        return try {
            val cleanUri = intentUriString.trim()
            val isScheme = (cleanUri.contains("://") || cleanUri.matches(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:.*"))) &&
                    !cleanUri.startsWith("intent:", ignoreCase = true) &&
                    !cleanUri.contains("#Intent;", ignoreCase = true)

            val intent = if (isScheme) {
                Intent(Intent.ACTION_VIEW, Uri.parse(cleanUri)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent.parseUri(cleanUri, Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            if (tileService != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
                    val pendingIntent = PendingIntent.getActivity(
                        tileService,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    tileService.startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    tileService.startActivityAndCollapse(intent)
                }
            } else {
                context.startActivity(intent)
            }

            LaunchResult(
                success = true,
                message = if (prefixMsg.isBlank()) "标准模式启动成功" else "$prefixMsg -> 标准模式成功",
                isRootUsed = false,
                logOutput = "Standard Intent launched: $intentUriString"
            )
        } catch (e: Exception) {
            LaunchResult(
                success = false,
                message = "启动失败: ${e.localizedMessage ?: e.message}",
                isRootUsed = false,
                logOutput = "Standard Launch Error: ${e.stackTraceToString()}"
            )
        }
    }

    fun buildAmStartCommand(info: ParsedIntentInfo, rawUri: String): String {
        val sb = StringBuilder("am start")

        // Component or Package
        info.fullComponent?.let { comp ->
            sb.append(" -n ").append(escapeShellArg(comp))
        } ?: info.componentPackage?.let { pkg ->
            sb.append(" -p ").append(escapeShellArg(pkg))
        }

        // Action
        info.action?.let { act ->
            sb.append(" -a ").append(escapeShellArg(act))
        }

        // Flags
        if (info.flagsHex != "0x0" && info.flagsHex.isNotBlank()) {
            sb.append(" -f ").append(info.flagsHex)
        } else {
            sb.append(" -f 0x10000000")
        }

        // Data URI
        info.dataUri?.let { data ->
            sb.append(" -d ").append(escapeShellArg(data))
        }

        // Type / MIME
        info.type?.let { mime ->
            sb.append(" -t ").append(escapeShellArg(mime))
        }

        // Categories
        for (category in info.categories) {
            sb.append(" -c ").append(escapeShellArg(category))
        }

        // Extra Bundle Parameters (Strings, Booleans, Ints, Longs, Floats, Arrays, Uris)
        for ((key, value) in info.extrasMap) {
            when (value) {
                is String -> sb.append(" --es ").append(escapeShellArg(key)).append(" ").append(escapeShellArg(value))
                is Boolean -> sb.append(" --ez ").append(escapeShellArg(key)).append(" ").append(value)
                is Int -> sb.append(" --ei ").append(escapeShellArg(key)).append(" ").append(value)
                is Long -> sb.append(" --el ").append(escapeShellArg(key)).append(" ").append(value)
                is Float -> sb.append(" --ef ").append(escapeShellArg(key)).append(" ").append(value)
                is Double -> sb.append(" --ef ").append(escapeShellArg(key)).append(" ").append(value)
                is Uri -> sb.append(" --eu ").append(escapeShellArg(key)).append(" ").append(escapeShellArg(value.toString()))
                is IntArray -> sb.append(" --eia ").append(escapeShellArg(key)).append(" ").append(value.joinToString(","))
                is LongArray -> sb.append(" --ela ").append(escapeShellArg(key)).append(" ").append(value.joinToString(","))
                is FloatArray -> sb.append(" --efa ").append(escapeShellArg(key)).append(" ").append(value.joinToString(","))
                is DoubleArray -> sb.append(" --efa ").append(escapeShellArg(key)).append(" ").append(value.joinToString(","))
                is Array<*> -> {
                    if (value.isArrayOf<String>()) {
                        sb.append(" --esa ").append(escapeShellArg(key)).append(" ").append(escapeShellArg(value.filterIsInstance<String>().joinToString(",")))
                    }
                }
                is ArrayList<*> -> {
                    if (value.all { it is String }) {
                        sb.append(" --esa ").append(escapeShellArg(key)).append(" ").append(escapeShellArg(value.filterIsInstance<String>().joinToString(",")))
                    } else if (value.all { it is Int }) {
                        sb.append(" --eia ").append(escapeShellArg(key)).append(" ").append(value.filterIsInstance<Int>().joinToString(","))
                    }
                }
                null -> sb.append(" --esn ").append(escapeShellArg(key))
                else -> sb.append(" --es ").append(escapeShellArg(key)).append(" ").append(escapeShellArg(value.toString()))
            }
        }

        return sb.toString()
    }
}
