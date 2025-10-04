package dev.syoritohatsuki.nebula_desktop.ui.main

import androidx.lifecycle.ViewModel
import dev.syoritohatsuki.nebula_desktop.dto.NebulaConnection
import dev.syoritohatsuki.nebula_desktop.util.NebulaManager

class MainWindowViewModel() : ViewModel() {

    val manager = NebulaManager
    // Expose immutable view of manager data
    val connections = manager.connections

    fun startConnection(name: String) = manager.startConnection(name)
    fun stopConnection(name: String) = manager.stopConnection(name)
    fun addConnection(connection: NebulaConnection) = manager.addConnection(connection)
}