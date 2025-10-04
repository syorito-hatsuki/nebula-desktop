package dev.syoritohatsuki.nebuladesktop.ui.main

import androidx.lifecycle.ViewModel
import dev.syoritohatsuki.nebuladesktop.dto.NebulaConnection
import dev.syoritohatsuki.nebuladesktop.util.NebulaManager

class MainWindowViewModel() : ViewModel() {

    val manager = NebulaManager
    // Expose immutable view of manager data
    val connections = manager.connections

    fun startConnection(name: String) = manager.startConnection(name)
    fun stopConnection(name: String) = manager.stopConnection(name)
    fun addConnection(connection: NebulaConnection) = manager.addConnection(connection)
}