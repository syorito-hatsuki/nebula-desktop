package dev.syoritohatsuki.nebuladesktop.dto

data class LexResult(
    val tokens: List<YamlToken>, val errors: List<LintError>
) {
    data class YamlToken(
        val start: Int, val end: Int, val type: YamlTokenType
    ) {
        enum class YamlTokenType {
            KEY, STRING, NUMBER, BOOLEAN, COMMENT, BLOCK_SCALAR, PLAIN, FLOW
        }
    }

    data class LintError(val message: String, val line: Int?, val column: Int?, val severity: Severity) {
        enum class Severity { ERROR, WARNING }
    }
}