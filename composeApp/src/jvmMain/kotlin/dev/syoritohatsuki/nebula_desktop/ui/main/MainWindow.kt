package dev.syoritohatsuki.nebula_desktop.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.syoritohatsuki.nebula_desktop.dto.NebulaConnection
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

@Composable
fun MainWindow(mainWindowViewModel: MainWindowViewModel) {

    val connections by mainWindowViewModel.connections.collectAsState()
    var selectedConnection by remember { mutableStateOf<NebulaConnection?>(null) }
    val logLines = remember { mutableStateListOf<String>() }


    val filePicker = rememberFilePickerLauncher(
        title = "Select Nebula Config",
        type = FileKitType.File(extensions = listOf("yml", "yaml"))
    ) { file ->
        file?.let {
            println(it.file.readText())
        }
    }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.28f)
                .background(Color(0xFF252526))
                .padding(8.dp)
        ) {
            item {
                Text(
                    "Connections",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }

            items(connections) { conn ->
                val isSelected = selectedConnection == conn

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedConnection = conn
                            logLines.clear()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF3C3C3C) else Color(0xFF2D2D2D)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column() {
                            Text(conn.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(conn.configPath.toString(), color = Color.LightGray, fontSize = 12.sp)
                        }
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = if (conn.status == NebulaConnection.ConnectionStatus.ON) Color(0xFF2ECC71) else Color(0xFFE74C3C)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        filePicker.launch()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC))
                ) {
                    Text("Add Connection", color = Color.White)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(16.dp)
        ) {
            selectedConnection?.let { connection ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header with actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Column {
                            Text(connection.name, color = Color.White, fontSize = 20.sp)
                            Text(
                                "Status: ${connection.status}",
                                color = if (connection.status == NebulaConnection.ConnectionStatus.ON)
                                    Color(0xFF2ECC71) else Color(0xFFE74C3C)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (connection.status == NebulaConnection.ConnectionStatus.OFF) {
                                Button(
                                    onClick = { mainWindowViewModel.startConnection(connection.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                                ) { Text("Start") }
                            } else {
                                Button(
                                    onClick = { mainWindowViewModel.stopConnection(connection.name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                                ) { Text("Stop") }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Logs", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Logs window
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF111111))
                            .border(1.dp, Color(0xFF3C3C3C), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            reverseLayout = true,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(logLines) { line ->
                                Text(
                                    line,
                                    color = Color(0xFFCCCCCC),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                LaunchedEffect(connection) {
                    connection.logs.collect { line ->
                        logLines.add(0, line)
                        if (logLines.size > 1000) logLines.removeLast()
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a connection", color = Color.Gray)
                }
            }
        }
    }
}