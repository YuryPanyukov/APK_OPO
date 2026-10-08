package com.yury.recyclerview.domain.usecase

import android.content.Context
import android.net.Uri
import com.yury.recyclerview.database.Photo
import com.yury.recyclerview.database.PhotoDao
import com.yury.recyclerview.database.Remark
import com.yury.recyclerview.database.RemarkDao
import com.yury.recyclerview.util.ExifUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Добавление замечания в рамках комиссии.
 * photos — Uри из галереи/камеры, которые надо сохранить и прочитать EXIF.
 * Возвращает id созданного замечания.
 *
 * Вариант с контекстом из Hilt. Если вдруг понадобится передавать контекст
 * не из Application, можно оставить invokeWithContext, но сейчас оставляем
 * одну цепочку сохранения, чтобы не дублировать логику вставки remark + фото.
 */
class AddRemarkUseCase @Inject constructor(
    private val remarkDao: RemarkDao,
    private val photoDao: PhotoDao,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        commissionId: Long,
        location: String,
        objectName: String,
        remarkType: String,
        description: String,
        remarkLat: Double,
        remarkLng: Double,
        userLat: Double,
        userLng: Double,
        photos: List<Uri>
    ): Long {
        val remark = Remark(
            commissionId = commissionId,
            location = location,
            objectName = objectName,
            remarkType = remarkType,
            description = description,
            remarkLat = remarkLat,
            remarkLng = remarkLng,
            remarkTime = System.currentTimeMillis(),
            userLat = userLat,
            userLng = userLng,
            userTime = System.currentTimeMillis()
        )

        val remarkId = remarkDao.insert(remark)

        photos.forEach { uri ->
            // В реальном проекте вынести в отдельный сервис/поток, чтобы не блокировать useCase
            val exif = ExifUtils.extractExifInfo(context, uri)
            photoDao.insert(
                Photo(
                    remarkId = remarkId,
                    filePath = exif.path,
                    photoLat = exif.lat,
                    photoLng = exif.lng,
                    photoTime = exif.time
                )
            )
        }

        return remarkId
    }
}
