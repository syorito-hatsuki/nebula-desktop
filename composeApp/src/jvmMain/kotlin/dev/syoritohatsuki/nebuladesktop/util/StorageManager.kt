package dev.syoritohatsuki.nebuladesktop.util

import java.nio.file.Path

object StorageManager {
    val nebulaBinaryDirPath: Path
        get() = when {
            System.getProperty("os.name").contains("win", true) -> Path.of(
                System.getenv("APPDATA"), "NebulaTray", "bin"
            )

            else -> Path.of(System.getProperty("user.home"), ".local", "share", "nebula-tray", "bin")
        }

    val nebulaBinaryPath: Path
        get() = nebulaBinaryDirPath.resolve(
            when {
                System.getProperty("os.name").lowercase().contains("win") -> "nebula.exe"
                else -> "nebula"
            }
        )
}