package dev.syoritohatsuki.nebuladesktop.util.editor

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import dev.syoritohatsuki.nebuladesktop.dto.YamlToken
import dev.syoritohatsuki.nebuladesktop.dto.YamlTokenType

class YamlHighlightTransformation(
    private val tokens: List<YamlToken>
) : OutputTransformation {

    override fun TextFieldBuffer.transformOutput() {
        tokens.sortedBy { it.start }.forEach { token ->
            addStyle(
                SpanStyle(colorFor(token.type)), token.start.coerceIn(0, length), token.end.coerceIn(0, length)
            )
        }
    }

    private fun colorFor(type: YamlTokenType): Color = when (type) {
        YamlTokenType.KEY -> KeyColor
        YamlTokenType.STRING -> StringColor
        YamlTokenType.NUMBER -> NumberColor
        YamlTokenType.BOOLEAN -> BooleanColor
        YamlTokenType.COMMENT -> CommentColor
        YamlTokenType.BLOCK_SCALAR -> BlockStringColor
        YamlTokenType.FLOW -> FlowColor
        YamlTokenType.PLAIN -> PlainScalarColor
    }
}