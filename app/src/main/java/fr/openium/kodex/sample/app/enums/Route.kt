package fr.openium.kodex.sample.app.enums

import kotlinx.serialization.Serializable

sealed interface Graph {

    @Serializable
    data object Main : Graph
}

sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data class PdfVisualizer(
        val uri: String
    ) : Route

    @Serializable
    data object AssetsFileList : Route
}