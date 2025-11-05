package notes.project.notes

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun MarkdownTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
) {
    var text by remember { mutableStateOf(value) }

    BasicTextField(
        modifier = modifier,
        value = text,
        onValueChange = {
            text = it
            onValueChange(text)
        },
        visualTransformation = MarkdownVisualTransformation(),
    )
}