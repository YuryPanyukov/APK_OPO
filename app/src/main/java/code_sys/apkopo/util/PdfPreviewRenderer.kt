package code_sys.apkopo.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import java.io.File

/**
 * Растеризует страницы PDF в [Bitmap] для предпросмотра на экране.
 * Использует встроенный [PdfRenderer] (доступен с API 21).
 */
object PdfPreviewRenderer {

    private const val DEFAULT_MAX_WIDTH_PX = 1200
    private const val DEFAULT_MAX_PAGES = 50

    /**
     * Рендерит [file] в список растровых страниц.
     * Ширина каждой страницы ограничена [maxWidthPx] (минимум 1x),
     * число страниц — [maxPages].
     */
    fun render(
        file: File,
        maxWidthPx: Int = DEFAULT_MAX_WIDTH_PX,
        maxPages: Int = DEFAULT_MAX_PAGES
    ): List<Bitmap> {
        if (!file.exists()) return emptyList()
        val pages = mutableListOf<Bitmap>()
        try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val count = minOf(renderer.pageCount, maxPages)
                    for (index in 0 until count) {
                        val page = renderer.openPage(index)
                        try {
                            val scale = if (page.width > maxWidthPx) {
                                maxWidthPx.toFloat() / page.width
                            } else {
                                1f
                            }
                            val width = (page.width * scale).toInt().coerceAtLeast(1)
                            val height = (page.height * scale).toInt().coerceAtLeast(1)
                            val bitmap = createBitmap(
                                width, height, Bitmap.Config.ARGB_8888
                            )
                            // Фон белый: PDF-страницы прозрачны, иначе будет «шум».
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            pages += bitmap
                        } finally {
                            page.close()
                        }
                    }
                } finally {
                    renderer.close()
                }
            }
        } catch (e: Exception) {
            pages.forEach { if (!it.isRecycled) it.recycle() }
            return emptyList()
        }
        return pages
    }
}
