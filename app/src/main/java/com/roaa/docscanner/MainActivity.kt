package com.roaa.docscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.roaa.docscanner.ui.theme.DocScannerTheme
import com.roaa.docscanner.utils.Destinations

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DocScannerTheme {
                val backStack =
                    remember { mutableStateListOf<Destinations>(Destinations.HomeScreen) }

                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    },
                    entryProvider = entryProvider {
                        entry<Destinations.HomeScreen> {
                            HomeScreen(
                                navigateToPreviewScreen = {
                                    backStack.add(Destinations.PreviewScreen(result = it))
                                },
                                navigateToPreviewScreenFromPDF = {
                                    backStack.add(Destinations.PreviewScreen(pdfUri = it))
                                })
                        }

                        entry<Destinations.PreviewScreen> { result ->
                            PreviewScreen(
                                result = result.result,
                                pdfUri = result.pdfUri,
                                onSaveClicked = {
                                    if (backStack.size > 1) {
                                        backStack.removeLastOrNull()
                                    }
                                })
                        }

                    }
                )
            }
        }
    }
}
