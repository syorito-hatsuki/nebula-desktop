package dev.syoritohatsuki.nebuladesktop.dto

import androidx.compose.ui.text.AnnotatedString
import com.pty4j.PtyProcess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

data class NebulaConnection(
    val name: String,
    val configPath: Path,
    private val _status: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.OFF),
    val metadata: MutableMap<String, String> = ConcurrentHashMap(),
    private val _logs: MutableSharedFlow<AnnotatedString> = MutableSharedFlow(extraBufferCapacity = 1000),
    var process: Process? = null
) {
    val status: StateFlow<ConnectionStatus> = _status
    val logs: SharedFlow<AnnotatedString> = _logs

    fun emitLog(line: AnnotatedString) {
        _logs.tryEmit(line)
    }

    fun setStatus(status: ConnectionStatus) {
        _status.value = status
    }

    enum class ConnectionStatus { ON, OFF }
}