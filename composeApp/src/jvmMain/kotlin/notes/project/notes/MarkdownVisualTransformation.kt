package notes.project.notes

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import notes.project.notes.ui.theme.blockquoteStyle
import notes.project.notes.ui.theme.boldStyle
import notes.project.notes.ui.theme.codeBlockStyle
import notes.project.notes.ui.theme.headingStyle1
import notes.project.notes.ui.theme.headingStyle2
import notes.project.notes.ui.theme.inlineCodeStyle
import notes.project.notes.ui.theme.italicStyle
import notes.project.notes.ui.theme.linkStyle

class MarkdownVisualTransformation(private val cursorPosition: Int) : VisualTransformation {

    private val mapping = CursorMapping()

    override fun filter(text: AnnotatedString): TransformedText {
        val markdown = text.toString()

        val annotatedString = parseMarkdownToAnnotatedString(markdown)
        return TransformedText(annotatedString, MarkdownOffsetMapping())
    }

    inner class MarkdownOffsetMapping : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            return mapping[offset]
        }

        override fun transformedToOriginal(offset: Int): Int {
            return mapping.getOriginalFromTransformed(offset)
        }
    }

    private fun parseMarkdownToAnnotatedString(markdown: String): AnnotatedString {
        fun notAllWhitespaceOr(forbidden: String): String {
            return "(?=[\\s\\S]*[^\\s$forbidden])[\\s\\S]+?"
        }
        // Define regex patterns.
        val patterns = listOf(
            TokenType.LINK to """\[(.*?)]\((.*?)\)""".toRegex(),
            TokenType.BOLD to """(?<!\*)\*\*(${notAllWhitespaceOr("\\*")})\*\*(?!\*)""".toRegex(),
            TokenType.ITALIC to """(?<!\*)\*(${notAllWhitespaceOr("\\*")})\*(?!\*)""".toRegex(),
            TokenType.CODE_BLOCK to """(?<!`)```(${notAllWhitespaceOr("`")})```(?!`)""".toRegex(RegexOption.DOT_MATCHES_ALL),
            TokenType.INLINE_CODE to """(?<!`)`([^`]+?)`(?!`)""".toRegex(),
            TokenType.HEADING to """^(#{1,2})\s(.*)""".toRegex(RegexOption.MULTILINE),
            TokenType.LIST to """^- (.*)""".toRegex(RegexOption.MULTILINE),
            TokenType.BLOCKQUOTE to """^>(\s+)(.*)""".toRegex(RegexOption.MULTILINE),
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

        mapping.resetMapping()

        var currentIndex = 0

        return buildAnnotatedString {
            fun appendPlainText(upTo: Int) {
                if (upTo < currentIndex) {
                    return
                }
                // Add plain text before the token.
                append(markdown.substring(currentIndex, upTo))

                // Update the mapping to include up to token.start.
                // With plain text, the mapping is simple.
                val mappingsToAdd = upTo - currentIndex
                mapping.addPlainMappings(mappingsToAdd)

                currentIndex = upTo
            }

            tokens.forEach { token ->
                if (token.start < currentIndex) {
                    return@forEach
                }

                appendPlainText(token.start)

                // If the cursor is inside the token, don't hide the Markdown element.
                if (cursorPosition in token.start..token.end) {
                    appendPlainText(token.end)
                    return@forEach
                }

                // Style the token text with the correct Markdown.
                when (token.type) {
                    TokenType.CODE_BLOCK -> {
                        val codeContent = token.groups[0]
                        val styleStart = this.length
                        append(codeContent)
                        addStyle(
                            codeBlockStyle,
                            styleStart,
                            this.length
                        )

                        val codeBlockMarkdownLength = 3
                        mapping.skipAddSkipMappings(codeBlockMarkdownLength, codeContent.length)
                    }

                    TokenType.INLINE_CODE -> {
                        val codeContent = token.groups[0]
                        val styleStart = this.length
                        append(codeContent)
                        addStyle(
                            inlineCodeStyle,
                            styleStart,
                            this.length
                        )

                        val codeMarkdownLength = 1
                        mapping.skipAddSkipMappings(codeMarkdownLength, codeContent.length)
                    }

                    TokenType.LINK -> {
                        val (linkText, linkUrl) = token.groups
                        val styleStart = this.length
                        append(linkText)
                        addStyle(
                            linkStyle,
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

                        // Adding the mappings up to the end of the link
                        mapping.skipMappings(1)
                        mapping.addPlainMappings(linkText.length)
                        mapping.skipMappings(1)

                        // Adding the mappings for the link url
                        val totalLinkLength = linkUrl.length + 2
                        mapping.skipMappings(totalLinkLength)
                    }

                    TokenType.BOLD -> {
                        val boldContent = token.groups[0]
                        val styleStart = this.length
                        append(boldContent)
                        addStyle(
                            boldStyle,
                            styleStart,
                            this.length
                        )

                        val boldMarkdownLength = 2
                        mapping.skipAddSkipMappings(boldMarkdownLength, boldContent.length)
                    }

                    TokenType.ITALIC -> {
                        val italicContent = token.groups[0]
                        val styleStart = this.length
                        append(italicContent)
                        addStyle(
                            italicStyle,
                            styleStart,
                            this.length
                        )

                        val italicsMarkdownLength = 1
                        mapping.skipAddSkipMappings(italicsMarkdownLength, italicContent.length)
                    }

                    TokenType.HEADING -> {
                        val headingLevel = token.groups[0].length // # or ##
                        val headingText = token.groups[1]
                        val styleStart = this.length
                        append(headingText)
                        addStyle(
                            if (headingLevel == 1) headingStyle1 else headingStyle2,
                            styleStart,
                            this.length
                        )

                        mapping.skipMappings(headingLevel + 1)
                        mapping.addPlainMappings(headingText.length)
                    }

                    TokenType.LIST -> {
                        val listItem = token.groups[0]
                        append("• $listItem")

                        mapping.addPlainMappings(listItem.length + 2);
                    }

                    TokenType.BLOCKQUOTE -> {
                        val (whitespace, quoteText) = token.groups
                        val styleStart = this.length
                        append(whitespace)
                        append(quoteText)
                        addStyle(
                            blockquoteStyle,
                            styleStart,
                            this.length
                        )

                        // Skip the ">"
                        mapping.skipMapping()
                        // Add plain mappings for whitespace and quote text
                        mapping.addPlainMappings(whitespace.length)
                        mapping.addPlainMappings(quoteText.length)
                    }
                }
                currentIndex = token.end
            }
            appendPlainText(markdown.length)

            // Add the final mapping at markdown.length index.
            mapping.addPlainMapping()

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