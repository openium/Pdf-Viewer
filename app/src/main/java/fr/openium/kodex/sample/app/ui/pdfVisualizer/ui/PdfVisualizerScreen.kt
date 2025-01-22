package fr.openium.kodex.sample.app.ui.pdfVisualizer.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.openium.kodex.sample.app.R
import fr.openium.kodex.sample.app.theme.KodexTheme
import fr.openium.kodex.sample.app.ui.core.SampleTopAppBar
import fr.openium.kodex.ui.Kodex

@Composable
fun PdfVisualizerScreen(
    data: Any?,
    navigateBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SampleTopAppBar(
            title = stringResource(R.string.app_name),
            onNavigationIconClicked = navigateBack
        )

        Kodex(
            modifier = Modifier.fillMaxSize(),
            data = data,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            onPageChanged = { currentPage, totalPages ->
                Log.d("PDF", "Page $currentPage / $totalPages")
            }
        )
    }
}

@Preview
@Composable
private fun PdfVisualizerScreenPreview() {
    KodexTheme {
        PdfVisualizerScreen(
            data = null,
            navigateBack = {}
        )
    }
}