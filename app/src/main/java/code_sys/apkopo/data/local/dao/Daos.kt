package code_sys.apkopo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import code_sys.apkopo.data.local.entity.Commission
import code_sys.apkopo.data.local.entity.Photo
import code_sys.apkopo.data.local.entity.Remark
import kotlinx.coroutines.flow.Flow

@Dao
interface CommissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commission: Commission): Long

    @Update
    suspend fun update(commission: Commission)

    @Delete
    suspend fun delete(commission: Commission)

    @Query("SELECT * FROM commissions ORDER BY date DESC")
    fun observeAll(): Flow<List<Commission>>

    @Query("SELECT * FROM commissions WHERE id = :id")
    suspend fun getById(id: Long): Commission?

    @Query("SELECT * FROM commissions WHERE id = :id")
    fun observeById(id: Long): Flow<Commission?>
}

@Dao
interface RemarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remark: Remark): Long

    @Update
    suspend fun update(remark: Remark)

    @Delete
    suspend fun delete(remark: Remark)

    @Query("SELECT * FROM remarks WHERE commissionId = :commissionId ORDER BY remarkTime ASC")
    fun observeByCommission(commissionId: Long): Flow<List<Remark>>

    @Query("SELECT * FROM remarks WHERE commissionId = :commissionId ORDER BY remarkTime ASC")
    suspend fun getByCommission(commissionId: Long): List<Remark>

    @Query("SELECT * FROM remarks WHERE id = :id")
    suspend fun getById(id: Long): Remark?

    @Query("SELECT * FROM remarks WHERE id = :id")
    fun observeById(id: Long): Flow<Remark?>
}

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo): Long

    @Query("SELECT * FROM photos WHERE remarkId = :remarkId ORDER BY id ASC")
    suspend fun getByRemark(remarkId: Long): List<Photo>

    @Query("SELECT * FROM photos WHERE remarkId = :remarkId ORDER BY id ASC")
    fun observeByRemark(remarkId: Long): Flow<List<Photo>>

    @Query("DELETE FROM photos WHERE filePath = :filePath")
    suspend fun deleteByPath(filePath: String)
}
