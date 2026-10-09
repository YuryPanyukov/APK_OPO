package code_sys.apkopo.di

import android.content.Context
import code_sys.apkopo.data.local.AppDatabase
import code_sys.apkopo.data.repository.CommissionRepositoryImpl
import code_sys.apkopo.data.repository.PhotoRepositoryImpl
import code_sys.apkopo.data.repository.RemarkRepositoryImpl
import code_sys.apkopo.domain.usecase.AddRemarkUseCase
import code_sys.apkopo.domain.usecase.CreateCommissionUseCase
import code_sys.apkopo.domain.usecase.DeleteCommissionUseCase
import code_sys.apkopo.domain.usecase.DeleteRemarkUseCase
import code_sys.apkopo.domain.usecase.GenerateReportUseCase
import code_sys.apkopo.domain.usecase.UpdateCommissionUseCase
import code_sys.apkopo.domain.usecase.UpdateRemarkUseCase
import code_sys.apkopo.util.LocationProvider
import code_sys.apkopo.util.PhotoStorage
import code_sys.apkopo.util.ReportGenerator

/**
 * Простейший контейнер зависимостей (без Hilt) — создаётся один раз
 * в [code_sys.apkopo.MainActivity] и передаётся во ViewModel-фабрики.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.get(appContext)

    val commissionRepository = CommissionRepositoryImpl(db)
    val remarkRepository = RemarkRepositoryImpl(db)
    val photoRepository = PhotoRepositoryImpl(db)

    val photoStorage = PhotoStorage(appContext)
    val locationProvider = LocationProvider(appContext)
    val reportGenerator = ReportGenerator(appContext)

    val createCommissionUseCase = CreateCommissionUseCase(commissionRepository)
    val deleteCommissionUseCase = DeleteCommissionUseCase(commissionRepository)
    val updateCommissionUseCase = UpdateCommissionUseCase(commissionRepository)
    val addRemarkUseCase = AddRemarkUseCase(remarkRepository, photoRepository)
    val updateRemarkUseCase = UpdateRemarkUseCase(remarkRepository, photoRepository, photoStorage)
    val deleteRemarkUseCase = DeleteRemarkUseCase(remarkRepository, photoRepository, photoStorage)
    val generateReportUseCase = GenerateReportUseCase(
        commissionRepository, remarkRepository, photoRepository, reportGenerator
    )
}
