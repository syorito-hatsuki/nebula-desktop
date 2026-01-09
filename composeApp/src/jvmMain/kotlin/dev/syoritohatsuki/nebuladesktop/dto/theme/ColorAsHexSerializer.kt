package dev.syoritohatsuki.nebuladesktop.dto.theme

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorAsHexSerializer : KSerializer<Color> {
    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Color {
        val value = decoder.decodeString()
        val hex = value.removePrefix("#")

        return Color(
            when (hex.length) {
                6 -> 0xFF000000 or hex.toLong(16)
                8 -> hex.toLong(16)
                else -> error("Invalid color format: $value")
            }
        )
    }

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString("#" + value.value.toString(16).padStart(8, '0'))
    }
}