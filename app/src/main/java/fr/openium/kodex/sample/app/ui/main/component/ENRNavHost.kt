package fr.openium.kodex.sample.app.ui.main.component

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import fr.openium.kodex.sample.app.enums.Graph
import fr.openium.kodex.sample.app.enums.Route
import fr.openium.kodex.sample.app.ui.assetsFileList.impl.assetsFileListRoute
import fr.openium.kodex.sample.app.ui.home.impl.homeRoute
import fr.openium.kodex.sample.app.ui.pdfVisualizer.impl.pdfVisualizerRoute

@Composable
fun SampleNavHost(
    navHostController: NavHostController,
    innerPadding: PaddingValues,
) {
    NavHost(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        navController = navHostController,
        startDestination = Graph.Main,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween()
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween()
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween()
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween()
            )
        }
    ) {
        navigation<Graph.Main>(
            startDestination = Route.Home
        ) {
            homeRoute(
                navHostController = navHostController
            )

            pdfVisualizerRoute(
                navHostController = navHostController
            )

            assetsFileListRoute(
                navHostController = navHostController
            )
        }
    }
}