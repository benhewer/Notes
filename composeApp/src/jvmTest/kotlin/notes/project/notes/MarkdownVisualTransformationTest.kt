package notes.project.notes

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import org.junit.Test
import kotlin.test.assertEquals

class MarkdownVisualTransformationTest {

    val transformation = MarkdownVisualTransformation()
    val offsetMapping = transformation.MarkdownOffsetMapping()

    @Test
    fun `Bold text is parsed correctly`() {
        val text = "**This should be bold**"
        val transformedText = transformation.filter(AnnotatedString(text)).text

        val expected = buildAnnotatedString {
            append("This should be bold")
            addStyle(
                SpanStyle(fontWeight = FontWeight.Bold),
                0,
                length
            )
        }
        assertEquals(expected, transformedText)
    }

    fun checkCursorMappings(text: String, expected: List<Int>) {
        // Used to update mapping table.
        transformation.filter(AnnotatedString(text)).text

        val originalIndices = (0..<text.length).toList()
        val transformedIndices = originalIndices.map(offsetMapping::originalToTransformed)

        assertEquals(expected, transformedIndices)
    }

    @Test
    fun `Cursor mapping is correct for plain text`() {
        val text = "This is some plain text"
        val expected = (0..<text.length).toList()

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for italics`() {
        val text = "This has *italic* text"
        // 0-8 before italics, 8-14 during italics, 14-19 after italics
        val expected = (0..8) + (8..14) + (14..19)

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for bold`() {
        val text = "This has **bold** text"
        // 0-8 before bold, 8-12 during bold, 12-19 after bold
        val expected = (0..8) + 8 + (8..12) + 12 + (12..17)

        checkCursorMappings(text, expected)
    }
}