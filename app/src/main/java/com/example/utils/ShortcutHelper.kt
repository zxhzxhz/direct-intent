package com.example.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.ShortcutLauncherActivity
import com.example.data.ShortcutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShortcutHelper {

    fun isPinShortcutSupported(context: Context): Boolean {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context)
    }

    fun openAppPermissionSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开设置页面", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Loads image via Coil / fallback, crops to squircle desktop icon, and saves permanently in internal storage.
     */
    suspend fun saveCustomImage(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Try loading & cropping with Coil first
                var bitmap = IconHelper.loadCustomIconBitmap(context, uri, size = 512, cornerRadiusRatio = 0.24f)

                // 2. Fallback if Coil failed for certain local provider content URIs
                if (bitmap == null) {
                    val rawBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        try {
                            android.graphics.ImageDecoder.decodeBitmap(
                                android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                            ) { decoder, _, _ -> decoder.isMutableRequired = true }
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        try { MediaStore.Images.Media.getBitmap(context.contentResolver, uri) } catch (e: Exception) { null }
                    }
                    if (rawBitmap != null) {
                        bitmap = IconHelper.createSquircleBitmap(rawBitmap, cornerRadiusRatio = 0.24f, targetSize = 512)
                    }
                }

                if (bitmap == null) return@withContext null

                val dir = File(context.filesDir, "custom_icons")
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, "icon_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun createRoundedBitmap(src: Bitmap, cornerRadiusRatio: Float = 0.24f): Bitmap {
        return IconHelper.createSquircleBitmap(src, cornerRadiusRatio, minOf(src.width, src.height))
    }

    fun pinShortcutToHomeScreen(
        context: Context,
        shortcut: ShortcutEntity,
        customImageBitmap: Bitmap? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        try {
            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                onResult(false, "当前系统启动器不支持添加桌面快捷方式")
                return
            }

            val launchIntent = Intent(context, ShortcutLauncherActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra(ShortcutLauncherActivity.EXTRA_SHORTCUT_ID, shortcut.id)
                putExtra(ShortcutLauncherActivity.EXTRA_ALIAS, shortcut.alias)
                putExtra(ShortcutLauncherActivity.EXTRA_INTENT_URI, shortcut.intentUri)
                putExtra(ShortcutLauncherActivity.EXTRA_USE_ROOT, shortcut.useRoot)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val iconCompat = if (customImageBitmap != null) {
                val rounded = createRoundedBitmap(customImageBitmap, 0.24f)
                IconCompat.createWithBitmap(rounded)
            } else if (!shortcut.customIconUri.isNullOrBlank()) {
                val file = File(shortcut.customIconUri)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    IconCompat.createWithBitmap(bmp ?: IconHelper.renderIconToBitmap(context, shortcut.iconName))
                } else {
                    val vectorBitmap = IconHelper.renderIconToBitmap(context = context, iconName = shortcut.iconName)
                    IconCompat.createWithBitmap(vectorBitmap)
                }
            } else {
                val vectorBitmap = IconHelper.renderIconToBitmap(
                    context = context,
                    iconName = shortcut.iconName
                )
                IconCompat.createWithBitmap(vectorBitmap)
            }

            val shortcutId = "shortcut_pin_${shortcut.id}_${System.currentTimeMillis()}"
            val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
                .setShortLabel(shortcut.alias.take(10))
                .setLongLabel(shortcut.alias)
                .setIcon(iconCompat)
                .setIntent(launchIntent)
                .setAlwaysBadged()
                .build()

            val success = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
            if (success) {
                onResult(true, "已请求添加至桌面，请在系统弹窗中允许或前往桌面查看")
            } else {
                onResult(false, "添加请求未被启动器接收，请检查桌面快捷方式权限")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, "添加桌面失败: ${e.localizedMessage ?: e.message}")
        }
    }
}
