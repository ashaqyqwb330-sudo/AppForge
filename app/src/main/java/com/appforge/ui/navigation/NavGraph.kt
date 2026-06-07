package com.appforge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appforge.ui.screens.splash.SplashScreen
import com.appforge.ui.screens.appmanager.AppManagerScreen
import com.appforge.ui.screens.appmanager.AppManagerViewModel
import com.appforge.ui.screens.settings.SettingsScreen
import com.appforge.ui.screens.about.AboutScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.AppManager.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.AppManager.route) {
            val viewModel: AppManagerViewModel = hiltViewModel()
            AppManagerScreen(
                viewModel = viewModel,
                onAppActivated = {
                    navController.navigate(Routes.ActiveApp.route) {
                        popUpTo(Routes.AppManager.route)
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Routes.About.route)
                }
            )
        }

        composable(Routes.ActiveApp.route) {
            // This will be replaced with the active app's UI
            // For now, placeholder that shows dashboard of active app
            ActiveAppHost(
                onBackToManager = {
                    navController.navigate(Routes.AppManager.route) {
                        popUpTo(Routes.ActiveApp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun ActiveAppHost(onBackToManager: () -> Unit) {
    // Placeholder – will be filled with template-based navigation in Part 2
    androidx.compose.material3.Text("Active App")
}
