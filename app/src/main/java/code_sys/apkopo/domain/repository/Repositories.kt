package code_sys.apkopo.domain.repository

import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark
import code_sys.apkopo.util.RemarkWithPhotos
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Абстракции слоя данных, от которых зависят use-cases.
 * Конкретные реализации живут в [code_sys.apkopo.data.repository].
 */
interface CommissionRepository {
    fun observeAll(): Flow<List<Commission>>
    fun observeById(id: Long): Flow<Commission?>
    suspend fun getById(id: Long): Commission?
    suspend fun insert(commission: Commission): Long
    suspend fun update(commission: Commission)
    suspend fun delete(commission: Commission)
}

interface RemarkRepository {
    fun observeByCommission(commissionId: Long): Flow<List<Remark>>
    suspend fun getByCommission(commissionId: Long): List<Remark>
    fun observeById(id: Long): Flow<Remark?>
    suspend fun getById(id: Long): Remark?
    suspend fun insert(remark: Remark): Long
    suspend fun update(remark: Remark)
    suspend fun delete(remark: Remark)
}

interface PhotoRepository {
    fun observeByRemark(remarkId: Long): Flow<List<Photo>>
    suspend fun getByRemark(remarkId: Long): List<Photo>
    suspend fun insert(photo: Photo): Long
    suspend fun deleteByPath(path: String)
}

/** Файловое хранилище изображений: позволяет удалять файлы фото. */
interface PhotoFiles {
    fun delete(path: String)
}

/** Формат отчёта, доступный для экспорта. */
enum class ReportFormat(val extension: String, val mimeType: String) {
    PDF(extension = "pdf", mimeType = "application/pdf"),
    HTML(extension = "html", mimeType = "text/html")
}

/** Построение файла отчёта (PDF или HTML) по данным комиссии. */
interface ReportBuilder {
    fun build(
        commission: Commission,
        remarks: List<RemarkWithPhotos>,
        format: ReportFormat
    ): File
}
