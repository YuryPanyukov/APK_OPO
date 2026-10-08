package com.yury.recyclerview.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommissionDao {
    @Insert
    suspend fun insert(commission: Commission): Long

    @Query("SELECT * FROM commissions WHERE id = :id")
    suspend fun getById(id: Long): Commission?

    @Query("SELECT * FROM commissions ORDER BY date DESC")
    fun getAll(): Flow<List<Commission>>

    @Query("DELETE FROM commissions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
