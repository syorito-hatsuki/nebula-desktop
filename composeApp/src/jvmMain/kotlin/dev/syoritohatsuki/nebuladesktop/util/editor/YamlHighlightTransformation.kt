package dev.syoritohatsuki.nebuladesktop.util.editor

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import dev.syoritohatsuki.nebuladesktop.dto.YamlToken

class YamlHighlightTransformation(private val tokens: List<YamlToken>) : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        if (tokens.isEmpty()) return

        tokens.filterIsInstance<YamlToken.Key>().forEach { style(it, KeyColor) }
        tokens.filterIsInstance<YamlToken.Comment>().forEach { style(it, CommentColor) }
        tokens.filterIsInstance<YamlToken.BlockScalar>().forEach { style(it, BlockStringColor) }
        tokens.filterIsInstance<YamlToken.QuotedScalar>().forEach { style(it, StringColor) }
        tokens.filterIsInstance<YamlToken.PlainScalar>().forEach { style(it, PlainScalarColor) }
        tokens.filterIsInstance<YamlToken.Number>().forEach { style(it, NumberColor) }
        tokens.filterIsInstance<YamlToken.Boolean>().forEach { style(it, BooleanColor) }
        tokens.filterIsInstance<YamlToken.FlowStart>().forEach { style(it, FlowColor) }
        tokens.filterIsInstance<YamlToken.FlowEnd>().forEach { style(it, FlowColor) }
    }

    fun TextFieldBuffer.style(token: YamlToken, color: Color) = safeAddStyle(SpanStyle(color), token.start, token.end)

    fun TextFieldBuffer.safeAddStyle(style: SpanStyle, start: Int, end: Int) {
        val safeStart = start.coerceIn(0, length)
        val safeEnd = end.coerceIn(safeStart, length)
        if (safeStart < safeEnd) {
            addStyle(style, safeStart, safeEnd)
        }
    }
}