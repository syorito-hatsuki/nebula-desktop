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
        return try {
            process.destroy()
            if (process.waitFor(250, TimeUnit.MILLISECONDS)) return true

            process.destroyForcibly()
            if (process.waitFor(250, TimeUnit.MILLISECONDS)) return true

            try {
                ProcessBuilder(*getStopCommandByOS(process.pid()))
                    .redirectErrorStream(true)
                    .start()
            } catch (_: Exception) {}

            true
        } catch (_: Exception) {
            false
        }
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
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("nux") -> arrayOf("pkexec", "bash", "-lc", "kill -TERM $pid || true")
            os.contains("mac") -> arrayOf(
                "osascript", "-e", "do shell script \"kill -TERM $pid\" with administrator privileges"
            )

            else -> throw UnsupportedOperationException("Unknown OS: $os, please contact support")
        }
    }
}