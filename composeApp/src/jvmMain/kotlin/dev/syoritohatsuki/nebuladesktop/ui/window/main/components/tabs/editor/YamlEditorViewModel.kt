package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.tabs.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.syoritohatsuki.nebuladesktop.dto.YamlToken
import dev.syoritohatsuki.nebuladesktop.util.editor.YamlTokenizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
            _text.debounce(300).distinctUntilChanged().mapLatest {
                runCatching {
                    YamlTokenizer.tokenize(it)
                }.getOrDefault(emptyList())
            }.collect {
                _tokens.value = it
            }
        }
    }
}