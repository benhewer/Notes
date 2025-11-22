package notes.project.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import notes.project.notes.ui.theme.backgroundColor
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(backgroundColor)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var body by remember { mutableStateOf(
                TextFieldValue("This is some **default text**")
            ) }

            MarkdownTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                value = body,
                onValueChange = { body = it },
            )
        }
    }
}