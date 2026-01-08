package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.syoritohatsuki.nebuladesktop.dto.LexResult
import dev.syoritohatsuki.nebuladesktop.util.editor.YamlLexer
import dev.syoritohatsuki.nebuladesktop.util.editor.YamlSchemaValidator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import java.nio.file.Path
import kotlin.io.path.readText

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class YamlEditorViewModel(configPath: Path) : ViewModel() {

    private val _text = MutableStateFlow(configPath.readText())
    val text: StateFlow<String> = _text

    private val _tokens = MutableStateFlow<List<LexResult.YamlToken>>(emptyList())
    val tokens: StateFlow<List<LexResult.YamlToken>> = _tokens

    private val _errors = MutableStateFlow<List<LexResult.LintError>>(emptyList())
    val errors: StateFlow<List<LexResult.LintError>> = _errors


    fun onTextChange(newText: String) {
        _text.value = newText
    }

    init {
        viewModelScope.launch {
            _text.mapLatest { text ->
                withContext(Dispatchers.Default) {
                    val lex = YamlLexer.lex(text)
                    val schema = YamlSchemaValidator.validate(text)
                    lex.tokens to (lex.errors + schema)
                }
            }.collect { (tokens, errors) ->
                _tokens.value = tokens
                _errors.value = errors
            }
        }
    }
}