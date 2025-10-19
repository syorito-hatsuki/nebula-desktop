package dev.syoritohatsuki.nebuladesktop.util

import dev.syoritohatsuki.nebuladesktop.operationSystem
import java.nio.file.Path

object StorageManager {
    private val userHome = System.getProperty("user.home")

    val nebulaBinaryDirPath: Path = when {
        operationSystem.contains("win") -> Path.of(
            System.getenv("APPDATA"), "NebulaTray", "bin"
        )

        operationSystem.contains("mac") -> Path.of(
            userHome, "Library", "Application Support", "NebulaTray", "bin"
        )

        operationSystem.contains("nux") -> Path.of(
            userHome, ".local", "share", "nebula-tray", "bin"
        )

        else -> error("Unknown OS name: $operationSystem")
    }

    val nebulaBinaryPath: Path = nebulaBinaryDirPath.resolve(
        when {
            operationSystem.contains("win") -> "nebula.exe"
            else -> "nebula"
        }
    )
}