package chat.sphinx.concepts.network.query.chat.model

import kotlinx.serialization.Serializable

@Serializable
data class PostGroupDto(
    val name: String,
    val description: String,
    val is_tribe: Boolean? = null,
    val price_per_message: Long? = null,
    val price_to_join: Long? = null,
    val escrow_amount: Long? = null,
    val escrow_millis: Long? = null,
    val img: String? = null,
    val tags: Array<String>,
    val unlisted: Boolean? = null,
    val private: Boolean? = null,
    val app_url: String? = null,
    val feed_url: String? = null,
    val feed_type: Long? = null,
)
