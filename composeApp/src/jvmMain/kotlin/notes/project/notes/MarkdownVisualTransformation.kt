package notes.project.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

class MarkdownVisualTransformation(private val cursorPosition: Int) : VisualTransformation {

    private val mapping = mutableMapOf<Int, Int>()

    override fun filter(text: AnnotatedString): TransformedText {
        val markdown = text.toString()

        val annotatedString = parseMarkdownToAnnotatedString(markdown)
        return TransformedText(annotatedString, MarkdownOffsetMapping())
    }

    inner class MarkdownOffsetMapping : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            return mapping[offset] ?: 0
        }

        override fun transformedToOriginal(offset: Int): Int {
            return mapping.entries.last { it.value <= offset }.key
        }
    }

    private fun parseMarkdownToAnnotatedString(markdown: String): AnnotatedString {
        // Define regex patterns.
        val patterns = listOf(
            TokenType.LINK        to """\[(.*?)\]\((.*?)\)""".toRegex(),
            TokenType.BOLD        to """\*\*(.*?)\*\*""".toRegex(),
            TokenType.ITALIC      to """\*(.*?)\*""".toRegex(),
            TokenType.CODE_BLOCK  to """```([\s\S]*?)```""".toRegex(),
            TokenType.INLINE_CODE to """`(.*?)`""".toRegex(),
            TokenType.HEADING     to """^(#{1,2})\s*(.*)""".toRegex(RegexOption.MULTILINE),
            TokenType.LIST        to """^- (.*)""".toRegex(RegexOption.MULTILINE),
            TokenType.BLOCKQUOTE  to """^>\s+(.*)""".toRegex(RegexOption.MULTILINE),
        )

        // For each pattern, find all examples of regex matches in the Markdown text
        // and create a MarkdownToken for each match. It has the corresponding TokenType,
        // start and end index of the pattern in the original Markdown text, and matched
        // groups, which contain the actual text (and e.g., in heading, the hashtags).
        val tokens = patterns
            .flatMap { (type, pattern) ->
                pattern.findAll(markdown).map { matchResult ->
                    val groupSize = matchResult.groups.size
                    // Check each group except the 0th group (whole match).
                    // If the whole match is ## Heading, group 1 is ##, group 2 is Heading.
                    // The groups are determined by the things in brackets in the regex pattern.
                    val matchedGroups = (1..<groupSize).map { i ->
                        matchResult.groups[i]?.value ?: ""
                    }
                    MarkdownToken(
                        type = type,
                        start = matchResult.range.first,
                        end = matchResult.range.last + 1,
                        groups = matchedGroups,
                    )
                }
            }
            // Sort by the start index, so it can be looped through to add to annotated
            // string in order.
            .sortedBy { it.start }

        var currentIndex = 0
        var transformedIndex = 0

        return buildAnnotatedString {
            fun appendPlainText(upTo: Int) {
                if (upTo > currentIndex) {
                    // Add plain text before the token.
                    append(markdown.substring(currentIndex, upTo))

                    // Update the mapping to include up to token.start.
                    // With plain text, the mapping is simple (just incrementing).
                    (currentIndex..<upTo).forEach { _ ->
                        mapping[currentIndex++] = transformedIndex++
                    }
                }
            }

            tokens.forEach { token ->
                if (token.start < currentIndex) {
                    return@forEach
                }

                appendPlainText(token.start)

                // If the cursor is inside the token, don't hide the Markdown element.
                if (cursorPosition in token.start until token.end) {
                    appendPlainText(token.end)
                    return@forEach
                }

                // Style the token text with the correct Markdown.
                when (token.type) {
                    TokenType.CODE_BLOCK -> {
                        val codeContent = token.groups[0].trim()
                        val styleStart = this.length
                        append(codeContent)
                        addStyle(
                            SpanStyle(
                                background = Color(0xFFEFEFEF),
                                color = Color(0xFF333333),
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            styleStart,
                            this.length
                        )
                    }

                    TokenType.INLINE_CODE -> {
                        val codeContent = token.groups[0]
                        val styleStart = this.length
                        append(codeContent)
                        addStyle(
                            SpanStyle(
                                background = Color.LightGray,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            styleStart,
                            this.length
                        )
                    }

                    TokenType.LINK -> {
                        val (linkText, linkUrl) = token.groups
                        val styleStart = this.length
                        append(linkText)
                        addStyle(
                            SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            ),
                            styleStart,
                            this.length
                        )
                        // Attach a string annotation with tag = "URL"
                        addStringAnnotation(
                            tag = "URL",
                            annotation = linkUrl,
                            start = styleStart,
                            end = this.length
                        )
                    }

                    TokenType.BOLD -> {
                        val boldContent = token.groups[0]
                        val styleStart = this.length
                        append(boldContent)
                        addStyle(
                            SpanStyle(fontWeight = FontWeight.Bold),
                            styleStart,
                            this.length
                        )

                        transformedIndex--
                        mapping[currentIndex++] = transformedIndex

                        (0..boldContent.length).forEach { _ ->
                            mapping[currentIndex++] = transformedIndex++
                        }

                        transformedIndex--
                        mapping[currentIndex++] = transformedIndex
                        mapping[currentIndex++] = transformedIndex++
                    }

                    TokenType.ITALIC -> {
                        val italicContent = token.groups[0]
                        val styleStart = this.length
                        append(italicContent)
                        addStyle(
                            SpanStyle(fontStyle = FontStyle.Italic),
                            styleStart,
                            this.length
                        )

                        transformedIndex--
                        (0..italicContent.length).forEach { _ ->
                            mapping[currentIndex++] = transformedIndex++
                        }

                        transformedIndex--
                        mapping[currentIndex++] = transformedIndex++
                    }

                    TokenType.HEADING -> {
                        val headingLevel = token.groups[0].length // # or ##
                        val headingText = token.groups[1]
                        val styleStart = this.length
                        append(headingText)
                        addStyle(
                            SpanStyle(
                                fontSize = if (headingLevel == 1) 26.sp else 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (headingLevel == 1) Color(0xFFA75CF2) else Color(0xFF48D883)
                            ),
                            styleStart,
                            this.length
                        )
                    }

                    TokenType.LIST -> {
                        val listItem = token.groups[0]
                        append("• $listItem\n")
                    }

                    TokenType.BLOCKQUOTE -> {
                        val quoteText = token.groups[0]
                        val styleStart = this.length
                        append(quoteText)
                        addStyle(
                            SpanStyle(
                                background = Color(0xFFE0E0E0),
                                fontStyle = FontStyle.Italic
                            ),
                            styleStart,
                            this.length
                        )
                        append("\n")
                    }
                }
                currentIndex = token.end
            }
            appendPlainText(markdown.length)

            toAnnotatedString()
        }
    }

    private data class MarkdownToken(
        val type: TokenType,
        val start: Int,
        val end: Int,
        val groups: List<String>
    )


    private enum class TokenType {
        CODE_BLOCK,
        INLINE_CODE,
        LINK,
        BOLD,
        ITALIC,
        HEADING,
        LIST,
        BLOCKQUOTE
    }
}