package dev.syoritohatsuki.nebuladesktop.util

import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.process.NebulaUnix
import dev.syoritohatsuki.nebuladesktop.process.NebulaWinshit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    fun startConnection(configFilePath: Path) {
        val conn = _connections.value.find { it.configPath.absolutePathString() == configFilePath.absolutePathString() }
            ?: return
        if (conn.status.value == NebulaConnection.ConnectionStatus.ENABLED || conn.status.value == NebulaConnection.ConnectionStatus.STARTING) return

        scope.launch {
            conn.setStatus(NebulaConnection.ConnectionStatus.STARTING)
            emitListIdentity()

            val os = System.getProperty("os.name").lowercase()
            var proc: Process?

            try {
                proc = when {
                    os.contains("win") -> NebulaWinshit.start(conn.configPath)
                    os.contains("nux") || os.contains("mac") -> NebulaUnix.start(conn.configPath)
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
        val conn = _connections.value.find { it.configPath.absolutePathString() == configFilePath.absolutePathString() }
            ?: return
        val proc = conn.process ?: run {
            conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
            emitListIdentity()
            return
        }

        scope.launch {
            conn.emitLog("Stopping Nebula (pid=${proc.pid()})...".toAnnotatedString())
            conn.setStatus(NebulaConnection.ConnectionStatus.STOPPING)
            emitListIdentity()

            NebulaUnix.stop(proc)

            launch {
                proc.inputStream.bufferedReader().forEachLine {
                    conn.emitLog(it.ansiToAnnotatedString())
                }
            }

            launch {
                proc.errorStream.bufferedReader().forEachLine {
                    conn.emitLog(it.ansiToAnnotatedString())
                }
            }

            val deadline = System.currentTimeMillis() + 15_000 // 15s safety
            while (proc.isAlive && System.currentTimeMillis() < deadline) {
                delay(100)
            }

            if (proc.isAlive) {
                conn.emitLog("Process did not exit in time; still running (pid=${proc.pid()}).".toAnnotatedString())
                emitListIdentity()
                return@launch
            }

            conn.setStatus(NebulaConnection.ConnectionStatus.DISABLED)
            conn.process = null
            emitListIdentity()
        }
    }


    private fun emitListIdentity() {
        _connections.update { it.toList() }
    }
}