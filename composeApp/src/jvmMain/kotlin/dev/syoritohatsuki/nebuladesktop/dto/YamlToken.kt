package dev.syoritohatsuki.nebuladesktop.dto

//sealed class YamlToken(val start: Int, val end: Int) {
//    class Key(start: Int, end: Int) : YamlToken(start, end)
//
//    class PlainScalar(start: Int, end: Int) : YamlToken(start, end)
//    class QuotedScalar(start: Int, end: Int) : YamlToken(start, end)
//    class BlockScalar(start: Int, end: Int) : YamlToken(start, end)
//
//    class Number(start: Int, end: Int) : YamlToken(start, end)
//    class Boolean(start: Int, end: Int) : YamlToken(start, end)
//
//    class Comment(start: Int, end: Int) : YamlToken(start, end)
//
//    class FlowStart(start: Int, end: Int) : YamlToken(start, end)
//    class FlowEnd(start: Int, end: Int) : YamlToken(start, end)
//}

enum class YamlTokenType {
    KEY,
    STRING,
    NUMBER,
    BOOLEAN,
    COMMENT,
    BLOCK_SCALAR,
    PLAIN,
    FLOW
}

data class YamlToken(
    val start: Int,
    val end: Int,
    val type: YamlTokenType
)