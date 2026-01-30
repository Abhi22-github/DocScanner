package com.roaa.docscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.roaa.docscanner.ui.theme.DocScannerTheme
import com.roaa.docscanner.utils.Destinations

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                            HomeScreen(navigateToPreviewScreen = {
                                backStack.add(Destinations.PreviewScreen(it))
                            })
                        }

                        entry<Destinations.PreviewScreen> { result ->
                            PreviewScreen(result = result.result )
                        }

                    }
                )
            }
        }
    }
}
