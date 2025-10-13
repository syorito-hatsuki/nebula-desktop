package dev.syoritohatsuki.nebuladesktop.dto

import androidx.compose.ui.text.AnnotatedString
import com.pty4j.PtyProcess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

data class NebulaConnection(
    val name: String,
    val configPath: Path,
    var status: ConnectionStatus = ConnectionStatus.OFF,
    val metadata: MutableMap<String, String> = ConcurrentHashMap(),
    private val _logs: MutableSharedFlow<AnnotatedString> = MutableSharedFlow(extraBufferCapacity = 1000),
    var process: PtyProcess? = null
) {
    val logs: SharedFlow<AnnotatedString> = _logs

    fun emitLog(line: AnnotatedString) {
        _logs.tryEmit(line)
    }

    enum class ConnectionStatus { ON, OFF }
}
