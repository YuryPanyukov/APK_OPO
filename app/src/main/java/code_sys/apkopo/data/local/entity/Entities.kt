package code_sys.apkopo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Комиссия — корневая сущность осмотра.
 * Все временные поля хранятся в Unix-millis (UTC).
 */
@Entity(tableName = "commissions")
data class Commission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

/** Замечание, привязанное к комиссии. */
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
    indices = [Index("commissionId")]
)
data class Remark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commissionId: Long,
    val location: String,
    val objectName: String,
    val remarkType: String,
    val description: String,
    val remarkLat: Double,
    val remarkLng: Double,
    val remarkTime: Long,
    val userLat: Double,
    val userLng: Double,
    val userTime: Long
)

/** Фото, прикреплённое к замечанию; координаты и время — из EXIF. */
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
    indices = [Index("remarkId")]
)
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remarkId: Long,
    val filePath: String,
    val photoLat: Double,
    val photoLng: Double,
    val photoTime: Long
)
