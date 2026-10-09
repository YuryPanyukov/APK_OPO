package code_sys.apkopo.data.repository

import code_sys.apkopo.data.local.AppDatabase
import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark
import code_sys.apkopo.domain.repository.CommissionRepository
import code_sys.apkopo.domain.repository.PhotoRepository
import code_sys.apkopo.domain.repository.RemarkRepository
import kotlinx.coroutines.flow.Flow

/** Репозиторий комиссий: тонкая обёртка над DAO. */
class CommissionRepositoryImpl(private val db: AppDatabase) : CommissionRepository {

    override fun observeAll(): Flow<List<Commission>> = db.commissionDao().observeAll()

    override fun observeById(id: Long): Flow<Commission?> = db.commissionDao().observeById(id)

    override suspend fun getById(id: Long): Commission? = db.commissionDao().getById(id)

    override suspend fun insert(commission: Commission): Long = db.commissionDao().insert(commission)

    override suspend fun update(commission: Commission) = db.commissionDao().update(commission)

    override suspend fun delete(commission: Commission) = db.commissionDao().delete(commission)
}

/** Репозиторий замечаний. */
class RemarkRepositoryImpl(private val db: AppDatabase) : RemarkRepository {

    override fun observeByCommission(commissionId: Long): Flow<List<Remark>> =
        db.remarkDao().observeByCommission(commissionId)

    override suspend fun getByCommission(commissionId: Long): List<Remark> =
        db.remarkDao().getByCommission(commissionId)

    override fun observeById(id: Long): Flow<Remark?> = db.remarkDao().observeById(id)

    override suspend fun getById(id: Long): Remark? = db.remarkDao().getById(id)

    override suspend fun insert(remark: Remark): Long = db.remarkDao().insert(remark)

    override suspend fun update(remark: Remark) = db.remarkDao().update(remark)

    override suspend fun delete(remark: Remark) = db.remarkDao().delete(remark)
}

/** Репозиторий фото. */
class PhotoRepositoryImpl(private val db: AppDatabase) : PhotoRepository {

    override fun observeByRemark(remarkId: Long): Flow<List<Photo>> =
        db.photoDao().observeByRemark(remarkId)

    override suspend fun getByRemark(remarkId: Long): List<Photo> = db.photoDao().getByRemark(remarkId)

    override suspend fun insert(photo: Photo): Long = db.photoDao().insert(photo)

    override suspend fun deleteByPath(path: String) = db.photoDao().deleteByPath(path)
}
