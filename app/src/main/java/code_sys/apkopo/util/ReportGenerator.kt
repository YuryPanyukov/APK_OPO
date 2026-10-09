package code_sys.apkopo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.util.Base64
import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark
import code_sys.apkopo.domain.repository.ReportBuilder
import code_sys.apkopo.domain.repository.ReportFormat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/** Данные для отчёта: замечание + его фото. */
data class RemarkWithPhotos(val remark: Remark, val photos: List<Photo>)

/**
 * Генератор отчёта: PDF на базе встроенного [PdfDocument] и самодостаточный
 * HTML (фото встраиваются как base64-миниатюры, файл можно открыть в браузере).
 */
class ReportGenerator(private val context: Context) : ReportBuilder {

    override fun build(
        commission: Commission,
        remarks: List<RemarkWithPhotos>,
        format: ReportFormat
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val out = File(
            reportsDir,
            "report_${commission.id}_${System.currentTimeMillis()}.${format.extension}"
        )
        when (format) {
            ReportFormat.PDF -> buildPdf(commission, remarks, out)
            ReportFormat.HTML -> buildHtml(commission, remarks, out)
        }
        return out
    }

    // ------------------------------------------------------------------
    // PDF
    // ------------------------------------------------------------------

    // A4 при 72 dpi (в пунктах)
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f
    private val contentWidth = pageWidth - 2 * margin

    private val titlePaint = Paint().apply {
        color = Color.BLACK
        textSize = 18f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val headerPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val bodyPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 11f
        isAntiAlias = true
    }
    private val captionPaint = Paint().apply {
        color = Color.GRAY
        textSize = 9f
        isAntiAlias = true
    }

    private fun buildPdf(
        commission: Commission,
        remarks: List<RemarkWithPhotos>,
        out: File
    ) {
        val document = PdfDocument()
        var pageNumber = 1
        var page = document.startPage(newPageInfo(pageNumber))
        var canvas = page.canvas
        var y = margin

        fun newPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(newPageInfo(pageNumber))
            canvas = page.canvas
            y = margin
        }

        /** Печатает текст с переносами; возвращает новое значение y. */
        fun drawWrapped(text: String, paint: Paint, startY: Float, indent: Float = 0f): Float {
            var curY = startY
            var lineStart = 0
            val maxWidth = contentWidth - indent
            while (lineStart < text.length) {
                val remaining = text.substring(lineStart)
                val broken = paint.breakText(remaining, true, maxWidth, null)
                // Не рвём слово, если оно помещается целиком в начале строки
                var take = broken
                if (lineStart + broken < text.length) {
                    val spaceIdx = remaining.lastIndexOf(' ', broken)
                    if (spaceIdx > 0) take = spaceIdx
                }
                val line = remaining.take(take).trim()
                if (curY + paint.textSize > pageHeight - margin) {
                    newPage()
                    curY = margin
                }
                canvas.drawText(line, margin + indent, curY, paint)
                curY += paint.textSize + 4f
                lineStart += take
                while (lineStart < text.length && text[lineStart] == ' ') lineStart++
            }
            return curY
        }

        fun ensureSpace(height: Float): Float {
            if (y + height > pageHeight - margin) {
                newPage()
            }
            return y
        }

        // ---------- Шапка ----------
        y = drawWrapped("Комиссия: ${commission.title}", titlePaint, y + 10f)
        y = drawWrapped("Дата создания: ${PhotoStorage.formatTime(commission.date)}", bodyPaint, y)
        y = drawWrapped("ID: ${commission.id}  •  Всего замечаний: ${remarks.size}", bodyPaint, y + 4f)
        y += 8f
        canvas.drawLine(margin, y, pageWidth - margin, y, headerPaint)
        y += 16f

