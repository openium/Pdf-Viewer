package fr.openium.kodex.sample.app.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import fr.openium.kodex.sample.app.ui.main.component.SampleNavHost

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0.dp),
        ) { innerPadding ->
            SampleNavHost(
                navHostController = navController,
                innerPadding = innerPadding
            )
        }
    }
}