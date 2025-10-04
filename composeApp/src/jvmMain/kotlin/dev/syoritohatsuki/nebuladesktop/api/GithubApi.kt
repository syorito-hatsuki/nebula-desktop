package dev.syoritohatsuki.nebuladesktop.api

import dev.syoritohatsuki.nebuladesktop.dto.github.GithubRelease
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object GithubApi {
    private val httpClient = HttpClient(CIO) {
        install(DefaultRequest) {
            url("https://api.github.com")
            contentType(ContentType.Application.Json)
        }

        install(UserAgent) {
            agent = "User-Agent: syorito-hatsuki/nebula-desktop (github.com/syorito-hatsuki)"
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    suspend fun fetchLatestRelease(repo: String): GithubRelease = httpClient.get("repos/${repo}/releases/latest").body()
}