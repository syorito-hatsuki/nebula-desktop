package dev.syoritohatsuki.nebuladesktop.dto.theme

import androidx.compose.ui.graphics.Color
import dev.syoritohatsuki.nebuladesktop.ui.*
import kotlinx.serialization.Serializable

@Serializable
data class SyntaxTheme(
    val name: String? = null,

    @Serializable(with = ColorAsHexSerializer::class) val key: Color = WISTERIA,
    @Serializable(with = ColorAsHexSerializer::class) val plainScalar: Color = BELIZE_HOLE,
    @Serializable(with = ColorAsHexSerializer::class) val string: Color = GREEN_SEA,
    @Serializable(with = ColorAsHexSerializer::class) val blockString: Color = GREEN_SEA,
    @Serializable(with = ColorAsHexSerializer::class) val number: Color = BELIZE_HOLE,
    @Serializable(with = ColorAsHexSerializer::class) val boolean: Color = CARROT,
    @Serializable(with = ColorAsHexSerializer::class) val flow: Color = CARROT,
    @Serializable(with = ColorAsHexSerializer::class) val comment: Color = ASBESTOS,
    @Serializable(with = ColorAsHexSerializer::class) val operator: Color = Color(0xFFFF0000),
    @Serializable(with = ColorAsHexSerializer::class) val keyword: Color = Color(0xFF00FF00),
)
