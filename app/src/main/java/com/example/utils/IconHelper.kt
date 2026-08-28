package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
     * Render high-res 192x192 squircle icon bitmap for desktop shortcuts
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
            "scan", "qrcode" -> {
                val d = size * 0.18f
                // Top left bracket
                canvas.drawLine(cx - d, cy - d, cx - d + size * 0.12f, cy - d, strokePaint)
                canvas.drawLine(cx - d, cy - d, cx - d, cy - d + size * 0.12f, strokePaint)
                // Top right bracket
                canvas.drawLine(cx + d, cy - d, cx + d - size * 0.12f, cy - d, strokePaint)
                canvas.drawLine(cx + d, cy - d, cx + d, cy - d + size * 0.12f, strokePaint)
                // Bottom left
                canvas.drawLine(cx - d, cy + d, cx - d + size * 0.12f, cy + d, strokePaint)
                canvas.drawLine(cx - d, cy + d, cx - d, cy + d - size * 0.12f, strokePaint)
                // Bottom right
                canvas.drawLine(cx + d, cy + d, cx + d - size * 0.12f, cy + d, strokePaint)
                canvas.drawLine(cx + d, cy + d, cx + d, cy + d - size * 0.12f, strokePaint)
                // Center scan line
                canvas.drawLine(cx - d + size * 0.04f, cy, cx + d - size * 0.04f, cy, strokePaint)
            }
            "pay" -> {
                val cardRect = RectF(cx - size * 0.24f, cy - size * 0.16f, cx + size * 0.24f, cy + size * 0.16f)
                canvas.drawRoundRect(cardRect, size * 0.04f, size * 0.04f, strokePaint)
                canvas.drawLine(cx - size * 0.24f, cy - size * 0.06f, cx + size * 0.24f, cy - size * 0.06f, strokePaint)
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
            "power" -> {
                canvas.drawArc(
                    RectF(cx - size * 0.18f, cy - size * 0.18f, cx + size * 0.18f, cy + size * 0.18f),
                    135f, 270f, false, strokePaint
                )
                canvas.drawLine(cx, cy - size * 0.22f, cx, cy, strokePaint)
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
            "play" -> {
                val path = Path().apply {
                    moveTo(cx - size * 0.12f, cy - size * 0.18f)
                    lineTo(cx + size * 0.18f, cy)
                    lineTo(cx - size * 0.12f, cy + size * 0.18f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            else -> {
                // Generic elegant monogram or symbol
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

        return bitmap
    }
}
