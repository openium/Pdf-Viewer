package fr.openium.kodex.sample.app.ui.assetsFileList.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import fr.openium.kodex.SCHEME_ASSETS
import fr.openium.kodex.sample.app.R
import fr.openium.kodex.sample.app.ui.core.SampleTopAppBar

@Composable
fun AssetsFileListScreen(
    navigateToPdfVisualizerFromFile: (uri: Uri) -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    val assetsFileNames: List<String> by remember {
        mutableStateOf(
            context.assets.list("")?.filter { it.endsWith(".pdf") } ?: listOf()
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SampleTopAppBar(
            title = stringResource(R.string.app_name),
            onNavigationIconClicked = navigateBack
        )

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(assetsFileNames) { assetFileName ->
                FileCell(
                    fileName = assetFileName,
                    onCellClicked = {
                        val uri = "file:///$SCHEME_ASSETS/$assetFileName".toUri()

                        navigateToPdfVisualizerFromFile(uri)
                    }
                )
            }
        }
    }
}

@Composable
private fun FileCell(
    fileName: String,
    onCellClicked: (fileName: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable {
                onCellClicked(fileName)
            }
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = fileName,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}