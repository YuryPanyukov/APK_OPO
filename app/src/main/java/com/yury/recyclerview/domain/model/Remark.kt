package com.yury.recyclerview.domain.model

data class Remark(
    val id: Long,
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
