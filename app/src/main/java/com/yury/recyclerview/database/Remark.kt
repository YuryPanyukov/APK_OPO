package com.yury.recyclerview.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Замечание внутри комиссии.
 */
@Entity(
    tableName = "remarks",
    foreignKeys = [
        ForeignKey(
            entity = Commission::class,
            parentColumns = ["id"],
            childColumns = ["commissionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["commissionId"])]
)
data class Remark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val commissionId: Long,
    val location: String,          // свободное описание места
    val objectName: String,        // объект
    val remarkType: String,        // тип замечания (можно сделать enum, здесь строкой)
    val description: String,
    val remarkLat: Double,         // GPS, где замечание зафиксировано
    val remarkLng: Double,
    val remarkTime: Long,          // время создания замечания
    val userLat: Double,           // GPS пользователя в момент создания
    val userLng: Double,
    val userTime: Long
)
