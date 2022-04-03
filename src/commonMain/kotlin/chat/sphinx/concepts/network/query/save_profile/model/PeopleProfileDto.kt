package chat.sphinx.concepts.network.query.save_profile.model

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class PeopleProfileDto(
    val id: Int,
    val host: String,
    val owner_alias: String,
    val description: String,
    val img: String,
    val tags: List<String>? = null,
    val price_to_meet: Int,
    val extras: @Polymorphic Any,
)