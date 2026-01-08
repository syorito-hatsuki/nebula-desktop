package dev.syoritohatsuki.nebuladesktop.util.editor

import dev.syoritohatsuki.nebuladesktop.dto.LexResult
import dev.syoritohatsuki.nebuladesktop.dto.LexResult.YamlToken
import dev.syoritohatsuki.nebuladesktop.dto.LexResult.YamlToken.YamlTokenType

object YamlLexer {

    fun lex(text: String): LexResult {
        val tokens = mutableListOf<YamlToken>()
        val errors = mutableListOf<LexResult.LintError>()
        var i = 0

        while (i < text.length) {
            val c = text[i]

            when {
                c.isWhitespace() -> i++

                c == '#' -> {
                    val start = i
                    while (i < text.length && text[i] != '\n') i++
                    tokens += YamlToken(start, i, YamlTokenType.COMMENT)
                }

                c == '"' || c == '\'' -> {
                    val start = i++
                    var closed = false
                    while (i < text.length) {
                        if (text[i] == c) {
                            closed = true
                            i++
                            break
                        }
                        if (text[i] == '\\') i++
                        i++
                    }
                    if (!closed) {
                        val (line, col) = offsetToLineColumn(text, start)
                        errors += LexResult.LintError("Unterminated string", line, col, LexResult.LintError.Severity.ERROR)
                    }
                    tokens += YamlToken(start, i, YamlTokenType.STRING)
                }

                c.isDigit() || (c == '-' && text.getOrNull(i + 1)?.isDigit() == true) -> {
                    val start = i++
                    while (i < text.length && (text[i].isDigit() || text[i] == '.')) i++
                    tokens += YamlToken(start, i, YamlTokenType.NUMBER)
                }

                text.startsWith("true", i, true) && isWordEnd(text, i + 4) -> {
                    tokens += YamlToken(i, i + 4, YamlTokenType.BOOLEAN)
                    i += 4
                }

                text.startsWith("false", i, true) && isWordEnd(text, i + 5) -> {
                    tokens += YamlToken(i, i + 5, YamlTokenType.BOOLEAN)
                    i += 5
                }

                c in "{}[]:," -> {
                    tokens += YamlToken(i, i + 1, YamlTokenType.FLOW)
                    i++
                }

                else -> {
                    val start = i
                    while (
                        i < text.length &&
                        !text[i].isWhitespace() &&
                        text[i] !in ":#{}[],"
                    ) i++

                    tokens += if (i < text.length && text[i] == ':') {
                        YamlToken(start, i, YamlTokenType.KEY)
                    } else {
                        YamlToken(start, i, YamlTokenType.PLAIN)
                    }
                }
            }
        }

        return LexResult(tokens, errors)
    }

    private fun isWordEnd(text: String, index: Int): Boolean = index >= text.length || !text[index].isLetterOrDigit()

    private fun offsetToLineColumn(text: String, offset: Int): Pair<Int, Int> {
        val lines = text.substring(0, offset).lines()
        return lines.size to (lines.lastOrNull()?.length ?: 0)
    }
}