package dev.syoritohatsuki.nebuladesktop.dto
data class LintError(val message: String, val line: Int?, val column: Int?, val severity: Severity) {
    enum class Severity { ERROR, WARNING }
}