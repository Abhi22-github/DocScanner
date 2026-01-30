package com.roaa.docscanner.utils

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destinations : NavKey {
    @Serializable
    data object HomeScreen : Destinations

    @Serializable
    data class PreviewScreen(
        val result: GmsDocumentScanningResult? = null,
        val pdfUri: Uri? = null
    ) :
        Destinations
}

enum class ActionType{
    PDF,
    SCAN,
}