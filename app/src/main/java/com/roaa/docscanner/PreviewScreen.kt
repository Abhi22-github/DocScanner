package com.roaa.docscanner

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import coil.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.roaa.docscanner.utils.ActionType
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PreviewScreen(
    modifier: Modifier = Modifier,
    result: GmsDocumentScanningResult?,
    pdfUri: Uri?,
    onSaveClicked: () -> Unit
) {

    val context = LocalContext.current
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val type = if (pdfUri != null) ActionType.PDF else ActionType.SCAN

    LaunchedEffect(type, pdfUri, result) {
        imageUris = when (type) {
            ActionType.PDF -> {
                pdfUri?.let {
                    renderPdfUriToImageUris(context, it)
                } ?: emptyList()
            }

            ActionType.SCAN -> {
                result?.pages?.map { it.imageUri } ?: emptyList()
            }
        }
    }

    val pagerState = rememberPagerState { imageUris.size }


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

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red)
                            .weight(.8f), // takes available space above buttons
                        pageSpacing = 12.dp
                    ) { page ->
                        AsyncImage(
                            model = imageUris[page],
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                            result?.pdf?.let { pdf ->
                                val time = getDateTimeForFileName()
                                savePdfInternal(context, pdf.uri, time)
                                savePdfPublic(context, pdf.uri, time)
                            }
                            onSaveClicked()

                        }) {
                            Text("Save")
                        }
                    }
                }
            }

        }
    }

}

fun savePdfInternal(context: Context, pdfUri: Uri, fileName: String) {
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

fun renderPdfUriToImageUris(
    context: Context,
    pdfUri: Uri
): List<Uri> {

    val imageUris = mutableListOf<Uri>()

    // ✅ Unique folder per PDF
    val pdfKey = pdfUri.toString().hashCode().toString()
    val outputDir = File(context.cacheDir, "pdf_pages/$pdfKey").apply {
        mkdirs()
    }

    val fileDescriptor =
        context.contentResolver.openFileDescriptor(pdfUri, "r")
            ?: return emptyList()

    PdfRenderer(fileDescriptor).use { renderer ->
        for (pageIndex in 0 until renderer.pageCount) {

            val imageFile = File(outputDir, "page_${pageIndex + 1}.jpg")

            // ✅ Skip rendering if already cached
            if (imageFile.exists()) {
                val cachedUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )
                imageUris.add(cachedUri)
                continue
            }

            renderer.openPage(pageIndex).use { page ->
                val bitmap = createBitmap(page.width, page.height)

                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                val imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )

                imageUris.add(imageUri)
            }
        }
    }

    return imageUris
}


