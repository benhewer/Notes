package notes.project.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Test
import kotlin.test.assertEquals

class MarkdownVisualTransformationTest {

    var transformation = MarkdownVisualTransformation(0)
    var offsetMapping = transformation.MarkdownOffsetMapping()

    fun setCursorPosition(position: Int) {
        transformation = MarkdownVisualTransformation(position)
        offsetMapping = transformation.MarkdownOffsetMapping()
    }

    @Test
    fun `Bold text is parsed correctly`() {
        val text = "This **should be bold**"
        val transformedText = transformation.filter(AnnotatedString(text)).text

        val expected = buildAnnotatedString {
            append("This should be bold")
            addStyle(
                SpanStyle(fontWeight = FontWeight.Bold),
                5,
                length
            )
        }
        assertEquals(expected, transformedText)
    }

    @Test
    fun `Link text is parsed correctly`() {
        val text = "This is a [link](google.com)"
        val transformedText = transformation.filter(AnnotatedString(text)).text

        val expected = buildAnnotatedString {
            append("This is a link")
            addStyle(
                SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                ),
                10,
                length
            )
            // Attach a string annotation with tag = "URL"
            addStringAnnotation(
                tag = "URL",
                annotation = "google.com",
                start = 10,
                end = length
            )
        }
        assertEquals(expected, transformedText)
    }

    fun checkCursorMappings(text: String, expected: List<Int>) {
        // Used to update mapping table.
        transformation.filter(AnnotatedString(text)).text

        val originalIndices = (0..text.length).toList()
        val transformedIndices = originalIndices.map(offsetMapping::originalToTransformed)

        assertEquals(expected, transformedIndices)
    }

    @Test
    fun `Cursor mapping is correct for plain text`() {
        val text = "This is some plain text"
        val expected = (0..text.length).toList()

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for italics`() {
        val text = "This has *italic* text"
        // 0-8 before italics, 8-14 during italics, 14-20 after italics
        val expected = (0..8) + (8..14) + (14..20)

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for bold`() {
        val text = "This has **bold** text"
        // 0-8 before bold, 8-12 during bold, 12-18 after bold
        val expected = (0..8) + 8 + (8..12) + 12 + (12..18)

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for link`() {
        val text = "This is a [link](google.com)"
        // Ranges represent: "This is a ", "[link", "]", "(google.com)"
        val expected = (0..9) + (9..13) + 13 + List("(google.com)".length) { 13 }

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct with cursor in Markdown element`() {
        setCursorPosition(12)
        val text = "This has **bold** text"
        val expected = (0..text.length).toList()

        checkCursorMappings(text, expected)
    }
}