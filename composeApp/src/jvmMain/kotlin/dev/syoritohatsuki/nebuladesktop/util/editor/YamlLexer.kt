package dev.syoritohatsuki.nebuladesktop.util.editor

import dev.syoritohatsuki.nebuladesktop.dto.LexResult
import dev.syoritohatsuki.nebuladesktop.dto.LexResult.YamlToken.YamlScope

object YamlLexer {

    fun lex(text: String): LexResult {
        val tokens = mutableListOf<LexResult.YamlToken>()
        val errors = mutableListOf<LexResult.LintError>()

        var index = 0
        var flowDepth = 0

        var blockIndent: Int? = null
        var inBlockScalar = false

        while (index < text.length) {
            val c = text[index]

            // ---------- BLOCK SCALAR CONTENT ----------
            if (inBlockScalar) {
                val lineStart = index
                val indent = indentationAt(text, index)

                if (indent <= (blockIndent ?: 0)) {
                    inBlockScalar = false
                    continue
                }

                while (index < text.length && text[index] != '\n') index++
                tokens += LexResult.YamlToken(
                    lineStart, index, YamlScope.SCALAR_BLOCK_CONTENT
                )
                continue
            }

            when {
                c.isWhitespace() -> index++

                // ---------- COMMENT ----------
                c == '#' -> {
                    val start = index
                    while (index < text.length && text[index] != '\n') index++
                    tokens += LexResult.YamlToken(start, index, YamlScope.COMMENT)
                }

                // ---------- FLOW ----------
                c == '{' || c == '[' -> {
                    flowDepth++
                    tokens += LexResult.YamlToken(index, index + 1, YamlScope.FLOW_PUNCTUATION)
                    index++
                }

                c == '}' || c == ']' -> {
                    flowDepth--
                    tokens += LexResult.YamlToken(index, index + 1, YamlScope.FLOW_PUNCTUATION)
                    index++
                }

                // ---------- BLOCK SCALAR INDICATOR ----------
                c == '|' || c == '>' -> {
                    val start = index
                    index++
                    while (index < text.length && text[index] != '\n') index++

                    blockIndent = indentationAt(text, start)
                    inBlockScalar = true

                    tokens += LexResult.YamlToken(
                        start, start + 1, YamlScope.SCALAR_BLOCK_INDICATOR
                    )
                }

                // ---------- STRING (DOUBLE) ----------
                c == '"' -> {
                    val start = index++
                    while (index < text.length) {
                        if (text[index] == '\\') {
                            index += 2
                            continue
                        }
                        if (text[index] == '"') {
                            index++
                            break
                        }
                        index++
                    }

                    tokens += LexResult.YamlToken(
                        start, index, YamlScope.SCALAR_QUOTED_DOUBLE
                    )
                }

                // ---------- STRING (SINGLE) ----------
                c == '\'' -> {
                    val start = index++
                    while (index < text.length) {
                        if (text[index] == '\'' && text.getOrNull(index + 1) != '\'') {
                            index++
                            break
                        }
                        if (text[index] == '\'' && text.getOrNull(index + 1) == '\'') {
                            index += 2
                            continue
                        }
                        index++
                    }

                    tokens += LexResult.YamlToken(
                        start, index, YamlScope.SCALAR_QUOTED_SINGLE
                    )
                }

                // ---------- BOOLEAN ----------
                text.startsWith("true", index, true) && isWordEnd(text, index + 4) -> {
                    tokens += LexResult.YamlToken(index, index + 4, YamlScope.BOOLEAN)
                    index += 4
                }

                text.startsWith("false", index, true) && isWordEnd(text, index + 5) -> {
                    tokens += LexResult.YamlToken(index, index + 5, YamlScope.BOOLEAN)
                    index += 5
                }

                // ---------- SEQUENCE INDICATOR ----------
                c == '-' && isSequenceIndicator(text, index) -> {
                    tokens += LexResult.YamlToken(
                        index, index + 1, YamlScope.SEQUENCE_INDICATOR
                    )
                    index++
                }

                // ---------- NUMBER ----------
                c.isDigit() || (c == '-' && text.getOrNull(index + 1)?.isDigit() == true) -> {
                    val start = index++
                    while (index < text.length && (text[index].isDigit() || text[index] == '.')) index++

                    // IMPORTANT: check what comes next
                    if (isWordEnd(text, index)) {
                        tokens += LexResult.YamlToken(start, index, YamlScope.NUMBER)
                    } else {
                        // rollback: treat entire thing as plain scalar
                        while (index < text.length && !text[index].isWhitespace() && text[index] !in "#:{}[],") {
                            index++
                        }
                        tokens += LexResult.YamlToken(start, index, YamlScope.SCALAR_PLAIN)
                    }
                }

                // ---------- COLON ----------
                c == ':' -> {
                    val scope = if (flowDepth == 0 && isMappingColon(text, index)) {
                        markPreviousKey(tokens)
                        YamlScope.MAPPING_SEPARATOR
                    } else {
                        YamlScope.FLOW_PUNCTUATION
                    }

                    tokens += LexResult.YamlToken(index, index + 1, scope)
                    index++
                }

                // ---------- PLAIN ----------
                else -> {
                    val start = index
                    while (index < text.length && !text[index].isWhitespace() && text[index] !in "#:{}[],") index++

                    tokens += LexResult.YamlToken(
                        start, index, YamlScope.SCALAR_PLAIN
                    )
                }
            }
        }

        return LexResult(tokens, errors)
    }

    // ---------- HELPERS ----------

    private fun markPreviousKey(tokens: MutableList<LexResult.YamlToken>) {
        val last = tokens.lastOrNull() ?: return
        if (last.scope == YamlScope.SCALAR_PLAIN) {
            last.scope = YamlScope.MAPPING_KEY
        }
    }

    private fun isMappingColon(text: String, index: Int): Boolean {
        val before = text.substring(0, index).takeLastWhile { it != '\n' }
        if (before.trim().isEmpty()) return false

        val after = text.drop(index + 1).dropWhile { it == ' ' }
        return after.isEmpty() || after.first() !in ":,"
    }

    private fun isSequenceIndicator(text: String, index: Int): Boolean {
        var i = index - 1
        while (i >= 0 && text[i] == ' ') i--
        if (i >= 0 && text[i] != '\n') return false

        val next = text.getOrNull(index + 1) ?: return true
        return next == ' ' || next == '\n'
    }

    private fun indentationAt(text: String, index: Int): Int {
        var i = index - 1
        while (i >= 0 && text[i] != '\n') i--
        i++
        var count = 0
        while (i < text.length && text[i] == ' ') {
            count++
            i++
        }
        return count
    }

    private fun isWordEnd(text: String, index: Int): Boolean = index >= text.length || !text[index].isLetterOrDigit()

    private fun offsetToLineColumn(text: String, offset: Int): Pair<Int, Int> {
        val lines = text.substring(0, offset).lines()
        return lines.size to (lines.lastOrNull()?.length ?: 0)
    }
}