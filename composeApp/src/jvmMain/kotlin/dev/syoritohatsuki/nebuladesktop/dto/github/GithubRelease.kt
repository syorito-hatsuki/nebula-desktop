package dev.syoritohatsuki.nebuladesktop.dto.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    val assets: List<GithubReleaseAsset>,
    @SerialName("tag_name") val tagName: String
) {
    @Serializable
    data class GithubReleaseAsset(
        val name: String,
        @SerialName("browser_download_url") val browserDownloadUrl: String
    )
}