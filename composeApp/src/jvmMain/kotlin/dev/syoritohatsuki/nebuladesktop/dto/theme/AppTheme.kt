package dev.syoritohatsuki.nebuladesktop.dto.theme

import androidx.compose.ui.graphics.Color
import dev.syoritohatsuki.nebuladesktop.ui.*
import kotlinx.serialization.Serializable

@Serializable
data class AppTheme(
    val name: String? = null,

    // Backgrounds
    @Serializable(with = ColorAsHexSerializer::class) val background: Color = NEUTRAL_10,
    @Serializable(with = ColorAsHexSerializer::class) val surface: Color = NEUTRAL_17,
    @Serializable(with = ColorAsHexSerializer::class) val surfaceVariant: Color = NEUTRAL_22,
    @Serializable(with = ColorAsHexSerializer::class) val sidebar: Color = NEUTRAL_12,
    @Serializable(with = ColorAsHexSerializer::class) val logsBackground: Color = NEUTRAL_6,

    // Borders
    @Serializable(with = ColorAsHexSerializer::class) val borderSubtle: Color = NEUTRAL_22,

    // Text
    @Serializable(with = ColorAsHexSerializer::class) val textPrimary: Color = NEUTRAL_100,
    @Serializable(with = ColorAsHexSerializer::class) val textSecondary: Color = NEUTRAL_80,

    // States
    @Serializable(with = ColorAsHexSerializer::class) val success: Color = EMERALD,
    @Serializable(with = ColorAsHexSerializer::class) val error: Color = ALIZARIN,
    @Serializable(with = ColorAsHexSerializer::class) val warning: Color = CARROT,
    @Serializable(with = ColorAsHexSerializer::class) val info: Color = PETER_RIVER,

    // Progress
    @Serializable(with = ColorAsHexSerializer::class) val progress: Color = PETER_RIVER,
    @Serializable(with = ColorAsHexSerializer::class) val progressTrack: Color = BELIZE_HOLE
)