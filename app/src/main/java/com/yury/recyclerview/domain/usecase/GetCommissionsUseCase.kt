package com.yury.recyclerview.domain.usecase

import com.yury.recyclerview.database.Commission
import com.yury.recyclerview.database.CommissionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommissionsUseCase @Inject constructor(
    private val commissionDao: CommissionDao
) {
    operator fun invoke(): Flow<List<Commission>> = commissionDao.getAll()
}
