package fr.openium.kodex.sample.app.ui.assetsFileList.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import fr.openium.kodex.sample.app.enums.Route
import fr.openium.kodex.sample.app.ui.assetsFileList.ui.AssetsFileListScreen

fun NavGraphBuilder.assetsFileListRoute(navHostController: NavHostController) {
    composable<Route.AssetsFileList> {
        AssetsFileListScreen(
            navigateToPdfVisualizerFromFile = { uri ->
                navHostController.navigate(
                    Route.PdfVisualizer(uri = uri.toString())
                )
            },
            navigateBack = navHostController::popBackStack
        )
    }
}