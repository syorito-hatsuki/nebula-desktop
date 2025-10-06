package dev.syoritohatsuki.nebuladesktop.util

import java.io.File
import java.lang.ProcessBuilder
import java.nio.file.Path

object ProcessLauncher {
    fun runNebulaWithElevation(config: Path, nebulaBinary: Path): Process {
        val os = System.getProperty("os.name").lowercase()

        val command = when {
            os.contains("win") -> listOf(
                "powershell",
                "-Command",
                "Start-Process -FilePath '${nebulaBinary}' -ArgumentList '-config \"${config}\"' -Verb RunAs -Wait"
            )

            os.contains("mac") -> listOf(
                "osascript", "-e",
                "do shell script \"${nebulaBinary} -config ${config}\" with administrator privileges"
            )

            else -> when {
                File("/usr/bin/pkexec").exists() ->
                    listOf("pkexec", nebulaBinary.toString(), "-config", config.toString())
                else ->
                    listOf("sudo", nebulaBinary.toString(), "-config", config.toString())
            }
        }

        return ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
    }
}