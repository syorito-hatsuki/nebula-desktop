package dev.syoritohatsuki.nebuladesktop.process

import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import dev.syoritohatsuki.nebuladesktop.util.StorageManager
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

object NebulaUnix : NebulaProcess {
    override fun start(config: Path): PtyProcess? = builtProcess(*getLaunchCommandByOS(config))

    override fun stop(process: Process): Boolean {
        process.destroyForcibly()
        if (!process.waitFor(500, TimeUnit.MILLISECONDS)) {
            Runtime.getRuntime().exec(getStopCommandByOS(process.pid()))
        }
        return process.waitFor() == 0
    }

    private fun builtProcess(vararg commands: String): PtyProcess? = try {
        PtyProcessBuilder(commands).setEnvironment(HashMap(System.getenv()).apply {
            this["TERM"] = "xterm-256color"
        }).setConsole(true).start()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun getLaunchCommandByOS(config: Path): Array<String> {
        val osProperty = System.getProperty("os.name").lowercase()

        return when {
            osProperty.contains("nux") -> arrayOf(
                "pkexec", StorageManager.nebulaBinaryPath.absolutePathString(), "-config", config.toString()
            )

            osProperty.contains("mac") -> arrayOf(
                "osascript",
                "-e",
                "do shell script \"'${StorageManager.nebulaBinaryPath.absolutePathString()}' -config '${config}'\" with administrator privileges"
            )

            else -> throw UnsupportedOperationException("Unknown OS: $osProperty, please contact support")
        }
    }

    private fun getStopCommandByOS(pid: Long): Array<String> {
        val osProperty = System.getProperty("os.name").lowercase()

        return when {
            osProperty.contains("nux") -> arrayOf(
                "pkexec", "kill", "-TERM", pid.toString()
            )

            osProperty.contains("mac") -> arrayOf(
                "osascript", "-e", "'do shell script \"kill -TERM $pid\" with administrator privileges'"
            )

            else -> throw UnsupportedOperationException("Unknown OS: $osProperty, please contact support")
        }
    }
}