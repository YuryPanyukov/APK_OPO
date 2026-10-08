package com.yury.recyclerview.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RemarkDao {
    @Insert
    suspend fun insert(remark: Remark): Long

    @Query("SELECT * FROM remarks WHERE id = :id")
    suspend fun getById(id: Long): Remark?

    @Query("SELECT * FROM remarks WHERE commissionId = :commissionId ORDER BY remarkTime DESC")
    fun getByCommissionId(commissionId: Long): Flow<List<Remark>>

    @Query("SELECT * FROM remarks WHERE commissionId = :commissionId ORDER BY remarkTime DESC")
    suspend fun getByCommissionIdSync(commissionId: Long): List<Remark>

    @Query("DELETE FROM remarks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
