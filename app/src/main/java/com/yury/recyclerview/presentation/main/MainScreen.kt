package com.yury.recyclerview.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yury.recyclerview.presentation.commissions.CommissionsScreen
import com.yury.recyclerview.presentation.report.ReportScreen
import com.yury.recyclerview.presentation.remark.AddRemarkScreen

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Commissions : Screen("commissions", "Комиссии", Icons.Default.List)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    startDestination: String = Screen.Commissions.route
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(Screen.Commissions)
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Commissions.route) {
                CommissionsScreen(
                    onCommissionClick = { commissionId ->
                        navController.navigate("commission_detail/$commissionId")
                    },
                    onAddRemarkClick = { commissionId ->
                        navController.navigate("add_remark/$commissionId")
                    }
                )
            }

            composable("commission_detail/{commissionId}") { backStackEntry ->
                val commissionId = backStackEntry.arguments?.getString("commissionId")?.toLongOrNull() ?: return@composable
                CommissionDetailScreen(
                    commissionId = commissionId,
                    onBack = { navController.popBackStack() },
                    onAddRemark = { navController.navigate("add_remark/$commissionId") },
                    onReport = { navController.navigate("report/$commissionId") }
                )
            }

            composable("add_remark/{commissionId}") { backStackEntry ->
                val commissionId = backStackEntry.arguments?.getString("commissionId")?.toLongOrNull() ?: return@composable
                AddRemarkScreen(
                    commissionId = commissionId,
                    onBack = { navController.popBackStack() },
                    onRemarkSaved = { remarkId ->
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Report.route) { backStackEntry ->
                val commissionId = backStackEntry.arguments?.getString("commissionId")?.toLongOrNull() ?: return@composable
                ReportScreen(commissionId = commissionId)
            }
        }
    }
}
