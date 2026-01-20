package dev.syoritohatsuki.nebuladesktop.util

import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.process.NebulaUnix
import dev.syoritohatsuki.nebuladesktop.process.NebulaWinshit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object NebulaManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connections: MutableStateFlow<List<NebulaConnection>> =
        MutableStateFlow(StorageManager.loadConnections())

    val connections: StateFlow<List<NebulaConnection>> = _connections.asStateFlow()

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            _connections.value.forEach { stopConnection(it.configPath) }
        })
    }

    fun addConnection(configFile: File): Boolean {
        if (_connections.value.any { it.configPath.absolutePathString() == configFile.absolutePath }) return false

        val newConnection = NebulaConnection(
            name = configFile.nameWithoutExtension,
            configPath = configFile.toPath(),
        )

        _connections.update { it + newConnection }
        StorageManager.updateConnectionFile(_connections.value)
        return true
    }

    suspend fun deleteConnection(uuid: String): Boolean {
        val conn = _connections.value.find { it.uuid == uuid } ?: return false

        if (conn.status.value == NebulaConnection.ConnectionStatus.STARTING) {
            conn.emitLog("Cannot delete while starting".toAnnotatedString())
            return false
        }

        conn.setStatus(NebulaConnection.ConnectionStatus.DELETING)
        emitListIdentity()

        if (conn.process != null) {
            val stopped = stopConnectionAndWait(conn)

            if (!stopped) {
                conn.setStatus(NebulaConnection.ConnectionStatus.ENABLED)
                conn.emitLog(
                    "Deletion canceled: authorization denied or process still running"
                        .toAnnotatedString()
                )
                emitListIdentity()
                return false
            }
        }

        _connections.update { it - conn }
        StorageManager.updateConnectionFile(_connections.value)
        return true
    }

    private suspend fun stopConnectionAndWait(conn: NebulaConnection): Boolean {
        val proc = conn.process ?: return true

        conn.setStatus(NebulaConnection.ConnectionStatus.STOPPING)
        emitListIdentity()

        return withContext(Dispatchers.IO) {
            val stopped = NebulaUnix.stop(proc)

            if (!stopped) {
                false
            } else {
                conn.process = null
                conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
                emitListIdentity()
                true
            }
        }
    }

    fun startConnection(configFilePath: Path) {
        val conn = _connections.value.find { it.configPath.absolutePathString() == configFilePath.absolutePathString() }
            ?: return
        if (conn.status.value == NebulaConnection.ConnectionStatus.ENABLED || conn.status.value == NebulaConnection.ConnectionStatus.STARTING) return

        scope.launch {
            conn.setStatus(NebulaConnection.ConnectionStatus.STARTING)
            emitListIdentity()

            var proc: Process?

            try {
                proc = when (hostOs) {
                    OS.Windows -> NebulaWinshit.start(conn.configPath)
                    OS.Linux, OS.MacOS -> NebulaUnix.start(conn.configPath)
                    else -> null
                }

                if (proc == null) {
                    conn.emitLog("Failed to start process".toAnnotatedString())
                    conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
                    emitListIdentity()
                    return@launch
                }

                conn.process = proc
                emitListIdentity()

                val outReader = launch {
                    proc.inputStream.bufferedReader().forEachLine { line ->
                        conn.emitLog(line.ansiToAnnotatedString())
                    }
                }

                val errReader = launch {
                    proc.errorStream.bufferedReader().forEachLine { line ->
                        conn.emitLog(line.ansiToAnnotatedString())
                    }
                }

                val startedAt = System.currentTimeMillis()
                val deadline = startedAt + 5000L
                var sawOutput = false

                val outputWatcher = launch {
                    while (isActive && System.currentTimeMillis() < deadline) {
                        if (!proc.isAlive) break
                        delay(150)
                        sawOutput = true
                        break
                    }
                }

                while (System.currentTimeMillis() < deadline) {
                    if (!proc.isAlive) break
                    if (sawOutput) break
                    delay(50)
                }
                outputWatcher.cancel()

                if (!proc.isAlive) {
                    conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
                    conn.process = null
                    emitListIdentity()
                    outReader.cancel()
                    errReader.cancel()
                    return@launch
                }

                conn.setStatus(NebulaConnection.ConnectionStatus.ENABLED)
                emitListIdentity()

                launch {
                    proc.waitFor()
                    conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
                    conn.process = null
                    emitListIdentity()
                    outReader.cancel()
                    errReader.cancel()
                }
            } catch (e: Exception) {
                conn.emitLog("Start error: ${e.message}".toAnnotatedString())
                conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
                conn.process = null
                emitListIdentity()
            }
        }
    }

    fun stopConnection(configFilePath: Path) {
        val conn = _connections.value.find {
            it.configPath.absolutePathString() == configFilePath.absolutePathString()
        } ?: return

        val proc = conn.process ?: run {
            conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
            emitListIdentity()
            return
        }

        scope.launch {
            conn.emitLog("Stopping Nebula (pid=${proc.pid()})...".toAnnotatedString())
            conn.setStatus(NebulaConnection.ConnectionStatus.STOPPING)
            emitListIdentity()

            val stopped = withContext(Dispatchers.IO) {
                NebulaUnix.stop(proc)
            }

            if (!stopped) {
                conn.emitLog(
                    "Stop canceled: authorization denied or process still running"
                        .toAnnotatedString()
                )
                conn.setStatus(NebulaConnection.ConnectionStatus.ENABLED)
                emitListIdentity()
                return@launch
            }

            val deadline = System.currentTimeMillis() + 15_000
            while (proc.isAlive && System.currentTimeMillis() < deadline) {
                delay(100)
            }

            if (proc.isAlive) {
                conn.emitLog(
                    "Process did not exit in time; still running (pid=${proc.pid()})."
                        .toAnnotatedString()
                )
                conn.setStatus(NebulaConnection.ConnectionStatus.ENABLED)
                emitListIdentity()
                return@launch
            }

            conn.process = null
            conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
            emitListIdentity()
        }
    }

    fun updateConnectionName(uuid: String, newName: String) {
        _connections.update { list ->
            list.map { conn ->
                if (conn.uuid == uuid) {
                    conn.copy(name = newName)
                } else conn
            }
        }

        StorageManager.updateConnectionFile(_connections.value)
    }

    private fun emitListIdentity() {
        _connections.update { it.toList() }
    }
}