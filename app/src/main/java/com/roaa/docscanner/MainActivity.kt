package com.roaa.docscanner

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.roaa.docscanner.ui.theme.DocScannerTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create options using the builder(Builder Pattern)
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()

        // Create the scanner instance and pass the options
        val scanner = GmsDocumentScanning.getClient(options)



        setContent {
            DocScannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

                        val scannerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartIntentSenderForResult(),
                            onResult = {
                                // Handle the result
                                if (it.resultCode == RESULT_OK) {
                                    val result = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                                    imageUris = result?.pages?.map { it.imageUri } ?: emptyList()

                                    result?.pdf?.let { pdf ->
                                        val fos = FileOutputStream(File(filesDir,"scan.pdf"))
                                        contentResolver.openInputStream(pdf.uri)?.use {
                                            it.copyTo(fos)
                                        }
                                    }
                                }
                            }
                        )

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            imageUris.forEach { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Button(
                                onClick = {
                                    scanner.getStartScanIntent(this@MainActivity)
                                        .addOnSuccessListener {
                                            scannerLauncher.launch(
                                                IntentSenderRequest.Builder(it).build()
                                            )
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                this@MainActivity,
                                                it.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            ) {
                                Text(text = "Scan PDF")
                            }

                        }

                    }
                }
            }
        }
    }
}
