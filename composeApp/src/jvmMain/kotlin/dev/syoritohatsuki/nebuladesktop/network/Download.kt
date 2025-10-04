package dev.syoritohatsuki.nebuladesktop.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Path

suspend fun downloadFile(
    url: String,
    target: Path,
    onProgress: (Double) -> Unit
) {
    val client = HttpClient(CIO)
    val response = client.get(url)
    val total = response.contentLength() ?: -1L
    var downloaded = 0L

    response.bodyAsChannel().let { channel ->
        FileOutputStream(target.toFile()).use { fos ->
            val buffer = ByteArray(8192)
            val byteBuffer = ByteBuffer.wrap(buffer)
            while (!channel.isClosedForRead) {
                byteBuffer.clear()
                val read = channel.readAvailable(byteBuffer)
                if (read == -1) break
                fos.write(buffer, 0, read)
                downloaded += read
                if (total > 0) onProgress(downloaded.toDouble() / total)
            }
        }
    }
    client.close()
}
