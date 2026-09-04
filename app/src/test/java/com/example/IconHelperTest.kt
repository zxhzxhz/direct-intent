package com.example

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tiles.QuickTile10Service
import com.example.tiles.QuickTile1Service
import com.example.utils.IconHelper
import com.example.utils.TileHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IconHelperTest {

    @Test
    fun testTileHelperClassesCount() {
        assertEquals(10, TileHelper.tileClasses.size)
        assertEquals(QuickTile1Service::class.java, TileHelper.tileClasses[0])
        assertEquals(QuickTile10Service::class.java, TileHelper.tileClasses[9])
    }

    @Test
    fun testCreateSquircleBitmap() {
        val src = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val squircle = IconHelper.createSquircleBitmap(src, cornerRadiusRatio = 0.24f, targetSize = 128)
        assertNotNull(squircle)
        assertEquals(128, squircle.width)
        assertEquals(128, squircle.height)
    }

    @Test
    fun testRenderAll20BuiltInIcons() {
        assertEquals(20, IconHelper.iconMetas.size)
        for (meta in IconHelper.iconMetas) {
            val glyph = IconHelper.renderTileGlyphBitmap(meta.key, size = 96)
            assertNotNull("Glyph for ${meta.key} should not be null", glyph)
            assertEquals(96, glyph.width)
            assertEquals(96, glyph.height)

            // Verify desktop icon bitmap also renders cleanly
            val desktopBmp = IconHelper.renderIconToBitmap(
                context = org.robolectric.RuntimeEnvironment.getApplication(),
                iconName = meta.key,
                size = 192
            )
            assertNotNull("Desktop icon for ${meta.key} should not be null", desktopBmp)
            assertEquals(192, desktopBmp.width)
        }
    }

    @Test
    fun testCreateTileMonochromeBitmap_TransparentImage() {
        // Transparent PNG with an opaque center dot
        val src = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        val inPixels = IntArray(96 * 96)
        for (y in 0 until 96) {
            for (x in 0 until 96) {
                val dx = x - 48
                val dy = y - 48
                if (dx * dx + dy * dy <= 20 * 20) {
                    inPixels[y * 96 + x] = Color.RED
                } else {
                    inPixels[y * 96 + x] = Color.TRANSPARENT
                }
            }
        }
        src.setPixels(inPixels, 0, 96, 0, 0, 96, 96)

        val tileBmp = IconHelper.createTileMonochromeBitmap(src, size = 96)
        assertNotNull(tileBmp)
        assertEquals(96, tileBmp.width)

        // Verify corner is transparent
        val cornerPixel = tileBmp.getPixel(2, 2)
        val cornerAlpha = (cornerPixel ushr 24) and 0xFF
        assertEquals(0, cornerAlpha)

        // Verify center circle is visible
        val centerPixel = tileBmp.getPixel(48, 48)
        val centerAlpha = (centerPixel ushr 24) and 0xFF
        assertTrue("Center pixel should be visible, alpha=$centerAlpha", centerAlpha > 0)
    }

    @Test
    fun testCreateTileMonochromeBitmap_OpaqueImageNeverSolidBlock() {
        // Opaque bitmap (blue background with white center logo)
        val src = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        val inPixels = IntArray(96 * 96)
        for (y in 0 until 96) {
            for (x in 0 until 96) {
                val dx = x - 48
                val dy = y - 48
                if (dx * dx + dy * dy <= 22 * 22) {
                    inPixels[y * 96 + x] = Color.WHITE
                } else {
                    inPixels[y * 96 + x] = Color.BLUE
                }
            }
        }
        src.setPixels(inPixels, 0, 96, 0, 0, 96, 96)

        val tileBmp = IconHelper.createTileMonochromeBitmap(src, size = 96)
        assertNotNull(tileBmp)

        var transparentCount = 0
        var visibleCount = 0
        for (y in 0 until 96) {
            for (x in 0 until 96) {
                val alpha = (tileBmp.getPixel(x, y) ushr 24) and 0xFF
                if (alpha == 0) transparentCount++
                else visibleCount++
            }
        }

        // Must NOT be a solid block (must have significant transparent pixels!)
        assertTrue("Tile icon must have transparent pixels to avoid solid white block. transparentCount=$transparentCount", transparentCount > 500)
        assertTrue("Tile icon must have visible pixels for the glyph/outline. visibleCount=$visibleCount", visibleCount > 500)
    }
}
