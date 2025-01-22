package fr.openium.kodex.sample.app.ui.home.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.openium.kodex.sample.app.enums.DocumentResource
import fr.openium.kodex.sample.app.ui.core.KodexModalBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFileSelectorBottomSheet(
    openDocument: () -> Unit,
    openFromAssets: (DocumentResource) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val animateToDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }

    KodexModalBottomSheet(
        state = sheetState,
        onDismiss = onDismiss
    ) {
        HomeFileSelectorBottomSheetContent(
            openDocument = openDocument,
            openFromAssets = openFromAssets,
            onDismiss = animateToDismiss
        )
    }
}

@Composable
private fun HomeFileSelectorBottomSheetContent(
    openDocument: () -> Unit,
    openFromAssets: (DocumentResource) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = "Choose an option",
            fontSize = 20.sp
        )

        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

        // Option: Open a local PDF
        TextButton(
            onClick = {
                onDismiss()
                openDocument()
            }
        ) {
            Text("Open from device")
        }

        // Option: Open a PDF from assets
        Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            text = "Open from assets",
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            DocumentResource.entries.forEach { document ->
                TextButton(
                    onClick = {
                        onDismiss()
                        openFromAssets(document)
                    }
                ) {
                    Text(text = document.name)
                }
            }
        }
    }
}