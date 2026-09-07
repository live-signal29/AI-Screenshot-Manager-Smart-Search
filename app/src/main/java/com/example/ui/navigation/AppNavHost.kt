package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.MainViewModel
import com.example.ui.screens.categories.CategoriesScreen
import com.example.ui.screens.cleaner.StorageCleanerScreen
import com.example.ui.screens.detail.ScreenshotDetailScreen
import com.example.ui.screens.gallery.GalleryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.search.SmartSearchScreen
import com.example.ui.screens.settings.PrivacyPolicyScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.TermsOfUseScreen

@Composable
fun AppNavHost(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val startDestination = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    viewModel.completeOnboarding()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSearch = { navController.navigate(Screen.SmartSearch.route) },
                onNavigateToGallery = { category ->
                    navController.navigate(Screen.Gallery.createRoute(category))
                },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToCleaner = { navController.navigate(Screen.StorageCleaner.route) },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Gallery.route,
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            GalleryScreen(
                viewModel = viewModel,
                initialCategory = category,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onNavigateToSearch = { navController.navigate(Screen.SmartSearch.route) }
            )
        }

        composable(Screen.SmartSearch.route) {
            SmartSearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onCategoryClick = { category ->
                    navController.navigate(Screen.Gallery.createRoute(category))
                }
            )
        }

        composable(Screen.StorageCleaner.route) {
            StorageCleanerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("screenshotId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("screenshotId") ?: 0L
            ScreenshotDetailScreen(
                screenshotId = id,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToTerms = { navController.navigate(Screen.TermsOfUse.route) }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TermsOfUse.route) {
            TermsOfUseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
