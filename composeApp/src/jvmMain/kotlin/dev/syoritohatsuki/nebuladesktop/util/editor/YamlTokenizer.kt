package dev.syoritohatsuki.nebuladesktop.util.editor

import dev.syoritohatsuki.nebuladesktop.dto.YamlToken
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.events.*
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader

object YamlTokenizer {
    fun tokenize(text: String): List<YamlToken> {
        val normalizedText = text.normalizeYaml()

        val settings = LoadSettings.builder().setParseComments(true).build()
        val parser = ParserImpl(settings, StreamReader(settings, normalizedText))

        var insideSequence = 0
        var mappingDepth = 0

        val expectingKeyStack = ArrayDeque<Boolean>()
        val tokens = mutableListOf<YamlToken>()

        val mapper = LineOffsetMapper(normalizedText)

        while (parser.hasNext()) {
            when (val event = parser.next()) {

                is MappingStartEvent -> {
                    mappingDepth++
                    expectingKeyStack.addLast(true)
                }

                is MappingEndEvent -> {
                    mappingDepth--
                    expectingKeyStack.removeLast()
                }

                is ScalarEvent -> {
                    val start = mapper.offset(event.startMark.get().line, event.startMark.get().column)
                    val end = mapper.offset(event.endMark.get().line, event.endMark.get().column)
                        .coerceAtMost(normalizedText.length)

                    val expectingKey = expectingKeyStack.lastOrNull() == true
                    val isKey = expectingKey && insideSequence == 0

                    if (isKey) {
                        tokens += YamlToken.Key(start, end)
                        expectingKeyStack.removeLast()
                        expectingKeyStack.addLast(false)
                    } else {
                        tokens += when {
                            event.scalarStyle == ScalarStyle.LITERAL || event.scalarStyle == ScalarStyle.FOLDED -> YamlToken.BlockScalar(
                                start, end
                            )

                            event.scalarStyle == ScalarStyle.DOUBLE_QUOTED || event.scalarStyle == ScalarStyle.SINGLE_QUOTED -> YamlToken.QuotedScalar(
                                start, end
                            )

                            event.value.equals("true", true) || event.value.equals("false", true) -> YamlToken.Boolean(
                                start, end
                            )

                            event.value.toDoubleOrNull() != null -> YamlToken.Number(start, end)

                            else -> YamlToken.PlainScalar(start, end)
                        }

                        if (mappingDepth > 0 && insideSequence == 0) {
                            expectingKeyStack.removeLast()
                            expectingKeyStack.addLast(true)
                        }
                    }
                }

                is CommentEvent -> {
                    val start = mapper.offset(
                        event.startMark.get().line, event.startMark.get().column
                    )
                    val end = mapper.offset(
                        event.endMark.get().line, event.endMark.get().column
                    ).coerceAtMost(normalizedText.length)
                    tokens += YamlToken.Comment(start, end)
                }

                is SequenceStartEvent -> {
                    insideSequence++
                    findCharAtLine(normalizedText, event.startMark.get().line, '[')?.let {
                        tokens += YamlToken.FlowStart(it, it + 1)
                    }
                }

                is SequenceEndEvent -> {
                    insideSequence--
                    findCharAtLine(normalizedText, event.startMark.get().line, ']')?.let {
                        tokens += YamlToken.FlowEnd(it, it + 1)
                    }
                }
            }
        }

        return tokens
    }

    fun findCharAtLine(text: String, line: Int, target: Char): Int? {
        val lines = text.lines()
        return lines.getOrNull(line)?.indexOf(target)?.takeIf { it >= 0 }?.let {
            lines.take(line).sumOf { str -> str.length + 1 } + it
        }
    }

    private fun String.normalizeYaml(): String = replace("\r\n", "\n").replace("\r", "\n")
}