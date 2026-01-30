package com.roaa.docscanner

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PreviewScreen(modifier: Modifier = Modifier, result: GmsDocumentScanningResult) {

    val context = LocalContext.current
    val imageUris = result.pages?.map { it.imageUri } ?: emptyList()


    Scaffold(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    // 🔼 Top content (LazyRow)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(imageUris) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .width(160.dp)
                                    .aspectRatio(3f / 4f)
                            )
                        }
                    }

                    // 🧲 Push buttons to bottom
                    Spacer(modifier = Modifier.weight(1f))

                    // 🔽 Bottom actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { /* keep editing */ }) {
                            Text("Keep Editing")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        FilledTonalButton(onClick = {
                            result.pdf?.let { pdf ->
                                val time = getDateTimeForFileName()
                                savePdfInternal(context, pdf.uri,time)
                                savePdfPublic(context, pdf.uri,time)
                            }

                        }) {
                            Text("Save")
                        }
                    }
                }
            }

        }
    }

}

fun savePdfInternal(context: Context, pdfUri: Uri, fileName: String ) {
    val file = File(context.filesDir, "$fileName.pdf")

    context.contentResolver.openInputStream(pdfUri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    }
}

fun savePdfPublic(context: Context, pdfUri: Uri, fileName: String) {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        put(
            MediaStore.MediaColumns.RELATIVE_PATH,
            Environment.DIRECTORY_DOCUMENTS + "/DocScanner"
        )
    }

    val uri = context.contentResolver.insert(
        MediaStore.Files.getContentUri("external"),
        values
    ) ?: return

    context.contentResolver.openInputStream(pdfUri)?.use { input ->
        context.contentResolver.openOutputStream(uri)?.use { output ->
            input.copyTo(output)
        }
    }
}


fun getDateTimeForFileName(): String {
    val formatter = DateTimeFormatter.ofPattern("MMMdd_HHmm", Locale.ENGLISH)
    return LocalDateTime.now().format(formatter)
}
