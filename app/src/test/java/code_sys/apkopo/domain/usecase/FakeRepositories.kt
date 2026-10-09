package code_sys.apkopo.domain.usecase

import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark
import code_sys.apkopo.domain.repository.CommissionRepository
import code_sys.apkopo.domain.repository.PhotoFiles
import code_sys.apkopo.domain.repository.PhotoRepository
import code_sys.apkopo.domain.repository.ReportBuilder
import code_sys.apkopo.domain.repository.ReportFormat
import code_sys.apkopo.domain.repository.RemarkRepository
import code_sys.apkopo.util.RemarkWithPhotos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.io.File

/** In-memory реализация репозитория комиссий. */
class FakeCommissionRepository : CommissionRepository {

    private val items = MutableStateFlow<List<Commission>>(emptyList())
    private var nextId = 1L

    /** Все вставленные записи в порядке вставки. */
    val inserted = mutableListOf<Commission>()

    /** Все удалённые записи. */
    val deletedItems = mutableListOf<Commission>()

    /** Все обновлённые записи. */
    val updatedItems = mutableListOf<Commission>()

    override fun observeAll(): Flow<List<Commission>> = items

    override fun observeById(id: Long): Flow<Commission?> =
        items.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getById(id: Long): Commission? =
        items.value.firstOrNull { it.id == id }

    override suspend fun insert(commission: Commission): Long {
        val id = if (commission.id != 0L) commission.id else nextId++
        val saved = commission.copy(id = id)
        inserted += saved
        items.value = items.value.filterNot { it.id == id } + saved
        return id
    }

    override suspend fun update(commission: Commission) {
        updatedItems += commission
        items.value = items.value.filterNot { it.id == commission.id } + commission
    }

    override suspend fun delete(commission: Commission) {
        deletedItems += commission
        items.value = items.value.filterNot { it.id == commission.id }
    }
}

/** In-memory реализация репозитория замечаний. */
class FakeRemarkRepository : RemarkRepository {

    private val items = MutableStateFlow<List<Remark>>(emptyList())
    private var nextId = 1L

    val inserted = mutableListOf<Remark>()

    val updatedItems = mutableListOf<Remark>()

    val deletedItems = mutableListOf<Remark>()

    override fun observeByCommission(commissionId: Long): Flow<List<Remark>> =
        items.map { list -> list.filter { it.commissionId == commissionId } }

    override suspend fun getByCommission(commissionId: Long): List<Remark> =
        items.value.filter { it.commissionId == commissionId }

    override fun observeById(id: Long): Flow<Remark?> =
        items.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getById(id: Long): Remark? = items.value.firstOrNull { it.id == id }

    override suspend fun insert(remark: Remark): Long {
        val id = if (remark.id != 0L) remark.id else nextId++
        val saved = remark.copy(id = id)
        inserted += saved
        items.value = items.value.filterNot { it.id == id } + saved
        return id
    }

    override suspend fun update(remark: Remark) {
        updatedItems += remark
        items.value = items.value.filterNot { it.id == remark.id } + remark
    }

    override suspend fun delete(remark: Remark) {
        deletedItems += remark
        items.value = items.value.filterNot { it.id == remark.id }
    }
}

/** In-memory реализация репозитория фото. */
class FakePhotoRepository : PhotoRepository {

    private val items = MutableStateFlow<List<Photo>>(emptyList())
    private var nextId = 1L

    val inserted = mutableListOf<Photo>()

    val deletedPaths = mutableListOf<String>()

    override fun observeByRemark(remarkId: Long): Flow<List<Photo>> =
        items.map { list -> list.filter { it.remarkId == remarkId } }

    override suspend fun getByRemark(remarkId: Long): List<Photo> =
        items.value.filter { it.remarkId == remarkId }

    override suspend fun insert(photo: Photo): Long {
        val id = if (photo.id != 0L) photo.id else nextId++
        val saved = photo.copy(id = id)
        inserted += saved
        items.value = items.value + saved
        return id
    }

    override suspend fun deleteByPath(path: String) {
        deletedPaths += path
        items.value = items.value.filterNot { it.filePath == path }
    }
}

/** Фейковое файловое хранилище фото: запоминает удалённые пути. */
class FakePhotoFiles : PhotoFiles {

    val deleted = mutableListOf<String>()

    override fun delete(path: String) {
        deleted += path
    }
}

/** Фейковый генератор отчёта: запоминает аргументы и отдаёт заранее заданный файл. */
class FakeReportBuilder : ReportBuilder {

    var lastCommission: Commission? = null
        private set
    var lastRemarks: List<RemarkWithPhotos> = emptyList()
        private set
    var lastFormat: ReportFormat? = null
        private set
    var callCount = 0
        private set

    override fun build(
        commission: Commission,
        remarks: List<RemarkWithPhotos>,
        format: ReportFormat
    ): File {
        callCount++
        lastCommission = commission
        lastRemarks = remarks
        lastFormat = format
        return resultFile
    }

    companion object {
        /** Файл, который «строит» фейк (в тестах не читается, важен лишь факт возврата). */
        var resultFile: File = File("report.pdf")
    }
}
