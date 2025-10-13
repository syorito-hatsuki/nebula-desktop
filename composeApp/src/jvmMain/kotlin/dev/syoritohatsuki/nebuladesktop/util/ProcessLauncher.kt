package dev.syoritohatsuki.nebuladesktop.util

import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import dev.syoritohatsuki.nebuladesktop.util.ProcessHandler.OS.*
import java.nio.file.Path

object ProcessHandler {

    enum class OS {
        LINUX, MACOS, WINSHIT
    }

    private fun getLaunchCommandByOS(config: Path, os: OS): Array<String> = when (os) {
        LINUX -> arrayOf(
            "pkexec", StorageManager.nebulaBinaryPath.toString(), "-config", config.toString()
        )

        MACOS -> arrayOf(
            "osascript",
            "-e",
            "do shell script \"'${StorageManager.nebulaBinaryPath}' -config '${config}'\" with administrator privileges"
        )

        WINSHIT -> arrayOf(
            "powershell",
            "-Command",
            "Start-Process -FilePath \"${StorageManager.nebulaBinaryPath}\" -ArgumentList '-config \"${config}\"' -Verb RunAs -Wait"
        )
    }

    private fun getStopCommandByOS(pid: Long, os: OS): Array<String> = when (os) {
        LINUX -> arrayOf(
            "pkexec", "kill", "-TERM", pid.toString()
        )

        MACOS -> arrayOf(
            "osascript",
            "-e",
            "'do shell script \"kill -TERM $pid\" with administrator privileges'"
        )

        WINSHIT -> arrayOf(
            "taskkill", "/PID", pid.toString(), "/T", "/F"
        )
    }

    private fun builtProcess(vararg commands: String): PtyProcess? = try {
        PtyProcessBuilder(commands).setEnvironment(HashMap(System.getenv()).apply {
            this["TERM"] = "xterm-256color"
        }).setConsole(true).start()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun getOs(): OS {
        val osProperty = System.getProperty("os.name").lowercase()
        return when {
            osProperty.contains("nux") -> LINUX
            osProperty.contains("mac") -> MACOS
            osProperty.contains("win") -> WINSHIT
            else -> throw UnsupportedOperationException("Unknown OS: $osProperty, please contact support")
        }
    }

    fun startNebula(config: Path): PtyProcess? = builtProcess(*getLaunchCommandByOS(config, getOs()))

    fun stopNebula(pid: Long): Process? = Runtime.getRuntime().exec(getStopCommandByOS(pid, getOs()))
}