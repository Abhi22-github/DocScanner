package com.roaa.docscanner.utils

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destinations : NavKey {
    @Serializable
    data object HomeScreen : Destinations

    @Serializable
    data class PreviewScreen(val uriList: List<Uri>) : Destinations
}