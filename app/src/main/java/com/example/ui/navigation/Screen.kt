package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Gallery : Screen("gallery?category={category}") {
        fun createRoute(category: String? = null): String {
            return if (category != null) "gallery?category=$category" else "gallery"
        }
    }
    object SmartSearch : Screen("smart_search")
    object Categories : Screen("categories")
    object StorageCleaner : Screen("storage_cleaner")
    object Detail : Screen("detail/{screenshotId}") {
        fun createRoute(id: Long): String = "detail/$id"
    }
    object Settings : Screen("settings")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfUse : Screen("terms_of_use")
}
