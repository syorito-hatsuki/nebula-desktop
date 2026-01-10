package dev.syoritohatsuki.nebuladesktop.util.editor

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import dev.syoritohatsuki.nebuladesktop.dto.LexResult.YamlToken
import dev.syoritohatsuki.nebuladesktop.dto.LexResult.YamlToken.YamlTokenType
import dev.syoritohatsuki.nebuladesktop.ui.ThemeManager

object YamlEditorTransformers {
    val theme = ThemeManager.syntaxTheme.value

    fun getYamlInputTransformation() = InputTransformation {
        this.inputTabToSpace()
    }

    fun getYamlOutputTransformation(tokens: List<YamlToken>) = OutputTransformation {
        this.highlight(tokens)
    }

    private fun TextFieldBuffer.inputTabToSpace() {
        var i = 0
        while (i < length) {
            if (charAt(i) == '\t') {
                val col = columnAt(i)
                val spaces = 2 - (col % 2)
                replace(i, i + 1, " ".repeat(spaces))
                i += spaces
            } else {
                i++
            }
        }
    }

    private fun TextFieldBuffer.columnAt(index: Int): Int {
        var col = 0
        var i = index - 1
        while (i >= 0 && charAt(i) != '\n') {
            col++
            i--
        }
        return col
    }

    private fun TextFieldBuffer.highlight(tokens: List<YamlToken>) {
        tokens.sortedBy { it.start }.forEach { token ->
            addStyle(
                SpanStyle(colorFor(token.type)), token.start.coerceIn(0, length), token.end.coerceIn(0, length)
            )
        }
    }

    private fun colorFor(type: YamlTokenType): Color = when (type) {
        YamlTokenType.KEY -> theme.key
        YamlTokenType.STRING -> theme.string
        YamlTokenType.NUMBER -> theme.number
        YamlTokenType.BOOLEAN -> theme.boolean
        YamlTokenType.COMMENT -> theme.comment
        YamlTokenType.BLOCK_SCALAR -> theme.blockString
        YamlTokenType.FLOW -> theme.flow
        YamlTokenType.PLAIN -> theme.plainScalar
    }
}