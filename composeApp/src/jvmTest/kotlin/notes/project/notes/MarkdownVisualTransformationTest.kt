package notes.project.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
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

    @Test
    fun `Code block text is parsed correctly`() {
        val text = "This is ```SomeCode() { " +
                "// do something" +
                "}```"
        val transformedText = transformation.filter(AnnotatedString(text)).text

        val expected = buildAnnotatedString {
            append("This is SomeCode() { " +
                    "// do something" +
                    "}")
            addStyle(
                SpanStyle(
                    background = Color(0xFFEFEFEF),
                    color = Color(0xFF333333),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                ),
                8,
                length
            )
        }
    }

    fun checkCursorMappings(text: String, expected: List<Int>) {
        // Used to update mapping table.
        transformation.filter(AnnotatedString(text))

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
        // Ranges represent: "This is a ", "[link", "]", "(google.com)", " "
        val expected = (0..9) + (9..13) + 13 + List("(google.com)".length) { 13 } + 14

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for code block`() {
        val text = "This is ```SomeCode() { \n" +
                "// do something\n" +
                "}```"
        val expected = (0..7) + 7 + 7 + (7..38) + 38 + 38 + 38 + 39

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for inline code`() {
        val text = "This is `inline code`"
        val expected = (0..7) + (7..18) + 18 + 19

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for heading`() {
        // Set the cursor position to the outside of Markdown
        setCursorPosition(30)
        val text1 = "# This is heading 1"
        val text2 = "## This is heading 2"

        val expected1 = listOf(0) + 0 + (0..17)
        val expected2 = listOf(0) + 0 + 0 + (0..17)

        checkCursorMappings(text1, expected1)
        checkCursorMappings(text2, expected2)
    }

    @Test
    fun `Cursor mapping is correct for list item`() {
        // Set the cursor position to the outside of Markdown
        setCursorPosition(30)
        val text = "- This is a list item"
        val expected = (0..text.length).toList()

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct for blockquote`() {
        // Set the cursor position to the outside of Markdown
        setCursorPosition(30)
        val text = "> This is a quote"
        val expected = listOf(0) + (0..16)

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct with cursor in Markdown element`() {
        setCursorPosition(12)
        val text = "This has **bold** text"
        val expected = (0..text.length).toList()

        checkCursorMappings(text, expected)
    }

    @Test
    fun `Cursor mapping is correct with cursor at end of Markdown element`() {
        setCursorPosition(20)
        val text = "> This is a quote"
        val transformedText = transformation.filter(AnnotatedString(text)).text

        val expected =
            buildAnnotatedString {
                append("This is a quote")
                addStyle(
                    SpanStyle(
                        background = Color(0xFFE0E0E0),
                        fontStyle = FontStyle.Italic
                    ),
                    0,
                    this.length
                )
            }
        assertEquals(expected, transformedText)
    }
}