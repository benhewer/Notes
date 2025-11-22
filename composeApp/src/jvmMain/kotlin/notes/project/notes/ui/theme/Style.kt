package notes.project.notes.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

val codeBlockStyle = SpanStyle(
    background = codeBackgroundColor,
    color = codeTextColor,
    fontSize = 16.sp,
    fontFamily = FontFamily.Monospace
)

val inlineCodeStyle = SpanStyle(
    background = codeBackgroundColor,
    fontSize = 14.sp,
    fontFamily = FontFamily.Monospace
)

val linkStyle = SpanStyle(
    color = Color.Blue,
    textDecoration = TextDecoration.Underline
)

val boldStyle = SpanStyle(
    fontWeight = FontWeight.Bold
)

val italicStyle = SpanStyle(
    fontStyle = FontStyle.Italic
)

val headingStyle1 = SpanStyle(
    fontSize =  26.sp,
    fontWeight = FontWeight.Bold,
    color = heading1Color
)

val headingStyle2 = SpanStyle(
    fontSize = 22.sp,
    fontWeight = FontWeight.Bold,
    color = heading2Color
)

val blockquoteStyle = SpanStyle(
    background = codeBackgroundColor,
    fontStyle = FontStyle.Italic
)