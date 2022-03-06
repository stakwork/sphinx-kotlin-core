package chat.sphinx.concepts.network.query.version.model

import kotlinx.serialization.Serializable

@Serializable
data class AppVersionsDto(
    val ios: Long,
    val android: Long,
    val windows: Long,
    val linux: Long,
    val mac: Long,
    val kotlin: Long,
)