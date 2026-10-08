package com.yury.recyclerview.domain.usecase

import com.yury.recyclerview.database.Commission
import com.yury.recyclerview.database.CommissionDao
import javax.inject.Inject

/**
 * Создание новой комиссии.
 * Возвращает id созданной комиссии.
 */
class CreateCommissionUseCase @Inject constructor(
    private val commissionDao: CommissionDao
) {
    suspend operator fun invoke(title: String): Long {
        val commission = Commission(title = title, date = System.currentTimeMillis())
        return commissionDao.insert(commission)
    }
}
