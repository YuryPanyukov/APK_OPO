package code_sys.apkopo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import code_sys.apkopo.MainActivity
import code_sys.apkopo.ui.screens.AddRemarkScreen
import code_sys.apkopo.ui.screens.CommissionDetailScreen
import code_sys.apkopo.ui.screens.CommissionListScreen
import code_sys.apkopo.ui.screens.PermissionGate
import code_sys.apkopo.ui.screens.RemarkDetailScreen
import code_sys.apkopo.ui.screens.ReportScreen
import code_sys.apkopo.ui.viewmodel.AddRemarkViewModel
import code_sys.apkopo.ui.viewmodel.CommissionDetailViewModel
import code_sys.apkopo.ui.viewmodel.CommissionListViewModel
import code_sys.apkopo.ui.viewmodel.RemarkDetailViewModel
import code_sys.apkopo.ui.viewmodel.ReportViewModel

object Routes {
    const val PERMISSIONS = "permissions"
    const val LIST = "commissions"
    const val DETAIL = "commissions/{commissionId}"
    const val ADD_REMARK = "commissions/{commissionId}/remark/new"
    const val EDIT_REMARK = "remarks/{remarkId}/edit"
    const val REPORT = "commissions/{commissionId}/report"
    const val REMARK_DETAIL = "remarks/{remarkId}"

    fun detail(id: Long) = "commissions/$id"
    fun addRemark(id: Long) = "commissions/$id/remark/new"
    fun editRemark(id: Long) = "remarks/$id/edit"
    fun report(id: Long) = "commissions/$id/report"
    fun remarkDetail(id: Long) = "remarks/$id"
}

@Composable
fun AppNav(container: code_sys.apkopo.di.AppContainer) {
    val navController = rememberNavController()
    val startDestination = Routes.PERMISSIONS

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.PERMISSIONS) {
            PermissionGate(
                onAllGranted = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.PERMISSIONS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LIST) {
            val vm: CommissionListViewModel = viewModel(factory = CommissionListViewModel.factory(container))
            val commissions by vm.commissions.collectAsStateWithLifecycle()
            CommissionListScreen(
                commissions = commissions,
                onCreate = { title -> vm.create(title) },
                onDelete = { vm.delete(it) },
                onClick = { navController.navigate(Routes.detail(it.id)) }
            )
        }

        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("commissionId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("commissionId") ?: return@composable
            val vm: CommissionDetailViewModel =
                viewModel(factory = CommissionDetailViewModel.factory(container, id))
            val commission by vm.commission.collectAsStateWithLifecycle()
            val remarks by vm.remarks.collectAsStateWithLifecycle()
            CommissionDetailScreen(
                commission = commission,
                remarks = remarks,
                onBack = { navController.popBackStack() },
                onAddRemark = { navController.navigate(Routes.addRemark(id)) },
                onReport = { navController.navigate(Routes.report(id)) },
                onOpenRemark = { remark -> navController.navigate(Routes.remarkDetail(remark.id)) },
                onRename = { title -> vm.rename(title) },
                onDelete = {
                    vm.deleteCommission(onDone = { navController.popBackStack() })
                }
            )
        }

        composable(
            Routes.ADD_REMARK,
            arguments = listOf(navArgument("commissionId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("commissionId") ?: return@composable
            val vm: AddRemarkViewModel =
                viewModel(factory = AddRemarkViewModel.factory(container, id))
            AddRemarkScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.EDIT_REMARK,
            arguments = listOf(navArgument("remarkId") { type = NavType.LongType })
        ) { entry ->
            val remarkId = entry.arguments?.getLong("remarkId") ?: return@composable
            val vm: AddRemarkViewModel =
                viewModel(factory = AddRemarkViewModel.factory(container, editRemarkId = remarkId))
            AddRemarkScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.REMARK_DETAIL,
            arguments = listOf(navArgument("remarkId") { type = NavType.LongType })
        ) { entry ->
            val remarkId = entry.arguments?.getLong("remarkId") ?: return@composable
            val vm: RemarkDetailViewModel =
                viewModel(factory = RemarkDetailViewModel.factory(container, remarkId))
            RemarkDetailScreen(
                viewModel = vm,
                onEdit = { navController.navigate(Routes.editRemark(remarkId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.REPORT,
            arguments = listOf(navArgument("commissionId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("commissionId") ?: return@composable
            val vm: ReportViewModel =
                viewModel(factory = ReportViewModel.factory(container, id))
            val state by vm.state.collectAsStateWithLifecycle()
            val commission by vm.commission.collectAsStateWithLifecycle()
            val format by vm.format.collectAsStateWithLifecycle()
            ReportScreen(
                commissionTitle = commission?.title ?: "",
                state = state,
                format = format,
                onFormatChange = { vm.setFormat(it) },
                onGenerate = { vm.generate() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
