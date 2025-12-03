package com.drweb.appinfo.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.drweb.appinfo.presentation.appdetail.AppDetailScreen
import com.drweb.appinfo.presentation.applist.AppListScreen

sealed class Screen(val route: String) {
    object AppList : Screen("app_list")
    object AppDetail : Screen("app_detail/{packageName}") {
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
}

fun NavGraphBuilder.setupNavigation(navController: NavHostController) {
    composable(Screen.AppList.route) {
        AppListScreen(
            onAppClick = { packageName ->
                navController.navigate(Screen.AppDetail.createRoute(packageName))
            }
        )
    }

    composable(Screen.AppDetail.route) { backStackEntry ->
        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
        AppDetailScreen(
            packageName = packageName,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
