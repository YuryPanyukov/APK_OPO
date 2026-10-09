package code_sys.apkopo.domain.usecase

import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark
import code_sys.apkopo.domain.repository.CommissionRepository
import code_sys.apkopo.domain.repository.PhotoFiles
import code_sys.apkopo.domain.repository.PhotoRepository
import code_sys.apkopo.domain.repository.RemarkRepository
import code_sys.apkopo.domain.repository.ReportBuilder
import code_sys.apkopo.domain.repository.ReportFormat
import code_sys.apkopo.util.PhotoMeta
import code_sys.apkopo.util.RemarkWithPhotos
import java.io.File

/** Создание комиссии. */
class CreateCommissionUseCase(private val commissions: CommissionRepository) {

    suspend operator fun invoke(title: String, date: Long = System.currentTimeMillis()): Long {
        require(title.isNotBlank()) { "Название комиссии не может быть пустым" }
        return commissions.insert(Commission(title = title.trim(), date = date))
    }
}

/** Удаление комиссии (вместе с замечаниями и фото — каскад в БД). */
class DeleteCommissionUseCase(private val commissions: CommissionRepository) {
    suspend operator fun invoke(commission: Commission) = commissions.delete(commission)
}

/** Переименование комиссии. */
class UpdateCommissionUseCase(private val commissions: CommissionRepository) {

    suspend operator fun invoke(commission: Commission): Commission {
        require(commission.title.isNotBlank()) { "Название комиссии не может быть пустым" }
        val saved = commission.copy(title = commission.title.trim())
        commissions.update(saved)
        return saved
    }
}

/**
 * Удаление замечания: строка удаляется вместе с фото-записями (каскад в БД
 * плюс явное удаление — не полагаемся только на каскад), затем удаляются
 * сами файлы изображений из хранилища.
 */
class DeleteRemarkUseCase(
    private val remarks: RemarkRepository,
    private val photos: PhotoRepository,
    private val photoFiles: PhotoFiles
) {

    suspend operator fun invoke(remark: Remark) {
        val files = photos.getByRemark(remark.id)
        remarks.delete(remark)
        files.forEach { photo ->
            runCatching { photos.deleteByPath(photo.filePath) }
            runCatching { photoFiles.delete(photo.filePath) }
        }
    }
}

/**
 * Редактирование замечания: обновляет текстовые поля,
 * добавляет новые фото и удаляет снятые с публикации.
 * Гео- и временные метки замечания не трогаются — они фиксируются при создании.
 */
class UpdateRemarkUseCase(
    private val remarks: RemarkRepository,
    private val photos: PhotoRepository,
    private val photoFiles: PhotoFiles
) {

    suspend operator fun invoke(
        remark: Remark,
        addedPhotos: List<PhotoMeta> = emptyList(),
        removedPaths: List<String> = emptyList()
    ) {
        remarks.update(
            remark.copy(
                location = remark.location.trim(),
                objectName = remark.objectName.trim(),
                remarkType = remark.remarkType.trim(),
                description = remark.description.trim()
            )
        )
        addedPhotos.forEach { meta ->
            photos.insert(
                Photo(
                    remarkId = remark.id,
                    filePath = meta.filePath,
                    photoLat = meta.lat,
                    photoLng = meta.lng,
                    photoTime = meta.time
                )
            )
        }
        removedPaths.forEach { path ->
            photos.deleteByPath(path)
            runCatching { photoFiles.delete(path) }
        }
    }
}

/**
 * Добавление замечания: сохраняет запись в БД и привязывает
 * уже сохранённые в хранилище фото (с EXIF-метаданными).
 */
class AddRemarkUseCase(
    private val remarks: RemarkRepository,
    private val photos: PhotoRepository
) {

    suspend operator fun invoke(
        commissionId: Long,
        location: String,
        objectName: String,
        remarkType: String,
        description: String,
        userLat: Double,
        userLng: Double,
        userTime: Long,
        photoMetas: List<PhotoMeta>
    ): Long {
        val remark = Remark(
            commissionId = commissionId,
            location = location.trim(),
            objectName = objectName.trim(),
            remarkType = remarkType.trim(),
            description = description.trim(),
            remarkLat = userLat,
            remarkLng = userLng,
            remarkTime = System.currentTimeMillis(),
            userLat = userLat,
            userLng = userLng,
            userTime = userTime
        )
        val remarkId = remarks.insert(remark)
        photoMetas.forEach { meta ->
            photos.insert(
                Photo(
                    remarkId = remarkId,
                    filePath = meta.filePath,
                    photoLat = meta.lat,
                    photoLng = meta.lng,
                    photoTime = meta.time
                )
            )
        }
        return remarkId
    }
}

/** Генерация отчёта (PDF или HTML) по комиссии. */
class GenerateReportUseCase(
    private val commissions: CommissionRepository,
    private val remarks: RemarkRepository,
    private val photos: PhotoRepository,
    private val reportBuilder: ReportBuilder
) {

    suspend operator fun invoke(
        commissionId: Long,
        format: ReportFormat = ReportFormat.PDF
    ): File? {
        val commission = commissions.getById(commissionId) ?: return null
        val remarkList = remarks.getByCommission(commissionId)
        val withPhotos = remarkList.map { remark ->
            RemarkWithPhotos(remark, photos.getByRemark(remark.id))
        }
        return reportBuilder.build(commission, withPhotos, format)
    }
}
