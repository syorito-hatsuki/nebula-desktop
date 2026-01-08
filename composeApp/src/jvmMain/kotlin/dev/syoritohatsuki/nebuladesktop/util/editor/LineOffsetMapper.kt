package dev.syoritohatsuki.nebuladesktop.util.editor

class LineOffsetMapper(text: String) {
    private val lineOffsets: IntArray

    init {
        val offsets = mutableListOf(0)
        text.forEachIndexed { i, c ->
            if (c == '\n') offsets += i + 1
        }
        lineOffsets = offsets.toIntArray()
    }

    fun offset(line: Int, column: Int): Int = lineOffsets.getOrElse(line) { lineOffsets.last() } + column
}