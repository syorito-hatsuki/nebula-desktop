package dev.syoritohatsuki.nebuladesktop.process

import java.nio.file.Path

interface NebulaProcess {
    fun start(config: Path): Process?
    fun stop(process: Process): Boolean
}