package dev.syoritohatsuki.nebuladesktop.util

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

fun isRequireAdminRights(): Boolean = when(hostOs) {
    OS.Windows -> try {
        ProcessBuilder(
            "powershell",
            "-Command",
            "[bool]([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)"
        ).start().inputStream.bufferedReader().readText().trim().equals("True", ignoreCase = true)
    } catch (_: Exception) {
        false
    }

    // TODO Add required checks for MacOS and Linux when required.
    //      Linux works fine over pkexec
    else -> false
}