package com.roaa.docscanner.utils

import kotlinx.serialization.Serializable

sealed interface Destinations {
    @Serializable
    data object HomeScreen : Destinations
    @Serializable
    data object PreviewScreen : Destinations
}