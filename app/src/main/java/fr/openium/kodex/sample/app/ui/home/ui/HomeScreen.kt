package fr.openium.kodex.sample.app.ui.home.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.openium.kodex.sample.app.R
import fr.openium.kodex.sample.app.theme.KodexTheme
import fr.openium.kodex.sample.app.ui.core.SampleTopAppBar

@Composable
fun HomeScreen(
    navigateToPdfVisualizerFromUri: (Uri) -> Unit,
    navigateToAssetFilesList: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let(navigateToPdfVisualizerFromUri)
        }
    )

    // From local file
    val openDocument = {
        launcher.launch(arrayOf("application/pdf"))
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SampleTopAppBar(
            title = stringResource(R.string.app_name)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = openDocument
            ) {
                Text(text = "Load from local file")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = navigateToAssetFilesList
            ) {
                Text(text = "Load from assets file")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = {

                }
            ) {
                Text(text = "Load from url")
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    KodexTheme {
        HomeScreen(
            navigateToPdfVisualizerFromUri = {},
            navigateToAssetFilesList = {}
        )
    }
}