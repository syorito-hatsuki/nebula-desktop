package dev.syoritohatsuki.nebuladesktop.util

import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.operationSystem
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object StorageManager {
    private val json = Json { ignoreUnknownKeys = true }

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