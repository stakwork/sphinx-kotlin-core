package chat.sphinx.concepts.network.query.save_profile.model

import kotlinx.serialization.Serializable

@Serializable
data class DeletePeopleProfileDto(
    val id: Int,
    val host: String,
)