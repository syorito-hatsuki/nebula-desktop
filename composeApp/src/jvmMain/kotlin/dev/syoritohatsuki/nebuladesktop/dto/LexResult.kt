package dev.syoritohatsuki.nebuladesktop.dto

data class LexResult(
    val tokens: List<YamlToken>, val errors: List<LintError>
) {
    data class YamlToken(
        val start: Int, val end: Int, var scope: YamlScope
    ) {
        enum class YamlScope {
            MAPPING_KEY, MAPPING_SEPARATOR,

            SEQUENCE_INDICATOR,

            SCALAR_PLAIN, SCALAR_QUOTED_SINGLE, SCALAR_QUOTED_DOUBLE, SCALAR_BLOCK_INDICATOR, SCALAR_BLOCK_CONTENT,

            NUMBER, BOOLEAN,

            FLOW_PUNCTUATION,

            COMMENT
        }
    }

    data class LintError(val message: String, val line: Int?, val column: Int?, val severity: Severity) {
        enum class Severity { ERROR, WARNING }
    }
}