        // ---------- Замечания ----------
        remarks.forEachIndexed { index, item ->
            val r = item.remark
            y = ensureSpace(60f)
            y = drawWrapped("${index + 1}. ${r.objectName} — ${r.remarkType}", headerPaint, y)
            y = drawWrapped("Место: ${r.location}", bodyPaint, y)
            y = drawWrapped("Описание: ${r.description}", bodyPaint, y)
            y = drawWrapped(
                "GPS замечания: ${formatCoord(r.remarkLat, r.remarkLng)} " +
                    "(${PhotoStorage.formatTime(r.remarkTime)})",
                bodyPaint, y
            )
            y = drawWrapped(
                "GPS пользователя: ${formatCoord(r.userLat, r.userLng)} " +
                    "(${PhotoStorage.formatTime(r.userTime)})",
                bodyPaint, y
            )

            if (item.photos.isEmpty()) {
                y = drawWrapped("Фото: нет", captionPaint, y)
            } else {
                item.photos.forEach { photo ->
                    val caption = "Фото — съёмка: ${PhotoStorage.formatTime(photo.photoTime)}, " +
                        "GPS: ${formatCoord(photo.photoLat, photo.photoLng)}"
                    val bitmap = decodeScaled(photo.filePath) ?: return@forEach
                    val targetH = 140f * bitmap.height / bitmap.width

                    y = ensureSpace(targetH + captionPaint.textSize * 2 + 12f)
                    canvas.drawBitmap(bitmap, margin, y, null)
                    y += targetH + 4f
                    y = drawWrapped(caption, captionPaint, y)
                    y += 8f
                    bitmap.recycle()
                }
            }
            y += 14f
            if (index < remarks.lastIndex) {
                canvas.drawLine(margin, y, pageWidth - margin, y, bodyPaint)
                y += 14f
            }
        }

        // ---------- Итог ----------
        y = ensureSpace(70f)
        y += 6f
        canvas.drawLine(margin, y, pageWidth - margin, y, headerPaint)
        y += 18f
        val totalPhotos = remarks.sumOf { it.photos.size }
        val types = remarks.map { it.remark.remarkType }.distinct()
        y = drawWrapped("Итого замечаний: ${remarks.size}", headerPaint, y)
        y = drawWrapped("Всего фото: $totalPhotos", bodyPaint, y)
        y = drawWrapped("Типы замечаний: ${if (types.isEmpty()) "—" else types.joinToString()}", bodyPaint, y)

