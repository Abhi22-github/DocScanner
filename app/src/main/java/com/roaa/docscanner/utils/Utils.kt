package com.roaa.docscanner.utils

import android.net.Uri
import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object HomeScreen : Destinations
    @Serializable
    data class PreviewScreen(val uriList: List<Uri>) : Destinations
}