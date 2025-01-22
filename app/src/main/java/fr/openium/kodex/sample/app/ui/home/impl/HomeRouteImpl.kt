package fr.openium.kodex.sample.app.ui.home.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import fr.openium.kodex.sample.app.enums.Route
import fr.openium.kodex.sample.app.ui.home.ui.HomeScreen

fun NavGraphBuilder.homeRoute(
    navHostController: NavHostController,
) {
    composable<Route.Home> {
        HomeScreen(
            navigateToPdfVisualizerFromUri = { uri ->
                navHostController.navigate(
                    Route.PdfVisualizer(uri = uri.toString())
                )
            },
            navigateToAssetFilesList = {
                navHostController.navigate(Route.AssetsFileList)
            }
        )
    }
}