package dev.syoritohatsuki.nebuladesktop.util.editor

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import dev.syoritohatsuki.nebuladesktop.dto.LexResult.YamlToken
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
                SpanStyle(colorFor(token.scope)), token.start.coerceIn(0, length), token.end.coerceIn(0, length)
            )
        }
    }

    private fun colorFor(scope: YamlToken.YamlScope): Color = when (scope) {
        YamlToken.YamlScope.MAPPING_KEY -> theme.key
        YamlToken.YamlScope.MAPPING_SEPARATOR -> theme.operator

        YamlToken.YamlScope.SEQUENCE_INDICATOR -> theme.operator

        YamlToken.YamlScope.SCALAR_PLAIN -> theme.plainScalar
        YamlToken.YamlScope.SCALAR_QUOTED_SINGLE, YamlToken.YamlScope.SCALAR_QUOTED_DOUBLE -> theme.string

        YamlToken.YamlScope.SCALAR_BLOCK_INDICATOR -> theme.keyword
        YamlToken.YamlScope.SCALAR_BLOCK_CONTENT -> theme.blockString

        YamlToken.YamlScope.NUMBER -> theme.number
        YamlToken.YamlScope.BOOLEAN -> theme.boolean

        YamlToken.YamlScope.FLOW_PUNCTUATION -> theme.flow
        YamlToken.YamlScope.COMMENT -> theme.comment
    }
}