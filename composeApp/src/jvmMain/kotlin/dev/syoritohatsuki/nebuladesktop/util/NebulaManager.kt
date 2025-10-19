package dev.syoritohatsuki.nebuladesktop.util

import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.process.NebulaUnix
import dev.syoritohatsuki.nebuladesktop.process.NebulaWinshit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Paths

object NebulaManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connections: MutableStateFlow<List<NebulaConnection>> = MutableStateFlow(
        listOf(
            NebulaConnection(
                name = "Linux Test Connection",
                configPath = Paths.get(System.getProperty("user.home"), ".config", "nebula", "nebula.yml"),
            ),
            NebulaConnection(
                name = "Windows Test Connection",
                configPath = Paths.get(
                    System.getenv("APPDATA") ?: System.getProperty("user.home"), "NebulaTray", "configs", "nebula.yml"
                ),
            )
        )
    )

    val connections: StateFlow<List<NebulaConnection>> = _connections.asStateFlow()

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            _connections.value.forEach { stopConnection(it.name) }
        })
    }

    fun addConnection(connection: NebulaConnection): Boolean {
        if (_connections.value.any { it.name == connection.name }) return false
        _connections.update { it + connection }
        return true
    }

    fun startConnection(name: String) {
        val connection = _connections.value.find { it.name == name } ?: return
        if (connection.status.value == NebulaConnection.ConnectionStatus.ON) {
            connection.emitLog("Already connected".toAnnotatedString())
            return
        }

        val os = System.getProperty("os.name").lowercase()
        scope.launch {
            try {
                var currentProcess: Process? = when {
                    os.contains("win") -> NebulaWinshit.start(connection.configPath)
                    os.contains("nux") || os.contains("mac") -> NebulaUnix.start(connection.configPath)
                    else -> throw UnsupportedOperationException("Unsupported OS: $os, please contact support")
                }

                if (currentProcess == null) {
                    connection.emitLog("Failed to start process".toAnnotatedString())
                    return@launch
                }

                connection.process = currentProcess
                connection.setStatus(NebulaConnection.ConnectionStatus.ON)
                emitListIdentity()

                while (currentProcess != null) {
                    val reader = BufferedReader(InputStreamReader(currentProcess.inputStream))
                    var restartRequired = false

                    reader.forEachLine { line ->
                        connection.emitLog(line.ansiToAnnotatedString())
                        if (line.contains("operation not permitted", ignoreCase = true)) {
                            connection.emitLog("Permission issue detected, restarting with elevation...".toAnnotatedString())
                            currentProcess?.destroy()
                            restartRequired = true
                            return@forEachLine
                        }
                    }

                    currentProcess.waitFor()

                    if (restartRequired) {
                        currentProcess = when {
                            os.contains("win") -> NebulaWinshit.start(connection.configPath)
                            os.contains("nux") || os.contains("mac") -> NebulaUnix.start(connection.configPath)
                            else -> throw UnsupportedOperationException("Unsupported OS: $os, please contact support")
                        }
                        if (currentProcess == null) break
                        connection.process = currentProcess
                        connection.setStatus(NebulaConnection.ConnectionStatus.ON)
                        emitListIdentity()
                    } else {
                        break
                    }
                }
            } catch (e: Exception) {
                connection.emitLog("Failed to start Nebula: ${e.message}".toAnnotatedString())
            } finally {
                connection.setStatus(NebulaConnection.ConnectionStatus.OFF)
                connection.process = null
                emitListIdentity()
            }
        }
    }

    fun stopConnection(name: String) {
        val conn = _connections.value.find { it.name == name } ?: return
        val process = conn.process ?: return
        try {
            conn.emitLog("Stopping Nebula (pid=${process.pid()})...".toAnnotatedString())
            process.destroy()
            if (process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                conn.setStatus(NebulaConnection.ConnectionStatus.OFF)
                conn.process = null
                emitListIdentity()
                return
            }
            when {
                System.getProperty("os.name").lowercase().contains("win") -> NebulaWinshit.stop(process)
                else -> NebulaUnix.stop(process)
            }
        } catch (e: Exception) {
            conn.emitLog("Failed to stop Nebula: ${e.message}".toAnnotatedString())
        } finally {
            conn.setStatus(NebulaConnection.ConnectionStatus.OFF)
            conn.process = null
            emitListIdentity()
        }
    }

    private fun emitListIdentity() {
        _connections.update { it.toList() }
    }
}