package com.roaa.docscanner

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun PreviewScreen(modifier: Modifier = Modifier, imageUris: List<Uri>) {

    Scaffold(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn() {
                items(imageUris) {
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

}