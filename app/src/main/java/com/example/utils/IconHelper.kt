package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.R
import com.example.data.ShortcutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IconHelper {

    data class IconMeta(
        val key: String,
        val label: String,
        val vector: ImageVector,
        val defaultBgColor: Int = 0xFF0066FF.toInt(),
        val composeColor: Color = Color(defaultBgColor)
    )

    val iconMetas = listOf(
        IconMeta("bolt", "闪电", Icons.Default.Bolt, 0xFF0066FF.toInt()),
        IconMeta("scan", "扫一扫", Icons.Default.QrCodeScanner, 0xFF10B981.toInt()),
        IconMeta("pay", "支付", Icons.Default.Payment, 0xFF0284C7.toInt()),
        IconMeta("qrcode", "二维码", Icons.Default.QrCode, 0xFF6366F1.toInt()),
        IconMeta("check", "打卡", Icons.Default.Check, 0xFF059669.toInt()),
        IconMeta("navigate", "导航", Icons.Default.NearMe, 0xFF2563EB.toInt()),
        IconMeta("link", "链接", Icons.Default.Link, 0xFF8B5CF6.toInt()),
        IconMeta("mic", "录音", Icons.Default.Mic, 0xFFEA580C.toInt()),
        IconMeta("flash", "手电筒", Icons.Default.FlashlightOn, 0xFFEAB308.toInt()),
        IconMeta("terminal", "终端", Icons.Default.Terminal, 0xFF374151.toInt()),
        IconMeta("power", "电源", Icons.Default.PowerSettingsNew, 0xFFDC2626.toInt()),
        IconMeta("settings", "设置", Icons.Default.Settings, 0xFF4B5563.toInt()),
        IconMeta("shield", "安全", Icons.Default.Shield, 0xFF0D9488.toInt()),
        IconMeta("flame", "性能", Icons.Default.LocalFireDepartment, 0xFFF97316.toInt()),
        IconMeta("wrench", "工具", Icons.Default.Build, 0xFF64748B.toInt()),
        IconMeta("star", "收藏", Icons.Default.Star, 0xFFF59E0B.toInt()),
        IconMeta("play", "播放", Icons.Default.PlayArrow, 0xFF84CC16.toInt()),
        IconMeta("camera", "相机", Icons.Default.CameraAlt, 0xFFEC4899.toInt()),
        IconMeta("notifications", "通知", Icons.Default.Notifications, 0xFF06B6D4.toInt()),
        IconMeta("home", "主页", Icons.Default.Home, 0xFF3B82F6.toInt())
    )

    private val iconMetaMap: Map<String, IconMeta> = iconMetas.associateBy { it.key }
    val defaultIconMeta: IconMeta = iconMetas[0]

    val iconList: List<Pair<String, ImageVector>> = iconMetas.map { it.key to it.vector }

    fun getIcon(name: String): ImageVector {
        return iconMetaMap[name]?.vector ?: defaultIconMeta.vector
    }

    fun getIconMeta(name: String): IconMeta {
        return iconMetaMap[name] ?: defaultIconMeta
    }

    /**
     * Center-crop source bitmap into a square of targetSize and apply a modern squircle rounded rectangle.
     * Used for full-color desktop shortcuts.
     */
    fun createSquircleBitmap(
        src: Bitmap,
        cornerRadiusRatio: Float = 0.24f,
        targetSize: Int = 192
    ): Bitmap {
        val minDim = minOf(src.width, src.height)
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rectF = RectF(0f, 0f, targetSize.toFloat(), targetSize.toFloat())
        val cornerRadius = targetSize * cornerRadiusRatio
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val left = (src.width - minDim) / 2
        val top = (src.height - minDim) / 2
        val srcRect = Rect(left, top, left + minDim, top + minDim)
        val destRect = Rect(0, 0, targetSize, targetSize)
        canvas.drawBitmap(src, srcRect, destRect, paint)

        return output
    }

    /**
     * Convert an external image into a monochrome white-on-transparent mask bitmap suitable for Quick Settings Tiles.
     * SystemUI unconditionally applies SRC_IN white/tint coloring to qsTile.icon.
     * By converting background to transparent (alpha = 0) and foreground/contrast/outline to white (alpha > 0),
     * this avoids the image being rendered as a solid white rectangle ("白块").
     */
    fun createTileMonochromeBitmap(src: Bitmap, size: Int = 96): Bitmap {
        val minDim = minOf(src.width, src.height)
        // Center crop src into square
        val squareSrc = if (src.width == src.height) {
            src
        } else {
            Bitmap.createBitmap(src, (src.width - minDim) / 2, (src.height - minDim) / 2, minDim, minDim)
        }
        val scaled = Bitmap.createScaledBitmap(squareSrc, size, size, true)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val totalPixels = size * size
        val pixels = IntArray(totalPixels)
        scaled.getPixels(pixels, 0, size, 0, 0, size, size)

        // 1. Check if the image already has transparent background (e.g. PNG logo)
        var transparentPixels = 0
        for (p in pixels) {
            val a = (p ushr 24) and 0xFF
            if (a < 180) {
                transparentPixels++
            }
        }
        val hasTransparency = transparentPixels > (totalPixels * 0.05f)

        if (hasTransparency) {
            // Retain original transparency, replace color with white
            val outPixels = IntArray(totalPixels)
            for (i in 0 until totalPixels) {
                val a = (pixels[i] ushr 24) and 0xFF
                if (a > 25) {
                    outPixels[i] = (a shl 24) or 0x00FFFFFF
                } else {
                    outPixels[i] = 0
                }
            }
            output.setPixels(outPixels, 0, size, 0, 0, size, size)
            return output
        }

        // 2. Image is opaque: sample border pixels to estimate background luminance
        var borderLumSum = 0L
        var borderCount = 0
        for (x in 0 until size) {
            val pTop = pixels[x]
            val pBottom = pixels[(size - 1) * size + x]
            borderLumSum += (0.299 * ((pTop shr 16) and 0xFF) + 0.587 * ((pTop shr 8) and 0xFF) + 0.114 * (pTop and 0xFF)).toLong()
            borderLumSum += (0.299 * ((pBottom shr 16) and 0xFF) + 0.587 * ((pBottom shr 8) and 0xFF) + 0.114 * (pBottom and 0xFF)).toLong()
            borderCount += 2
        }
        for (y in 1 until size - 1) {
            val pLeft = pixels[y * size]
            val pRight = pixels[y * size + (size - 1)]
            borderLumSum += (0.299 * ((pLeft shr 16) and 0xFF) + 0.587 * ((pLeft shr 8) and 0xFF) + 0.114 * (pLeft and 0xFF)).toLong()
            borderLumSum += (0.299 * ((pRight shr 16) and 0xFF) + 0.587 * ((pRight shr 8) and 0xFF) + 0.114 * (pRight and 0xFF)).toLong()
            borderCount += 2
        }
        val avgBgLum = (borderLumSum / maxOf(1, borderCount)).toInt()

        // Sample center luminance
        var centerLumSum = 0L
        var centerCount = 0
        val centerStart = size / 4
        val centerEnd = size * 3 / 4
        for (y in centerStart until centerEnd) {
            for (x in centerStart until centerEnd) {
                val p = pixels[y * size + x]
                centerLumSum += (0.299 * ((p shr 16) and 0xFF) + 0.587 * ((p shr 8) and 0xFF) + 0.114 * (p and 0xFF)).toLong()
                centerCount++
            }
        }
        val avgCenterLum = (centerLumSum / maxOf(1, centerCount)).toInt()

        val isDarkOnLight = avgBgLum > avgCenterLum
        val outPixels = IntArray(totalPixels)
        val threshold = 30

        for (y in 0 until size) {
            for (x in 0 until size) {
                val idx = y * size + x
                val p = pixels[idx]
                val lum = (0.299 * ((p shr 16) and 0xFF) + 0.587 * ((p shr 8) and 0xFF) + 0.114 * (p and 0xFF)).toInt()

                val diff = if (isDarkOnLight) (avgBgLum - lum) else (lum - avgBgLum)
                val alpha = if (diff > threshold) {
                    val normalized = ((diff - threshold).toFloat() / (255 - threshold) * 2.2f).coerceIn(0f, 1f)
                    (normalized * 255).toInt()
                } else {
                    0
                }

                if (alpha > 0) {
                    outPixels[idx] = (alpha shl 24) or 0x00FFFFFF
                } else {
                    outPixels[idx] = 0
                }
            }
        }
        output.setPixels(outPixels, 0, size, 0, 0, size, size)

        // Overlay a refined squircle stroke outline to ensure a clear icon frame
        val canvas = Canvas(output)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.05f
            alpha = 220
        }
        val inset = size * 0.06f
        val rectF = RectF(inset, inset, size - inset, size - inset)
        val radius = (size - 2 * inset) * 0.24f
        canvas.drawRoundRect(rectF, radius, radius, strokePaint)

        return output
    }

    /**
     * Load image from URI/Path/File using Coil and crop to squircle bitmap for desktop shortcuts.
     */
    suspend fun loadCustomIconBitmap(
        context: Context,
        data: Any,
        size: Int = 192,
        cornerRadiusRatio: Float = 0.24f
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(data)
                    .size(size, size)
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(request)
                if (result is SuccessResult) {
                    val rawBitmap = result.drawable.toBitmap(size, size, Bitmap.Config.ARGB_8888)
                    createSquircleBitmap(rawBitmap, cornerRadiusRatio, size)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Load custom image with Coil and process it into a Quick Settings monochrome mask.
     */
    suspend fun loadCustomTileBitmap(
        context: Context,
        data: Any,
        size: Int = 96
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(data)
                    .size(size, size)
                    .allowHardware(false)
                    .build()
                val result = context.imageLoader.execute(request)
                if (result is SuccessResult) {
                    val rawBitmap = result.drawable.toBitmap(size, size, Bitmap.Config.ARGB_8888)
                    createTileMonochromeBitmap(rawBitmap, size)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Creates an Icon instance suitable for Android Quick Settings Tiles (qsTile.icon).
     * Follows the shortcut settings (custom image with monochrome mask, or built-in icon).
     */
    suspend fun getTileIcon(context: Context, shortcut: ShortcutEntity?): Icon {
        if (shortcut == null) {
            return Icon.createWithResource(context, R.drawable.ic_launcher_foreground)
        }

        if (!shortcut.customIconUri.isNullOrBlank()) {
            val customTileBitmap = loadCustomTileBitmap(context, shortcut.customIconUri, size = 96)
            if (customTileBitmap != null) {
                return Icon.createWithBitmap(customTileBitmap)
            }
        }

        // Built-in icon: Render clean white glyph on transparent background
        val glyphBitmap = renderTileGlyphBitmap(shortcut.iconName, size = 96)
        return Icon.createWithBitmap(glyphBitmap)
    }

    /**
     * Render glyph on transparent background (ideal for Quick Settings tiles where the system applies tint).
     */
    fun renderTileGlyphBitmap(iconName: String, size: Int = 96): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.08f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        drawIconGlyph(canvas, iconName, size, iconPaint, strokePaint)
        return bitmap
    }

    /**
     * Render high-res squircle icon bitmap for desktop shortcuts (with colored background).
     */
    fun renderIconToBitmap(
        context: Context,
        iconName: String,
        customBgColor: Int? = null,
        size: Int = 192
    ): Bitmap {
        val meta = getIconMeta(iconName)
        val bgColor = customBgColor ?: meta.defaultBgColor

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        // Draw modern HyperOS squircle (rounded rect with 24% radius)
        val rectF = RectF(0f, 0f, size.toFloat(), size.toFloat())
        val cornerRadius = size * 0.24f
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, bgPaint)

        // Draw inner white icon / symbol
        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = size * 0.07f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        drawIconGlyph(canvas, iconName, size, iconPaint, strokePaint)

        return bitmap
    }

    /**
     * Explicitly draws every one of the 20 supported built-in icons.
     * None of the 20 keys will fall through to the fallback branch.
     */
    fun drawIconGlyph(
        canvas: Canvas,
        iconName: String,
        size: Int,
        iconPaint: Paint,
        strokePaint: Paint
    ) {
        val cx = size / 2f
        val cy = size / 2f

        when (iconName) {
            "bolt" -> {
                val path = Path().apply {
                    moveTo(cx + size * 0.05f, cy - size * 0.28f)
                    lineTo(cx - size * 0.18f, cy + size * 0.02f)
                    lineTo(cx - size * 0.02f, cy + size * 0.02f)
                    lineTo(cx - size * 0.08f, cy + size * 0.30f)
                    lineTo(cx + size * 0.20f, cy - size * 0.02f)
                    lineTo(cx + size * 0.03f, cy - size * 0.02f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            "check" -> {
                val path = Path().apply {
                    moveTo(cx - size * 0.20f, cy + size * 0.02f)
                    lineTo(cx - size * 0.06f, cy + size * 0.16f)
                    lineTo(cx + size * 0.22f, cy - size * 0.14f)
                }
                canvas.drawPath(path, strokePaint)
            }
            "scan" -> {
                val d = size * 0.18f
                // 4 corners of scanner
                canvas.drawLine(cx - d, cy - d, cx - d + size * 0.10f, cy - d, strokePaint)
                canvas.drawLine(cx - d, cy - d, cx - d, cy - d + size * 0.10f, strokePaint)
                canvas.drawLine(cx + d, cy - d, cx + d - size * 0.10f, cy - d, strokePaint)
                canvas.drawLine(cx + d, cy - d, cx + d, cy - d + size * 0.10f, strokePaint)
                canvas.drawLine(cx - d, cy + d, cx - d + size * 0.10f, cy + d, strokePaint)
                canvas.drawLine(cx - d, cy + d, cx - d, cy + d - size * 0.10f, strokePaint)
                canvas.drawLine(cx + d, cy + d, cx + d - size * 0.10f, cy + d, strokePaint)
                canvas.drawLine(cx + d, cy + d, cx + d, cy + d - size * 0.10f, strokePaint)
                // Center scan line
                canvas.drawLine(cx - d + size * 0.04f, cy, cx + d - size * 0.04f, cy, strokePaint)
            }
            "qrcode" -> {
                val d = size * 0.20f
                val box = size * 0.14f
                // Top-left square
                canvas.drawRect(cx - d, cy - d, cx - d + box, cy - d + box, strokePaint)
                canvas.drawRect(cx - d + size * 0.04f, cy - d + size * 0.04f, cx - d + box - size * 0.04f, cy - d + box - size * 0.04f, iconPaint)
                // Top-right square
                canvas.drawRect(cx + d - box, cy - d, cx + d, cy - d + box, strokePaint)
                canvas.drawRect(cx + d - box + size * 0.04f, cy - d + size * 0.04f, cx + d - size * 0.04f, cy - d + box - size * 0.04f, iconPaint)
                // Bottom-left square
                canvas.drawRect(cx - d, cy + d - box, cx - d + box, cy + d, strokePaint)
                canvas.drawRect(cx - d + size * 0.04f, cy + d - box + size * 0.04f, cx - d + box - size * 0.04f, cy + d, iconPaint)
                // Center / bottom-right details
                canvas.drawCircle(cx + size * 0.08f, cy + size * 0.08f, size * 0.035f, iconPaint)
                canvas.drawCircle(cx, cy, size * 0.035f, iconPaint)
            }
            "pay" -> {
                val cardRect = RectF(cx - size * 0.24f, cy - size * 0.16f, cx + size * 0.24f, cy + size * 0.16f)
                canvas.drawRoundRect(cardRect, size * 0.04f, size * 0.04f, strokePaint)
                canvas.drawLine(cx - size * 0.24f, cy - size * 0.05f, cx + size * 0.24f, cy - size * 0.05f, strokePaint)
                canvas.drawRect(cx - size * 0.16f, cy + size * 0.04f, cx - size * 0.06f, cy + size * 0.10f, iconPaint)
            }
            "navigate" -> {
                val path = Path().apply {
                    moveTo(cx - size * 0.16f, cy + size * 0.20f)
                    lineTo(cx, cy - size * 0.22f)
                    lineTo(cx + size * 0.16f, cy + size * 0.20f)
                    lineTo(cx, cy + size * 0.08f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            "link" -> {
                canvas.drawCircle(cx - size * 0.10f, cy, size * 0.09f, strokePaint)
                canvas.drawCircle(cx + size * 0.10f, cy, size * 0.09f, strokePaint)
                canvas.drawLine(cx - size * 0.04f, cy, cx + size * 0.04f, cy, strokePaint)
            }
            "mic" -> {
                // Mic body capsule
                val micRect = RectF(cx - size * 0.07f, cy - size * 0.22f, cx + size * 0.07f, cy + size * 0.04f)
                canvas.drawRoundRect(micRect, size * 0.07f, size * 0.07f, iconPaint)
                // Cradle arc
                val cradleRect = RectF(cx - size * 0.14f, cy - size * 0.10f, cx + size * 0.14f, cy + size * 0.10f)
                canvas.drawArc(cradleRect, 0f, 180f, false, strokePaint)
                // Stand stem and base
                canvas.drawLine(cx, cy + size * 0.10f, cx, cy + size * 0.22f, strokePaint)
                canvas.drawLine(cx - size * 0.10f, cy + size * 0.22f, cx + size * 0.10f, cy + size * 0.22f, strokePaint)
            }
            "flash" -> {
                // Flashlight body
                val path = Path().apply {
                    moveTo(cx - size * 0.14f, cy - size * 0.20f)
                    lineTo(cx + size * 0.14f, cy - size * 0.20f)
                    lineTo(cx + size * 0.07f, cy - size * 0.06f)
                    lineTo(cx + size * 0.07f, cy + size * 0.22f)
                    lineTo(cx - size * 0.07f, cy + size * 0.22f)
                    lineTo(cx - size * 0.07f, cy - size * 0.06f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
                // Light rays
                canvas.drawLine(cx, cy - size * 0.24f, cx, cy - size * 0.29f, strokePaint)
                canvas.drawLine(cx - size * 0.14f, cy - size * 0.23f, cx - size * 0.20f, cy - size * 0.28f, strokePaint)
                canvas.drawLine(cx + size * 0.14f, cy - size * 0.23f, cx + size * 0.20f, cy - size * 0.28f, strokePaint)
            }
            "terminal" -> {
                // Shell chevron >
                val path = Path().apply {
                    moveTo(cx - size * 0.16f, cy - size * 0.14f)
                    lineTo(cx - size * 0.02f, cy)
                    lineTo(cx - size * 0.16f, cy + size * 0.14f)
                }
                canvas.drawPath(path, strokePaint)
                // Underscore _
                canvas.drawLine(cx + size * 0.03f, cy + size * 0.14f, cx + size * 0.18f, cy + size * 0.14f, strokePaint)
            }
            "power" -> {
                canvas.drawArc(
                    RectF(cx - size * 0.18f, cy - size * 0.18f, cx + size * 0.18f, cy + size * 0.18f),
                    135f, 270f, false, strokePaint
                )
                canvas.drawLine(cx, cy - size * 0.22f, cx, cy, strokePaint)
            }
            "settings" -> {
                // Gear inner hole and outer ring
                canvas.drawCircle(cx, cy, size * 0.18f, strokePaint)
                canvas.drawCircle(cx, cy, size * 0.07f, iconPaint)
                // 4 cog spokes
                canvas.drawLine(cx, cy - size * 0.24f, cx, cy - size * 0.18f, strokePaint)
                canvas.drawLine(cx, cy + size * 0.18f, cx, cy + size * 0.24f, strokePaint)
                canvas.drawLine(cx - size * 0.24f, cy, cx - size * 0.18f, cy, strokePaint)
                canvas.drawLine(cx + size * 0.18f, cy, cx + size * 0.24f, cy, strokePaint)
                val d = size * 0.14f
                val d2 = size * 0.19f
                canvas.drawLine(cx - d2, cy - d2, cx - d, cy - d, strokePaint)
                canvas.drawLine(cx + d, cy + d, cx + d2, cy + d2, strokePaint)
                canvas.drawLine(cx + d, cy - d, cx + d2, cy - d2, strokePaint)
                canvas.drawLine(cx - d2, cy + d2, cx - d, cy + d, strokePaint)
            }
            "shield" -> {
                val path = Path().apply {
                    moveTo(cx, cy - size * 0.22f)
                    lineTo(cx + size * 0.18f, cy - size * 0.12f)
                    lineTo(cx + size * 0.18f, cy + size * 0.04f)
                    quadTo(cx, cy + size * 0.24f, cx, cy + size * 0.24f)
                    quadTo(cx - size * 0.18f, cy + size * 0.04f, cx - size * 0.18f, cy + size * 0.04f)
                    lineTo(cx - size * 0.18f, cy - size * 0.12f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            "flame" -> {
                val path = Path().apply {
                    moveTo(cx, cy - size * 0.24f)
                    quadTo(cx + size * 0.16f, cy - size * 0.08f, cx + size * 0.16f, cy + size * 0.08f)
                    quadTo(cx + size * 0.16f, cy + size * 0.24f, cx, cy + size * 0.24f)
                    quadTo(cx - size * 0.16f, cy + size * 0.24f, cx - size * 0.16f, cy + size * 0.08f)
                    quadTo(cx - size * 0.16f, cy - size * 0.08f, cx, cy - size * 0.24f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            "wrench" -> {
                // Diagonal handle
                canvas.drawLine(cx - size * 0.14f, cy + size * 0.16f, cx + size * 0.08f, cy - size * 0.06f, strokePaint)
                // Open wrench head
                val headPath = Path().apply {
                    moveTo(cx + size * 0.04f, cy - size * 0.16f)
                    lineTo(cx + size * 0.18f, cy - size * 0.22f)
                    lineTo(cx + size * 0.24f, cy - size * 0.08f)
                    lineTo(cx + size * 0.14f, cy - size * 0.02f)
                }
                canvas.drawPath(headPath, strokePaint)
            }
            "star" -> {
                val path = Path()
                val outer = size * 0.25f
                val inner = size * 0.11f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outer else inner
                    val angle = Math.toRadians((i * 36 - 90).toDouble())
                    val x = cx + (r * Math.cos(angle)).toFloat()
                    val y = cy + (r * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, iconPaint)
            }
            "play" -> {
                val path = Path().apply {
                    moveTo(cx - size * 0.12f, cy - size * 0.18f)
                    lineTo(cx + size * 0.18f, cy)
                    lineTo(cx - size * 0.12f, cy + size * 0.18f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            "camera" -> {
                val body = RectF(cx - size * 0.22f, cy - size * 0.10f, cx + size * 0.22f, cy + size * 0.18f)
                canvas.drawRoundRect(body, size * 0.04f, size * 0.04f, strokePaint)
                val bump = RectF(cx - size * 0.10f, cy - size * 0.18f, cx + size * 0.02f, cy - size * 0.10f)
                canvas.drawRoundRect(bump, size * 0.02f, size * 0.02f, strokePaint)
                canvas.drawCircle(cx, cy + size * 0.04f, size * 0.08f, strokePaint)
            }
            "notifications" -> {
                val path = Path().apply {
                    moveTo(cx, cy - size * 0.20f)
                    quadTo(cx + size * 0.16f, cy - size * 0.10f, cx + size * 0.16f, cy + size * 0.10f)
                    lineTo(cx + size * 0.20f, cy + size * 0.14f)
                    lineTo(cx - size * 0.20f, cy + size * 0.14f)
                    lineTo(cx - size * 0.16f, cy + size * 0.10f)
                    quadTo(cx - size * 0.16f, cy - size * 0.10f, cx, cy - size * 0.20f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
                canvas.drawCircle(cx, cy + size * 0.18f, size * 0.035f, iconPaint)
            }
            "home" -> {
                val roof = Path().apply {
                    moveTo(cx, cy - size * 0.22f)
                    lineTo(cx + size * 0.22f, cy - size * 0.02f)
                    lineTo(cx - size * 0.22f, cy - size * 0.02f)
                    close()
                }
                canvas.drawPath(roof, iconPaint)
                val houseRect = RectF(cx - size * 0.16f, cy - size * 0.02f, cx + size * 0.16f, cy + size * 0.20f)
                canvas.drawRect(houseRect, strokePaint)
            }
            else -> {
                // Fallback monogram
                val path = Path().apply {
                    moveTo(cx + size * 0.05f, cy - size * 0.25f)
                    lineTo(cx - size * 0.16f, cy + size * 0.02f)
                    lineTo(cx, cy + size * 0.02f)
                    lineTo(cx - size * 0.05f, cy + size * 0.26f)
                    lineTo(cx + size * 0.18f, cy - size * 0.01f)
                    lineTo(cx + size * 0.03f, cy - size * 0.01f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
        }
    }
}
