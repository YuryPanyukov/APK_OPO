package com.yury.recyclerview.domain.usecase

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.yury.recyclerview.database.Commission
import com.yury.recyclerview.database.CommissionDao
import com.yury.recyclerview.database.Photo
import com.yury.recyclerview.database.PhotoDao
import com.yury.recyclerview.database.Remark
import com.yury.recyclerview.database.RemarkDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject

/**
 * Генерация PDF-отчёта по комиссии.
 * В этом варианте используется встроенный PdfDocument для простых документов.
 * В реальном проекте вынести в отдельный ReportGenerator с поддержкой таблиц, переносов и т.д.
 */
class GenerateReportUseCase @Inject constructor(
    private val commissionDao: CommissionDao,
    private val remarkDao: RemarkDao,
    private val photoDao: PhotoDao,
    @ApplicationContext context: Context
) {
    suspend operator fun invoke(args: GenerateReportArgs): File {
        val commissionId = args.commissionId
        val commission = commissionDao.getById(commissionId)
            ?: throw IllegalStateException("Commission not found: $commissionId")

        val remarks = remarkDao.getByCommissionIdSync(commissionId)

        val reportFile = File.createTempFile("report_${commission.id}_", ".pdf", context.cacheDir)

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1, 1, 1).create()

        // Строим обёртку через Canvas, но PdfDocument ожидает размер страницы —
        // используем фиксированный размер для простоты.
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            textSize = 24f
            isAntiAlias = true
        }

        var y = 50f
        canvas.drawText("Комиссия: ${commission.title}", 50f, y, paint)
        y += 40f
        canvas.drawText("Дата: ${Date(commission.date)}", 50f, y, paint)
        y += 60f

        remarks.forEach { remark ->
            canvas.drawText("Замечание #${remark.id}", 50f, y, paint)
            y += 30f
            canvas.drawText("  Локация: ${remark.location}", 50f, y, paint)
            y += 25f
            canvas.drawText("  Объект: ${remark.objectName}", 50f, y, paint)
            y += 25f
            canvas.drawText("  Тип: ${remark.remarkType}", 50f, y, paint)
            y += 25f
            canvas.drawText("  Описание: ${remark.description}", 50f, y, paint)
            y += 25f
            canvas.drawText(
                "  GPS замечания: (${remark.remarkLat}, ${remark.remarkLng})",
                50f,
                y,
                paint
            )
            y += 25f
            canvas.drawText(
                "  GPS пользователя: (${remark.userLat}, ${remark.userLng})",
                50f,
                y,
                paint
            )
            y += 35f

            val photos = photoDao.getByRemarkId(remark.id)
            photos.forEach { photo ->
                val bitmap = BitmapFactory.decodeFile(photo.filePath)
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, 50f, y, null)
                    y += bitmap.height + 8f
                    canvas.drawText(
                        "Снимок сделан: ${Date(photo.photoTime)} (${photo.photoLat}, ${photo.photoLng})",
                        50f,
                        y,
                        paint
                    )
                    y += bitmap.height + 28f
                }
            }
            y += 20f
        }

        pdf.finishPage(page)
        FileOutputStream(reportFile).use { out ->
            pdf.writeTo(out)
        }
        pdf.close()

        return reportFile
    }
}
