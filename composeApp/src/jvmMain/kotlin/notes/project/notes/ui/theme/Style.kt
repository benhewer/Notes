package notes.project.notes.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

val codeBlockStyle = SpanStyle(
    background = Color(0xFFEFEFEF),
    color = Color(0xFF333333),
    fontSize = 16.sp,
    fontFamily = FontFamily.Monospace
)

val inlineCodeStyle = SpanStyle(
    background = Color.LightGray,
    fontSize = 14.sp,
    fontFamily = FontFamily.Monospace
)

val linkStyle = SpanStyle(
    color = Color.Blue,
    textDecoration = TextDecoration.Underline
)

val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)

val italicStyle = SpanStyle(fontStyle = FontStyle.Italic)

val headingStyle1 = SpanStyle(
    fontSize =  26.sp,
    fontWeight = FontWeight.Bold,
    color = Color(0xFFA75CF2)
)

val headingStyle2 = SpanStyle(
    fontSize = 22.sp,
    fontWeight = FontWeight.Bold,
    color = Color(0xFF48D883)
)

val blockquoteStyle = SpanStyle(
    background = Color(0xFFE0E0E0),
    fontStyle = FontStyle.Italic
)