package com.onebitsailor.codestoclientd

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.onebitsailor.codestoclientd.screens.ClientScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Send to Phone") {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                ClientScreen()
            }
        }
    }
}
