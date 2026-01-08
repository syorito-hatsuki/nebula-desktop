package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.syoritohatsuki.nebuladesktop.dto.YamlToken
import dev.syoritohatsuki.nebuladesktop.util.editor.YamlLexer
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

    private val _tokens = MutableStateFlow<List<YamlToken>>(emptyList())
    val tokens: StateFlow<List<YamlToken>> = _tokens

    fun onTextChange(newText: String) {
        _text.value = newText
    }

    init {
        viewModelScope.launch {
            _text.mapLatest {
                withContext(Dispatchers.Default) {
                    runCatching { YamlLexer.lex(it) }.getOrDefault(emptyList())
                }
            }.collect {
                _tokens.value = it
            }
        }
    }
}