package com.yury.recyclerview.domain.usecase

import com.yury.recyclerview.database.Remark
import com.yury.recyclerview.database.RemarkDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemarksUseCase @Inject constructor(
    private val remarkDao: RemarkDao
) {
    operator fun invoke(commissionId: Long): Flow<List<Remark>> =
        remarkDao.getByCommissionId(commissionId)
}
