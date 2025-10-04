package dev.syoritohatsuki.nebula_desktop.dto

import kotlinx.coroutines.flow.MutableSharedFlow
import java.nio.file.Path

data class NebulaConnection(
    val name: String,
    val configPath: Path,
    var status: ConnectionStatus = ConnectionStatus.OFF,
    val logs: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 1000),
    var process: Process? = null
) {
    enum class ConnectionStatus { ON, OFF }
}
