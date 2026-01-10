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
    val json = Json { ignoreUnknownKeys = true }

    private val _appTheme = mutableStateOf(AppTheme("Default"))
    val appTheme: State<AppTheme> = _appTheme

    private val _syntaxTheme = mutableStateOf(SyntaxTheme("Default"))
    val syntaxTheme: State<SyntaxTheme> = _syntaxTheme

    fun applyAppTheme(appTheme: AppTheme) {
        _appTheme.value = appTheme
    }

    fun applySyntaxTheme(syntaxTheme: SyntaxTheme) {
        _syntaxTheme.value = syntaxTheme
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
}
