package chat.sphinx.concepts.network.query.feed_search.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateProjectResponseDto(
    val success: Boolean? = null,
    val data: ProjectData? = null,
    val error: ErrorResponse? = null
)

@Serializable
data class ProjectData(
    val project_id: Long? = null
)
@Serializable
data class ErrorResponse(
    val message: String? = null
)
