package dev.syoritohatsuki.nebuladesktop.dto

import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.nio.file.Path
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class NebulaConnection(
    @EncodeDefault val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    @Serializable(with = PathAsStringSerializer::class) val configPath: Path,
    @Transient private val _status: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.DISABLED),
    @Transient private val _logs: MutableSharedFlow<AnnotatedString> = MutableSharedFlow(extraBufferCapacity = 1000),
    @Transient var process: Process? = null
) {
    @Transient
    val status: StateFlow<ConnectionStatus> = _status

    @Transient
    val logs: SharedFlow<AnnotatedString> = _logs

    fun emitLog(line: AnnotatedString) {
        _logs.tryEmit(line)
    }

    fun setStatus(status: ConnectionStatus) {
        _status.value = status
    }

    enum class ConnectionStatus { STARTING, ENABLED, STOPPING, DELETING, DISABLED }

    object PathAsStringSerializer : KSerializer<Path> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Path", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Path) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): Path {
            return Path.of(decoder.decodeString())
        }
    }
}