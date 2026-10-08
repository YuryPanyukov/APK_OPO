package com.yury.recyclerview.domain.model

/**
 * Доменная модель комиссии.
 * В реальном UseCase можно мапить Room-сущность на эту модель,
 * чтобы не раскрывать детали БД в слое презентации.
 */
data class Commission(
    val id: Long,
    val title: String,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
