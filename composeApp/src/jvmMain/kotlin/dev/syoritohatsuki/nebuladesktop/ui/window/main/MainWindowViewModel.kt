package dev.syoritohatsuki.nebuladesktop.ui.window.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection.ConnectionStatus
import dev.syoritohatsuki.nebuladesktop.util.NebulaManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Path

object MainWindowViewModel : ViewModel() {
    val manager = NebulaManager

    val connections: StateFlow<List<NebulaConnection>> =
        manager.connections.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _statusFlows = MutableStateFlow<Map<String, StateFlow<ConnectionStatus>>>(emptyMap())
    val statusFlows: StateFlow<Map<String, StateFlow<ConnectionStatus>>> = _statusFlows.asStateFlow()

    val logLines = mutableStateListOf<AnnotatedString>()
    private val logCache = mutableMapOf<String, List<AnnotatedString>>()

    private var logsJob: Job? = null
    private var observingConnection: String? = null

    init {
        viewModelScope.launch {
            connections.collectLatest { list ->
                _statusFlows.value = list.associate { it.name to it.status }
            }
        }
    }

    fun startConnection(configFilePath: Path) = manager.startConnection(configFilePath)

    fun stopConnection(configFilePath: Path) = manager.stopConnection(configFilePath)

    fun addConnection(configFile: File): Boolean = manager.addConnection(configFile)

    fun observeLogs(connection: NebulaConnection) {
        if (observingConnection == connection.name) return
        logsJob?.cancel()
        observingConnection = connection.name
        logsJob = viewModelScope.launch {
            connection.logs.collectLatest { line ->
                if (logLines.isEmpty() || logLines.firstOrNull() != line) {
                    logLines.add(0, line)
                    if (logLines.size > 1000) logLines.removeLast()
                    logCache[connection.name] = logLines.toList()
                }
            }
        }
    }

    fun preloadLogs(connection: NebulaConnection) {
        logLines.clear()
        logCache[connection.name]?.forEach { logLines.add(it) }
    }
}