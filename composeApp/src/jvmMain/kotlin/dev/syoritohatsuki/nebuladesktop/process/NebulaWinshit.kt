package dev.syoritohatsuki.nebuladesktop.process

import dev.syoritohatsuki.nebuladesktop.util.StorageManager
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

object NebulaWinshit : NebulaProcess {
    override fun start(config: Path): Process? = ProcessBuilder(
        StorageManager.nebulaBinaryPath.absolutePathString(),
        "-config",
        config.absolutePathString()
    ).start()

    override fun stop(process: Process): Boolean {
        process.destroyForcibly()
        if (!process.waitFor(500, TimeUnit.MILLISECONDS)) {
            process.destroy()
        }
        return process.waitFor() == 0
    }
}