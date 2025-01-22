package fr.openium.kodex.sample.app.ui.pdfVisualizer.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import fr.openium.kodex.sample.app.enums.Route
import fr.openium.kodex.sample.app.ui.pdfVisualizer.ui.PdfVisualizerScreen

fun NavGraphBuilder.pdfVisualizerRoute(navHostController: NavHostController) {
    composable<Route.PdfVisualizer> {
        val route = it.toRoute<Route.PdfVisualizer>()

        PdfVisualizerScreen(
            data = route.uri,
            navigateBack = navHostController::popBackStack
        )
    }
}