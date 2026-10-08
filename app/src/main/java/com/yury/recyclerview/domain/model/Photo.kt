package com.yury.recyclerview.domain.model

data class Photo(
    val id: Long,
    val remarkId: Long,
    val filePath: String,
    val photoLat: Double,
    val photoLng: Double,
    val photoTime: Long
)
