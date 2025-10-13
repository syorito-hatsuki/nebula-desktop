package dev.syoritohatsuki.nebuladesktop.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.util.NebulaManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainWindowViewModel() : ViewModel() {

    val manager = NebulaManager
    // Expose immutable view of manager data
    val connections = manager.connections

    val logLines = mutableStateListOf<AnnotatedString>()
    private val logCache = mutableMapOf<String, List<AnnotatedString>>() // connection name -> recent logs

    fun startConnection(name: String) = manager.startConnection(name)
    fun stopConnection(name: String) = manager.stopConnection(name)
    fun addConnection(connection: NebulaConnection) = manager.addConnection(connection)

    fun observeLogs(connection: NebulaConnection) {
        viewModelScope.launch {
            connection.logs.collectLatest { line ->
                logLines.add(0, line)
                if (logLines.size > 1000) logLines.removeLast()
                logCache[connection.name] = logLines.toList()
            }
        }
    }

    fun preloadLogs(connection: NebulaConnection) {
        logCache[connection.name]?.forEach { logLines.add(it) }
    }
}