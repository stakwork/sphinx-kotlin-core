package chat.sphinx.concepts.network.query.save_profile.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeletePeopleProfileDto(
    val id: Int,
    val host: String,
)