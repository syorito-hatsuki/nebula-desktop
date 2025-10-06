package dev.syoritohatsuki.nebuladesktop.util

import java.nio.file.Path

object StorageManager {

    private val osName = System.getProperty("os.name").lowercase()
    private val userHome = System.getProperty("user.home")

    val nebulaBinaryDirPath: Path
        get() = when {
            osName.contains("win") -> Path.of(
                System.getenv("APPDATA"), "NebulaTray", "bin"
            )

            osName.contains("mac") -> Path.of(
                userHome, "Library", "Application Support", "NebulaTray", "bin"
            )

            osName.contains("nux") -> Path.of(
                userHome, ".local", "share", "nebula-tray", "bin"
            )

            else -> error("Unknown OS name: $osName")
        }

    val nebulaBinaryPath: Path
        get() = nebulaBinaryDirPath.resolve(
            when {
                osName.contains("win") -> "nebula.exe"
                else -> "nebula"
            }
        )
}