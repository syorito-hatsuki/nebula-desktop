package dev.syoritohatsuki.nebuladesktop.util

import androidx.compose.ui.awt.ComposeWindow
import java.awt.Frame

fun focusAppWindow(window: ComposeWindow?) {
    if (window == null) return
    if (!window.isVisible) window.isVisible = true
    if (window.state == Frame.ICONIFIED) window.state = Frame.NORMAL

    window.toFront()
    window.requestFocus()
}