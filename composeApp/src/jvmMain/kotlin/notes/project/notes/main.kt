package notes.project.notes

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "notes",
        alwaysOnTop = true,
    ) {
        App()
    }
}