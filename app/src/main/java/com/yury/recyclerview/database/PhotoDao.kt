package com.yury.recyclerview.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: Photo): Long

    @Query("SELECT * FROM photos WHERE remarkId = :remarkId")
    suspend fun getByRemarkId(remarkId: Long): List<Photo>

    @Query("DELETE FROM photos WHERE remarkId = :remarkId")
    suspend fun deleteByRemarkId(remarkId: Long)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
