package com.appforge.ui.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object AppManager : Routes("app_manager")
    object ActiveApp : Routes("active_app")
    object Settings : Routes("settings")
    object About : Routes("about")

    object Dashboard : Routes("active_app/dashboard")
    object TableView : Routes("active_app/table/{tableName}") {
        fun createRoute(tableName: String) = "active_app/table/$tableName"
    }
}
