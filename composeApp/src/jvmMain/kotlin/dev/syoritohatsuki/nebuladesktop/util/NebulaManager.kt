package dev.syoritohatsuki.nebuladesktop.util

import com.pty4j.PtyProcess
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths

object NebulaManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connections = MutableStateFlow<List<NebulaConnection>>(
        listOf(
            NebulaConnection(
                name = "Linux Test Connection",
                configPath = Paths.get(System.getProperty("user.home"), ".config", "nebula", "nebula.yml"),
            ), NebulaConnection(
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

    fun waitForAllProcessesToExit(timeoutMillis: Long = 5000): Boolean = runBlocking {
        val processes = _connections.value.mapNotNull { it.process }
        if (processes.isEmpty()) return@runBlocking true

        val jobs = processes.map { process ->
            withTimeoutOrNull(timeoutMillis) {
                while (process.isAlive) {
                    delay(100)
                }
            }
        }

        jobs.all { it != null }
    }

    fun addConnection(connection: NebulaConnection): Boolean {
        if (_connections.value.any { it.name == connection.name }) return false
        _connections.update { it + connection }
        return true
    }

//    fun startConnection(name: String) {
//        val connection = _connections.value.find { it.name == name } ?: return
//        if (connection.status == NebulaConnection.ConnectionStatus.ON) {
//            connection.logs.tryEmit("${connection.name} already connected".toAnnotatedString())
//            return
//        }
//        scope.launch {
//            try {
//                var process = try {
//                    ProcessBuilder(
//                        StorageManager.nebulaBinaryPath.absolutePathString(),
//                        "-config",
//                        connection.configPath.toString()
//                    )
//                        .redirectErrorStream(true)
//                        .start()
//                } catch (e: IOException) {
//                    connection.logs.tryEmit("Standard start failed: ${e.message}".toAnnotatedString())
//                    null
//                }
//                if (process == null || !process.isAlive) {
//                    connection.logs.tryEmit("Attempting elevated start...".toAnnotatedString())
//                    process = ProcessLauncher.runNebulaWithElevation(connection.configPath, StorageManager.nebulaBinaryPath)
//                }
//                connection.process = process
//                connection.status = NebulaConnection.ConnectionStatus.ON
//                emitUpdated(connection)
//
//                process.inputStream.bufferedReader().useLines { lines ->
//                    lines.forEach { line ->
//                        val styledLine = ansiToAnnotatedString(line)
//                        connection.logs.tryEmit(styledLine)
//                        if (styledLine.contains("operation not permitted", ignoreCase = true)) {
//                            connection.logs.tryEmit("Permission issue detected, restarting with elevation...".toAnnotatedString())
//                            process.destroy()
//                            val elevated = ProcessLauncher.runNebulaWithElevation(connection.configPath, StorageManager.nebulaBinaryPath)
//                            connection.process = elevated
//                            elevated.inputStream.bufferedReader().useLines { elevatedLines ->
//                                elevatedLines.forEach { elevatedLine ->
//                                    val styledElevatedLines = ansiToAnnotatedString(elevatedLine)
//                                    connection.logs.tryEmit(styledElevatedLines)
//                                }
//                            }
//                            elevated.waitFor()
//                            return@launch
//                        }
//                    }
//                }
//                process.waitFor()
//            } catch (e: Exception) {
//                connection.logs.tryEmit("Failed to start Nebula: ${e.message}".toAnnotatedString())
//            } finally {
//                connection.status = NebulaConnection.ConnectionStatus.OFF
//                connection.process = null
//                emitUpdated(connection)
//            }
//        }
//    }
//
//    fun stopConnection(name: String) {
//        val conn = _connections.value.find { it.name == name } ?: return
//        val process = conn.process ?: return
//        try {
//            conn.logs.tryEmit("Stopping Nebula (pid=${process.pid()})...".toAnnotatedString())
//            process.destroy()
//            if (process.waitFor(1000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
//                conn.status = NebulaConnection.ConnectionStatus.OFF
//                conn.process = null
//                emitUpdated(conn)
//                return
//            }
//            val pid = process.pid()
//            val os = System.getProperty("os.name").lowercase()
//            when {
//                os.contains("win") -> Runtime.getRuntime().exec(arrayOf("taskkill", "/PID", pid.toString(), "/T", "/F"))
//                os.contains("mac") -> {
//                    val cmd = "osascript -e 'do shell script \"kill -TERM $pid\" with administrator privileges'"
//                    Runtime.getRuntime().exec(arrayOf("bash", "-c", cmd))
//                }
//                os.contains("nux") -> when {
//                    File("/usr/bin/pkexec").exists() -> Runtime.getRuntime().exec(arrayOf("pkexec", "kill", "-TERM", pid.toString()))
//                    else -> Runtime.getRuntime().exec(arrayOf("sudo", "kill", "-TERM", pid.toString()))
//                }
//            }
//        } catch (e: Exception) {
//            conn.logs.tryEmit("Failed to stop Nebula: ${e.message}".toAnnotatedString())
//        } finally {
//            conn.status = NebulaConnection.ConnectionStatus.OFF
//            conn.process = null
//            emitUpdated(conn)
//        }
//    }

//    fun startConnection(name: String) {
//        val connection = _connections.value.find { it.name == name } ?: return
//        if (connection.status == NebulaConnection.ConnectionStatus.ON) {
//            connection.logs.tryEmit("${connection.name} already connected".toAnnotatedString())
//            return
//        }
//
//        scope.launch {
//            try {
//                // Start the process normally first
//                var process = try {
//                    val env = HashMap(System.getenv())
//                    env["TERM"] = "xterm-256color" // Terminal emulation
//                    PtyProcessBuilder(
//                        arrayOf(
//                            StorageManager.nebulaBinaryPath.absolutePathString(),
//                            "-config",
//                            connection.configPath.toString()
//                        )
//                    ).setEnvironment(env)
//                        .setConsole(true)
//                        .start()
//                } catch (e: Exception) {
//                    connection.logs.tryEmit("Standard start failed: ${e.message}".toAnnotatedString())
//                    null
//                }
//
//                if (process == null || !process.isAlive) {
//                    connection.logs.tryEmit("Attempting elevated start...".toAnnotatedString())
//                    // Run elevated process inside PTY to preserve ANSI colors
//                    val ptyProcess: PtyProcess? =
//                        ProcessLauncher.runNebulaWithElevation(connection.configPath, StorageManager.nebulaBinaryPath)
//                    if (ptyProcess == null) {
//                        connection.logs.tryEmit("Failed to start elevated process".toAnnotatedString())
//                        return@launch
//                    }
//                    process = ptyProcess
//                }
//
//                connection.process = process
//                connection.status = NebulaConnection.ConnectionStatus.ON
//                emitUpdated(connection)
//
//                // Read process output with BufferedReader from PTY input stream
//                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
//                    while (true) {
//                        val line = reader.readLine() ?: break
//                        val styledLine = ansiToAnnotatedString(line)
//                        connection.logs.tryEmit(styledLine)
//
//                        if (line.contains("operation not permitted", ignoreCase = true)) {
//                            connection.logs.tryEmit("Permission issue detected, restarting with elevation...".toAnnotatedString())
//                            process.destroy()
//                            val elevated = ProcessLauncher.runNebulaWithElevation(
//                                connection.configPath,
//                                StorageManager.nebulaBinaryPath
//                            )
//                            connection.process = elevated
//                            if (elevated == null) break
//                            BufferedReader(InputStreamReader(elevated.inputStream)).use { elevatedReader ->
//                                elevatedReader.forEachLine { elevatedLine ->
//                                    connection.logs.tryEmit(ansiToAnnotatedString(elevatedLine))
//                                }
//                            }
//                            elevated.waitFor()
//                            return@launch
//                        }
//                    }
//                }
//
//                process.waitFor()
//            } catch (e: Exception) {
//                connection.logs.tryEmit("Failed to start Nebula: ${e.message}".toAnnotatedString())
//            } finally {
//                connection.status = NebulaConnection.ConnectionStatus.OFF
//                connection.process = null
//                emitUpdated(connection)
//            }
//        }
//    }
//
//    fun stopConnection(name: String) {
//        val conn = _connections.value.find { it.name == name } ?: return
//        val process = conn.process ?: return
//        try {
//            conn.logs.tryEmit("Stopping Nebula (pid=${process.pid()})...".toAnnotatedString())
//            process.destroy()
//            if (process.waitFor(1000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
//                conn.status = NebulaConnection.ConnectionStatus.OFF
//                conn.process = null
//                emitUpdated(conn)
//                return
//            }
//            // Forced kill by OS if graceful stop fails
//            val pid = process.pid()
//            val os = System.getProperty("os.name").lowercase()
//            when {
//                os.contains("win") -> Runtime.getRuntime().exec(arrayOf("taskkill", "/PID", pid.toString(), "/T", "/F"))
//                os.contains("mac") -> {
//                    val cmd = "osascript -e 'do shell script \"kill -TERM $pid\" with administrator privileges'"
//                    Runtime.getRuntime().exec(arrayOf("bash", "-c", cmd))
//                }
//
//                os.contains("nux") -> when {
//                    File("/usr/bin/pkexec").exists() -> Runtime.getRuntime()
//                        .exec(arrayOf("pkexec", "kill", "-TERM", pid.toString()))
//
//                    else -> Runtime.getRuntime().exec(arrayOf("sudo", "kill", "-TERM", pid.toString()))
//                }
//            }
//        } catch (e: Exception) {
//            conn.logs.tryEmit("Failed to stop Nebula: ${e.message}".toAnnotatedString())
//        } finally {
//            conn.status = NebulaConnection.ConnectionStatus.OFF
//            conn.process = null
//            emitUpdated(conn)
//        }
//    }


    fun startConnection(name: String) {
        val connection = _connections.value.find { it.name == name } ?: return
        if (connection.status == NebulaConnection.ConnectionStatus.ON) {
            connection.emitLog("Already connected".toAnnotatedString())
            return
        }

        scope.launch {
            try {
                val ptyProcess: PtyProcess? =
                    ProcessHandler.startNebula(connection.configPath)
                if (ptyProcess == null) {
                    connection.emitLog("Failed to start elevated process".toAnnotatedString())
                    return@launch
                }

                connection.process = ptyProcess
                connection.status = NebulaConnection.ConnectionStatus.ON
                emitUpdated(connection)

                while (true) {
                    val line = reader.readLine() ?: break
                    val styledLine = line.ansiToAnnotatedString()
                    connection.emitLog(styledLine)

                    if (line.contains("operation not permitted", ignoreCase = true)) {
                        connection.emitLog("Permission issue detected, restarting with elevation...".toAnnotatedString())
                        ptyProcess.destroy()
                        val elevated = ProcessHandler.startNebula(connection.configPath)
                        connection.process = elevated
                        if (elevated == null) break
                        BufferedReader(InputStreamReader(elevated.inputStream)).use { elevatedReader ->
                            elevatedReader.forEachLine { elevatedLine ->
                                connection.emitLog(elevatedLine.ansiToAnnotatedString())
                            }
                        }
                        elevated.waitFor()
                        return@launch
                    }
                }

                ptyProcess.waitFor()
            } catch (e: Exception) {
                connection.emitLog("Failed to start Nebula: ${e.message}".toAnnotatedString())
            } finally {
                connection.status = NebulaConnection.ConnectionStatus.OFF
                connection.process = null
                emitUpdated(connection)
            }
        }
    }

    fun stopConnection(name: String) {
        val conn = _connections.value.find { it.name == name } ?: return
        val process = conn.process ?: return

        try {
            conn.emitLog("Stopping Nebula (pid=${process.pid()})...".toAnnotatedString())

            // 1) Graceful stop
            process.destroy() // POSIX: SIGTERM
            if (process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                conn.status = NebulaConnection.ConnectionStatus.OFF
                conn.process = null
                emitUpdated(conn)
                return
            }

            val pid = process.pid()
            val os = System.getProperty("os.name").lowercase()

            // 2) Elevated TERM if needed, per OS
            when {
                os.contains("win") -> {
                    // Prefer Java API first; fallback to taskkill if still alive
                    process.destroyForcibly()
                    if (!process.waitFor(500, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                        ProcessHandler.stopNebula(pid)
                    }
                }
                else -> {
                    ProcessHandler.stopNebula(pid)
                    process.destroyForcibly()
                }
            }

            // 3) Final wait and hard kill if possible
            if (!process.waitFor(1500, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                process.destroyForcibly()
                process.waitFor()
            }

        } catch (e: Exception) {
            conn.emitLog("Failed to stop Nebula: ${e.message}".toAnnotatedString())
        } finally {
            conn.status = NebulaConnection.ConnectionStatus.OFF
            conn.process = null
            emitUpdated(conn)
        }
    }
    private fun emitUpdated(connection: NebulaConnection) {
        _connections.update { list ->
            list.map { if (it.name == connection.name) connection else it }
        }
    }
}