package dev.syoritohatsuki.nebuladesktop.util.editor

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

val YamlTabTransformation = InputTransformation {
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