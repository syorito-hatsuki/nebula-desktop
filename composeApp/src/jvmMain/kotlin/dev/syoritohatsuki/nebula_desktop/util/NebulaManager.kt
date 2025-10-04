package dev.syoritohatsuki.nebula_desktop.util

import dev.syoritohatsuki.nebula_desktop.dto.NebulaConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object NebulaManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connections = MutableStateFlow<List<NebulaConnection>>(emptyList())
    val connections: StateFlow<List<NebulaConnection>> = _connections.asStateFlow()

    fun addConnection(connection: NebulaConnection): Boolean {
        if (_connections.value.any { it.name == connection.name }) return false
        _connections.update { it + connection }
        return true
    }

    fun startConnection(name: String) {
        val connection = _connections.value.find { it.name == name } ?: return
        if (connection.status == NebulaConnection.ConnectionStatus.ON) return

        scope.launch {
            val process = ProcessBuilder("nebula", "-config", connection.configPath.toString())
                .redirectErrorStream(true)
                .start()

            connection.process = process
            connection.status = NebulaConnection.ConnectionStatus.ON
            emitUpdated(connection)

            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    connection.logs.tryEmit(line)
                }
            }

            process.waitFor()
            connection.status = NebulaConnection.ConnectionStatus.OFF
            connection.process = null
            emitUpdated(connection)
        }
    }

    private fun emitUpdated(connection: NebulaConnection) {
        _connections.update { list ->
            list.map { if (it.name == connection.name) connection else it }
        }
    }

    fun stopConnection(name: String) {
        val conn = _connections.value.find { it.name == name } ?: return
        conn.process?.destroy()
        conn.status = NebulaConnection.ConnectionStatus.OFF
        emitUpdated(conn)
    }
}