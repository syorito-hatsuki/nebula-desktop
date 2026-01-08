package dev.syoritohatsuki.nebuladesktop.util.editor

import dev.syoritohatsuki.nebuladesktop.dto.YamlToken
import dev.syoritohatsuki.nebuladesktop.dto.YamlTokenType

object YamlLexer {
    fun lex(text: String): List<YamlToken> {
        val tokens = mutableListOf<YamlToken>()
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
                    while (i < text.length && text[i] != c) {
                        if (text[i] == '\\') i++ // escape
                        i++
                    }
                    if (i < text.length) i++
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
                    while (i < text.length && !text[i].isWhitespace() && text[i] !in ":#{}[],") i++

                    if (i < text.length && text[i] == ':') {
                        tokens += YamlToken(start, i, YamlTokenType.KEY)
                    } else {
                        tokens += YamlToken(start, i, YamlTokenType.PLAIN)
                    }
                }
            }
        }
        return tokens
    }

    private fun isWordEnd(text: String, index: Int): Boolean = index >= text.length || !text[index].isLetterOrDigit()
}
