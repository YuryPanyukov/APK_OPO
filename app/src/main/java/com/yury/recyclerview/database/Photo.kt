package com.yury.recyclerview.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Фотография, привязанная к замечанию.
 * filePath — путь к файлу во внутреннем хранилище приложения.
 */
@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Remark::class,
            parentColumns = ["id"],
            childColumns = ["remarkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["remarkId"])]
)
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remarkId: Long,
    val filePath: String,
    val photoLat: Double,         // из EXIF
    val photoLng: Double,
    val photoTime: Long           // timestamp из EXIF
)
