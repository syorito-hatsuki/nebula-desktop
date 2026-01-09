package dev.syoritohatsuki.nebuladesktop.dto.theme

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class AppTheme(
    val name: String? = null,

    // Backgrounds
    @Serializable(with = ColorAsHexSerializer::class) val background: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val surface: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val surfaceVariant: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val sidebar: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val logsBackground: Color? = null,

    // Borders
    @Serializable(with = ColorAsHexSerializer::class) val borderSubtle: Color? = null,

    // Text
    @Serializable(with = ColorAsHexSerializer::class) val textPrimary: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val textSecondary: Color? = null,

    // States
    @Serializable(with = ColorAsHexSerializer::class) val success: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val error: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val warning: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val info: Color? = null,

    // Progress
    @Serializable(with = ColorAsHexSerializer::class) val progress: Color? = null,
    @Serializable(with = ColorAsHexSerializer::class) val progressTrack: Color? = null
)