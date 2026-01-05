package dev.syoritohatsuki.nebuladesktop.ui.window.main.components.left

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.syoritohatsuki.nebuladesktop.BuildKonfig
import dev.syoritohatsuki.nebuladesktop.api.GithubApi
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR
import dev.syoritohatsuki.nebuladesktop.ui.TEXT_COLOR_SECONDARY
import kotlinx.coroutines.launch
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

@Composable
fun UpdateAvailableText() {
    val scope = rememberCoroutineScope()

    var remoteAppVersion by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            val latestTag = GithubApi.fetchLatestRelease("syorito-hatsuki/nebula-desktop").tagName
            remoteAppVersion = latestTag
        }
    }

    if (BuildKonfig.appVersion == remoteAppVersion) {
        Text(
            text = "Build: ${BuildKonfig.appVersion}",
            color = TEXT_COLOR_SECONDARY,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            modifier = Modifier.padding(4.dp).fillMaxWidth()
        )
    } else {
        Text(
            text = "Update available :)",
            color = TEXT_COLOR,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            modifier = Modifier.padding(4.dp).fillMaxWidth().clickable {
                val url = "https://github.com/syorito-hatsuki/nebula-desktop/releases"
                try {
                    Runtime.getRuntime().exec(
                        when (hostOs) {
                            OS.Linux -> arrayOf("xdg-open", url)
                            OS.MacOS -> arrayOf("open", url)
                            OS.Windows -> arrayOf("rundll32", "url.dll,FileProtocolHandler", url)
                            else -> error("Unsupported OS: $hostOs")
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
    }
}