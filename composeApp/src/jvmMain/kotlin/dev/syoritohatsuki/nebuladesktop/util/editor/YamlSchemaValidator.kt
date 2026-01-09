package dev.syoritohatsuki.nebuladesktop.util.editor

import dev.syoritohatsuki.nebuladesktop.dto.LexResult
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.lowlevel.Compose
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeTuple
import org.snakeyaml.engine.v2.nodes.ScalarNode
import kotlin.jvm.optionals.getOrNull

object YamlSchemaValidator {
    fun validate(text: String): List<LexResult.LintError> {
        val errors = mutableListOf<LexResult.LintError>()

        val composer = Compose(LoadSettings.builder().setLabel("config").build())

        val root: Node = try {
            composer.composeString(text).getOrNull() ?: return emptyList()
        } catch (e: YamlEngineException) {
            errors += LexResult.LintError(
                e.message ?: "YAML parse error", null, null, LexResult.LintError.Severity.ERROR
            )
            return errors
        }

        root as? MappingNode ?: run {
            errors += LexResult.LintError(
                "Root must be a mapping",
                root.startMark?.getOrNull()?.line?.plus(1),
                root.startMark?.getOrNull()?.column?.plus(1),
                LexResult.LintError.Severity.ERROR
            )
            return errors
        }

        validateRequiredKeys(root, errors)
        validateLocalAllowList(root, errors)

        return errors
    }

    private fun validateLocalAllowList(root: MappingNode, errors: MutableList<LexResult.LintError>) {
        val lighthouseTuple = root.findKey("lighthouse") ?: return
        val lighthouseNode = lighthouseTuple.valueNode as? MappingNode ?: return

        val allowTuple = lighthouseNode.findKey("local_allow_list") ?: return
        val allowNode = allowTuple.valueNode

        if (allowNode !is MappingNode) {
            errors += error(allowNode, "local_allow_list must be a mapping")
            return
        }

        for (tuple in allowNode.value) {
            val keyNode = tuple.keyNode as? ScalarNode ?: continue
            val valueNode = tuple.valueNode
            val key = keyNode.value

            if (key == "interfaces") {
                if (valueNode !is MappingNode) {
                    errors += error(valueNode, "interfaces must be a mapping")
                } else {
                    for (rule in valueNode.value) {
                        val v = rule.valueNode as? ScalarNode
                        if (v == null || v.value !in listOf("true", "false")) {
                            errors += error(rule.valueNode, "interface rule must be boolean")
                        }
                    }
                }
            } else {
                val v = valueNode as? ScalarNode
                if (v == null || v.value !in listOf("true", "false")) {
                    errors += error(valueNode, "subnet rule must be boolean")
                }
            }
        }
    }

    private fun validateRequiredKeys(root: MappingNode, errors: MutableList<LexResult.LintError>) {
        val required = setOf("pki", "lighthouse", "static_host_map")
        val existing = root.value.mapNotNull { it.keyNode as? ScalarNode }.map { it.value }.toSet()

        for (key in required) {
            if (key !in existing) {
                errors += LexResult.LintError(
                    "Missing required key: $key", null, null, LexResult.LintError.Severity.ERROR
                )
            }
        }
    }

    private fun MappingNode.findKey(name: String): NodeTuple? =
        value.firstOrNull { (it.keyNode as? ScalarNode)?.value == name }

    private fun error(node: Node, message: String): LexResult.LintError = LexResult.LintError(
        message,
        node.startMark?.getOrNull()?.line?.plus(1),
        node.startMark?.getOrNull()?.column?.plus(1),
        LexResult.LintError.Severity.ERROR
    )
}