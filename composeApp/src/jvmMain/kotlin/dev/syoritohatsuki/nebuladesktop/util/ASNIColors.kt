package dev.syoritohatsuki.nebuladesktop.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

val ansiColorMap = mapOf(
    30 to Color(0xFF2C3E50), // Midnight Blue (approx for Black)
    31 to Color(0xFFE74C3C), // Alizarin (Red)
    32 to Color(0xFF27AE60), // Nephritis (Green)
    33 to Color(0xFFF39C12), // Orange (Yellow substitute)
    34 to Color(0xFF2980B9), // Belize Hole (Blue)
    35 to Color(0xFF8E44AD), // Wisteria (Magenta)
    36 to Color(0xFF16A085), // Green Sea (Cyan)
    37 to Color(0xFFECF0F1), // Clouds (White)

    // bright colors (approximate)
    90 to Color(0xFF7F8C8D), // Asbestos (Gray)
    91 to Color(0xFFEF4836), // Light Red
    92 to Color(0xFF2ECC71), // Emerald (Bright Green)
    93 to Color(0xFFF4D03F), // Bright Yellow
    94 to Color(0xFF3498DB), // Light Blue
    95 to Color(0xFF9B59B6), // Amethyst (Bright Magenta)
    96 to Color(0xFF1ABC9C), // Turquoise (Bright Cyan)
    97 to Color(0xFFFEFEFE)  // Almost White
)

fun String.ansiToAnnotatedString(): AnnotatedString {
    val regex = Regex("""\u001B\[([;\d]*)m""")

    val fallbackColor = Color.White

    var lastColor: Color? = null
    var idx = 0
    val builder = AnnotatedString.Builder()

    for (match in regex.findAll(this)) {
        val colorCodes = match.groupValues[1].split(';').mapNotNull { it.toIntOrNull() }
        val start = match.range.first

        // Append pre-ANSI substring with last set color or fallback white
        if (start > idx) {
            builder.pushStyle(SpanStyle(color = lastColor ?: fallbackColor))
            builder.append(this.substring(idx, start))
            builder.pop()
        }

        for (code in colorCodes) {
            when (code) {
                0 -> lastColor = null // Reset color
                in ansiColorMap.keys -> lastColor = ansiColorMap[code]
            }
        }
        idx = match.range.last + 1
    }

    // Append remaining text
    if (idx < this.length) {
        builder.pushStyle(SpanStyle(color = lastColor ?: fallbackColor))
        builder.append(this.substring(idx))
        builder.pop()
    }

    return builder.toAnnotatedString()
}

fun String.toAnnotatedString(): AnnotatedString = AnnotatedString.Builder().apply {
    pushStyle(
        SpanStyle(
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    )
    append(this@toAnnotatedString)
    pop()
}.toAnnotatedString()