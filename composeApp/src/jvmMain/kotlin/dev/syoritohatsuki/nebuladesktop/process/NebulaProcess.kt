package dev.syoritohatsuki.nebuladesktop.process

import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

interface NebulaProcess {
    fun start(config: Path): Process?
    fun stop(process: Process): Boolean
}