        document.finishPage(page)
        FileOutputStream(out).use { document.writeTo(it) }
        document.close()
    }

    // ------------------------------------------------------------------
    // HTML
    // ------------------------------------------------------------------

    private fun buildHtml(
        commission: Commission,
        remarks: List<RemarkWithPhotos>,
        out: File
    ) {
        val totalPhotos = remarks.sumOf { it.photos.size }
        val types = remarks.map { it.remark.remarkType }.distinct()

        val html = buildString {
            append("<!DOCTYPE html>\n")
            append("<html lang=\"ru\">\n<head>\n")
            append("<meta charset=\"utf-8\">\n")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
            append("<title>Отчёт: ${escape(commission.title)}</title>\n")
            append("<style>\n").append(CSS).append("</style>\n")
            append("</head>\n<body>\n")

            append("<header>\n")
            append("<h1>Комиссия: ${escape(commission.title)}</h1>\n")
            append("<p class=\"meta\">Дата создания: ${escape(PhotoStorage.formatTime(commission.date))} • ")
            append("ID: ${commission.id} • Всего замечаний: ${remarks.size}</p>\n")
            append("</header>\n")

            if (remarks.isEmpty()) {
                append("<p>Замечаний нет.</p>\n")
            } else {
                remarks.forEachIndexed { index, item ->
                    val r = item.remark
                    append("<section class=\"remark\">\n")
                    append("<h2>${index + 1}. ${escape(r.objectName)} — ${escape(r.remarkType)}</h2>\n")
                    append("<table class=\"fields\">\n")
                    appendField("Место", r.location)
                    appendField("Описание", r.description)
                    appendField(
                        "GPS замечания",
                        "${formatCoord(r.remarkLat, r.remarkLng)} (${PhotoStorage.formatTime(r.remarkTime)})"
                    )
                    appendField(
                        "GPS пользователя",
                        "${formatCoord(r.userLat, r.userLng)} (${PhotoStorage.formatTime(r.userTime)})"
                    )
                    append("</table>\n")

                    if (item.photos.isEmpty()) {
                        append("<p class=\"muted\">Фото: нет</p>\n")
                    } else {
                        append("<div class=\"photos\">\n")
                        item.photos.forEach { photo ->
                            val data = encodeThumbnail(photo.filePath)
                            if (data != null) {
                                append("<figure>\n")
                                append("<img src=\"data:image/jpeg;base64,$data\" alt=\"Фото\">\n")
                                append("<figcaption>Съёмка: ${escape(PhotoStorage.formatTime(photo.photoTime))}<br>")
                                append("GPS: ${escape(formatCoord(photo.photoLat, photo.photoLng))}</figcaption>\n")
                                append("</figure>\n")
                            }
                        }
                        append("</div>\n")
                    }
                    append("</section>\n")
                }
            }

            append("<footer>\n")
            append("<h2>Итого</h2>\n")
            append("<p>Итого замечаний: ${remarks.size}<br>")
            append("Всего фото: $totalPhotos<br>")
            append("Типы замечаний: ${escape(if (types.isEmpty()) "—" else types.joinToString())}</p>\n")
            append("</footer>\n")
            append("</body>\n</html>\n")
        }

        out.writeText(html, Charsets.UTF_8)
    }

    private fun StringBuilder.appendField(label: String, value: String) {
        if (value.isBlank()) return
        append("<tr><th>").append(escape(label)).append("</th>")
        append("<td>").append(escape(value)).append("</td></tr>\n")
    }

    /** Миниатюра фото в base64 (JPEG), чтобы HTML был самодостаточным. */
    private fun encodeThumbnail(path: String): String? {
        val bitmap = decodeScaled(path, maxDim = 800) ?: return null
        return try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private fun escape(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    // ------------------------------------------------------------------
    // Общее
    // ------------------------------------------------------------------

    private fun newPageInfo(pageNumber: Int) =
        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()

    private fun formatCoord(lat: Double, lng: Double): String =
        if (lat == 0.0 && lng == 0.0) "нет данных"
        else "%.6f, %.6f".format(java.util.Locale.US, lat, lng)

    private fun decodeScaled(path: String, maxDim: Int = 1024): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            var sample = 1
            while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeFile(path, opts)
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        val CSS = """
            * { box-sizing: border-box; }
            body { font-family: -apple-system, Segoe UI, Roboto, Arial, sans-serif;
                   margin: 0; padding: 24px; color: #212121; background: #fafafa; }
            header { border-bottom: 2px solid #333; padding-bottom: 12px; margin-bottom: 24px; }
            h1 { font-size: 22px; margin: 0 0 8px; }
            h2 { font-size: 16px; margin: 0 0 8px; }
            .meta { color: #666; font-size: 13px; margin: 0; }
            .remark { background: #fff; border: 1px solid #e0e0e0; border-radius: 8px;
                      padding: 16px; margin-bottom: 16px; }
            table.fields { width: 100%; border-collapse: collapse; font-size: 13px; }
            table.fields th { text-align: left; vertical-align: top; color: #757575;
                              font-weight: 600; width: 160px; padding: 4px 8px 4px 0; }
            table.fields td { padding: 4px 0; }
            .photos { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 12px; }
            figure { margin: 0; max-width: 220px; }
            figure img { width: 100%; border-radius: 6px; display: block; }
            figcaption { font-size: 11px; color: #777; margin-top: 4px; }
            .muted { color: #999; font-size: 13px; }
            footer { margin-top: 24px; border-top: 2px solid #333; padding-top: 12px; }
        """.trimIndent()
    }
}
