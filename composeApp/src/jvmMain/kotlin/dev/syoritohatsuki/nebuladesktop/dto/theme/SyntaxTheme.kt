package dev.syoritohatsuki.nebuladesktop.dto.theme

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class SyntaxTheme(
    val name: String? = null,

    @Serializable(with = ColorAsHexSerializer::class) val key: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val plainScalar: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val string: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val blockString: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val number: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val boolean: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val flow: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val comment: Color? = null,
)
