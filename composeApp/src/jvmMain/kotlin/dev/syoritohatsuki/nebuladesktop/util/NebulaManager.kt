package dev.syoritohatsuki.nebuladesktop.util

import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

object NebulaManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connections = MutableStateFlow<List<NebulaConnection>>(listOf(
        NebulaConnection(
            name = "Test Connection",
            configPath = Paths.get(System.getProperty("user.home"), ".config", "nebula", "nebula.yml"),
        )
    ))
    val connections: StateFlow<List<NebulaConnection>> = _connections.asStateFlow()

    fun addConnection(connection: NebulaConnection): Boolean {
        if (_connections.value.any { it.name == connection.name }) return false
        _connections.update { it + connection }
        return true
    }

    fun startConnection(name: String) {
        val connection = _connections.value.find { it.name == name } ?: return
        if (connection.status == NebulaConnection.ConnectionStatus.ON) {
            connection.logs.tryEmit("${connection.name} already connected")
            return
        }

        scope.launch {
            try {
                var process = try {
                    ProcessBuilder(
                        StorageManager.nebulaBinaryPath.absolutePathString(),
                        "-config",
                        connection.configPath.toString()
                    )
                        .redirectErrorStream(true)
                        .start()
                } catch (e: IOException) {
                    connection.logs.tryEmit("Standard start failed: ${e.message}")
                    null
                }

                if (process == null || !process.isAlive) {
                    connection.logs.tryEmit("Attempting elevated start...")
                    process = ProcessLauncher.runNebulaWithElevation(
                        connection.configPath,
                        StorageManager.nebulaBinaryPath
                    )
                }

                connection.process = process
                connection.status = NebulaConnection.ConnectionStatus.ON
                emitUpdated(connection)

                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        connection.logs.tryEmit(line)

                        // Detect permission error
                        if (line.contains("operation not permitted", ignoreCase = true)) {
                            connection.logs.tryEmit("Permission issue detected, restarting with elevation...")
                            process.destroy()
                            val elevated = ProcessLauncher.runNebulaWithElevation(
                                connection.configPath,
                                StorageManager.nebulaBinaryPath
                            )
                            connection.process = elevated
                            elevated.inputStream.bufferedReader().useLines { elevatedLines ->
                                elevatedLines.forEach { elevatedLine ->
                                    connection.logs.tryEmit(elevatedLine)
                                }
                            }
                            elevated.waitFor()
                        }
                    }
                }

                process.waitFor()
                connection.status = NebulaConnection.ConnectionStatus.OFF
                connection.process = null
                emitUpdated(connection)
            } catch (e: Exception) {
                connection.logs.tryEmit("Failed to start Nebula: ${e.message}")
            }
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