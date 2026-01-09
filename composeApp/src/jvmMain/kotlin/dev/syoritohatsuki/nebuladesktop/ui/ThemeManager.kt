package dev.syoritohatsuki.nebuladesktop.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import dev.syoritohatsuki.nebuladesktop.dto.theme.AppTheme
import dev.syoritohatsuki.nebuladesktop.dto.theme.SyntaxTheme
import dev.syoritohatsuki.nebuladesktop.dto.theme.Type
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.io.path.Path

object ThemeManager {
    private val DefaultAppTheme = AppTheme(
        name = "Default",

        background = NEUTRAL_10,
        surface = NEUTRAL_17,
        surfaceVariant = NEUTRAL_22,
        sidebar = NEUTRAL_12,
        logsBackground = NEUTRAL_6,

        borderSubtle = NEUTRAL_22,

        textPrimary = NEUTRAL_100,
        textSecondary = NEUTRAL_80,

        success = EMERALD,
        error = ALIZARIN,
        warning = CARROT,
        info = PETER_RIVER,

        progress = PETER_RIVER,
        progressTrack = BELIZE_HOLE,
    )
    
    private val DefaultSyntaxTheme = SyntaxTheme(
        name = "Default",

        key = WISTERIA,
        plainScalar = BELIZE_HOLE,
        string = GREEN_SEA,
        blockString = GREEN_SEA,
        number = BELIZE_HOLE,
        boolean = CARROT,
        flow = CARROT,
        comment = ASBESTOS,
    )

    val json = Json { ignoreUnknownKeys = true }

    private val _appTheme = mutableStateOf(DefaultAppTheme)
    val appTheme: State<AppTheme> = _appTheme

    private val _syntaxTheme = mutableStateOf(DefaultSyntaxTheme)
    val syntaxTheme: State<SyntaxTheme> = _syntaxTheme

    fun applyAppTheme(appTheme: AppTheme) {
        _appTheme.value = appTheme.withFallback(DefaultAppTheme)
    }

    fun applySyntaxTheme(syntaxTheme: SyntaxTheme) {
        _syntaxTheme.value = syntaxTheme.withFallback(DefaultSyntaxTheme)
    }

    fun scanThemes(type: Type): List<AppTheme> = Files.list(
        when (type) {
            Type.APP -> Path("Add app theme path")
            Type.SYNTAX -> Path("Add syntax theme path")
        }
    ).filter {
        it.toString().endsWith(".json")
    }.map {
        json.decodeFromString<AppTheme>(Files.readString(it))
    }.toList()

    private fun AppTheme.withFallback(default: AppTheme): AppTheme = AppTheme(
        name = name ?: default.name,

        background = background ?: default.background,
        surface = surface ?: default.surface,
        surfaceVariant = surfaceVariant ?: default.surfaceVariant,
        sidebar = sidebar ?: default.sidebar,
        logsBackground = logsBackground ?: default.logsBackground,

        borderSubtle = borderSubtle ?: default.borderSubtle,

        textPrimary = textPrimary ?: default.textPrimary,
        textSecondary = textSecondary ?: default.textSecondary,

        success = success ?: default.success,
        error = error ?: default.error,
        warning = warning ?: default.warning,
        info = info ?: default.info,

        progress = progress ?: default.progress,
        progressTrack = progressTrack ?: default.progressTrack
    )

    private fun SyntaxTheme.withFallback(default: SyntaxTheme): SyntaxTheme = SyntaxTheme(
        name = name ?: default.name,

        key = key ?: default.key,
        plainScalar = plainScalar ?: default.plainScalar,
        string = string ?: default.string,
        blockString = blockString ?: default.blockString,
        number = number ?: default.number,
        boolean = boolean ?: default.boolean,
        flow = flow ?: default.flow,
        comment = comment ?: default.comment,
    )
}
