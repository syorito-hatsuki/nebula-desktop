package dev.syoritohatsuki.nebuladesktop.util

import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import kotlinx.serialization.json.Json
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object StorageManager {
    private val json = Json { ignoreUnknownKeys = true }

    private val userHome = System.getProperty("user.home")

    val nebulaBinaryDirPath: Path = when (hostOs) {
        OS.Windows -> Path.of(System.getenv("APPDATA"), "NebulaTray", "bin")
        OS.MacOS -> Path.of(userHome, "Library", "Application Support", "NebulaTray", "bin")
        OS.Linux -> Path.of(userHome, ".local", "share", "nebula-tray", "bin")

        else -> error("Unsupported OS: $hostOs")
    }

    val nebulaBinaryPath: Path = nebulaBinaryDirPath.resolve(
        when (hostOs) {
            OS.Windows -> "nebula.exe"
            else -> "nebula"
        }
    )

    private val connectionsFile: Path = nebulaBinaryDirPath.parent.resolve("connections.json")

    fun loadConnections(): List<NebulaConnection> = when {
        connectionsFile.exists() -> try {
            json.decodeFromString<List<NebulaConnection>>(connectionsFile.readText())
        } catch (e: Exception) {
            print("Failed to load connections.")
            print(e.message)
            emptyList()
        }

        else -> emptyList()
    }

    fun updateConnectionFile(connections: List<NebulaConnection>) {
        connectionsFile.writeText(json.encodeToString(connections))
    }
}