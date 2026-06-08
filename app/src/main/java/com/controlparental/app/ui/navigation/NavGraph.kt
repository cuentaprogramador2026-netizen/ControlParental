package com.controlparental.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.controlparental.app.domain.model.AppMode
import com.controlparental.app.domain.repository.AppRepository
import com.controlparental.app.ui.child.ChildMainScreen
import com.controlparental.app.ui.onboarding.OnboardingScreen
import com.controlparental.app.ui.parent.ParentMainScreen
import com.controlparental.app.ui.parent.SettingsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val PARENT_HOME = "parent_home"
    const val PARENT_SETTINGS = "parent_settings"
    const val CHILD_HOME = "child_home"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: AppRepository,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    val mode by repository.appMode.collectAsState(initial = null)
                    val dest = when (mode) {
                        AppMode.PARENT -> Routes.PARENT_HOME
                        AppMode.CHILD -> Routes.CHILD_HOME
                        else -> Routes.ONBOARDING
                    }
                    navController.navigate(dest) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PARENT_HOME) {
            ParentMainScreen(
                onNavigateToSettings = {
                    navController.navigate(Routes.PARENT_SETTINGS)
                }
            )
        }

        composable(Routes.PARENT_SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CHILD_HOME) {
            ChildMainScreen()
        }
    }
}
