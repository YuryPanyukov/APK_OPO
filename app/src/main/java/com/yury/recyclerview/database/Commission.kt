package com.yury.recyclerview.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Комиссия (инспекция) — основной контейнер для замечаний.
 * date хранится в millis (Unix-метка).
 */
@Entity(tableName = "commissions")
data class Commission(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
